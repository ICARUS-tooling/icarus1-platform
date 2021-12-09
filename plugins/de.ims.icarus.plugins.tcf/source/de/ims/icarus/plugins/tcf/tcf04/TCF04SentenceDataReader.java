/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.tcf.tcf04;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.logging.Level;

import de.ims.icarus.language.LanguageConstants;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.SentenceDataReader;
import de.ims.icarus.language.dependency.DependencyConstants;
import de.ims.icarus.language.dependency.SimpleDependencyData;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.UnsupportedFormatException;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;
import de.ims.icarus.util.location.DefaultFileLocation;
import de.ims.icarus.util.location.Location;
import de.ims.icarus.util.location.UnsupportedLocationException;
import de.tuebingen.uni.sfs.wlf1.io.TextCorpusStreamed;
import de.tuebingen.uni.sfs.wlf1.io.WLFormatException;
import de.tuebingen.uni.sfs.wlf1.tc.api.Dependency;
import de.tuebingen.uni.sfs.wlf1.tc.api.Feature;
import de.tuebingen.uni.sfs.wlf1.tc.api.Sentence;
import de.tuebingen.uni.sfs.wlf1.tc.api.Token;
import de.tuebingen.uni.sfs.wlf1.tc.xb.TextCorpusLayerTag;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;



/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class TCF04SentenceDataReader implements SentenceDataReader {

	protected TextCorpusStreamed textCorpusStreamed;
	protected EnumSet<TextCorpusLayerTag> layersToRead;
	protected int sentenceIndex;

	/**
	 *
	 */
	public TCF04SentenceDataReader() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @throws WLFormatException
	 * @see de.ims.icarus.language.SentenceDataReader#init(de.ims.icarus.util.location.Location, de.ims.icarus.util.Options)
	 */
	@Override
	public void init(Location location, Options options) throws IOException,
			UnsupportedLocationException {

		Path file = location.getLocalPath();

		if(file == null)
			throw new IllegalArgumentException("Filelocation Undef"); //$NON-NLS-1$


		if(Files.notExists(file))
			throw new FileNotFoundException("Missing File: " //$NON-NLS-1$
											+file);

//		if (options == null){
//			options = Options.emptyOptions;
//		}


		// TODO extend with options FixME: Gold/System?!
		// specify which layer/layers annotations should be read in order to process
		layersToRead = EnumSet.of(
				TextCorpusLayerTag.TOKENS, TextCorpusLayerTag.SENTENCES,
				TextCorpusLayerTag.LEMMAS, TextCorpusLayerTag.POSTAGS,
				TextCorpusLayerTag.MORPHOLOGY,
				TextCorpusLayerTag.PARSING_DEPENDENCY);


		// System.out.println("Layers2Read: " + layersToRead);

		// TextCorpusStreamed object with the layers specified in (layersToRead)
		// and file-inputstream. This object will _only_ load the specified layers to
		// memory and skip other layers (if there are other layers inside the tcf)
		sentenceIndex = 0;

		try {
			textCorpusStreamed = new TextCorpusStreamed(Files.newInputStream(file), layersToRead);
		} catch (WLFormatException e) {
			LoggerFactory.log(this, Level.SEVERE,
					"TextCorpusFormat Exception", e); //$NON-NLS-1$
		}

	}

	/**
	 * @see de.ims.icarus.language.SentenceDataReader#next()
	 */
	@Override
	public SentenceData next() throws IOException, UnsupportedFormatException {
		String[] forms, lemmas, features, poss, relations;
		short[] heads;
		long[] flags;

		SimpleDependencyData sdd = null;

		// For every sentence in corpus
		if ( sentenceIndex < textCorpusStreamed.getSentencesLayer().size()) {

			// Get Sentence Layer
			Sentence sentence = textCorpusStreamed.getSentencesLayer().getSentence(sentenceIndex);
			// Extract Tokens from Sentence Layer
			Token[] token = textCorpusStreamed.getSentencesLayer().getTokens(sentence);
			int size = token.length;

			TObjectIntMap<Object> tokenMap = new TObjectIntHashMap<>(size);

			heads = new short[size];
			poss = new String[size];
			forms = new String[size];
			lemmas = new String[size];
			features = new String[size];
			relations = new String[size];
			flags = new long[size];

			// For every Token specified Layers (layerToRead) are checked and
			// the information extracted
			for (int index = 0; index < token.length; index++) {


				forms[index] = ensureDummy(token[index].getString(), "<empty>"); //$NON-NLS-1$

				tokenMap.put(token[index], index);

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

				//default value when sentence invalid
				heads[index] = LanguageConstants.DATA_UNDEFINED_VALUE;
				relations[index] = ""; //$NON-NLS-1$

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

			//dependency stuff
			if (textCorpusStreamed.getDependencyParsingLayer() != null) {
				Dependency[] deps = textCorpusStreamed.getDependencyParsingLayer().getParse(sentenceIndex)
				.getDependencies();

				for(Dependency dep : deps) {
					Token[] depTokens = textCorpusStreamed.getDependencyParsingLayer().getDependentTokens(dep);
					Token[] govTokens = textCorpusStreamed.getDependencyParsingLayer().getGovernorTokens(dep);

					for(int i=0; i<depTokens.length; i++) {
						int depIndex = tokenMap.get(depTokens[i]);

						if(govTokens==null) {
							// root
							heads[depIndex] = LanguageConstants.DATA_HEAD_ROOT;
						} else {
							int govIndex = tokenMap.get(govTokens[i]);

							heads[depIndex] = (short) govIndex;
						}

						relations[depIndex] = ensureValid(dep.getFunction());
					}
				}
			}

			sdd = new SimpleDependencyData(sentenceIndex, forms, lemmas, features, poss,relations, heads, flags);
//			DependencyUtils.fillProjectivityFlags(heads, flags);
			sentenceIndex++;

		}


		return sdd;
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataReader#close()
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
	 * @see de.ims.icarus.language.SentenceDataReader#getContentType()
	 */
	@Override
	public ContentType getContentType() {
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

		Path file = Paths.get("E:\\tcf04-karin-wl.xml"); //$NON-NLS-1$

		DefaultFileLocation dloc = new DefaultFileLocation(file);
		Options o = null;

		try (TCF04SentenceDataReader t4 = new TCF04SentenceDataReader()) {
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
