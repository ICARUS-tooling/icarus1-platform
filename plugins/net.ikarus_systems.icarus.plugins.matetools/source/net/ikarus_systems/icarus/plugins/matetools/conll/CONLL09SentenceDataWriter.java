/* 
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package net.ikarus_systems.icarus.plugins.matetools.conll;

import is2.data.SentenceData09;
import is2.io.CONLLWriter09;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;

import sun.nio.cs.Surrogate;



import net.ikarus_systems.icarus.language.SentenceData;
import net.ikarus_systems.icarus.language.SentenceDataWriter;
import net.ikarus_systems.icarus.language.UnsupportedSentenceDataException;
import net.ikarus_systems.icarus.language.dependency.DependencyConstants;
import net.ikarus_systems.icarus.language.dependency.SimpleDependencyData;
import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.tcf.tcf04.TCF04SentenceDataReader;
import net.ikarus_systems.icarus.plugins.tcf.tcf04.TCF04SentenceDataWriter;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.UnsupportedFormatException;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.ContentTypeRegistry;
import net.ikarus_systems.icarus.util.location.DefaultFileLocation;
import net.ikarus_systems.icarus.util.location.Location;
import net.ikarus_systems.icarus.util.location.UnsupportedLocationException;

/**
 * @author Gregor Thiele
 * @version $Id$
 * 
 */
public class CONLL09SentenceDataWriter implements SentenceDataWriter {
	
	protected CONLLWriter09 writer;
	protected boolean writeRoot;
	protected boolean gold;
	protected boolean system;
	protected int outputFormat; // 0 (default) or 1

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceDataWriter#init(net.ikarus_systems.icarus.util.location.Location,
	 *      net.ikarus_systems.icarus.util.Options)
	 */
	@Override
	public void init(Location location, Options options) throws IOException,
			UnsupportedLocationException {
		
		File file = location.getFile();

		if (file == null)
			throw new IllegalArgumentException("Filelocation Undef"); //$NON-NLS-1$		
		
		if (options == null){
			options = options.emptyOptions;
		}
			
		//TODO extend me
		writeRoot = false;

		gold = options.get(INCLUDE_GOLD_OPTION, false);
		system = options.get(INCLUDE_SYSTEM_OPTION, false);
		
		if (!gold && !system){
			system = true;
		}
		outputFormat = 0;

		writer = new CONLLWriter09(file.toString(), outputFormat);

	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceDataWriter#write(net.ikarus_systems.icarus.language.SentenceData)
	 */
	@Override
	public void write(SentenceData data) throws IOException,
			UnsupportedSentenceDataException {
		
		//null check
		if (data == null){
			return;
		}

		SentenceData09 currentData = new SentenceData09();
		
		SimpleDependencyData sdd;
				
		try {
			if (Thread.currentThread().isInterrupted())
					throw new InterruptedException();
				
				sdd = (SimpleDependencyData) data;
				currentData.init(sdd.getForms());
				
				if (gold){
					initGold(currentData, sdd.length());
				}
				
				if (system){
					initSystem(currentData, sdd.length());
				}
				

				//Sentence Debug
				/*
				for (int j = 0 ; j < sdd.getForms().length;j++){
					String [] t = sdd.getForms();
					System.out.println(t[j]);		
				}
				*/			

				for(int i=0; i<currentData.length(); i++) {	
					/*
					System.out.print("Form: "+ sdd.getForm(i));
					System.out.print(" Head: "+ sdd.getHead(i));
					System.out.print(" PoS: "+ sdd.getPos(i));
					System.out.print(" Feat: "+ sdd.getFeatures(i));
					System.out.print(" Lemma: "+ sdd.getLemma(i));
					System.out.println(" Relation: "+ sdd.getRelation(i));
					*/
					
					currentData.forms[i] = sdd.getForm(i);
					if(system) {
     					currentData.pheads[i] = sdd.getHead(i) + 1;
						currentData.ppos[i] = sdd.getPos(i);						
						currentData.pfeats[i] = sdd.getFeatures(i);
						currentData.plemmas[i] = sdd.getLemma(i);
						currentData.plabels[i] = sdd.getRelation(i);
					}
					
					if(gold) {
						currentData.heads[i] = sdd.getHead(i) + 1;
						currentData.gpos[i] = sdd.getPos(i);
						currentData.ofeats[i] = sdd.getFeatures(i);
						currentData.lemmas[i] = sdd.getLemma(i);
						currentData.labels[i] = sdd.getRelation(i);
					}

				}

				writer.write(currentData, writeRoot);
			
		} catch (InterruptedException e) {
			LoggerFactory.log(this, Level.SEVERE,
					"Write to File interrupted", e); //$NON-NLS-1$
		} finally {
			writer.finishWriting();
		}

	}
	
	
	private void initSystem(SentenceData09 data, int size){
		if (data.forms == null)		data.forms  = new String[size];
		if (data.pheads == null)	data.pheads = new int[size];
		if (data.ppos == null)		data.ppos = new String[size];					
		if (data.pfeats == null)	data.pfeats = new String[size];
		if (data.plemmas == null)	data.plemmas = new String[size];
		if (data.plabels == null)	data.plabels = new String[size];		
	}
	
	private void initGold(SentenceData09 data, int size){
		if (data.forms == null)		data.forms  = new String[size];
		if (data.heads == null)		data.heads = new int[size];
		if (data.gpos == null)		data.gpos = new String[size];
		if (data.ofeats == null)	data.ofeats = new String[size];
		if (data.lemmas == null)	data.lemmas = new String[size];
		if (data.labels == null)	data.labels = new String[size];		
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceDataWriter#close()
	 */
	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceDataWriter#getDataType()
	 */
	@Override
	public ContentType getDataType() {
		return ContentTypeRegistry.getInstance().getType(
				DependencyConstants.CONTENT_TYPE_ID);
	}

	public static void main(String[] args) throws UnsupportedFormatException {

		File fileIn = new File("E:\\test_small.txt"); //$NON-NLS-1$		
		DefaultFileLocation dloc = new DefaultFileLocation(fileIn);

		File fileOut = new File("E:\\test_small_out.txt"); //$NON-NLS-1$		
		DefaultFileLocation dlocOut = new DefaultFileLocation(fileOut);

		Options o = new Options();
		o.put(INCLUDE_GOLD_OPTION, true);
		o.put(INCLUDE_SYSTEM_OPTION, true);
		CONLL09SentenceDataReader tr = new CONLL09SentenceDataReader();
		CONLL09SentenceDataWriter tw = new CONLL09SentenceDataWriter();
		try {
			tr.init(dloc, o);
			tw.init(dlocOut, o);
			SentenceData sd = tr.next();
			tw.write(sd);

			System.out.println("Finished: Output@ E:\\test_out.xml"); //$NON-NLS-1$
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
