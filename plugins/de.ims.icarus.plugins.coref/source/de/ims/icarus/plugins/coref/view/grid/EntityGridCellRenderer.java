/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.coref.view.grid;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import de.ims.icarus.plugins.coref.view.grid.labels.GridLabelBuilder;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class EntityGridCellRenderer extends JComponent implements TableCellRenderer {

	private static final long serialVersionUID = 8877308537117345014L;
	
	protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

	protected Color unselectedForeground;
    protected Color unselectedBackground;
    
    protected Color missingGoldNodeColor = Color.red;
    protected Color falseNodeColor = Color.green;
    
    protected EntityGridNode node;
    
    // If true the renderer will only display the number
    protected boolean showCompactLabel = false;
	
	protected GridLabelBuilder labelBuilder = null;

	public EntityGridCellRenderer() {
		// no-op
	}

    protected Border getNoFocusBorder() {
        Border border = UIManager.getBorder("Table.cellNoFocusBorder"); //$NON-NLS-1$
        return border==null ? noFocusBorder : border;
    }

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if (table == null) {
            return this;
        }

        Color fg = null;
        Color bg = null;

        JTable.DropLocation dropLocation = table.getDropLocation();
        if (dropLocation != null
                && !dropLocation.isInsertRow()
                && !dropLocation.isInsertColumn()
                && dropLocation.getRow() == row
                && dropLocation.getColumn() == column) {

            fg = UIManager.getColor("Table.dropCellForeground"); //$NON-NLS-1$
            bg = UIManager.getColor("Table.dropCellBackground"); //$NON-NLS-1$

            isSelected = true;
        }

        if (isSelected) {
            super.setForeground(fg == null ? table.getSelectionForeground()
                                           : fg);
            super.setBackground(bg == null ? table.getSelectionBackground()
                                           : bg);
        } else {
            Color background = unselectedBackground != null
                                    ? unselectedBackground
                                    : table.getBackground();
            if (background == null || background instanceof javax.swing.plaf.UIResource) {
                Color alternateColor = UIManager.getColor("Table.alternateRowColor"); //$NON-NLS-1$
                if (alternateColor != null && row % 2 != 0) {
                    background = alternateColor;
                }
            }
            super.setForeground(unselectedForeground != null
                                    ? unselectedForeground
                                    : table.getForeground());
            super.setBackground(background);
        }

        setFont(table.getFont());

        if (hasFocus) {
            Border border = null;
            if (isSelected) {
                border = UIManager.getBorder("Table.focusSelectedCellHighlightBorder"); //$NON-NLS-1$
            }
            if (border == null) {
                border = UIManager.getBorder("Table.focusCellHighlightBorder"); //$NON-NLS-1$
            }
            setBorder(border);

            if (!isSelected && table.isCellEditable(row, column)) {
                Color col;
                col = UIManager.getColor("Table.focusCellForeground"); //$NON-NLS-1$
                if (col != null) {
                    super.setForeground(col);
                }
                col = UIManager.getColor("Table.focusCellBackground"); //$NON-NLS-1$
                if (col != null) {
                    super.setBackground(col);
                }
            }
        } else {
            setBorder(getNoFocusBorder());
        }

        setValue(value);

        return this;
	}
	
	protected void setValue(Object value) {
		if(value instanceof EntityGridNode) {
			node = (EntityGridNode) value;
		} else {
			node = null;
		}
	}
	
	@Override
    public void setForeground(Color c) {
        super.setForeground(c);
        unselectedForeground = c;
    }

    @Override
    public void setBackground(Color c) {
        super.setBackground(c);
        unselectedBackground = c;
    }

	@Override
	protected void paintComponent(Graphics g) {
		Color c = g.getColor();
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(c);
		
		if(node==null) {
			return;
		}
		
		if(isShowCompactLabel() || labelBuilder==null) {
			paintCompactLabel(g);
		} else {
			paintCompleteLabel(g);
		}
	}
	
	protected void paintCompactLabel(Graphics g) {

		int w = getWidth();
		int h = getHeight();
		
		Font f = getFont();
		FontMetrics fm = getFontMetrics(f);
		
		String label = String.valueOf(node.getSpanCount());
		
		int width = fm.charWidth('[')+fm.stringWidth(label)+fm.charWidth(']');
		int height = fm.getHeight();

		int x = Math.max(1, (w-width)/2);
		int y = Math.max(0, (h-height)/2);
		y += fm.getAscent();
		
		Color col = g.getColor();
		g.drawString("[", x, y); //$NON-NLS-1$
		x += fm.charWidth('[');
		
		Color c = null;
		if(node.hasMissingGoldSpan()) {
			c = missingGoldNodeColor;
		} else if(node.hasFalsePredictedSpan()) {
			c = falseNodeColor;
		}
		if(c!=null) {
			g.setColor(c);
		}
		g.drawString(label, x, y);
		x += fm.stringWidth(label);
		
		g.setColor(col);
		g.drawString("]", x, y); //$NON-NLS-1$
	}
	
	protected void paintCompleteLabel(Graphics g) {
		
		int w = getWidth();
		int h = getHeight();
		
		Font f = getFont();
		FontMetrics fm = getFontMetrics(f);
		
		int width = 0;
		int height = fm.getHeight();
		int count = node.getSpanCount();
		String[] tokens = new String[node.getSpanCount()];
		
		// Calculate required total width
		width += fm.charWidth('[');
		for(int i=0; i<count; i++) {
			if(i>0) {
				width += fm.charWidth(',');
			}
			tokens[i] = labelBuilder.getLabel(node, i);
			width += fm.stringWidth(tokens[i]);
		}
		width += fm.charWidth(']');
		
		int x = Math.max(1, (w-width)/2);
		int y = Math.max(0, (h-height)/2);
		y += fm.getAscent();
		
		// Now draw the entire string with highlight colors
		Color col = g.getColor();
		g.drawString("[", x, y); //$NON-NLS-1$
		x += fm.charWidth('[');
		
		for(int i=0; i<count; i++) {
			if(i>0) {
				g.setColor(col);
				g.drawString(",", x, y); //$NON-NLS-1$
				x += fm.charWidth(',');
			}
			
			Color c = null;
			if(node.isMissingGoldSpan(i)) {
				c = missingGoldNodeColor;
			} else if(node.isFalsePredictedSpan(i)) {
				c = falseNodeColor;
			}
			
			if(c==null) {
				c = col;
			}
			
			g.setColor(col);
			g.drawString(tokens[i], x, y);
			x += fm.stringWidth(tokens[i]);
		}
		
		g.setColor(col);
		g.drawString("]", x, y); //$NON-NLS-1$
	}
	
    @Override
    public boolean isOpaque() {
        return true;
    }

    @Override
    public void invalidate() {
    	// no-op
    }

    @Override
    public void validate() {
    	// no-op
    }

    @Override
    public void revalidate() {
    	// no-op
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    public void repaint(long tm, int x, int y, int width, int height) {
    	// no-op
    }

    @Override
    public void repaint(Rectangle r) { 
    	// no-op
    }

    @Override
    public void repaint() {
    	// no-op
    }

    @Override
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (propertyName == "font" || propertyName == "foreground") { //$NON-NLS-1$ //$NON-NLS-2$
            super.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    @Override
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) { 
    	// no-op
    }

	public boolean isShowCompactLabel() {
		return showCompactLabel;
	}

	public GridLabelBuilder getLabelBuilder() {
		return labelBuilder;
	}

	public void setShowCompactLabel(boolean showCompactLabel) {
		this.showCompactLabel = showCompactLabel;
	}

	public void setLabelBuilder(GridLabelBuilder labelBuilder) {
		this.labelBuilder = labelBuilder;
	}
    
}
