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

import de.ims.icarus.plugins.jgraph.cells.GraphCell;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class CorefCellData<E extends Object> implements Serializable, GraphCell {

	private static final long serialVersionUID = 5316908730346053116L;

	public static final int FALSE_PREDICTED = 1;
	public static final int MISSING_GOLD = 2;

	protected String label;
	
	protected int type = 0;
	protected long highlight = 0L;
	
	protected E data;
	
	protected CorefCellData() {
		// no-op
	}

	protected CorefCellData(E data) {
		setData(data);
	}
	
	protected CorefCellData(E data, int type) {
		setData(data);
		setType(type);
	}
	
	protected CorefCellData(E data, int type, long highlight) {
		setData(data);
		setType(type);
		setHighlight(highlight);
	}
	
	protected void setData(E data) {
		if(data==null)
			throw new NullPointerException("Invalid data"); //$NON-NLS-1$
		
		this.data = data;
		label = null;
	}
	
	protected String createLabel() {
		return data.toString();
	}

	public String getLabel() {
		if(label==null) {
			label = createLabel();
		}
		return label;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return getLabel();
	}

	public boolean isFalsePredicted() {
		return type==FALSE_PREDICTED;
	}
	
	public boolean isMissingGold() {
		return type==MISSING_GOLD;
	}

	public long getHighlight() {
		return highlight;
	}

	public void setHighlight(long highlight) {
		this.highlight = highlight;
	}
}
