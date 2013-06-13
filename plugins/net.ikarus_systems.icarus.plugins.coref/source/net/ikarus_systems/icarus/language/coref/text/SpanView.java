/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.coref.text;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.TextAttribute;

import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.ParagraphView;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SpanView extends ParagraphView {

	public SpanView(Element elem) {
        super(elem);
        //setInsets((short)2,(short)2,(short)2,(short)2);
	}

	@Override
	public void paint(Graphics graphics, Shape alloc) {
		AttributeSet attr = getAttributes();
		
		Graphics2D g = (Graphics2D) graphics;
		
		Color clusterColor = (Color) attr.getAttribute(TextAttribute.BACKGROUND);
		if(clusterColor!=null) {
			Color c = g.getColor();
			
			g.setColor(clusterColor);
			g.fill(alloc);
			
			g.setColor(c);
		}
		
		super.paint(g, alloc);
	}

	@Override
	public int getResizeWeight(int axis) {
		return 0;
	}

	@Override
	public float getPreferredSpan(int axis) {
		return super.getPreferredSpan(axis);
	}

	@Override
	public float getMinimumSpan(int axis) {
		return getPreferredSpan(axis);
	}

	@Override
	public float getMaximumSpan(int axis) {
		return getPreferredSpan(axis);
	}
}
