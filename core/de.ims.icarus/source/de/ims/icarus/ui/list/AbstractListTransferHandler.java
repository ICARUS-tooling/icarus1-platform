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
package de.ims.icarus.ui.list;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.TransferHandler;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractListTransferHandler<O extends Object> extends TransferHandler {

	private static final long serialVersionUID = 2447981704323279662L;

	private int[] indices = null;
	private int addIndex = -1; // Location where items were added
	private int addCount = 0; // Number of items added.
	private Reference<JList<O>> source;

	/**
	 * We only support importing strings.
	 */
	@Override
	public boolean canImport(TransferHandler.TransferSupport info) {
		// Check for String flavor
		if (!info.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			return false;
		}
		return true;
	}

	/**
	 * Bundle up the selected items in a single list for export. Each line is
	 * separated by a newline.
	 */
	@Override
	protected Transferable createTransferable(JComponent c) {
		@SuppressWarnings("unchecked")
		JList<O> list = (JList<O>) c;

		source = new WeakReference<>(list);

		indices = list.getSelectedIndices();
		List<O> values = list.getSelectedValuesList();

		StringBuffer buff = new StringBuffer();

		for (int i = 0; i < values.size(); i++) {
			if (i > 0) {
				buff.append('\n');
			}
			buff.append(serialize(values.get(i)));
		}

		return new StringSelection(buff.toString());
	}

	/**
	 * We support both copy and move actions.
	 */
	@Override
	public int getSourceActions(JComponent c) {
		return TransferHandler.COPY_OR_MOVE;
	}

	/**
	 * Perform the actual import. This demo only supports drag and drop.
	 */
	@Override
	public boolean importData(TransferHandler.TransferSupport info) {
		if (!info.isDrop()) {
			return false;
		}

		@SuppressWarnings("unchecked")
		JList<O> list = (JList<O>) info.getComponent();
		ListModel<O> listModel = list.getModel();
		JList.DropLocation dl = (JList.DropLocation) info.getDropLocation();
		int index = dl.getIndex();
		boolean insert = dl.isInsert();

		// Get the string that is being dropped.
		Transferable t = info.getTransferable();
		String data;
		try {
			data = (String) t.getTransferData(DataFlavor.stringFlavor);
		} catch (Exception e) {
			return false;
		}

		// Wherever there is a newline in the incoming data,
		// break it into a separate item in the list.
		String[] values = data.split("\n"); //$NON-NLS-1$

		addIndex = index;
		addCount = values.length;

		// Perform the actual import.
		for (int i = 0; i < values.length; i++) {
			O value = deserialize(values[i]);
			if(value==null) {
				continue;
			}
			addToList(listModel, value, index, insert);
			index++;
		}
		return true;
	}

	protected void addToList(ListModel<O> model, O item, int index, boolean insert) {
		DefaultListModel<O> listModel = (DefaultListModel<O>) model;
		if (insert) {
			listModel.add(index++, item);
		} else {
			// If the items go beyond the end of the current
			// list, add them in.
			if (index < listModel.getSize()) {
				listModel.set(index, item);
			} else {
				listModel.add(index, item);
			}
		}

	}

	protected abstract String serialize(O item);

	protected abstract O deserialize(String s);

	/**
	 * Remove the items moved from the list.
	 */
	@Override
	protected void exportDone(JComponent c, Transferable data, int action) {
		@SuppressWarnings("unchecked")
		JList<O> list = (JList<O>) c;
		ListModel<O> listModel = (ListModel<O>) list.getModel();

		if (action == TransferHandler.MOVE && (source==null || source.get()!=list)) {
			removeFromList(listModel, indices);
		}

		indices = null;
		addCount = 0;
		addIndex = -1;
		source = null;
	}

	protected void removeFromList(ListModel<O> model, int[] indices) {
		DefaultListModel<O> listModel = (DefaultListModel<O>) model;
		for (int i = indices.length - 1; i >= 0; i--) {
			listModel.remove(indices[i]);
		}
	}
}