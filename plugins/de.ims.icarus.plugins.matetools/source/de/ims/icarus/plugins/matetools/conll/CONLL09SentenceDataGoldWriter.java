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
package de.ims.icarus.plugins.matetools.conll;

import is2.data.SentenceData09;
import is2.io.CONLLWriter09;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;

import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.SentenceDataWriter;
import de.ims.icarus.language.UnsupportedSentenceDataException;
import de.ims.icarus.language.dependency.DependencyConstants;
import de.ims.icarus.language.dependency.SimpleDependencyData;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;
import de.ims.icarus.util.location.Location;
import de.ims.icarus.util.location.UnsupportedLocationException;


/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class CONLL09SentenceDataGoldWriter implements SentenceDataWriter {

	protected CONLLWriter09 writer;
	protected boolean writeRoot;
	protected int outputFormat; // 0 (default) or 1

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

//		if (options == null){
//			options = Options.emptyOptions;
//		}

		//TODO extend me
		writeRoot = false;

		outputFormat = 0;

		writer = new CONLLWriter09(file.toString(), outputFormat);

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

		SentenceData09 currentData = new SentenceData09();

		SimpleDependencyData sdd;

		try {
			if (Thread.currentThread().isInterrupted())
					throw new InterruptedException();

				sdd = (SimpleDependencyData) data;
				currentData.init(LanguageUtils.getForms(sdd));

				initGold(currentData, sdd.length());

				//Sentence Debug
				/*
				for (int j = 0 ; j < sdd.getForms().length;j++){
					String [] t = sdd.getForms();
					System.out.println(t[j]);
				}
				*/

				for(short i=0; i<currentData.length(); i++) {
					/*
					System.out.print("Form: "+ sdd.getForm(i));
					System.out.print(" Head: "+ sdd.getHead(i));
					System.out.print(" PoS: "+ sdd.getPos(i));
					System.out.print(" Feat: "+ sdd.getFeatures(i));
					System.out.print(" Lemma: "+ sdd.getLemma(i));
					System.out.println(" Relation: "+ sdd.getRelation(i));
					*/

					currentData.forms[i] = sdd.getForm(i);
					currentData.heads[i] = sdd.getHead(i) + 1;
					currentData.gpos[i] = sdd.getPos(i);
					currentData.ofeats[i] = sdd.getFeatures(i);
					currentData.lemmas[i] = sdd.getLemma(i);
					currentData.labels[i] = sdd.getRelation(i);
				}

				writer.write(currentData, writeRoot);

		} catch (InterruptedException e) {
			LoggerFactory.log(this, Level.SEVERE,
					"Write to File interrupted", e); //$NON-NLS-1$
		} catch (IllegalArgumentException e) {
			LoggerFactory.log(this, Level.SEVERE,
					"CoNLL State Exception", e.getCause()); //$NON-NLS-1$
		} finally {
			writer.finishWriting();
		}

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
	 * @see de.ims.icarus.language.SentenceDataWriter#close()
	 */
	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.language.SentenceDataWriter#getDataType()
	 */
	@Override
	public ContentType getDataType() {
		return ContentTypeRegistry.getInstance().getType(
				DependencyConstants.CONTENT_TYPE_ID);
	}
}
