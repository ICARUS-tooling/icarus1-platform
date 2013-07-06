/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.ui.dialog;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;

import net.ikarus_systems.icarus.ui.dialog.FormBuilder.FormEntry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ControlFormEntry implements FormEntry {
	
	private final JButton[] buttons;

	public ControlFormEntry(Action...actions) {
		if(actions==null || actions.length==0)
			throw new IllegalArgumentException("Invalid actions list"); //$NON-NLS-1$
		
		buttons = new JButton[actions.length];
		
		for(int i=0; i<actions.length; i++) {
			buttons[i] = new JButton(actions[i]);
		}
	}
	
	public int getButtonCount() {
		return buttons.length;
	}
	
	public JButton getButton(int index) {
		return buttons[index];
	}
	
	public Action getAction(int index) {
		return getButton(index).getAction();
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.dialog.FormBuilder.FormEntry#addToForm(net.ikarus_systems.icarus.ui.dialog.FormBuilder)
	 */
	@Override
	public ControlFormEntry addToForm(FormBuilder builder) {
		
		JPanel panel = new JPanel(new FlowLayout());
		
		for(JButton button : buttons) {
			panel.add(button);
		}
		
		builder.feedSeparator();
		builder.feedComponent(panel, null, GridBagConstraints.CENTER, FormBuilder.RESIZE_REMAINDER);
		
		return this;
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.dialog.FormBuilder.FormEntry#setValue(java.lang.Object)
	 */
	@Override
	public ControlFormEntry setValue(Object value) {
		return this;
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.dialog.FormBuilder.FormEntry#getValue()
	 */
	@Override
	public Object getValue() {
		return null;
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.dialog.FormBuilder.FormEntry#clear()
	 */
	@Override
	public ControlFormEntry clear() {
		return this;
	}

}
