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
package de.ims.icarus.language.dependency.annotation;

import de.ims.icarus.language.Grammar;
import de.ims.icarus.language.annotation.AnnotatedSentenceData;
import de.ims.icarus.language.dependency.DependencyData;
import de.ims.icarus.util.annotation.Annotation;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class AnnotatedDependencyData implements DependencyData, AnnotatedSentenceData {
	
	private static final long serialVersionUID = -883053201659702672L;
	
	private final DependencyData source;
	private Annotation annotation;

	public AnnotatedDependencyData(DependencyData source, Annotation annotation) {
		if(source==null)
			throw new NullPointerException("Invalid source"); //$NON-NLS-1$
		
		this.source = source;
		this.annotation = annotation;
	}

	public AnnotatedDependencyData(DependencyData source) {
		this(source, null);
	}

	@Override
	public AnnotatedDependencyData clone() {
		return new AnnotatedDependencyData(source, annotation);
	}

	/**
	 * @see de.ims.icarus.language.SentenceData#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return source.isEmpty();
	}

	/**
	 * @see de.ims.icarus.language.SentenceData#length()
	 */
	@Override
	public int length() {
		return source.length();
	}

	/**
	 * @see de.ims.icarus.language.SentenceData#getSourceGrammar()
	 */
	@Override
	public Grammar getSourceGrammar() {
		return source.getSourceGrammar();
	}

	/**
	 * @see de.ims.icarus.ui.helper.TextItem#getText()
	 */
	@Override
	public String getText() {
		return source.getText();
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencyData#getForm(int)
	 */
	@Override
	public String getForm(int index) {
		return source.getForm(index);
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencyData#getPos(int)
	 */
	@Override
	public String getPos(int index) {
		return source.getPos(index);
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencyData#getRelation(int)
	 */
	@Override
	public String getRelation(int index) {
		return source.getRelation(index);
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencyData#getLemma(int)
	 */
	@Override
	public String getLemma(int index) {
		return source.getLemma(index);
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencyData#getFeatures(int)
	 */
	@Override
	public String getFeatures(int index) {
		return source.getFeatures(index);
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencyData#getHead(int)
	 */
	@Override
	public int getHead(int index) {
		return source.getHead(index);
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencyData#isFlagSet(int, long)
	 */
	@Override
	public boolean isFlagSet(int index, long flag) {
		return source.isFlagSet(index, flag);
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencyData#getFlags(int)
	 */
	@Override
	public long getFlags(int index) {
		return source.getFlags(index);
	}

	/**
	 * @see de.ims.icarus.language.annotation.AnnotatedSentenceData#getAnnotation()
	 */
	@Override
	public Annotation getAnnotation() {
		return annotation;
	}

	public void setAnnotation(Annotation annotation) {
		this.annotation = annotation;
	}
}
