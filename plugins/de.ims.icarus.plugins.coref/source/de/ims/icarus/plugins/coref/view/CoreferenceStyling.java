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
package de.ims.icarus.plugins.coref.view;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

import de.ims.icarus.language.coref.text.CoreferenceEditorKit;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.util.annotation.HighlightType;
import de.ims.icarus.util.annotation.HighlightUtils;
import de.ims.icarus.util.annotation.HighlightUtils.OutlineHighlightPainter;
import de.ims.icarus.util.annotation.HighlightUtils.UnderlineHighlightPainter;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class CoreferenceStyling {

	private CoreferenceStyling() {
		// no-op
	}
	
	public static final HighlightType[] supportedHighlightTypes = {
		HighlightType.BACKGROUND,
		HighlightType.FOREGROUND,
		HighlightType.UNDERLINED,
		HighlightType.ITALIC,
		HighlightType.BOLD,
	};
	
	private static CoreferenceEditorKit editorKit;
	
	private static final Color baseColor = new Color(60, 235, 0);
	
	private static Map<Object, Object> colorMap;

	public static Color getClusterColor(int index) {
		if(colorMap==null) {
			colorMap = new HashMap<>();
		}
		
		Color col = (Color) colorMap.get(index);
		
		if(col==null) {
			while(colorMap.containsKey((col=UIUtil.generateRandomColor(baseColor))));
			colorMap.put(index, col);
		}
		
		return col;
	}
	
    private static HighlightPainter applyHighlightType(HighlightType type, Style style, Color col) {
    	HighlightPainter painter = null;
    	
    	switch (type) {
		case BACKGROUND:
			StyleConstants.setBackground(style, col);
			break;
			
		case FOREGROUND:
			StyleConstants.setForeground(style, col);
			break;
			
		case UNDERLINED:
			painter =  HighlightUtils.getPainter(col, UnderlineHighlightPainter.class);
			break;
			
		case OUTLINED:
			painter = HighlightUtils.getPainter(col, OutlineHighlightPainter.class);
			break;

		default:
			throw new IllegalArgumentException("Highlight type not supported: "+type); //$NON-NLS-1$
		}
    	
    	return painter;
    }
    
    public static CoreferenceEditorKit getSharedEditorKit() {
    	if(editorKit==null) {
    		editorKit = new CoreferenceEditorKit();
    	}
    	return editorKit;
    }
}
