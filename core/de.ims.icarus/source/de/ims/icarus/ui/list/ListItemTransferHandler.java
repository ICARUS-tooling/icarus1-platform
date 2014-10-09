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

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.activation.DataHandler;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.ui.UIUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ListItemTransferHandler extends TransferHandler {
	protected int[] indices = null;
	protected int addIndex = -1; // Location where items were added
	protected int addCount = 0; // Number of items added.
	protected Object[] transferedObjects = null;
	protected boolean logErrors = false;

	public boolean isLogErrors() {
		return logErrors;
	}

	public void setLogErrors(boolean logErrors) {
		this.logErrors = logErrors;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected Transferable createTransferable(JComponent c) {
		JList list = (JList) c;
		indices = list.getSelectedIndices();
		transferedObjects = list.getSelectedValues();
		return new DataHandler(transferedObjects,
				UIUtil.localObjectFlavor.getMimeType());
	}

	@Override
	public boolean canImport(TransferSupport info) {
		if (!info.isDrop() || !info.isDataFlavorSupported(UIUtil.localObjectFlavor)) {
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
		JList target = (JList) info.getComponent();
		JList.DropLocation dl = (JList.DropLocation) info.getDropLocation();
		DefaultListModel listModel = (DefaultListModel) target.getModel();
		int index = dl.getIndex();
		int max = listModel.getSize();
		if (index < 0 || index > max) {
			index = max;
		}
		addIndex = index;
		try {
			Object[] values = (Object[]) info.getTransferable()
					.getTransferData(UIUtil.localObjectFlavor);
			addCount = values.length;
			for (int i = 0; i < values.length; i++) {
				int idx = index++;
				listModel.add(idx, values[i]);
				target.addSelectionInterval(idx, idx);
			}
			return true;
		} catch (UnsupportedFlavorException e) {
			if(logErrors) {
				LoggerFactory.error(this, "Unsupported data flavor in import operation", e);
			}
		} catch (IOException e) {
			if(logErrors) {
				LoggerFactory.error(this, "Error deserializing data", e);
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
			JList source = (JList) c;
			DefaultListModel model = (DefaultListModel) source.getModel();
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