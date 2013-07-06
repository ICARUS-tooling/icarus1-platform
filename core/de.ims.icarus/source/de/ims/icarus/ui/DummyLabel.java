/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.ui;

import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DummyLabel extends JLabel {

	private static final long serialVersionUID = 9043849424712452029L;

	protected static DummyLabel sharedInstance;


	static {
		try {
			sharedInstance = new DummyLabel();
		} catch (Exception e) {
			// ignore
		}
	}

	public static DummyLabel getSharedInstance() {
		return sharedInstance;
	}

	public DummyLabel() {
		setVerticalAlignment(SwingConstants.TOP);
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
