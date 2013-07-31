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
package de.ims.icarus.util.annotation;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.JTextComponent;
import javax.swing.text.LayeredHighlighter.LayerPainter;
import javax.swing.text.Position;
import javax.swing.text.View;

import de.ims.icarus.logging.LoggerFactory;


/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class HighlightUtils {
	
	private static Map<Integer, List<WeakReference<HighlightPainter>>> painters;
	
	@SuppressWarnings("unchecked")
	public static <P extends HighlightPainter> P getPainter(Color c,
			Class<P> clazz) {
		if(c==null || clazz==null) 
			throw new IllegalArgumentException();
		
		if(painters==null) {
			painters = new HashMap<>();
		}
		
		int col = c.getRGB();
		List<WeakReference<HighlightPainter>> list = painters.get(col);
		if(list==null) {
			list = new LinkedList<WeakReference<HighlightPainter>>();
			painters.put(col, list);
		}
		
		// check if there is a usable painter in the list and remove
		// dangling references
		WeakReference<HighlightPainter> ref;
		for(Iterator<WeakReference<HighlightPainter>> i = list.iterator(); i.hasNext();) {
			ref = i.next();
			if(ref.get()==null)
				i.remove();
			else if(clazz.isInstance(ref.get()))
				return (P)ref.get();
		}
		
		// no painter available -> create a new one
		try {
			HighlightPainter painter = clazz.getConstructor(Color.class).newInstance(c);
			list.add(new WeakReference<HighlightPainter>(painter));
			
			return (P) painter;
		} catch (Exception e) {
			LoggerFactory.log(HighlightUtils.class, Level.SEVERE, String.format(
					"Failed to instantiate painter: class=%s color=#%s",  //$NON-NLS-1$
					clazz.getName(), Integer.toHexString(c.getRGB())), e);
		}
		
		return null;
	}


	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public abstract static class AbstractHighlightPainter extends
			LayerPainter {

		protected final Color color;
		
		public AbstractHighlightPainter(Color c) {
			color = c;
		}

		public void paint(Graphics g, int offs0, int offs1, Shape bounds,
				JTextComponent c) {
			// Do nothing: this method will never be called
		}

		public Shape paintLayer(Graphics g, int offs0, int offs1,
				Shape bounds, JTextComponent c, View view) {
			g.setColor(color == null ? c.getSelectionColor() : color);

			Rectangle alloc = null;
			if (offs0 == view.getStartOffset()
					&& offs1 == view.getEndOffset()) {
				if (bounds instanceof Rectangle) {
					alloc = (Rectangle) bounds;
				} else {
					alloc = bounds.getBounds();
				}
			} else {
				try {
					Shape shape = view.modelToView(offs0,
							Position.Bias.Forward, offs1,
							Position.Bias.Backward, bounds);
					alloc = (shape instanceof Rectangle) ? (Rectangle) shape
							: shape.getBounds();
				} catch (BadLocationException e) {
					return null;
				}
			}

			FontMetrics fm = c.getFontMetrics(c.getFont());
			
			paint(g, alloc, fm);

			return alloc;
		}
		
		protected abstract void paint(Graphics g, Rectangle alloc, FontMetrics fm);
	}


	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public static class UnderlineHighlightPainter extends
			AbstractHighlightPainter {
		
		public UnderlineHighlightPainter(Color c) {
			super(c);
		}

		@Override
		protected void paint(Graphics g, Rectangle alloc, FontMetrics fm) {
			int baseline = alloc.y + alloc.height - fm.getDescent() + 1;
			g.drawLine(alloc.x, baseline, alloc.x + alloc.width, baseline);
			g.drawLine(alloc.x, baseline + 1, alloc.x + alloc.width,
					baseline + 1);
		}
	}


	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public static class OutlineHighlightPainter extends
			AbstractHighlightPainter {
		
		public OutlineHighlightPainter(Color c) {
			super(c);
		}

		@Override
		protected void paint(Graphics g, Rectangle alloc, FontMetrics fm) {
			int height = alloc.height - fm.getDescent() + 1;
			g.drawRect(alloc.x-2, alloc.y, alloc.width+2, height);
		}
	}


	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public static class BackgroundHighlightPainter extends
			AbstractHighlightPainter {
		
		public BackgroundHighlightPainter(Color c) {
			super(c);
		}

		@Override
		protected void paint(Graphics g, Rectangle alloc, FontMetrics fm) {
			int height = alloc.height - fm.getDescent() + 1;
			g.fillRect(alloc.x, alloc.y, alloc.width, height);
		}
	}
}
