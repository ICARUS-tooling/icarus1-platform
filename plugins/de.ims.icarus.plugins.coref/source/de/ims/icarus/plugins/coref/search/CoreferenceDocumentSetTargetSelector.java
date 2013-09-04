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
 * $Revision: 123 $
 * $Date: 2013-07-31 17:22:01 +0200 (Mi, 31 Jul 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.search_tools/source/de/ims/icarus/search_tools/corpus/TreebankTargetSelector.java $
 *
 * $LastChangedDate: 2013-07-31 17:22:01 +0200 (Mi, 31 Jul 2013) $ 
 * $LastChangedRevision: 123 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.plugins.coref.search;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;

import de.ims.icarus.language.coref.registry.AllocationDescriptor;
import de.ims.icarus.language.coref.registry.CoreferenceRegistry;
import de.ims.icarus.language.coref.registry.DocumentSetDescriptor;
import de.ims.icarus.search_tools.SearchTargetSelector;
import de.ims.icarus.ui.dialog.ChoiceFormEntry;
import de.ims.icarus.ui.dialog.FormBuilder;
import de.ims.icarus.ui.list.ComboBoxListWrapper;
import de.ims.icarus.util.data.ContentType;


/**
 * @author Markus Gärtner
 * @version $Id: TreebankTargetSelector.java 123 2013-07-31 15:22:01Z mcgaerty $
 *
 */
public class CoreferenceDocumentSetTargetSelector 
		implements SearchTargetSelector, ActionListener {

	protected JPanel contentPanel;
	protected FormBuilder formBuilder;
	protected DefaultComboBoxModel<Object> allocationModel;
	
	protected static final Object dummyEntry = "-"; //$NON-NLS-1$
	
	public CoreferenceDocumentSetTargetSelector() {
		// no-op
	}
	
	protected void ensureUI() {
		if(contentPanel==null) {
			contentPanel = new JPanel();
			formBuilder = FormBuilder.newLocalizingBuilder(contentPanel);
			
			ComboBoxModel<?> model = new ComboBoxListWrapper<>(
					CoreferenceRegistry.getInstance().getDocumentSetListModel());
			ChoiceFormEntry entry = new ChoiceFormEntry(
					"plugins.coref.labels.documentSet",  //$NON-NLS-1$
					model);
			entry.getComboBox().addActionListener(this);
			formBuilder.addEntry("documentSet", entry); //$NON-NLS-1$
			
			allocationModel = new DefaultComboBoxModel<Object>();
			entry = new ChoiceFormEntry(
					"plugins.coref.labels.allocation",  //$NON-NLS-1$
					allocationModel);
			formBuilder.addEntry("allocation", entry); //$NON-NLS-1$
			
			formBuilder.buildForm();
			formBuilder.pack();
		}
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchTargetSelector#getSelectedItem()
	 */
	@Override
	public Object getSelectedItem() {
		ensureUI();
		DocumentSetDescriptor documentSet = (DocumentSetDescriptor) formBuilder.getValue("documentSet"); //$NON-NLS-1$
		Object allocation = formBuilder.getValue("allocation"); //$NON-NLS-1$
		if(dummyEntry==allocation) {
			allocation = null;
		}
		
		return new CoreferenceDocumentSearchTarget(
				documentSet, (AllocationDescriptor) allocation);
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchTargetSelector#setSelectedItem(java.lang.Object)
	 */
	@Override
	public void setSelectedItem(Object item) {
		ensureUI();
		
		CoreferenceDocumentSearchTarget target = (CoreferenceDocumentSearchTarget) item;
		
		formBuilder.setValue("documentSet", target.getDocumentSet()); //$NON-NLS-1$
		Object allocation = target.getAllocation();
		if(allocation==null) {
			allocation = dummyEntry;
		}
		formBuilder.setValue("allocation", allocation); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchTargetSelector#clear()
	 */
	@Override
	public void clear() {
		ensureUI();
		formBuilder.clear();
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchTargetSelector#getSelectorComponent()
	 */
	@Override
	public Component getSelectorComponent() {
		ensureUI();
		return contentPanel;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchTargetSelector#setAllowedContentType(de.ims.icarus.util.data.ContentType)
	 */
	@Override
	public void setAllowedContentType(ContentType contentType) {
		// no-op
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		DocumentSetDescriptor documentSet = (DocumentSetDescriptor) formBuilder.getValue("documentSet"); //$NON-NLS-1$
		allocationModel.removeAllElements();
		allocationModel.addElement(dummyEntry);
		for(int i=0; i<documentSet.size(); i++) {
			allocationModel.addElement(documentSet.get(i));
		}
	}
}
