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

import java.awt.Insets;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;

import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SelectFormEntry extends LabeledFormEntry<SelectFormEntry> {
	
	protected JLabel contentLabel;
	protected JButton selectButton;

	public static final Insets DEFAULT_CONTENT_INSETS = new Insets(0, 8, 0, 5);
	
	public SelectFormEntry(String label, String value, Action a) {
		super(label);
		contentLabel = new JLabel(value);
		UIUtil.disableHtml(contentLabel);
		selectButton = new JButton(a);
		selectButton.setHideActionText(true);
		selectButton.setFocusPainted(false);
		selectButton.setFocusable(false);
		UIUtil.resizeComponent(selectButton, 22, 22);
	}
	
	public SelectFormEntry(String value, Action a) {
		this(null, value, a);
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.dialog.FormBuilder.FormEntry#setValue(java.lang.Object)
	 */
	@Override
	public SelectFormEntry setValue(Object value) {
		if(value instanceof String) {
			contentLabel.setText((String)value);
			contentLabel.setToolTipText(null);
			contentLabel.setIcon(null);
		} else if(value instanceof Identity) {
			contentLabel.setText(((Identity)value).getName());
			contentLabel.setToolTipText(UIUtil.toSwingTooltip(
					((Identity)value).getDescription()));
			contentLabel.setIcon(((Identity)value).getIcon());
		}
		return this;
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.dialog.FormBuilder.FormEntry#getValue()
	 */
	@Override
	public Object getValue() {
		return contentLabel.getText();
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.dialog.FormBuilder.FormEntry#clear()
	 */
	@Override
	public SelectFormEntry clear() {
		contentLabel.setText(null);
		return this;
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.dialog.LabeledFormEntry#addComponents(net.ikarus_systems.icarus.ui.dialog.FormBuilder)
	 */
	@Override
	protected void addComponents(FormBuilder builder) {
		builder.feedComponent(contentLabel, DEFAULT_CONTENT_INSETS, FormBuilder.RESIZE_HORIZONTAL);
		builder.feedComponent(selectButton);
	}

}
