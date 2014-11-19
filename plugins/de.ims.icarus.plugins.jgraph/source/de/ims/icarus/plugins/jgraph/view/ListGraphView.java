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
package de.ims.icarus.plugins.jgraph.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.logging.Level;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.core.View;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.helper.Outline;
import de.ims.icarus.ui.helper.UIHelperRegistry;
import de.ims.icarus.ui.view.AWTPresenter.GraphBasedPresenter;
import de.ims.icarus.ui.view.ListPresenter;
import de.ims.icarus.ui.view.PresenterUtils;
import de.ims.icarus.ui.view.UnsupportedPresentationDataException;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.DataList;
import de.ims.icarus.util.mpi.Commands;
import de.ims.icarus.util.mpi.Message;
import de.ims.icarus.util.mpi.ResultMessage;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ListGraphView extends View implements Outline {

	protected GraphBasedPresenter graphPresenter;
	protected ListPresenter listPresenter;

	protected JTextArea infoLabel;
	protected JSplitPane splitPane;

	protected Handler handler;

	public ListGraphView() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.plugins.core.View#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		container.setLayout(new BorderLayout());

		handler = createHandler();

		infoLabel = UIUtil.defaultCreateInfoLabel(container);
		container.add(infoLabel, BorderLayout.NORTH);

		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setContinuousLayout(true);
		splitPane.setDividerSize(5);
		splitPane.setBorder(null);
		splitPane.setResizeWeight(1);
		splitPane.addComponentListener(handler);
		container.add(splitPane, BorderLayout.CENTER);

		showInfo(null);
	}

	protected Handler createHandler() {
		return new Handler();
	}

	protected void setGraphPresenter(GraphBasedPresenter graphPresenter) {
		if(this.graphPresenter==graphPresenter) {
			return;
		}

		if(this.graphPresenter!=null) {
			this.graphPresenter.close();
		}

		this.graphPresenter = graphPresenter;

		if(this.graphPresenter!=null) {
			splitPane.setLeftComponent(graphPresenter.getPresentingComponent());
		} else {
			showGraphInfo(null);
		}
	}

	protected void setListPresenter(ListPresenter listPresenter) {
		if(this.listPresenter==listPresenter) {
			return;
		}

		if(this.listPresenter!=null) {
			this.listPresenter.getSelectionModel().removeListSelectionListener(handler);
			this.listPresenter.close();
		}

		this.listPresenter = listPresenter;

		if(this.listPresenter!=null) {
			this.listPresenter.getSelectionModel().addListSelectionListener(handler);

			Component comp = listPresenter.getPresentingComponent();
			splitPane.setRightComponent(comp);
		} else {
			showInfo(null);
		}
	}

	protected void displaySelectedData() throws Exception {
		if(listPresenter==null || graphPresenter==null) {
			return;
		}

		ListSelectionModel selectionModel = listPresenter.getSelectionModel();

		if(selectionModel.getValueIsAdjusting()) {
			return;
		}

		int selectedIndex = selectionModel.getMinSelectionIndex();
		Object selectedObject = null;

		if(selectedIndex!=-1) {
			selectedObject = listPresenter.getListModel().getElementAt(selectedIndex);
		}

		if(selectedObject==null) {
			graphPresenter.clear();
			return;
		}

		// Display selected object in graph presenter
		Options options = new Options();
		options.put(Options.INDEX, selectedIndex);
		options.put(Options.CONTENT_TYPE, listPresenter.getContentType());

		graphPresenter.present(selectedObject, options);

		// Broadcast selected object via message
		Message message = new Message(this, Commands.DISPLAY, selectedObject, options);
		sendRequest(Outline.class, message);
	}

	protected void displayData(Object data, Options options) {

		// Show default info if nothing available to be displayed
		if(data==null) {
			showInfo(null);
			return;
		}

		selectViewTab();

		DataList<?> dataList = (DataList<?>) data;

		if(options==null) {
			options = Options.emptyOptions;
		}

		// Ensure list presenter
		ListPresenter listPresenter = this.listPresenter;
		if(listPresenter==null || !PresenterUtils.presenterSupports(listPresenter, data)) {
			listPresenter = UIHelperRegistry.globalRegistry().findHelper(ListPresenter.class, data);
		}

		// Signal missing list presenter
		if(listPresenter==null) {
			String text = ResourceManager.getInstance().get(
					"plugins.jgraph.listGraphView.unsupportedListType", data.getClass()); //$NON-NLS-1$
			showInfo(text);
			return;
		}

		// Ensure graph presenter
		ContentType entryType = dataList.getContentType();
		//entryType = ContentTypeRegistry.getInstance().getType("DependencyDataContentType");
		GraphBasedPresenter graphPresenter = this.graphPresenter;
		if(graphPresenter==null || !PresenterUtils.presenterSupports(graphPresenter, entryType)) {
			graphPresenter = UIHelperRegistry.globalRegistry().findHelper(GraphBasedPresenter.class, entryType, true, true);
		}

		// Signal missing graph presenter
		if(graphPresenter==null) {
			String text = ResourceManager.getInstance().get(
					"plugins.jgraph.listGraphView.unsupportedEntryType", entryType.getId()); //$NON-NLS-1$
			showInfo(text);
			return;
		}

		// Now present data
		try {
			listPresenter.present(dataList, options);
		} catch (UnsupportedPresentationDataException e) {
			LoggerFactory.log(this, Level.SEVERE,
					"Failed to present data list: "+dataList, e); //$NON-NLS-1$

			String text = ResourceManager.getInstance().get(
					"plugins.jgraph.listGraphView.presentationFailed", data.getClass()); //$NON-NLS-1$
			showInfo(text);
			return;
		}

		setListPresenter(listPresenter);
		setGraphPresenter(graphPresenter);

		/*try {
			displaySelectedData();
		} catch (Exception e) {
			LoggerFactory.log(this, Level.SEVERE,
					"Failed to present selected item", e); //$NON-NLS-1$

			String text = ResourceManager.getInstance().get(
					"plugins.jgraph.listGraphView.presentationFailed", entryType.getId()); //$NON-NLS-1$
			showGraphInfo(text);
			return;
		}*/

		if(dataList.size()>0) {
			listPresenter.getSelectionModel().setSelectionInterval(0, 0);
		} else {
			listPresenter.getSelectionModel().clearSelection();
		}

		infoLabel.setVisible(false);
		splitPane.setVisible(true);
	}

	protected void showInfo(String text) {
		if(text==null) {
			text = ResourceManager.getInstance().get(
					"plugins.jgraph.listGraphView.notAvailable"); //$NON-NLS-1$
		}
		infoLabel.setText(text);

		infoLabel.setVisible(true);
		splitPane.setVisible(false);
		splitPane.setLeftComponent(null);
		splitPane.setRightComponent(null);

		// Close any active presenter and discard its reference
		if(graphPresenter!=null) {
			graphPresenter.close();
			graphPresenter = null;
		}
		if(listPresenter!=null) {
			listPresenter.close();
			listPresenter = null;
		}
	}

	protected void showGraphInfo(String text) {
		if(text==null) {
			text = ResourceManager.getInstance().get(
					"plugins.jgraph.listGraphView.notAvailable"); //$NON-NLS-1$
		}

		JLabel label = new JLabel(text);
		label.setBorder(new EmptyBorder(10, 10, 10, 10));
		label.setHorizontalAlignment(SwingConstants.CENTER);

		splitPane.setLeftComponent(label);

		// Close any active presenter and discard its reference
		if(graphPresenter!=null) {
			graphPresenter.close();
			graphPresenter = null;
		}
	}

	@Override
	public void reset() {
		displayData(null, null);
	}

	@Override
	public void close() {
		super.close();

		// Close any active presenter and discard its reference
		if(graphPresenter!=null) {
			graphPresenter.close();
			graphPresenter = null;
		}
		if(listPresenter!=null) {
			listPresenter.close();
			listPresenter = null;
		}
	}

	/**
	 * Accepted commands:
	 * <ul>
	 * <li>{@link Commands#DISPLAY}</li>
	 * <li>{@link Commands#PRESENT}</li>
	 * <li>{@link Commands#SELECT}</li>
	 * <li>{@link Commands#CLEAR}</li>
	 * </ul>
	 *
	 * @see de.ims.icarus.plugins.core.View#handleRequest(de.ims.icarus.util.mpi.Message)
	 */
	@Override
	protected ResultMessage handleRequest(Message message) throws Exception {
		if(Commands.PRESENT.equals(message.getCommand())
				|| Commands.DISPLAY.equals(message.getCommand())) {

			Object data = message.getData();
			if(!(data instanceof DataList)) {
				return message.unsupportedDataResult(this);
			}

			displayData(data, message.getOptions());

			return message.successResult(this, null);
		} else if(Commands.SELECT.equals(message.getCommand())) {
			if(listPresenter==null) {
				return message.errorResult(this, null);
			}

			// Accept index
			int selectedIndex = -1;
			if(message.getData() instanceof Integer) {
				selectedIndex = (int)message.getData();
			} else {
				// TODO traverse list and check items
			}

			Object selectedItem = null;
			if(selectedIndex==-1) {
				listPresenter.getSelectionModel().clearSelection();
			} else {
				listPresenter.getSelectionModel().setSelectionInterval(selectedIndex, selectedIndex);
			}

			return message.successResult(this, selectedItem);
		} else if(Commands.CLEAR.equals(message.getCommand())) {
			reset();
			return message.successResult(this, null);
		} else {
			return message.unknownRequestResult(this);
		}
	}

	protected class Handler extends ComponentAdapter implements ListSelectionListener {

		protected boolean trackResizing = true;

		/**
		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
		 */
		@Override
		public void valueChanged(ListSelectionEvent e) {
			try {
				displaySelectedData();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to handle change in selection: "+e, ex); //$NON-NLS-1$
			}
		}

		/**
		 * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
		 */
		@Override
		public void componentResized(ComponentEvent e) {
			if(!trackResizing) {
				return;
			}

			int height = splitPane.getHeight();
			if(height==0) {
				return;
			}

			splitPane.setDividerLocation(Math.max(height/2, height-100));

			trackResizing = false;
		}

		/**
		 * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
		 */
		@Override
		public void componentHidden(ComponentEvent e) {
			trackResizing = true;
		}
	}
}
