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
package de.ims.icarus.plugins.prosody.search;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.ims.icarus.io.Loadable;
import de.ims.icarus.language.coref.registry.CoreferenceRegistry;
import de.ims.icarus.language.coref.registry.DocumentSetDescriptor;
import de.ims.icarus.language.helper.WrappedSentenceDataList;
import de.ims.icarus.plugins.coref.view.manager.CoreferenceListCellRenderer;
import de.ims.icarus.plugins.prosody.ProsodicDocumentData;
import de.ims.icarus.plugins.prosody.ProsodicDocumentSet;
import de.ims.icarus.plugins.prosody.ProsodyUtils;
import de.ims.icarus.search_tools.SearchTargetSelector;
import de.ims.icarus.ui.events.ListenerProxies;
import de.ims.icarus.ui.helper.FilteredListModel;
import de.ims.icarus.util.Filter;
import de.ims.icarus.util.NamedObject;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ProsodyTargetSelector implements SearchTargetSelector, Filter {

	protected JList<DocumentSetDescriptor> list;
	protected JScrollPane scrollPane;
	protected ContentType contentType;

	public ProsodyTargetSelector() {
		// no-op
	}

	protected void ensureUI() {
		if(list==null) {
			ListModel<DocumentSetDescriptor> model = CoreferenceRegistry.getInstance().getDocumentSetListModel();
			model = new FilteredListModel<>(model, this);

			list = new JList<>(model);
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			list.setCellRenderer(new CoreferenceListCellRenderer());
			list.setBorder(null);
			list.setSelectedIndex(0);

			scrollPane = new JScrollPane(list);
		}
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchTargetSelector#getSelectedItem()
	 */
	@Override
	public Object getSelectedItem() {
		ensureUI();
		DocumentSetDescriptor descriptor = list.getSelectedValue();

		return descriptor==null ? null : new DocumentSetDelegate(descriptor);
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchTargetSelector#setSelectedItem(java.lang.Object)
	 */
	@Override
	public void setSelectedItem(Object item) {
		ensureUI();

		list.setSelectedValue(item, true);
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchTargetSelector#clear()
	 */
	@Override
	public void clear() {
		ensureUI();
		list.clearSelection();
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchTargetSelector#getSelectorComponent()
	 */
	@Override
	public Component getSelectorComponent() {
		ensureUI();
		return scrollPane;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchTargetSelector#setAllowedContentType(de.ims.icarus.util.data.ContentType)
	 */
	@Override
	public void setAllowedContentType(ContentType contentType) {
		this.contentType = contentType;
	}

	/**
	 * @see de.ims.icarus.util.Filter#accepts(java.lang.Object)
	 */
	@Override
	public boolean accepts(Object obj) {
		DocumentSetDescriptor documentSet = (DocumentSetDescriptor) obj;

		ContentType targetType = documentSet.get().getContentType();
		if(targetType==null) {
			return false;
		}

		return ContentTypeRegistry.isCompatible(contentType, targetType)
				|| ContentTypeRegistry.isCompatible(ProsodyUtils.getProsodyDocumentContentType(), targetType);
	}

	private static class DocumentSetDelegate extends WrappedSentenceDataList implements Loadable, ChangeListener, NamedObject {

		private final DocumentSetDescriptor descriptor;

		/**
		 * @param contentType
		 * @param capacity
		 */
		public DocumentSetDelegate(DocumentSetDescriptor descriptor) {
			super(ProsodyUtils.getProsodySentenceContentType());

			if (descriptor == null)
				throw new NullPointerException("Invalid descriptor"); //$NON-NLS-1$

			this.descriptor = descriptor;

			descriptor.addChangeListener(ListenerProxies.getProxy(ChangeListener.class, this));

			reload();
		}

		/**
		 * @see de.ims.icarus.io.Loadable#isLoaded()
		 */
		@Override
		public boolean isLoaded() {
			return descriptor.isLoaded();
		}

		/**
		 * @see de.ims.icarus.io.Loadable#isLoading()
		 */
		@Override
		public boolean isLoading() {
			return descriptor.isLoading();
		}

		/**
		 * @see de.ims.icarus.io.Loadable#load()
		 */
		@Override
		public void load() throws Exception {
			descriptor.load();
		}

		/**
		 * @see de.ims.icarus.io.Loadable#free()
		 */
		@Override
		public void free() {
			descriptor.free();
		}

		public void reload() {
			clearUnnoticed();

			if(descriptor.isLoaded()) {
				ProsodicDocumentSet documentSet = (ProsodicDocumentSet) descriptor.get();
				for(int i=0; i<documentSet.size(); i++) {
					// Cast to prosody document as sanity check
					add((ProsodicDocumentData)documentSet.get(i));
				}
			}

			fireStateChanged();
		}

		/**
		 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
		 */
		@Override
		public void stateChanged(ChangeEvent e) {
			reload();
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof DocumentSetDelegate) {
				DocumentSetDelegate other = (DocumentSetDelegate)obj;
				return descriptor==other.descriptor;
			}
			return false;
		}

		@Override
		public String toString() {
			return getName();
		}

		/**
		 * @see de.ims.icarus.util.NamedObject#getName()
		 */
		@Override
		public String getName() {
			return descriptor.getName();
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return descriptor.hashCode();
		}
	}
}
