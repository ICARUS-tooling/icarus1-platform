/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.ui.list;

import java.awt.Component;
import java.awt.Rectangle;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class FilterListCellRenderer extends JCheckBox implements ListCellRenderer<Boolean> {

	private static final long serialVersionUID = 2520551821817723967L;

	public FilterListCellRenderer() {
		setBorderPaintedFlat(true);
	}
	
	protected String getTextForValue(int index, Boolean value) {
		return null;
	}

	/**
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(
			JList<? extends Boolean> list, Boolean value, int index,
			boolean isSelected, boolean cellHasFocus) {
		
		setForeground(list.getForeground());
		setBackground(list.getBackground());
		
		setSelected(value);
		setText(getTextForValue(index, value));
		
		return this;
	}

	@Override
	public void validate() {
	}

	@Override
	public void invalidate() {
	}

	@Override
	public void revalidate() {
	}

	@Override
	public void repaint(long tm, int x, int y, int width, int height) {
	}

	@Override
	public void repaint(Rectangle r) {
	}

	@Override
	protected void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		if (propertyName == "text" || propertyName == "font") { //$NON-NLS-1$ //$NON-NLS-2$
			super.firePropertyChange(propertyName, oldValue, newValue);
		}
	}

	@Override
	public void firePropertyChange(String propertyName, byte oldValue,
			byte newValue) {
	}

	@Override
	public void firePropertyChange(String propertyName, char oldValue,
			char newValue) {
	}

	@Override
	public void firePropertyChange(String propertyName, short oldValue,
			short newValue) {
	}

	@Override
	public void firePropertyChange(String propertyName, int oldValue,
			int newValue) {
	}

	@Override
	public void firePropertyChange(String propertyName, long oldValue,
			long newValue) {
	}

	@Override
	public void firePropertyChange(String propertyName, float oldValue,
			float newValue) {
	}

	@Override
	public void firePropertyChange(String propertyName, double oldValue,
			double newValue) {
	}

	@Override
	public void firePropertyChange(String propertyName, boolean oldValue,
			boolean newValue) {
	}
}
