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
package de.ims.icarus.plugins.coref.view.ea;

import java.util.Set;
import java.util.concurrent.CancellationException;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;

import de.ims.icarus.language.coref.CoreferenceAllocation;
import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.tasks.TaskManager;
import de.ims.icarus.ui.tasks.TaskPriority;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractErrorAnalysisTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -7927978522711366080L;

	protected Set<CoreferenceDocumentData> documents;
	
	protected CoreferenceAllocation allocation;
	protected CoreferenceAllocation goldAllocation;
	protected ListModel<String> rowHeaderModel;
	
	protected AbstractErrorAnalysisTableModel() {
		// no-op
	}

	protected void setRowHeaders(String...labels) {
		DefaultListModel<String> model = new DefaultListModel<>();
		for(String label : labels) {
			model.addElement(label);
		}
		
		rowHeaderModel = model;
	}
	
	public ListModel<String> getRowHeaderModel() {
		if(rowHeaderModel==null)
			throw new IllegalStateException("No row header defined"); //$NON-NLS-1$
		
		return rowHeaderModel;
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return rowHeaderModel==null ? 0 : rowHeaderModel.getSize();
	}
	
	/**
	 * @return the documents
	 */
	public Set<CoreferenceDocumentData> getDocuments() {
		return CollectionUtils.getSetProxy(documents);
	}
	
	/**
	 * @return the allocation
	 */
	public CoreferenceAllocation getAllocation() {
		return allocation;
	}
	
	/**
	 * @return the goldAllocation
	 */
	public CoreferenceAllocation getGoldAllocation() {
		return goldAllocation;
	}
	
	/**
	 * @param documents the documents to set
	 */
	public void setDocuments(Set<CoreferenceDocumentData> documents) {
		if(documents==null)
			throw new NullPointerException("Invalid documents"); //$NON-NLS-1$
		
		this.documents = documents;
	}
	
	/**
	 * @param allocation the allocation to set
	 */
	public void setAllocation(CoreferenceAllocation allocation) {
		if(allocation==null)
			throw new NullPointerException("Invalid allocation"); //$NON-NLS-1$
		
		this.allocation = allocation;
	}
	
	/**
	 * @param goldAllocation the goldAllocation to set
	 */
	public void setGoldAllocation(CoreferenceAllocation goldAllocation) {
		if(goldAllocation==null)
			throw new NullPointerException("Invalid gold allocation"); //$NON-NLS-1$
		
		this.goldAllocation = goldAllocation;
	}
	
	public void rebuild() {
		if(documents==null || allocation==null || goldAllocation==null) {
			return;
		}
		
		AnalysisTask task = new AnalysisTask();
		
		String title = ResourceManager.getInstance().get(
				"plugins.coref.errorAnalysisView.analysisTask.name"); //$NON-NLS-1$

		String id = documents.size()==1 ? documents.iterator().next().getId()
				: String.valueOf(documents.size());
		
		String info = ResourceManager.getInstance().get(
				"plugins.coref.errorAnalysisView.analysisTask.description", //$NON-NLS-1$
				id);
		
		TaskManager.getInstance().schedule(
				task, title, info, null, TaskPriority.DEFAULT, true);
	}
	
	protected abstract void analyzeDocuments();
	
	private class AnalysisTask extends SwingWorker<Object, Object> {
		
		private Object getOwner() {
			return AbstractErrorAnalysisTableModel.this;
		}

		/**
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected Object doInBackground() throws Exception {
			analyzeDocuments();
			return null;
		}

		/**
		 * @see javax.swing.SwingWorker#done()
		 */
		@Override
		protected void done() {
			try {
				get();
			} catch(CancellationException | InterruptedException e) {
				// ignore
			} catch (Exception e) {
				String id = documents.size()==1 ? documents.iterator().next().getId()
						: String.valueOf(documents.size());
				
				LoggerFactory.error(this, "Failed to analyze document: "+id, e); //$NON-NLS-1$
			}
			
			fireTableStructureChanged();
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return getOwner().hashCode();
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof AnalysisTask) {
				return ((AnalysisTask)obj).getOwner()==getOwner();
			}
			return false;
		}
		
	}
}
