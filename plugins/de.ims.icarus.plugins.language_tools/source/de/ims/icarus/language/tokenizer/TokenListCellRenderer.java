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
package de.ims.icarus.language.tokenizer;

import java.awt.Component;

import javax.swing.JList;

import de.ims.icarus.ui.list.TooltipListCellRenderer;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TokenListCellRenderer extends TooltipListCellRenderer {

	private static final long serialVersionUID = -4509333264353657850L;
	
	private final char delimiter;
	
	public static final char DEFAULT_DELIMITER = (char)0xFE4F; // ﹏ WAVY LOW LINE U+FE4F

	public TokenListCellRenderer() {
		this(DEFAULT_DELIMITER);
	}

	public TokenListCellRenderer(char delimiter) {
		this.delimiter = delimiter;
	}

	/**
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList<?> list,
			Object value, int index, boolean isSelected, boolean cellHasFocus) {
		
		if(value instanceof String[]) {
			String[] tokens = (String[]) value;
			StringBuilder sb = new StringBuilder(tokens.length*13);
			
			for(int i=0; i<tokens.length; i++) {
				if(i>0) {
					sb.append(delimiter);
				}
				
				sb.append(tokens[i]);
			}
			
			value = sb.toString();
		}
		
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		
		return this;
	}

}
