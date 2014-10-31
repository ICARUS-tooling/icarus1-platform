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
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.activation.DataHandler;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

import de.ims.icarus.logging.LoggerFactory;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class FileListTransferHandler extends TransferHandler {

	private static final long serialVersionUID = 5282181100643442395L;

	protected int[] indices = null;
	protected int addIndex = -1; // Location where items were added
	protected int addCount = 0; // Number of items added.
	protected List<File> transferedObjects = null;
	protected boolean logErrors = false;

	public boolean isLogErrors() {
		return logErrors;
	}

	public void setLogErrors(boolean logErrors) {
		this.logErrors = logErrors;
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		@SuppressWarnings("unchecked")
		JList<File> list = (JList<File>) c;
		indices = list.getSelectedIndices();
		transferedObjects = list.getSelectedValuesList();
		return new DataHandler(transferedObjects, DataFlavor.javaFileListFlavor.getMimeType());
	}

	@Override
	public boolean canImport(TransferSupport info) {
		if (!info.isDrop() || !info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			return false;
		}
		return true;
	}

	@Override
	public int getSourceActions(JComponent c) {
		return COPY_OR_MOVE; // TransferHandler.COPY_OR_MOVE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean importData(TransferSupport info) {
		if (!canImport(info)) {
			return false;
		}
		JList<File> target = (JList<File>) info.getComponent();
		JList.DropLocation dl = (JList.DropLocation) info.getDropLocation();
		DefaultListModel<File> listModel = (DefaultListModel<File>) target.getModel();
		int index = dl.getIndex();
		int max = listModel.getSize();
		if (index < 0 || index > max) {
			index = max;
		}
		addIndex = index;
		try {
			List<File> values = (List<File>) info.getTransferable()
					.getTransferData(DataFlavor.javaFileListFlavor);
			addCount = values.size();
			for (int i = 0; i < values.size(); i++) {
				int idx = index++;
				listModel.add(idx, values.get(i));
				target.addSelectionInterval(idx, idx);
			}
			return true;
		} catch (UnsupportedFlavorException e) {
			if(logErrors) {
				LoggerFactory.error(this, "Unsupported data flavor in import operation", e); //$NON-NLS-1$
			}
		} catch (IOException e) {
			if(logErrors) {
				LoggerFactory.error(this, "Error deserializing data", e); //$NON-NLS-1$
			}
		}
		return false;
	}

	@Override
	protected void exportDone(JComponent c, Transferable data, int action) {
		cleanup(c, action == MOVE);
	}

	protected void cleanup(JComponent c, boolean remove) {
		if (remove && indices != null) {
			@SuppressWarnings("unchecked")
			JList<File> source = (JList<File>) c;
			DefaultListModel<File> model = (DefaultListModel<File>) source.getModel();
			if (addCount > 0) {
				// http://java-swing-tips.googlecode.com/svn/trunk/DnDReorderList/src/java/example/MainPanel.java
				for (int i = 0; i < indices.length; i++) {
					if (indices[i] >= addIndex) {
						indices[i] += addCount;
					}
				}
			}
			for (int i = indices.length - 1; i >= 0; i--) {
				model.remove(indices[i]);
			}
		}
		indices = null;
		addCount = 0;
		addIndex = -1;
	}
}