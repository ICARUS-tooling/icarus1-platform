/*
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
import java.awt.Component;
import java.util.logging.Level;

import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.BadLocationException;

import de.ims.icarus.language.coref.CoreferenceData;
import de.ims.icarus.language.coref.text.CoreferenceDocument;
import de.ims.icarus.language.coref.text.CoreferenceEditorKit;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.ui.DummyTextPane;

import sun.swing.DefaultLookup;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferenceCellRenderer extends DummyTextPane implements
		ListCellRenderer<CoreferenceData>, TableCellRenderer {

	private static final long serialVersionUID = 3528239616495396258L;
	
    protected static final Border DEFAULT_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
    
	public CoreferenceCellRenderer() {
		setEditorKit(new CoreferenceEditorKit());
        setOpaque(true);
        setBorder(getNoFocusBorder());
	}

    protected final Border getNoFocusBorder() {
        Border border = UIManager.getBorder("List.cellNoFocusBorder"); //$NON-NLS-1$
        return border==null ? DEFAULT_NO_FOCUS_BORDER : border;
    }
    
	/**
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(
			JList<? extends CoreferenceData> list, CoreferenceData value,
			int index, boolean isSelected, boolean cellHasFocus) {

        setComponentOrientation(list.getComponentOrientation());

        Color bg = null;
        Color fg = null;

        JList.DropLocation dropLocation = list.getDropLocation();
        if (dropLocation != null
                && !dropLocation.isInsert()
                && dropLocation.getIndex() == index) {

            bg = UIManager.getColor("List.dropCellBackground"); //$NON-NLS-1$
            fg = UIManager.getColor("List.dropCellForeground"); //$NON-NLS-1$

            isSelected = true;
        }

        if (isSelected) {
            setBackground(bg == null ? list.getSelectionBackground() : bg);
            setForeground(fg == null ? list.getSelectionForeground() : fg);
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        setEnabled(list.isEnabled());
        setFont(list.getFont());

        Border border = null;
        if (cellHasFocus) {
            if (isSelected) {
                border = UIManager.getBorder("List.focusSelectedCellHighlightBorder"); //$NON-NLS-1$
            }
            if (border == null) {
                border = UIManager.getBorder("List.focusCellHighlightBorder"); //$NON-NLS-1$
            }
        } else {
            border = getNoFocusBorder();
        }
        setBorder(border);
        
        showData(value);

        return this;
	}
	
	
    // We need a place to store the color the JLabel should be returned
    // to after its foreground and background colors have been set
    // to the selection background color.
    // These ivars will be made protected when their names are finalized.
    private Color unselectedForeground;
    private Color unselectedBackground;

	/**
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
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
        
        showData((CoreferenceData) value);
        
        return this;
	}
	
	protected void showData(CoreferenceData data) {		
		try {
			CoreferenceDocument doc = (CoreferenceDocument) getStyledDocument();
			doc.remove(0, doc.getLength());
			
			//doc.insertString(0, index+": ", null); //$NON-NLS-1$
			doc.appendBatchCoreferenceData(data);
			doc.applyBatchUpdates(0);
		} catch (BadLocationException e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to highlight spans", e); //$NON-NLS-1$
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
    public void updateUI() {
        super.updateUI();
        setForeground(null);
        setBackground(null);
    }
    
    @Override
    public boolean isOpaque() {
        Color back = getBackground();
        Component p = getParent();
        if (p != null) {
            p = p.getParent();
        }

        // p should now be the JTable or JList.
        boolean colorMatch = (back != null) && (p != null) &&
            back.equals(p.getBackground()) && p.isOpaque();
        return !colorMatch && super.isOpaque();
    }
}
