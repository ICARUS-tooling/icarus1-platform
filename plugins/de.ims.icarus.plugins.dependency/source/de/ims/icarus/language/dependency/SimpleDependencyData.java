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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.ims.icarus.language.BasicSentenceData;
import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.util.CompactProperties;
import de.ims.icarus.util.mem.HeapMember;
import de.ims.icarus.util.mem.Link;
import de.ims.icarus.util.mem.Primitive;


/**
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@HeapMember
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SimpleDependencyData extends BasicSentenceData<CompactProperties> implements DependencyData {

	private static final long serialVersionUID = -6877590672027658180L;

	@Primitive
	private int index;

	@Link
	@XmlElement(name="pos")
	protected String[] poss;

	@Link
	@XmlElement(name="lemma")
	protected String[] lemmas;

	@Link
	@XmlElement(name="feature")
	protected String[] features;

	@Link
	@XmlElement(name="relation")
	protected String[] relations;

	@Link
	@XmlElement(name="head")
	protected short[] heads;

	@Link
	@XmlElement(name="flag", required=false)
	protected long[] flags;

	public SimpleDependencyData(int index, String[] forms, String[] lemmas,
			String[] features, String[] poss, String[] relations, short[] heads, long[] flags) {
		super(forms);
		this.index = index;
		this.lemmas = lemmas;
		this.features = features;
		this.poss = poss;
		this.relations = relations;
		this.heads = heads;
		this.flags = flags;
	}

	public SimpleDependencyData(DependencyData source) {
		super(source);

		int size = source.length();

		index = source.getIndex();
		lemmas = new String[size];
		features = new String[size];
		poss = new String[size];
		relations = new String[size];
		heads = new short[size];

		for(int index=0; index<size; index++) {
			lemmas[index] = source.getLemma(index);
			features[index] = source.getFeatures(index);
			poss[index] = source.getPos(index);
			relations[index] = source.getRelation(index);
			heads[index] = (short) source.getHead(index);
			flags[index] = source.getFlags(index);
		}
	}

	public SimpleDependencyData() {
		// Creates the empty sentence

		index = -1;
		lemmas = new String[0];
		features = new String[0];
		poss = new String[0];
		relations = new String[0];
		heads = new short[0];
	}

	@Override
	public Object getProperty(int index, String key) {
		switch (key) {

		case FORM_KEY:
			return getForm(index);

		case LEMMA_KEY:
			return getLemma(index);

		case DEPREL_KEY:
			return getRelation(index);

		case HEAD_KEY:
			return getHead(index);

		case POS_KEY:
			return getPos(index);

		case FEATURES_KEY:
			return getFeatures(index);

		default:
			return super.getProperty(index, key);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(200);

		for (int i = 0; i < forms.length; i++) {
			sb.append(i + 1).append(": ").append(forms[i]).append(" ").append( //$NON-NLS-1$ //$NON-NLS-2$
					LanguageUtils.getHeadLabel(heads[i])).append(" ").append( //$NON-NLS-1$
					poss[i]).append(" ").append(relations[i]).append(" ").append( //$NON-NLS-1$ //$NON-NLS-2$
					lemmas[i]).append(" ").append(features[i]); //$NON-NLS-1$
			if (i < forms.length - 1)
				sb.append("\n"); //$NON-NLS-1$
		}

		return sb.toString();
	}

	@Override
	public SimpleDependencyData clone() {
		return new SimpleDependencyData(this);
	}

	@Override
	public String getRelation(int index) {
		return relations[index];
	}

	@Override
	public String getForm(int index) {
		return forms[index];
	}

	@Override
	public int getHead(int index) {
		return heads[index];
	}

	@Override
	public String getPos(int index) {
		return poss[index];
	}

	@Override
	public String getFeatures(int index) {
		return features[index];
	}

	@Override
	public String getLemma(int index) {
		return lemmas[index];
	}

	@Override
	public boolean isFlagSet(int index, long flag) {
		return flags!=null && (flags[index] & flag)==flag;
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencyData#getFlags(int)
	 */
	@Override
	public long getFlags(int index) {
		return flags==null ? 0 : flags[index];
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencyData#getIndex()
	 */
	@Override
	public int getIndex() {
		return index;
	}
}
