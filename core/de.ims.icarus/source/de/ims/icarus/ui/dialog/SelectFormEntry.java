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
package de.ims.icarus.ui.dialog;

import java.awt.Insets;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;

import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.util.id.Identity;


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
	 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#setValue(java.lang.Object)
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
	 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#getValue()
	 */
	@Override
	public Object getValue() {
		return contentLabel.getText();
	}

	/**
	 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#clear()
	 */
	@Override
	public SelectFormEntry clear() {
		contentLabel.setText(null);
		return this;
	}

	/**
	 * @see de.ims.icarus.ui.dialog.LabeledFormEntry#addComponents(de.ims.icarus.ui.dialog.FormBuilder)
	 */
	@Override
	protected void addComponents(FormBuilder builder) {
		builder.feedComponent(contentLabel, DEFAULT_CONTENT_INSETS, FormBuilder.RESIZE_HORIZONTAL);
		builder.feedComponent(selectButton);
	}

}
