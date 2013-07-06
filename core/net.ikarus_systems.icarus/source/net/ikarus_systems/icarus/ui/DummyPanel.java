/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.ui;

import java.awt.LayoutManager;
import java.awt.Rectangle;

import javax.swing.JPanel;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DummyPanel extends JPanel {

	private static final long serialVersionUID = 8101383182871821635L;

	public DummyPanel() {
		// no-op
	}

	public DummyPanel(LayoutManager layout) {
		super(layout);
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
	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
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
