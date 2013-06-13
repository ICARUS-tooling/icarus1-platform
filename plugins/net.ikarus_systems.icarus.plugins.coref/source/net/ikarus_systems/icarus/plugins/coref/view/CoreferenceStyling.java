/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.coref.view;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

import net.ikarus_systems.icarus.language.coref.helper.SpanBuffer;
import net.ikarus_systems.icarus.language.coref.text.CoreferenceEditorKit;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.util.annotation.HighlightType;
import net.ikarus_systems.icarus.util.annotation.HighlightUtils;
import net.ikarus_systems.icarus.util.annotation.HighlightUtils.OutlineHighlightPainter;
import net.ikarus_systems.icarus.util.annotation.HighlightUtils.UnderlineHighlightPainter;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class CoreferenceStyling {

	private CoreferenceStyling() {
		// no-op
	}
	
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
