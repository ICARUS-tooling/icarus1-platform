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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.coref.view.graph;

import java.io.Serializable;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import de.ims.icarus.language.coref.CorefMember;
import de.ims.icarus.plugins.jgraph.cells.GraphCell;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class CorefCellData<E extends CorefMember> implements Serializable, GraphCell {

	private static final long serialVersionUID = 5316908730346053116L;
	@XmlTransient
	protected String label;
	@XmlAttribute(required=false)
	protected long highlight = 0L;

	@XmlAttribute(required=false)
	protected boolean gold;

	@XmlElement
	@XmlJavaTypeAdapter(value=CorefConverter.class)
	protected E data;

	protected CorefCellData() {
		// no-op
	}

	protected CorefCellData(E data) {
		setData(data);
	}

	protected CorefCellData(E data, boolean gold) {
		setData(data);
		setGold(gold);
	}

	protected CorefCellData(E data, boolean gold, long highlight) {
		setData(data);
		setGold(gold);
		setHighlight(highlight);
	}

	/**
	 * @return the gold
	 */
	public boolean isGold() {
		return gold;
	}

	/**
	 * @param gold the gold to set
	 */
	public void setGold(boolean gold) {
		this.gold = gold;
	}

	protected void setData(E data) {
		if(data==null)
			throw new NullPointerException("Invalid data"); //$NON-NLS-1$

		this.data = data;
		label = null;
	}

	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return getLabel();
	}

	public long getHighlight() {
		return highlight;
	}

	public void setHighlight(long highlight) {
		this.highlight = highlight;
	}
}
