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
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import sun.swing.DefaultLookup;
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
    
    protected Color missingGoldNodeColor;
    protected Color falseNodeColor;
    
    protected EntityGridNode node;
    
    // If true the renderer will only display the number
    protected boolean showCompactLabel = false;
	
	protected GridLabelBuilder labelBuilder = null;

	public EntityGridCellRenderer() {
		// no-op
	}

    protected Border getNoFocusBorder() {
        Border border = DefaultLookup.getBorder(this, ui, "Table.cellNoFocusBorder"); //$NON-NLS-1$
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

            fg = DefaultLookup.getColor(this, ui, "Table.dropCellForeground"); //$NON-NLS-1$
            bg = DefaultLookup.getColor(this, ui, "Table.dropCellBackground"); //$NON-NLS-1$

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
                Color alternateColor = DefaultLookup.getColor(this, ui, "Table.alternateRowColor"); //$NON-NLS-1$
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
                border = DefaultLookup.getBorder(this, ui, "Table.focusSelectedCellHighlightBorder"); //$NON-NLS-1$
            }
            if (border == null) {
                border = DefaultLookup.getBorder(this, ui, "Table.focusCellHighlightBorder"); //$NON-NLS-1$
            }
            setBorder(border);

            if (!isSelected && table.isCellEditable(row, column)) {
                Color col;
                col = DefaultLookup.getColor(this, ui, "Table.focusCellForeground"); //$NON-NLS-1$
                if (col != null) {
                    super.setForeground(col);
                }
                col = DefaultLookup.getColor(this, ui, "Table.focusCellBackground"); //$NON-NLS-1$
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
		if(node==null) {
			return;
		}
		
		if(isShowCompactLabel()) {
			paintCompactLabel(g);
		} else {
			paintCompleteLabel(g);
		}
	}
	
	protected void paintCompactLabel(Graphics g) {
		
	}
	
	protected void paintCompleteLabel(Graphics g) {
		
		int w = getWidth();
		int h = getHeight();
		
		Font f = getFont();
		FontMetrics fm = getFontMetrics(f);
		
		int width = 0;
		int count = node.getSpanCount();
		String[] tokens = new String[node.getSpanCount()];
		
		// Calculate required total width
		for(int i=0; i<count; i++) {
			tokens[i] =  
		}
	}
	
    @Override
    public boolean isOpaque() {
        Color back = getBackground();
        Component p = getParent();
        if (p != null) {
            p = p.getParent();
        }

        // p should now be the JTable.
        boolean colorMatch = (back != null) && (p != null) &&
            back.equals(p.getBackground()) &&
                        p.isOpaque();
        return !colorMatch && super.isOpaque();
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
