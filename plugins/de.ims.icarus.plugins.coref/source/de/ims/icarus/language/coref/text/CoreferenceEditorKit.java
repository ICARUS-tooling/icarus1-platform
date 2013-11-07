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
package de.ims.icarus.language.coref.text;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.config.ConfigRegistry.Handle;
import de.ims.icarus.language.coref.CoreferenceUtils;
import de.ims.icarus.language.coref.Span;
import de.ims.icarus.util.Filter;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferenceEditorKit extends StyledEditorKit {

	private static final long serialVersionUID = -4650749951539233462L;

	static ViewFactory defaultFactory = new CoreferenceViewFactory();
	
	public CoreferenceEditorKit() {
		// no-op
	}

	@Override
	public ViewFactory getViewFactory() {
		return defaultFactory;
	}

	@Override
	public Document createDefaultDocument() {
		return new CoreferenceDocument();
		//return new DefaultStyledDocument();
	}
	/**
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public static class CoreferenceViewFactory implements ViewFactory {

		public CoreferenceViewFactory() {
			// no-op
		}

		/**
		 * @see javax.swing.text.ViewFactory#create(javax.swing.text.Element)
		 */
		@Override
		public View create(Element elem) {
			//System.out.println(elem.toString());
			//System.out.println(elem.getName());
			
	        switch(elem.getName()) {
	        case AbstractDocument.ContentElementName:
	        	return new CorefLabelView(elem);
				
	        case AbstractDocument.ParagraphElementName:
				return new CorefParagraphView(elem);
				
	        case AbstractDocument.SectionElementName:
				return new BoxView(elem, View.Y_AXIS);
				
	        case StyleConstants.ComponentElementName:
				return new ComponentView(elem);
				
			case StyleConstants.IconElementName:
				return new IconView(elem);
				
			default:
		        return new LabelView(elem);
	        }
		}
	}


	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public static class CorefParagraphView extends ParagraphView {
		
		private Rectangle currentRun = new Rectangle();
		
	    public CorefParagraphView(Element elem) {
	        super(elem);
	    }
	
	    /*@Override
	    public void layout(int width, int height) {
	        super.layout(Short.MAX_VALUE, height);
	    }
	
	    @Override
	    public float getMinimumSpan(int axis) {
	        return super.getPreferredSpan(axis);
	    }*/
	    
	    @Override
	    protected void paintChild(Graphics g, Rectangle alloc, int index) {
	    	currentRun.setBounds(alloc);
	    	
	    	super.paintChild(g, alloc, index);
	    }
	    
	    public Rectangle getCurrentRun() {
	    	return currentRun;
	    }
	}


	public static class CorefLabelView extends LabelView {
		
		private CorefParagraphView box;
		
		private Handle markupHandle = ConfigRegistry.getGlobalRegistry().getHandle(
				"plugins.coref.appearance.text.clusterMarkup"); //$NON-NLS-1$
		
	    public CorefLabelView(Element elem) {
	        super(elem);
	    }
	    
	    private CorefParagraphView findBoxParent() {
	    	View v = this;
	    	while(v!=null) {
	    		v = v.getParent();
	    		if(v instanceof CorefParagraphView) {
	    			return (CorefParagraphView) v;
	    		}
	    	}
	    	
	    	return null;
	    }
	
		/**
		 * @see javax.swing.text.View#getDocument()
		 */
		@Override
		public CoreferenceDocument getDocument() {
			return (CoreferenceDocument) super.getDocument();
		}

		@Override
		public void paint(Graphics g, Shape a) {
			AttributeSet attr = getAttributes();
			
			CoreferenceDocument doc = getDocument();
			Span span = null;
			Filter markupFilter = doc.getMarkupFilter();

			if(markupFilter!=null && attr.isDefined(CoreferenceDocument.PARAM_SPAN)) {
				span = (Span) attr.getAttribute(CoreferenceDocument.PARAM_SPAN);
			}
			
			if(attr.isDefined(CoreferenceDocument.PARAM_FILL_COLOR)) {
				Color col = (Color) attr.getAttribute(CoreferenceDocument.PARAM_FILL_COLOR);
				Rectangle alloc = a instanceof Rectangle ? (Rectangle)a : a.getBounds();

				if(box==null) {
					box = findBoxParent();
				}
				
				if(box!=null) {
					Rectangle run = box.getCurrentRun();
					//System.out.printf("alloc=%s run=%s\n", alloc, run);
					
					int x = alloc.x;
					int y = run.y+1;
					int w = alloc.width;
					int h = run.height-2;
					
					Color c = g.getColor();
					g.setColor(col);
					g.fillRect(x, y, w, h);
					g.setColor(c);
				}
				
			} else if(attr.isDefined(CoreferenceDocument.PARAM_UNDERLINE_COLOR)) {
				Color col = (Color) attr.getAttribute(CoreferenceDocument.PARAM_UNDERLINE_COLOR);
				Rectangle alloc = a instanceof Rectangle ? (Rectangle)a : a.getBounds();
				
				int y = alloc.y+alloc.height - (int) getGlyphPainter().getDescent(this) + 1;

				Color c = g.getColor();
				g.setColor(col);
				g.drawLine(alloc.x, y, alloc.x+alloc.width, y);
				g.setColor(c);
			}
			
			super.paint(g, a);

			Color highlightColor = null;
			
			if(span!=null && markupFilter.accepts(span)) {
				highlightColor = ConfigRegistry.getGlobalRegistry().getColor(markupHandle);
			} else if(attr.isDefined(CoreferenceDocument.PARAM_HIGHLIGHT_COLOR)) {
				highlightColor = (Color) attr.getAttribute(CoreferenceDocument.PARAM_HIGHLIGHT_COLOR);
			}
			
			if(highlightColor!=null) {
				int highlight = (int) attr.getAttribute(CoreferenceDocument.PARAM_HIGHLIGHT_TYPE);
				Rectangle alloc = a instanceof Rectangle ? (Rectangle)a : a.getBounds();

				if(box==null) {
					box = findBoxParent();
				}
				
				Rectangle run = box.getCurrentRun();
				//System.out.printf("alloc=%s run=%s\n", alloc, run);
				
				int x = alloc.x-1;
				int y = run.y;
				int x2 = x+alloc.width;
				int y2 = y+run.height-1;
				
				Color c = g.getColor();
				g.setColor(highlightColor);
				// Always draw horizontal lines
				g.drawLine(x, y, x2, y);
				g.drawLine(x, y2, x2, y2);
				// Draw vertical lines if required
				if(CoreferenceDocument.isHighlightBegin(highlight)) {
					g.drawLine(x, y, x, y2);
				}
				if(CoreferenceDocument.isHighlightEnd(highlight)) {
					g.drawLine(x2, y, x2, y2);
				}
				g.setColor(c);
			}
		}

		@Override
		public String getToolTipText(float x, float y, Shape allocation) {
			AttributeSet attr = getAttributes();
			if(attr.isDefined(CoreferenceDocument.PARAM_SPAN)) {
				Span span = (Span) attr.getAttribute(CoreferenceDocument.PARAM_SPAN);
				return CoreferenceUtils.getSpanTooltip(span, null);
			}
			return super.getToolTipText(x, y, allocation);
		}
	}
	
	
}
