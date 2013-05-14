/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.tcf.tcf04;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.EnumSet;
import java.util.logging.Level;

import de.tuebingen.uni.sfs.wlf1.io.TextCorpusStreamed;
import de.tuebingen.uni.sfs.wlf1.io.WLFormatException;
import de.tuebingen.uni.sfs.wlf1.tc.api.Dependency;
import de.tuebingen.uni.sfs.wlf1.tc.api.Feature;
import de.tuebingen.uni.sfs.wlf1.tc.api.Sentence;
import de.tuebingen.uni.sfs.wlf1.tc.api.Token;
import de.tuebingen.uni.sfs.wlf1.tc.xb.TextCorpusLayerTag;


import net.ikarus_systems.icarus.language.SentenceData;
import net.ikarus_systems.icarus.language.SentenceDataReader;
import net.ikarus_systems.icarus.language.dependency.DependencyConstants;
import net.ikarus_systems.icarus.language.dependency.SimpleDependencyData;
import net.ikarus_systems.icarus.logging.LoggerFactory;
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
public class TCF04SentenceDataReader implements SentenceDataReader {
	
	protected TextCorpusStreamed textCorpusStreamed;
	protected EnumSet<TextCorpusLayerTag> layersToRead;
	protected int maxLength = 0;
	protected int sentenceIndex;

	/**
	 * 
	 */
	public TCF04SentenceDataReader() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @throws WLFormatException 
	 * @see net.ikarus_systems.icarus.language.SentenceDataReader#init(net.ikarus_systems.icarus.util.location.Location, net.ikarus_systems.icarus.util.Options)
	 */
	@Override
	public void init(Location location, Options options) throws IOException,
			UnsupportedLocationException {
				
		File file = location.getFile();
		
		if(file == null)
			throw new IllegalArgumentException("Filelocation Undef"); //$NON-NLS-1$		
		
		
		if(!file.exists())
			throw new FileNotFoundException("Missing File: " //$NON-NLS-1$
											+file.getAbsolutePath());
		
		if (options == null){
			options = Options.emptyOptions;
		}
		
		
		// specify which layer/layers annotations should be read in order to process
		layersToRead = EnumSet.of(
				TextCorpusLayerTag.TOKENS, TextCorpusLayerTag.SENTENCES,
				TextCorpusLayerTag.LEMMAS, TextCorpusLayerTag.POSTAGS,
				TextCorpusLayerTag.MORPHOLOGY,
				TextCorpusLayerTag.PARSING_DEPENDENCY);


		// System.out.println("Layers To Read: " + layersToRead);

		// TextCorpusStreamed object with the layers specified in (layersToRead)
		// and file-inputstream. This object will only load the specified layers to
		// memory and skip others
		
		FileInputStream fis = new FileInputStream(file);
		sentenceIndex = 0;

		try {
			textCorpusStreamed = new TextCorpusStreamed(fis, layersToRead);
		} catch (WLFormatException e) {
			LoggerFactory.log(this, Level.SEVERE,
					"TextCorpusFormat Exception", e); //$NON-NLS-1$
		}

	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceDataReader#next()
	 */
	@Override
	public SentenceData next() throws IOException, UnsupportedFormatException {
		String[] forms, lemmas, features, poss, relations;
		int[] heads;
		long[] flags;
		
		SimpleDependencyData sdd = null;
		
		// For every sentence in corpus
		if ( sentenceIndex < textCorpusStreamed.getSentencesLayer().size()) {

			// Get Sentence Layer
			Sentence sentence = textCorpusStreamed.getSentencesLayer().getSentence(sentenceIndex);
			// Extract Tokens from Sentence Layer
			Token[] token = textCorpusStreamed.getSentencesLayer().getTokens(sentence);
			int size = token.length;


			heads = new int[size];
			poss = new String[size];
			forms = new String[size];
			lemmas = new String[size];
			features = new String[size];
			relations = new String[size];
			flags = new long[size];
			
			// For every Token spec. Layers (layerToRead) are checked and
			// information extracted
			for (int index = 0; index < token.length; index++) {
				

				forms[index] = ensureDummy(token[index].getString(), "<empty>"); //$NON-NLS-1$
				
				//lemma
				if(textCorpusStreamed.getLemmasLayer() !=null){
				lemmas[index] = ensureValid(textCorpusStreamed.getLemmasLayer()
						.getLemma(token[index]).getString());
				} else {
					lemmas[index] = ""; //$NON-NLS-1$
				}
				
				//pos
				if(textCorpusStreamed.getPosTagsLayer() !=null){
				poss[index] = ensureValid(textCorpusStreamed.getPosTagsLayer()
						.getTag(token[index]).getString());
				} else {
					poss[index] = ""; //$NON-NLS-1$
				}

				
				//Morphology
				String morphfeatures = ""; //$NON-NLS-1$
				if (textCorpusStreamed.getMorphologyLayer() != null) {
					if (textCorpusStreamed.getMorphologyLayer().getAnalysis(
							token[index]) != null) {
						Feature feature[] = textCorpusStreamed.getMorphologyLayer()
								.getAnalysis(token[index]).getFeatures();
						StringBuilder sb = new StringBuilder(
								feature.length * 30);
						for (int f = 0; f < feature.length; f++) {
							// Only Extract Values of Features ?
							sb.append(feature[f].getName()).append("=") //$NON-NLS-1$
									.append(feature[f].getValue());
							if (f < feature.length - 1)
								sb.append("|"); //$NON-NLS-1$
						}
						morphfeatures = sb.toString();
					}
				}
				
				features[index] = ensureValid(morphfeatures);

				
				//dependency stuff
				if (textCorpusStreamed.getDependencyParsingLayer() != null) {
					Dependency[] dep = textCorpusStreamed.getDependencyParsingLayer().getParse(sentenceIndex)
					.getDependencies();
					relations[index] = ensureValid(dep[index].getFunction());
			
					// check if dependent or root
					if (textCorpusStreamed.getDependencyParsingLayer()
							.getGovernorTokens(dep[index]) != null) {
						Token[] tSource = textCorpusStreamed
								.getDependencyParsingLayer().getGovernorTokens(
										dep[index]);

						/*
						 * workaround, we count dependency per sentence in out
						 * simple dependency data, tcf use global counting for
						 * dependencys: tSource[0].getOrder() -
						 * tokenOffset[0].getOrder will give us the index we
						 * need
						 */
						Sentence toksentence = textCorpusStreamed.getSentencesLayer()
								.getSentence(tSource[0]);
						Token[] tokenOffset = textCorpusStreamed.getSentencesLayer()
								.getTokens(toksentence);
						// System.out.println(st[0].getOrder());

						heads[index] = tSource[0].getOrder()
								- tokenOffset[0].getOrder();

					} else {
						// root
						heads[index] = DependencyConstants.DATA_HEAD_ROOT;
					}
				} else {
					//default value when sentence invalid
					heads[index] = DependencyConstants.DATA_HEAD_ROOT;
					relations[index] = ""; //$NON-NLS-1$
				}
				
				/*
				System.out.println(
						"Form " + forms[index] + " " +
						"| Lemma "+ lemmas[index] + " " +
						"| Feat "+ features[index] + " " +
						"| PoS "+ poss[index] + " " +
						"| Relations "+ relations[index] + " " +
						"| Head "+ heads[index]);
				*/
			}
			
			sdd = new SimpleDependencyData(forms, lemmas, features, poss,relations, heads, flags);
			sentenceIndex++;
			
		}

		
		return (SentenceData) sdd;
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceDataReader#close()
	 */
	@Override
	public void close() {
			try {
				textCorpusStreamed.close();
			} catch (WLFormatException e) {
				LoggerFactory.log(this, Level.SEVERE,
						"TextCorpusFormat Exception", e); //$NON-NLS-1$
			}

	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceDataReader#getDataType()
	 */
	@Override
	public ContentType getDataType() {
		return ContentTypeRegistry.getInstance().getType(
				DependencyConstants.CONTENT_TYPE_ID);
	}
	
	protected String ensureValid(String input) {
		return input==null ? "" : input; //$NON-NLS-1$
	}
	
	
	protected String ensureDummy(String input, String dummy) {
		return input==null ? dummy : input;
	}
	
	
	public static void main(String[] args) throws UnsupportedFormatException {
		
		File file = new File ("E:\\tcf04-karin-wl.xml"); //$NON-NLS-1$
		
		DefaultFileLocation dloc = new DefaultFileLocation(file);
		Options o = null;
		
		TCF04SentenceDataReader t4 = new TCF04SentenceDataReader();
		try {
			t4.init(dloc, o);
			while (t4.next() != null){
				t4.next();
			}
			System.out.println("Finished"); //$NON-NLS-1$
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
