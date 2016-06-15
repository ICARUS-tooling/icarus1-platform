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
package de.ims.icarus.language.dependency;

import de.ims.icarus.language.CompoundSentenceData;
import de.ims.icarus.language.DataType;
import de.ims.icarus.language.LanguageConstants;
import de.ims.icarus.language.SentenceData;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CompoundDependencyData extends CompoundSentenceData implements
		DependencySentenceData {

	private static final long serialVersionUID = -689009503616872916L;

	public CompoundDependencyData() {
		// no-op
	}

	public CompoundDependencyData(DependencySentenceData systemData) {
		super(systemData);
	}

	public CompoundDependencyData(DependencySentenceData systemData,
			DependencySentenceData goldData, DependencySentenceData userData) {
		super(systemData, goldData, userData);
	}

	@Override
	public void setData(DataType type, SentenceData data) {
		if(!(data instanceof DependencySentenceData))
			throw new IllegalArgumentException("Sentence data type not supported: "+data.getClass()); //$NON-NLS-1$

		super.setData(type, data);
	}

	@Override
	public DependencySentenceData getData(DataType type) {
		return (DependencySentenceData) super.getData(type);
	}

	@Override
	protected DependencySentenceData getFirstSet() {
		return (DependencySentenceData) super.getFirstSet();
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencySentenceData#getIndex()
	 */
	@Override
	public int getIndex() {
		DependencySentenceData data = getFirstSet();
		return data==null ? -1 : data.getIndex();
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencySentenceData#getForm(int)
	 */
	@Override
	public String getForm(int index) {
		DependencySentenceData data = getFirstSet();
		return data==null ? null : data.getForm(index);
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencySentenceData#getPos(int)
	 */
	@Override
	public String getPos(int index) {
		DependencySentenceData data = getFirstSet();
		return data==null ? null : data.getPos(index);
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencySentenceData#getRelation(int)
	 */
	@Override
	public String getRelation(int index) {
		DependencySentenceData data = getFirstSet();
		return data==null ? null : data.getRelation(index);
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencySentenceData#getLemma(int)
	 */
	@Override
	public String getLemma(int index) {
		DependencySentenceData data = getFirstSet();
		return data==null ? null : data.getLemma(index);
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencySentenceData#getFeatures(int)
	 */
	@Override
	public String getFeatures(int index) {
		DependencySentenceData data = getFirstSet();
		return data==null ? null : data.getFeatures(index);
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencySentenceData#getHead(int)
	 */
	@Override
	public int getHead(int index) {
		DependencySentenceData data = getFirstSet();
		return data==null ? LanguageConstants.DATA_UNDEFINED_VALUE : data.getHead(index);
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencySentenceData#isFlagSet(int, long)
	 */
	@Override
	public boolean isFlagSet(int index, long flag) {
		DependencySentenceData data = getFirstSet();
		return data==null ? false : data.isFlagSet(index, flag);
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencySentenceData#getFlags(int)
	 */
	@Override
	public long getFlags(int index) {
		DependencySentenceData data = getFirstSet();
		return data==null ? 0 : data.getFlags(index);
	}

}
