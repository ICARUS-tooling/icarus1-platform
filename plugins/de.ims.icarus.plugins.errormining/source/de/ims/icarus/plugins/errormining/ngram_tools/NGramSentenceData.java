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
package de.ims.icarus.plugins.errormining.ngram_tools;

import java.util.ArrayList;
import java.util.List;

import de.ims.icarus.language.Grammar;
import de.ims.icarus.language.annotation.AnnotatedSentenceData;
import de.ims.icarus.language.dependency.DependencyUtils;
import de.ims.icarus.language.dependency.MutableDependencyData.DependencyDataEntry;
import de.ims.icarus.util.annotation.Annotation;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class NGramSentenceData implements AnnotatedSentenceData {

	private static final long serialVersionUID = 3303973536847711267L;

	private List<DependencyDataEntry> items = new ArrayList<>();

	protected Annotation annotation = null; // TODO change to default value?

	public NGramSentenceData(int index){
		//noop

	}



	//TODO
	@Override
	public NGramSentenceData clone() {
		return this;
	}



	/**
	 * @see net.ikarus_systems.icarus.language.SentenceData#getForm(int)
	 */
	@Override
	public String getForm(int index) {
		return items.get(index).getForm();
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceData#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return items.isEmpty();
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceData#length()
	 */
	@Override
	public int length() {
		return items.size();
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceData#getSourceGrammar()
	 */
	@Override
	public Grammar getSourceGrammar() {
		return DependencyUtils.getDependencyGrammar();
	}

	/**
	 * @see net.ikarus_systems.icarus.language.annotation.AnnotatedSentenceData#getAnnotation()
	 */
	@Override
	public Annotation getAnnotation() {
		return annotation;
	}

	/**
	 * @see de.ims.icarus.ui.text.ikarus_systems.icarus.ui.helper.TextItem#getText()
	 */
	@Override
	public String getText() {
		return null;
	}

	/**
	 * @see de.ims.icarus.language.SentenceData#getIndex()
	 */
	@Override
	public int getIndex() {
		return -1;
	}

	/**
	 * @see de.ims.icarus.language.SentenceData#getProperty(int, java.lang.String)
	 */
	@Override
	public Object getProperty(int index, String key) {
		return null;
	}

	/**
	 * @see de.ims.icarus.language.SentenceData#getPos(int)
	 */
	@Override
	public String getPos(int index) {
		return null;
	}

	/**
	 * @see de.ims.icarus.language.SentenceData#getLemma(int)
	 */
	@Override
	public String getLemma(int index) {
		return null;
	}

	/**
	 * @see de.ims.icarus.language.SentenceData#getFeatures(int)
	 */
	@Override
	public String getFeatures(int index) {
		return null;
	}

	/**
	 * @see de.ims.icarus.language.SentenceData#isFlagSet(int, long)
	 */
	@Override
	public boolean isFlagSet(int index, long flag) {
		return false;
	}

	/**
	 * @see de.ims.icarus.language.SentenceData#getFlags(int)
	 */
	@Override
	public long getFlags(int index) {
		return 0;
	}



	/**
	 * @see de.ims.icarus.language.SentenceData#getProperty(java.lang.String)
	 */
	@Override
	public Object getProperty(String key) {
		return null;
	}

}

