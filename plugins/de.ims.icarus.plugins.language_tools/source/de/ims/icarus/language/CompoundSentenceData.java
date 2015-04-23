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
package de.ims.icarus.language;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CompoundSentenceData implements SentenceData, Cloneable {

	private static final long serialVersionUID = 4260253017719158901L;

	private SentenceData[] items;

	public CompoundSentenceData() {
		// no-op
	}

	public CompoundSentenceData(SentenceData systemData) {
		setData(DataType.SYSTEM, systemData);
	}

	public CompoundSentenceData(SentenceData systemData, SentenceData goldData, SentenceData userData) {
		setData(DataType.SYSTEM, systemData);
		setData(DataType.GOLD, goldData);
		setData(DataType.USER, userData);
	}

	public void setData(DataType type, SentenceData data) {
		if(type==null)
			throw new NullPointerException("Invalid type"); //$NON-NLS-1$

		if(items==null) {
			items = new SentenceData[DataType.values().length];
		}

		items[type.ordinal()] = data;
	}

	public SentenceData getData(DataType type) {
		if(type==null)
			throw new NullPointerException("Invalid type"); //$NON-NLS-1$

		return items==null ? null : items[type.ordinal()];
	}

	protected SentenceData getFirstSet() {
		if(items==null) {
			return null;
		}

		for(SentenceData data : items) {
			if(data!=null) {
				return data;
			}
		}

		return null;
	}

	/**
	 * @see de.ims.icarus.language.SentenceData#getForms()
	 */
	@Override
	public String getForm(int index) {
		SentenceData data = getFirstSet();
		return data==null ? null : data.getForm(index);
	}

	/**
	 * @see de.ims.icarus.language.SentenceData#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		SentenceData data = getFirstSet();
		return data==null ? true : data.isEmpty();
	}

	/**
	 * @see de.ims.icarus.language.SentenceData#length()
	 */
	@Override
	public int length() {
		SentenceData data = getFirstSet();
		return data==null ? 0 : data.length();
	}

	/**
	 * @see de.ims.icarus.language.SentenceData#getSourceGrammar()
	 */
	@Override
	public Grammar getSourceGrammar() {
		SentenceData data = getFirstSet();
		return data==null ? null : data.getSourceGrammar();
	}

	@Override
	public CompoundSentenceData clone() {
		// Fetch data
		SentenceData systemData = getData(DataType.SYSTEM);
		SentenceData goldData = getData(DataType.GOLD);
		SentenceData userData = getData(DataType.USER);

		// Clone data
		if(systemData!=null) {
			systemData = systemData.clone();
		}
		if(goldData!=null) {
			goldData = systemData.clone();
		}
		if(userData!=null) {
			userData = systemData.clone();
		}

		return new CompoundSentenceData(systemData, goldData, userData);
	}

	/**
	 * @see de.ims.icarus.ui.text.TextItem#getText()
	 */
	@Override
	public String getText() {
		SentenceData data = getFirstSet();
		return data==null ? null : data.getText();
	}

	/**
	 * @see de.ims.icarus.language.SentenceData#getIndex()
	 */
	@Override
	public int getIndex() {
		SentenceData data = getFirstSet();
		return data==null ? -1 : data.getIndex();
	}

	/**
	 * @see de.ims.icarus.language.SentenceData#getProperty(java.lang.String, int)
	 */
	@Override
	public Object getProperty(int index, String key) {
		SentenceData data = getFirstSet();
		return data==null ? null : data.getProperty(index, key);
	}
}
