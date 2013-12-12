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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.coref.view.text;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;

import de.ims.icarus.language.coref.text.CoreferenceDocument;
import de.ims.icarus.ui.actions.ActionComponentBuilder;
import de.ims.icarus.ui.actions.ActionList.EntryType;
import de.ims.icarus.util.Filter;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ContextOutline extends CoreferenceDocumentPresenter {

	public static final int DEFAULT_SCOPE = 1;

	private JSlider scopeSelect;

	private int scope = DEFAULT_SCOPE;

	/**
	 * @see de.ims.icarus.plugins.coref.view.text.AbstractCoreferenceTextPresenter#createToolBar()
	 */
	@Override
	protected ActionComponentBuilder createToolBar() {
		ActionComponentBuilder builder = super.createToolBar();

		if(scopeSelect==null) {
			scopeSelect = new JSlider(SwingConstants.HORIZONTAL, 1, 5, DEFAULT_SCOPE);
			scopeSelect.addChangeListener(getHandler());
			scopeSelect.setPaintTicks(true);
			scopeSelect.setMajorTickSpacing(1);

			//XXX manual resizing
			Dimension d = scopeSelect.getPreferredSize();
			d.width = 140;
			scopeSelect.setPreferredSize(d);
			scopeSelect.setMinimumSize(new Dimension(60, d.height));
			scopeSelect.setMaximumSize(d);
			scopeSelect.setSize(d);
		}

		Object[] items = {
			"plugins.coref.contextOutline.labels.scopeSelect", //$NON-NLS-1$
			scopeSelect,
			EntryType.SEPARATOR,
		};

		builder.addOption("modifiers", items); //$NON-NLS-1$

		return builder;
	}

	/**
	 * @see de.ims.icarus.plugins.coref.view.text.CoreferenceDocumentPresenter#buildDocument(de.ims.icarus.language.coref.text.CoreferenceDocument)
	 */
	@Override
	protected boolean buildDocument(CoreferenceDocument doc) throws Exception {
		if(data==null)
			return false;

		Filter filter = (Filter) options.get("filter"); //$NON-NLS-1$
		doc.setFilter(filter);

		int index = options.getInteger("index"); //$NON-NLS-1$
		int scope = options.getInteger("scope"); //$NON-NLS-1$

		if(scope==0) {
			scope = getScope();
		}

		int fromIndex = Math.max(0, index-scope);
		int toIndex = Math.min(data.size()-1, index+scope);

		doc.appendBatchCoreferenceDocumentData(data,
				getAllocation(), getGoldAllocation(),
				fromIndex, toIndex);

		doc.applyBatchUpdates(0);

		return true;
	}

	/**
	 * @see de.ims.icarus.plugins.coref.view.text.AbstractCoreferenceTextPresenter#createContentPanel()
	 */
	@Override
	protected JComponent createContentPanel() {
		JComponent panel = super.createContentPanel();

		panel.setPreferredSize(new Dimension(200, 200));

		return panel;
	}

//	/**
//	 * @see de.ims.icarus.plugins.coref.view.text.AbstractCoreferenceTextPresenter#registerActionCallbacks()
//	 */
//	@Override
//	protected void registerActionCallbacks() {
//		super.registerActionCallbacks();
//
//		getActionManager().setSelected(false,
//				"plugins.coref.coreferenceDocumentPresenter.toggleFilterSingletonsAction"); //$NON-NLS-1$
//	}

	/**
	 * @see de.ims.icarus.plugins.coref.view.text.AbstractCoreferenceTextPresenter#createHandler()
	 */
	@Override
	protected Handler createHandler() {
		return new OutlineHandler();
	}

	/**
	 * @see de.ims.icarus.plugins.coref.view.text.AbstractCoreferenceTextPresenter#getHandler()
	 */
	@Override
	protected OutlineHandler getHandler() {
		return (OutlineHandler) super.getHandler();
	}

	/**
	 * @return the scope
	 */
	public int getScope() {
		return scope;
	}

	/**
	 * @param scope the scope to set
	 */
	public void setScope(int scope) {
		if(scope==this.scope)
			return;

		this.scope = scope;

		refresh();
	}

	protected class OutlineHandler extends Handler {

		/**
		 * @see de.ims.icarus.plugins.coref.view.text.AbstractCoreferenceTextPresenter.Handler#stateChanged(javax.swing.event.ChangeEvent)
		 */
		@Override
		public void stateChanged(ChangeEvent e) {
			if(e.getSource()==scopeSelect) {
				setScope(scopeSelect.getValue());
			} else {
				super.stateChanged(e);
			}
		}

	}
}
