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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.SentenceDataWriter;
import de.ims.icarus.language.UnsupportedSentenceDataException;
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
import de.tuebingen.uni.sfs.wlf1.io.WLDObjector;
import de.tuebingen.uni.sfs.wlf1.io.WLFormatException;
import de.tuebingen.uni.sfs.wlf1.tc.api.Dependency;
import de.tuebingen.uni.sfs.wlf1.tc.api.DependencyParsingLayer;
import de.tuebingen.uni.sfs.wlf1.tc.api.Feature;
import de.tuebingen.uni.sfs.wlf1.tc.api.LemmasLayer;
import de.tuebingen.uni.sfs.wlf1.tc.api.MorphologyLayer;
import de.tuebingen.uni.sfs.wlf1.tc.api.PosTagsLayer;
import de.tuebingen.uni.sfs.wlf1.tc.api.Sentence;
import de.tuebingen.uni.sfs.wlf1.tc.api.SentencesLayer;
import de.tuebingen.uni.sfs.wlf1.tc.api.Token;
import de.tuebingen.uni.sfs.wlf1.tc.api.TokensLayer;
import de.tuebingen.uni.sfs.wlf1.tc.xb.TextCorpusStored;
import de.tuebingen.uni.sfs.wlf1.xb.WLData;


/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class TCF04SentenceDataWriter implements SentenceDataWriter {

	protected OutputStream fos;
	protected TextCorpusStored textCorpusStored;

	// options
	protected String language;
	protected String parser;
	protected boolean multiGovernors;
	protected boolean emptyNode;

	/**
	 * @see de.ims.icarus.language.SentenceDataWriter#init(de.ims.icarus.util.location.Location,
	 *      de.ims.icarus.util.Options)
	 */
	@Override
	public void init(Location location, Options options) throws IOException,
			UnsupportedLocationException {

		Path file = location.getLocalPath();

		if (file == null)
			throw new IllegalArgumentException("Filelocation Undef"); //$NON-NLS-1$

		if (options == null){
			options = Options.emptyOptions;
		}


		fos = Files.newOutputStream(file);

		// TODO extend
		language = "de"; //$NON-NLS-1$
		parser = "tiger"; //$NON-NLS-1$
		multiGovernors = false;
		emptyNode = false;

		// create TextCorpus object, specifying that the data will be in German
		// language (de)
		textCorpusStored = new TextCorpusStored(language);

	}

	/**
	 * @see de.ims.icarus.language.SentenceDataWriter#write(de.ims.icarus.language.SentenceData)
	 */
	@Override
	public void write(SentenceData data) throws IOException,
			UnsupportedSentenceDataException {

		//null check
		if (data == null){
			return;
		}

		// create needed annotation layers
		TokensLayer tokenLayer = textCorpusStored.createTokensLayer();
		SentencesLayer sentenceLayer = textCorpusStored.createSentencesLayer();
		LemmasLayer lemmaLayer = textCorpusStored.createLemmasLayer();
		PosTagsLayer posLayer = textCorpusStored.createPosTagsLayer("stts"); //$NON-NLS-1$
		MorphologyLayer morphLayer = textCorpusStored.createMorphologyLayer();
		DependencyParsingLayer depLayer = textCorpusStored
				.createDependencyParsingLayer(parser, multiGovernors, emptyNode);

		SimpleDependencyData sdd;
		StringBuilder sb = new StringBuilder(1000);

		List<Feature> featureList = new ArrayList<Feature>();
		List<Dependency> dependencyParse = new ArrayList<Dependency>();
		String text = null;

		try {

			if (Thread.currentThread().isInterrupted())
				throw new InterruptedException();


			sdd = (SimpleDependencyData) data;

			List<Token> sentenceTokens = new ArrayList<Token>();

			for (int i = 0; i < sdd.length(); i++) {

				Token token = tokenLayer.addToken(sdd.getForm(i));
				sentenceTokens.add(token);
				text = text + token.getString();

				if (sb.length() == 0 || token.getString().equals(".")) //$NON-NLS-1$
					sb.append(token.getString());
				else {
					sb.append(" ").append(token.getString()); //$NON-NLS-1$
				}

				// lemma, pos stuff
				lemmaLayer.addLemma(sdd.getLemma(i), token);
				posLayer.addTag(sdd.getPos(i), token);

				// store feature string: like cat=noun|case=accusative|...
				String sfeatures = sdd.getFeatures(i);
				// only add features if there are features
				if (sfeatures != "") { //$NON-NLS-1$
					// split featurestring ( | split by char so use \\| )
					for (String keyValue : sfeatures.split("\\|")) { //$NON-NLS-1$
						String[] pairs = keyValue.split("="); //$NON-NLS-1$
						// System.out.println(pairs[0]);
						Feature feat = morphLayer.createFeature(pairs[0],
								pairs.length == 1 ? "" : pairs[1]); //$NON-NLS-1$
						featureList.add(feat);
					}
					morphLayer.addAnalysis(token, featureList);
					featureList.clear();
				}

				// System.out.print("Token: " + sdd.getForm(i) + " | ");
				// System.out.print("Head: " + sdd.getHead(i) + " | ");
				// System.out.print("PoS: " + sdd.getPos(i) + " | ");
				// System.out.println("Feat:" + sdd.getFeatures(i) + " | ");
				// System.out.print("Lemma: " + sdd.getLemma(i) + " | ");
				// System.out.println("Rel: " + sdd.getRelation(i));

			}

			// add sentence layer
			sentenceLayer.addSentence(sentenceTokens);

			// System.out.println(sb.toString());
			textCorpusStored.createTextLayer().addText(sb.toString());

			// one all tokens and sentences added we can add missing dependency
			// otherwise some tokens may not be in corpus to look up
			for (int s = 0; s < sentenceLayer.size(); s++) {
				Sentence sentence = sentenceLayer.getSentence(s);
				Token[] tokens = sentenceLayer.getTokens(sentence);

				/*
				 * simpledependencydata counts local per sentence, tcf use
				 * global counting for dependencys so we need to add a
				 * tokenOffset when rebuilding tcf from sdd. When the
				 * governorToken is calculated we simple add the tokenOffset
				 * from the first token in the sentence we are working at right
				 * now.
				 */
				int tokenOffset = tokens[0].getOrder();
				// System.out.println(tokenOffset);

				sdd = (SimpleDependencyData) data;

				// list need to be completed?
				for (int i = 0; i < sdd.length(); i++) {
					Token dependentToken = tokens[i];

					Dependency dep;

					// c9p
					if (sdd.getHead(i) != -1) {
						Token governorToken = tokenLayer.getToken(sdd
								.getHead(i) + tokenOffset);
						dep = depLayer.createDependency(sdd.getRelation(i),
								dependentToken, governorToken);
					} else {
						dep = depLayer.createDependency(sdd.getRelation(i),
								dependentToken);
					}
					dependencyParse.add(dep);
				}
				depLayer.addParse(dependencyParse);
				dependencyParse.clear();

			}

		} catch (InterruptedException e) {
			LoggerFactory.log(this, Level.SEVERE,
					"Write to File interrupted", e); //$NON-NLS-1$
		} finally {

			// wrap TextCorpus object into the object representing the annotated
			// data in the exchange format:
			WLData wlData = new WLData(textCorpusStored);
			// write the annotated data object into the output stream
			try {
				WLDObjector.write(wlData, fos);
			} catch (WLFormatException e) {
				LoggerFactory.log(this, Level.SEVERE,
						"TextCorpusFormat Exception", e); //$NON-NLS-1$
			}
		}

	}

	/**
	 * @see de.ims.icarus.language.SentenceDataWriter#close()
	 */
	@Override
	public void close() {
		try {
			fos.close();
		} catch (IOException e) {
			LoggerFactory.log(this, Level.SEVERE,
					"Error while closing FileOutputstream", e); //$NON-NLS-1$
		}
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataWriter#getDataType()
	 */
	@Override
	public ContentType getDataType() {
		return ContentTypeRegistry.getInstance().getType(
				DependencyConstants.CONTENT_TYPE_ID);
	}

	public static void main(String[] args) throws UnsupportedFormatException {

		Path fileIn = Paths.get("E:\\test.xml"); //$NON-NLS-1$
		DefaultFileLocation dloc = new DefaultFileLocation(fileIn);

		Path fileOut = Paths.get("E:\\test_out.xml"); //$NON-NLS-1$
		DefaultFileLocation dlocOut = new DefaultFileLocation(fileOut);

		Options o = null;

		TCF04SentenceDataReader t4 = new TCF04SentenceDataReader();
		TCF04SentenceDataWriter tw = new TCF04SentenceDataWriter();
		try {
			t4.init(dloc, o);
			tw.init(dlocOut, o);
			while (t4.next() != null) {
				tw.write(t4.next());
			}
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
