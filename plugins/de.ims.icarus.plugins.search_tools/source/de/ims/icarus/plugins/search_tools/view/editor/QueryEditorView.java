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
package de.ims.icarus.plugins.search_tools.view.editor;

import java.awt.BorderLayout;
import java.util.logging.Level;

import javax.swing.JComponent;
import javax.swing.JTextArea;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.core.View;
import de.ims.icarus.plugins.search_tools.SearchToolsConstants;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.search_tools.SearchDescriptor;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.util.mpi.Commands;
import de.ims.icarus.util.mpi.Message;
import de.ims.icarus.util.mpi.ResultMessage;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class QueryEditorView extends View {
	protected JTextArea infoLabel;
	protected QueryEditor queryEditor;
	
	public QueryEditorView() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.plugins.core.View#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		infoLabel = UIUtil.defaultCreateInfoLabel(container);
		container.setLayout(new BorderLayout());
		
		showInfo(null);
	}
	
	private void showInfo(String text) {
		if(text==null) {
			text = ResourceManager.getInstance().get(
					"plugins.searchTools.queryEditorView.notAvailable"); //$NON-NLS-1$
		}
		
		infoLabel.setText(text);
		
		JComponent container = getContainer();
		container.removeAll();
		container.add(infoLabel, BorderLayout.NORTH);
		container.revalidate();
		container.repaint();
		
		if(queryEditor!=null) {
			queryEditor.close();
			queryEditor = null;
		}
	}
	
	public void setSearchDescriptor(SearchDescriptor searchDescriptor) {
		if(searchDescriptor==null) {
			showInfo(null);
			return;
		}
		
		Class<?> requiredClass = searchDescriptor.getSearchFactory().getDefaultEditorClass();
		if(requiredClass==null) {
			requiredClass = DefaultQueryEditor.class;
		}
		boolean updateComponent = false;
		
		if(queryEditor!=null && !requiredClass.isAssignableFrom(queryEditor.getClass())) {
			queryEditor.close();
			queryEditor = null;
		}
		
		if(queryEditor==null) {
			try {
				queryEditor = (QueryEditor) requiredClass.newInstance();
				updateComponent = true;
			} catch(Exception e) {
				showInfo(ResourceManager.getInstance().get(
						"plugins.searchTools.queryEditorView.noEditor")); //$NON-NLS-1$
				return;
			}
		}
		
		try {
			queryEditor.setOwner(this);
			queryEditor.setEditingItem(searchDescriptor);
		} catch(Exception e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to forward search descriptor for editing", e); //$NON-NLS-1$
			UIUtil.beep();

			showInfo(ResourceManager.getInstance().get(
					"plugins.searchTools.queryEditorView.editingFailed")); //$NON-NLS-1$
			return;
		}
		
		if(updateComponent) {
			JComponent container = getContainer();
			container.removeAll();
			container.add(queryEditor.getEditorComponent(), BorderLayout.CENTER);
			container.revalidate();
			container.repaint();
		}
	}
	
	public SearchDescriptor getSearchDescriptor() {
		return queryEditor==null ? null : queryEditor.getEditingItem();
	}
	
	public void commitQuery() throws Exception {
		if(queryEditor==null) {
			return;
		}
		
		// Sync query information and prepare for commit
		queryEditor.applyEdit();
		
		Message message = new Message(this, Commands.SELECT, 
				queryEditor.getEditingItem(), null);
		
		sendRequest(SearchToolsConstants.SEARCH_MANAGER_VIEW_ID, message);
	}

	@Override
	public void reset() {
		setSearchDescriptor(null);
	}

	@Override
	protected ResultMessage handleRequest(Message message) throws Exception {
		if(Commands.PRESENT.equals(message.getCommand())
				|| Commands.DISPLAY.equals(message.getCommand())) {
			if(message.getData() instanceof SearchDescriptor) {
				selectViewTab();
				
				setSearchDescriptor((SearchDescriptor)message.getData());
				return message.successResult(this, null);
			} else {
				return message.unsupportedDataResult(this);
			}
		} else if(Commands.CLEAR.equals(message.getCommand())) {
			reset();
			return message.successResult(this, null);
		} else if(Commands.COMMIT.equals(message.getCommand())) {
			commitQuery();
			return message.successResult(this, getSearchDescriptor());
		} else {
			return message.unknownRequestResult(this);
		}
	}
}
