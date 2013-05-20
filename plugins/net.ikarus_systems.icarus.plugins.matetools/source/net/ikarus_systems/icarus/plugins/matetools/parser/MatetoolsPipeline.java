/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.matetools.parser;

import is2.data.SentenceData09;
import is2.lemmatizer.Lemmatizer;
import is2.parser.ParametersFloat;
import is2.parser.Parser;
import is2.parser.Pipe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import net.ikarus_systems.icarus.config.ConfigRegistry;
import net.ikarus_systems.icarus.language.dependency.DependencyData;
import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.matetools.conll.CONLLUtils;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.dialog.DialogDispatcher;
import net.ikarus_systems.icarus.ui.dialog.DialogFactory;
import net.ikarus_systems.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class MatetoolsPipeline {
	
	private static final Object lock = new Object();
	
	private static PipelineOwner currentOwner;
	
	private static MatetoolsPipeline instance;
	
	private static AtomicInteger runCount = new AtomicInteger(1);
	
	public static MatetoolsPipeline getPipeline(PipelineOwner owner) {
		if(owner==null)
			throw new IllegalArgumentException("Invalid owner"); //$NON-NLS-1$
		
		PipelineOwner blockingOwner = null;
		
		synchronized (lock) {
			if(instance==null) {
				instance = new MatetoolsPipeline();
			}
			
			if(currentOwner==null || currentOwner==owner) {
				currentOwner = owner;
				
				return instance;
			} else {
				blockingOwner = currentOwner;
			}
		}
		
		if(blockingOwner!=null) {
			new DialogDispatcher(null, 
				"plugins.matetools.parserPipeline.title",  //$NON-NLS-1$
				"plugins.matetools.parserPipeline.occupied",  //$NON-NLS-1$
				blockingOwner.getName()).showAsWarning();
		}
		
		return null;
	}
	
	public static void releasePipeline(PipelineOwner owner) {
		if(owner==null)
			throw new IllegalArgumentException("Invalid owner"); //$NON-NLS-1$
		
		synchronized (lock) {
			PipelineOwner oldOwner = currentOwner;
			currentOwner = null;
			
			if(oldOwner!=owner)
				throw new IllegalArgumentException("Supplied owner is not in posession of the pipeline: "+owner.getName()); //$NON-NLS-1$
		}
	}
	
	public static void freePipeline() {
		synchronized (lock) {
			if(currentOwner==null) {
				instance = null;
			}
		}
	}
	
	private Lemmatizer lemmatizer;
	private is2.tag.Tagger tagger;
	private is2.mtag.Tagger mtag;
	private Parser parser;
	
	private ModelStorage storage;

	private MatetoolsPipeline() {
		// no-op
	}
	
	public DependencyData runPipeline(String[] tokens, Options options) throws Exception {
		if(tokens==null || tokens.length==0)
			throw new IllegalArgumentException("Invalid tokens"); //$NON-NLS-1$
		
		if(options==null) {
			options = Options.emptyOptions;
		}
		
		ConfigRegistry config = ConfigRegistry.getGlobalRegistry();
		
		// Load language
		String language = (String) options.get(Options.LANGUAGE);
		if(language==null) {
			language = config.getString("plugins.matetools.parser.language"); //$NON-NLS-1$
		}
		
		// Fetch model storage
		ModelStorage storage = getModels(language);
		
		if(storage==null || storage.isEmpty()) {
			if(language.isEmpty()) {
				language = "<empty>"; //$NON-NLS-1$
			}
			new DialogDispatcher(null, 
					"plugins.matetools.parserPipeline.title",  //$NON-NLS-1$
					"plugins.matetools.parserPipeline.missingStorage",  //$NON-NLS-1$
					language).showAsError();
			return null;
		}
		
		// Set new models, this may clear some tools
		setModels(storage);
		
		boolean verbose = config.getBoolean("plugins.matetools.parser.verbose"); //$NON-NLS-1$
		boolean doUppercaseLemmas = config.getBoolean("plugins.matetools.parser.doUppercaseLemmas"); //$NON-NLS-1$
		boolean fastRelease = config.getBoolean("plugins.matetools.parser.fastRelease"); //$NON-NLS-1$
		
		int cores = config.getInteger("plugins.matetools.parser.maxCores"); //$NON-NLS-1$
		int availableCores = Math.max(1, Runtime.getRuntime().availableProcessors()/2);
		if(cores>0) {
			cores = Math.min(cores, availableCores);
		}
		cores = Math.max(cores, 1);
		
		List<String> missingModels = new ArrayList<>();
		storage.clear();
		
		// LEMMATIZER
		if(lemmatizer==null && storage.getLemmatizerModelPath()==null) {
			missingModels.add("plugins.matetools.parserModelEditor.lemmatizerModelLabel"); //$NON-NLS-1$
		}
		
		// TAGGER
		if(tagger==null && storage.getTaggerModelPath()==null) {
			missingModels.add("plugins.matetools.parserModelEditor.taggerModelLabel"); //$NON-NLS-1$
		}
		
		// MTAG
		if(mtag==null && storage.getMorphTaggerModelPath()==null) {
			missingModels.add("plugins.matetools.parserModelEditor.morphTaggerModelLabel"); //$NON-NLS-1$
		}
		
		// PARSER
		if(parser==null && storage.getParserModelPath()==null) {
			missingModels.add("plugins.matetools.parserModelEditor.parserModelLabel"); //$NON-NLS-1$
		}
		
		// Allow user to abort midway
		if(!missingModels.isEmpty()) {
			StringBuilder sb = new StringBuilder(200);
			for(String key : missingModels) {
				sb.append(ResourceManager.getInstance().get(key)).append("\n"); //$NON-NLS-1$
			}
			
			if(!DialogFactory.getGlobalFactory().showConfirm(null, 
					"plugins.matetools.parserPipeline.title",  //$NON-NLS-1$
					"plugins.matetools.parserPipeline.missingModels",  //$NON-NLS-1$
					sb.toString().trim())) {
				return null;
			}
		}
		
		int runCt = runCount.getAndIncrement();
		
		// Generate initial data
		SentenceData09 data = new SentenceData09();
		data.init(tokens);
		
		DependencyData output = null;
		
		// Now load tools if required and apply pipeline
		
		// LEMMATIZER
		if(lemmatizer==null && storage.getLemmatizerModelPath()!=null) {
			lemmatizer=new Lemmatizer(storage.getLemmatizerModelPath(),doUppercaseLemmas);
		}
		if(lemmatizer!=null) {
			lemmatizer.apply(data);
			output = CONLLUtils.readPredicted(data, true, true);
			if(verbose) {
				String msg = String.format("Matetools pipeline (run %d) - Lemmatizer result:\n%s",  //$NON-NLS-1$
						runCt, data.toString());
				LoggerFactory.log(this, Level.INFO, msg);
			}
		}
		currentOwner.outputChanged(output);
		if(fastRelease) {
			lemmatizer = null;
		}
		
		// TAGGER
		if(tagger==null && storage.getTaggerModelPath()!=null) {
			tagger=new is2.tag.Tagger(storage.getTaggerModelPath());
		}
		if(tagger!=null) {
			tagger.apply(data);
			output = CONLLUtils.readPredicted(data, true, true);
			if(verbose) {
				String msg = String.format("Matetools pipeline (run %d) - Tagger result:\n%s",  //$NON-NLS-1$
						runCt, data.toString());
				LoggerFactory.log(this, Level.INFO, msg);
			}
		}
		currentOwner.outputChanged(output);
		if(fastRelease) {
			tagger = null;
		}
		
		
		// MTAG
		if(mtag==null && storage.getMorphTaggerModelPath()!=null) {
			mtag=new is2.mtag.Tagger(storage.getMorphTaggerModelPath());
		}
		if(mtag!=null) {
			mtag.apply(data);

			// Workaround
			if(storage.getParserModelPath()!=null && data.pfeats!=null){
				for(int i=1;i<data.pfeats.length;++i){
					if(data.pfeats[i]!=null && !data.pfeats[i].equals("_")) //$NON-NLS-1$
						data.feats[i]=data.pfeats[i].split("\\|"); //$NON-NLS-1$
				}
			}
			
			output = CONLLUtils.readPredicted(data, true, true);
			if(verbose) {
				String msg = String.format("Matetools pipeline (run %d) - Morphologic-Tagger result:\n%s",  //$NON-NLS-1$
						runCt, data.toString());
				LoggerFactory.log(this, Level.INFO, msg);
			}
		}
		currentOwner.outputChanged(output);
		if(fastRelease) {
			mtag = null;
		}
		
		// PARSER
		if(parser==null && storage.getParserModelPath()!=null) {
			is2.parser.Options opts=new is2.parser.Options(new String[]{"-model",storage.getParserModelPath()}); //$NON-NLS-1$
			Parser.THREADS=cores;
			Parser p = new Parser();
			p.options=opts;
			p. pipe = new Pipe(opts);
			p. params = new ParametersFloat(0);
			p.readModel(opts, p.pipe, p.params);
			parser=p;
		}
		if(parser!=null) {
			parser.applyQuick(data);
			output = CONLLUtils.readPredicted(data, false, true);
			if(verbose) {
				String msg = String.format("Matetools pipeline (run %d) - Parser result:\n%s",  //$NON-NLS-1$
						runCt, data.toString());
				LoggerFactory.log(this, Level.INFO, msg);
			}
		}
		currentOwner.outputChanged(output);
		if(fastRelease) {
			parser = null;
		}
		
		return output;
	}
	
	private static ModelStorage getModels(String language) {
		List<?> storages = (List<?>) ConfigRegistry.getGlobalRegistry().getValue(
				"plugins.matetools.parser.models"); //$NON-NLS-1$
		
		if(storages==null || storages.isEmpty()) {
			return null;
		}
		
		for(Object item : storages) {
			ModelStorage storage = (ModelStorage) item;
			if(storage.isLanguage(language)) {
				return storage;
			}
		}
		
		return null;
	}
	
	private void setModels(ModelStorage newStorage) {
		if(storage==null) {
			lemmatizer = null;
			tagger = null;
			mtag = null;
			parser = null;
		} else {
			if(tagger!=null && !equals(storage.getTaggerModelPath(), newStorage.getTaggerModelPath())) {
				tagger = null;
			}
			if(parser!=null && !equals(storage.getParserModelPath(), newStorage.getParserModelPath())) {
				parser = null;
			}
			if(mtag!=null && !equals(storage.getMorphTaggerModelPath(), newStorage.getMorphTaggerModelPath())) {
				mtag = null;
			}
			if(lemmatizer!=null && !equals(storage.getLemmatizerModelPath(), newStorage.getLemmatizerModelPath())) {
				lemmatizer = null;
			}
		}
		storage = newStorage.clone();
	}
	
	private static boolean equals(String s1, String s2) {
		return (s1!=null && s1.equals(s2)) || (s1==null && s2==null);
	}

	/**
	 * Marks a potential owner of the pipeline. Only at most one instance
	 * can hold ownership of the pipeline singleton at any given time! Note that
	 * equality checks on {@code PipelineOwner} objects are performed via
	 * identity check (owner1==owner2).
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public interface PipelineOwner {
		String getName();
		
		void outputChanged(DependencyData currentOutput);
	}
}
