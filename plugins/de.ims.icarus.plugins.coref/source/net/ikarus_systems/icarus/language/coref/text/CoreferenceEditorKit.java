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

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferenceEditorKit extends StyledEditorKit {

	private static final long serialVersionUID = -4650749951539233462L;

	ViewFactory defaultFactory = new CoreferenceViewFactory();
	
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
	public class CoreferenceViewFactory implements ViewFactory {

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
	public class CorefParagraphView extends ParagraphView {
		
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


	public class CorefLabelView extends LabelView {
		
		private CorefParagraphView box;
		
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
	
		@Override
		public void paint(Graphics g, Shape a) {
			AttributeSet attr = getAttributes();
			if(attr.isDefined(CoreferenceDocument.PARAM_FILL_COLOR)) {
				Color col = (Color) attr.getAttribute(CoreferenceDocument.PARAM_FILL_COLOR);
				Rectangle alloc = a instanceof Rectangle ? (Rectangle)a : a.getBounds();

				if(box==null) {
					box = findBoxParent();
				}
				
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
		}

		@Override
		public String getToolTipText(float x, float y, Shape allocation) {
			AttributeSet attr = getAttributes();
			if(attr.isDefined(CoreferenceDocument.PARAM_CLUSTER_ID)) {
				return String.format("Cluster-Id: %d", attr.getAttribute(CoreferenceDocument.PARAM_CLUSTER_ID)); //$NON-NLS-1$
			}
			return super.getToolTipText(x, y, allocation);
		}
		
		/*private static final int height = 14;

		@Override
		public float getMinimumSpan(int axis) {
			if(axis==View.Y_AXIS) {
				return height;
			}
			return super.getMinimumSpan(axis);
		}

		@Override
		public float getPreferredSpan(int axis) {
			if(axis==View.Y_AXIS) {
				return isSuperscript() ? height+height/3 : height;
			}
			return super.getPreferredSpan(axis);
		}

		@Override
		public float getMaximumSpan(int axis) {
			if(axis==View.Y_AXIS) {
				return height;
			}
			return super.getMaximumSpan(axis);
		}*/


		/*@Override
	    public int getBreakWeight(int axis, float pos, float len) {
	        if (axis == View.X_AXIS) {
	            checkPainter();
	            int p0 = getStartOffset();
	            int p1 = getGlyphPainter().getBoundedPosition(this, p0, pos, len);
	            if (p1 == p0) {
	                // can't even fit a single character
	                return View.BadBreakWeight;
	            }
	            try {
	                //if the view contains line break char return forced break
	                if (getDocument().getText(p0, p1 - p0).indexOf("\r") >= 0) { //$NON-NLS-1$
	                    return View.ForcedBreakWeight;
	                }
	            }
	            catch (BadLocationException ex) {
	                //should never happen
	            }
	        }
	        return super.getBreakWeight(axis, pos, len);
	    }*/
	
	    /*@Override
	    public View breakView(int axis, int p0, float pos, float len) {
	        if (axis == View.X_AXIS) {
	            checkPainter();
	            int p1 = getGlyphPainter().getBoundedPosition(this, p0, pos, len);
	            try {
	                //if the view contains line break char break the view
	                int index = getDocument().getText(p0, p1 - p0).indexOf("\r"); //$NON-NLS-1$
	                if (index >= 0) {
	                    GlyphView v = (GlyphView) createFragment(p0, p0 + index + 1);
	                    return v;
	                }
	            }
	            catch (BadLocationException ex) {
	                //should never happen
	            }
			}
			return super.breakView(axis, p0, pos, len);
		}*/
	}
	
	
}
