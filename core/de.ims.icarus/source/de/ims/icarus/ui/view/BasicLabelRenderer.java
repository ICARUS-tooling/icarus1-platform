/*
 * $Revision: 7 $
 * $Date: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/ui/view/BasicLabelRenderer.java $
 *
 * $LastChangedDate: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $ 
 * $LastChangedRevision: 7 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.ui.view;

import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * 
 * @author Markus Gärtner
 * @version $Id: BasicLabelRenderer.java 7 2013-02-27 13:18:56Z mcgaerty $
 *
 */
public class BasicLabelRenderer extends JLabel {

	private static final long serialVersionUID = -8895335837725277971L;

	public BasicLabelRenderer() {
		setVerticalAlignment(SwingConstants.TOP);
	}

	@Override
	public boolean isShowing() {
		return true;
	}

	@Override
	public void validate() {
		// no-op
	}

	@Override
	public void invalidate() {
		// no-op
	}

	@Override
	public void revalidate() {
		// no-op
	}

	@Override
	public void repaint(long tm, int x, int y, int width, int height) {
		// no-op
	}

	@Override
	public void repaint(Rectangle r) {
		// no-op
	}

	@Override
	protected void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		if (propertyName == "text" || propertyName == "font") { //$NON-NLS-1$ //$NON-NLS-2$
			super.firePropertyChange(propertyName, oldValue, newValue);
		}
	}

	@Override
	public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {
		// no-op
	}

	@Override
	public void firePropertyChange(String propertyName, char oldValue, char newValue) {
		// no-op
	}

	@Override
	public void firePropertyChange(String propertyName, short oldValue,	short newValue) {
		// no-op
	}

	@Override
	public void firePropertyChange(String propertyName, int oldValue, int newValue) {
		// no-op
	}

	@Override
	public void firePropertyChange(String propertyName, long oldValue, long newValue) {
		// no-op
	}

	@Override
	public void firePropertyChange(String propertyName, float oldValue,	float newValue) {
		// no-op
	}

	@Override
	public void firePropertyChange(String propertyName, double oldValue, double newValue) {
		// no-op
	}

	@Override
	public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
		// no-op
	}
}
