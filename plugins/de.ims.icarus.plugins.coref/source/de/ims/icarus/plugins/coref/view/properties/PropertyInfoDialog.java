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
package de.ims.icarus.plugins.coref.view.properties;

import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import de.ims.icarus.Core;
import de.ims.icarus.io.Loadable;
import de.ims.icarus.language.coref.CoreferenceAllocation;
import de.ims.icarus.language.coref.DocumentSet;
import de.ims.icarus.language.coref.EdgeSet;
import de.ims.icarus.language.coref.SpanSet;
import de.ims.icarus.language.coref.registry.AllocationDescriptor;
import de.ims.icarus.language.coref.registry.CoreferenceRegistry;
import de.ims.icarus.language.coref.registry.DocumentSetDescriptor;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.core.FrameManager;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.GridBagUtil;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.dialog.ChoiceFormEntry;
import de.ims.icarus.ui.dialog.FormBuilder;
import de.ims.icarus.ui.list.ComboBoxListWrapper;
import de.ims.icarus.ui.table.CounterTableModel;
import de.ims.icarus.ui.tasks.TaskManager;
import de.ims.icarus.ui.tasks.TaskPriority;
import de.ims.icarus.util.Counter;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.Range;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class PropertyInfoDialog extends JFrame {

	private static final long serialVersionUID = 8546718818512715746L;

	protected JLabel header;
	protected JToggleButton togglePinButton;
	protected JButton reloadButton;
	protected JButton clearButton;

	protected FormBuilder formBuilder;
	protected DefaultComboBoxModel<Object> allocationModel;

	protected static final Object dummyEntry = "Default Allocation"; //$NON-NLS-1$

	protected PropertyPanel sentencePanel;
	protected PropertyPanel spanPanel;
	protected PropertyPanel edgePanel;

	protected Handler handler;
	protected SwingWorker<?, ?> task;

	protected JProgressBar progressBar;

	private static volatile Map<Object, Counter> cache;

	private static Reference<PropertyInfoDialog> instance;

	public static synchronized void showDialog() {
		showDialog(null);
	}

	public static synchronized void showDialog(Options options) {
		PropertyInfoDialog dialog = null;

		if(instance!=null) {
			dialog = instance.get();
		}

		if(dialog==null) {
			dialog = new PropertyInfoDialog();
			instance = new WeakReference<PropertyInfoDialog>(dialog);
			UIUtil.centerComponent(dialog);
		}

		dialog.setSelectedItems(options);
		dialog.setVisible(true);
	}

	protected synchronized static Counter getCachedCounter(Object item) {
		if(item==null)
			throw new NullPointerException("Invalid item"); //$NON-NLS-1$

		return cache==null ? null : cache.get(item);
	}

	protected synchronized static void cacheCounter(Object item, Counter counter) {
		if(item==null)
			throw new NullPointerException("Invalid item"); //$NON-NLS-1$

		if(counter==null || counter.isEmpty()) {
			return;
		}

		if(cache==null) {
			synchronized (PropertyInfoDialog.class) {
				if(cache==null) {
					cache = new WeakHashMap<>();
				}
			}
		}

		cache.put(item, counter);
	}

	protected static void releaseCache(Object item) {
		if(cache==null || item==null) {
			return;
		}

		cache.remove(item);
	}

	public PropertyInfoDialog() {
		setIconImages(Core.getIconImages());

		init();
	}

	public void setSelectedItems(Options options) {
		if(options==null) {
			return;
		}

		Object documentSet = options.get("documentSet"); //$NON-NLS-1$
		if(documentSet instanceof DocumentSetDescriptor) {
			formBuilder.setValue("documentSet", documentSet); //$NON-NLS-1$
		}

		Object allocation = options.get("allocation"); //$NON-NLS-1$
		if(allocation instanceof AllocationDescriptor) {
			formBuilder.setValue("allocation", allocation); //$NON-NLS-1$
		}
	}

	protected void init() {

		ResourceManager rm = ResourceManager.getInstance();

		header = new JLabel();
		header.setFont(header.getFont().deriveFont(Font.BOLD, 18F));
		header.setHorizontalAlignment(SwingConstants.LEFT);
		header.setBorder(UIUtil.defaultContentBorder);

		togglePinButton = new JToggleButton();
		togglePinButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("pinned_ovr.gif")); //$NON-NLS-1$
		togglePinButton.addActionListener(getHandler());
		UIUtil.resizeComponent(togglePinButton, 20, 20);
		togglePinButton.setFocusable(false);
		togglePinButton.setHideActionText(true);
		rm.getGlobalDomain().prepareComponent(togglePinButton,
				null,
				"plugins.coref.propertyInfoDialog.pinWindowAction.description"); //$NON-NLS-1$
		rm.getGlobalDomain().addComponent(togglePinButton);

		reloadButton = new JButton();
		reloadButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("refresh.gif")); //$NON-NLS-1$
		reloadButton.addActionListener(getHandler());
		UIUtil.resizeComponent(reloadButton, 20, 20);
		reloadButton.setFocusable(false);
		reloadButton.setHideActionText(true);
		rm.getGlobalDomain().prepareComponent(reloadButton,
				null,
				"plugins.coref.propertyInfoDialog.refreshAction.description"); //$NON-NLS-1$
		rm.getGlobalDomain().addComponent(reloadButton);

		JPanel formPanel = new JPanel();
		formPanel.setBorder(UIUtil.topLineBorder);
		formBuilder = FormBuilder.newLocalizingBuilder(formPanel);

		ComboBoxModel<?> model = new ComboBoxListWrapper<>(
				CoreferenceRegistry.getInstance().getDocumentSetListModel());
		ChoiceFormEntry entry = new ChoiceFormEntry(
				"plugins.coref.labels.documentSet",  //$NON-NLS-1$
				model);
		entry.getComboBox().addActionListener(getHandler());
		formBuilder.addEntry("documentSet", entry); //$NON-NLS-1$

		allocationModel = new DefaultComboBoxModel<Object>();
		entry = new ChoiceFormEntry(
				"plugins.coref.labels.allocation",  //$NON-NLS-1$
				allocationModel);
		formBuilder.addEntry("allocation", entry); //$NON-NLS-1$

		formBuilder.buildForm();
		formBuilder.pack();

		sentencePanel = new PropertyPanel("plugins.coref.labels.sentences"); //$NON-NLS-1$
		spanPanel = new PropertyPanel("plugins.coref.labels.spans"); //$NON-NLS-1$
		edgePanel = new PropertyPanel("plugins.coref.labels.edges"); //$NON-NLS-1$

		progressBar = new JProgressBar();

		JPanel panel = new JPanel(new GridBagLayout());

		// Header
		panel.add(header, GridBagUtil.makeGbcHN(0, 0, 1, 1));
		panel.add(reloadButton, GridBagUtil.makeGbc(1, 0));
		panel.add(togglePinButton, GridBagUtil.makeGbc(2, 0));

		// Selection
		panel.add(formPanel, GridBagUtil.makeGbcHN(0, 1, 3, 1));

		// Outlines
		panel.add(sentencePanel, GridBagUtil.makeGbcHN(0, 2, 3, 1));
		panel.add(sentencePanel.getContentArea(), GridBagUtil.makeGbcRN(0, 3, 3, 1));
		panel.add(spanPanel, GridBagUtil.makeGbcHN(0, 4, 3, 1));
		panel.add(spanPanel.getContentArea(), GridBagUtil.makeGbcRN(0, 5, 3, 1));
		panel.add(edgePanel, GridBagUtil.makeGbcHN(0, 6, 3, 1));
		panel.add(edgePanel.getContentArea(), GridBagUtil.makeGbcRN(0, 7, 3, 1));

		// Progress
		panel.add(progressBar, GridBagUtil.makeGbcH(0, 8, 3, 1));

		add(panel);

		setSize(300, 450);

		ResourceManager.getInstance().getGlobalDomain().addFrame(
				this, "plugins.coref.propertyInfoDialog.titel"); //$NON-NLS-1$
		FrameManager.getInstance().registerWindow(this);
	}

	protected void publishData(Object data) {
		if(data==null) {
			return;
		}

		String str = data.toString();
		if(str==null || str.trim().isEmpty()) {
			return;
		}

		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		if(clipboard==null) {
			return;
		}

		Transferable t = new StringSelection(str);

		clipboard.setContents(t, getHandler());
	}

	protected Handler getHandler() {
		if(handler==null) {
			handler = new Handler();
		}
		return handler;
	}

	protected void refreshButtons() {
		reloadButton.setEnabled(task==null);
	}

	public synchronized void reload() {
		if(task!=null) {
			return;
		}

		DocumentSetDescriptor documentSet = (DocumentSetDescriptor) formBuilder.getValue("documentSet"); //$NON-NLS-1$
		Object alloc = formBuilder.getValue("allocation"); //$NON-NLS-1$
		AllocationDescriptor  allocation = alloc==dummyEntry ? null : (AllocationDescriptor)alloc;

		if(documentSet==null) {
			return;
		}

		task = new CountTask(documentSet, allocation);

		TaskManager.getInstance().schedule(task, TaskPriority.DEFAULT, true);

		refreshButtons();
	}

	protected synchronized void countFinished(Counter sentenceCounter,
			Counter spanCounter, Counter edgeCounter) {

		task = null;

		sentencePanel.reload(sentenceCounter);
		spanPanel.reload(spanCounter);
		edgePanel.reload(edgeCounter);

		refreshButtons();
	}

	/**
	 *
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	protected class Handler extends MouseAdapter implements ClipboardOwner, ActionListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			if(!SwingUtilities.isLeftMouseButton(e) || e.getClickCount()!=2) {
				return;
			}

			JTable table = (JTable) e.getSource();
			int row = table.rowAtPoint(e.getPoint());
			//XXX switched to static column so the entire row acts as source for the property name
			int col = 1;

			if(row==-1 || col==-1) {
				return;
			}

			Object data = table.getValueAt(row, col);
			publishData(data);
		}

		/**
		 * @see java.awt.datatransfer.ClipboardOwner#lostOwnership(java.awt.datatransfer.Clipboard, java.awt.datatransfer.Transferable)
		 */
		@Override
		public void lostOwnership(Clipboard clipboard, Transferable contents) {
			// no-op
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource()==togglePinButton) {
				setAlwaysOnTop(togglePinButton.isSelected());
			} else if(e.getSource()==reloadButton) {
				reload();
			} else {
				DocumentSetDescriptor documentSet = (DocumentSetDescriptor) formBuilder.getValue("documentSet"); //$NON-NLS-1$
				allocationModel.removeAllElements();
				allocationModel.addElement(dummyEntry);
				for(int i=0; i<documentSet.size(); i++) {
					allocationModel.addElement(documentSet.get(i));
				}
			}
		}

	}

	protected static class PropertyTableModel extends CounterTableModel {

		private static final long serialVersionUID = -7952452998284531824L;

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return ResourceManager.getInstance().get(
						"plugins.coref.propertyInfoDialog.labels.index"); //$NON-NLS-1$
			case 1:
				return ResourceManager.getInstance().get(
						"plugins.coref.propertyInfoDialog.labels.key"); //$NON-NLS-1$
			case 2:
				return ResourceManager.getInstance().get(
						"plugins.coref.propertyInfoDialog.labels.count"); //$NON-NLS-1$

			default:
				return null;
			}
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	protected class PropertyPanel extends JPanel implements ActionListener {

		private static final long serialVersionUID = 4013252444101192150L;

		protected JLabel header;
		protected JTable table;
		protected PropertyTableModel model;
		protected JToggleButton toggleButton;
		protected JScrollPane scrollPane;

		public PropertyPanel(String key) {

			header = new JLabel();
			header.setFont(header.getFont().deriveFont(14F));
			ResourceManager.getInstance().getGlobalDomain().prepareComponent(header, key, null);
			ResourceManager.getInstance().getGlobalDomain().addComponent(header);

			model = new PropertyTableModel();
			table = new JTable(model);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
			table.setAutoCreateRowSorter(true);
			table.addMouseListener(getHandler());
			table.setBorder(UIUtil.emptyBorder);

			/*JList<String> rowHeader = new JList<>(new TableIndexListModel(model));
			TableRowHeaderRenderer headerRenderer = new TableRowHeaderRenderer(rowHeader, table);
			rowHeader.setCellRenderer(headerRenderer);*/

			scrollPane = new JScrollPane(table);
			//scrollPane.setRowHeaderView(rowHeader);
			scrollPane.setBorder(UIUtil.emptyBorder);

			toggleButton = new JToggleButton();
			toggleButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("up.gif")); //$NON-NLS-1$
			toggleButton.setSelectedIcon(IconRegistry.getGlobalRegistry().getIcon("down.gif")); //$NON-NLS-1$
			toggleButton.addActionListener(this);
			toggleButton.setFocusable(false);
			UIUtil.resizeComponent(toggleButton, 20, 20);

			setLayout(new GridBagLayout());

			// Header area
			add(header, GridBagUtil.makeGbcHN(0, 0, 1, 1));
			add(toggleButton, GridBagUtil.makeGbcN(1, 0, 1, 1));

			// Content area
			//add(scrollPane, GridBagUtil.makeGbcRN(0, 1, 2, 1));

			setBorder(UIUtil.topLineBorder);
		}

		public void reload(Counter counter) {
			model.setCounter(counter);
		}

		public JComponent getContentArea() {
			return scrollPane;
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			scrollPane.setVisible(!toggleButton.isSelected());
			PropertyInfoDialog.this.revalidate();
			PropertyInfoDialog.this.repaint();
		}
	}

	protected class CountTask extends SwingWorker<Options, Object> {

		private final DocumentSetDescriptor documentSet;
		private AllocationDescriptor allocation;

		public CountTask(DocumentSetDescriptor documentSet,
				AllocationDescriptor allocation) {
			if(documentSet==null)
				throw new IllegalArgumentException("Ivalid document set"); //$NON-NLS-1$

			this.documentSet = documentSet;
			this.allocation = allocation;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof CountTask) {
				CountTask other = (CountTask) obj;
				return documentSet==other.documentSet && allocation==other.allocation;
			}
			return false;
		}

		@Override
		public int hashCode() {
			int hash = documentSet.hashCode();
			if(allocation!=null) {
				hash *= allocation.hashCode();
			}

			return hash;
		}

		/**
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected Options doInBackground() throws Exception {

			Options result = new Options();

			// Load document set if required
			if(!documentSet.isLoaded()) {
				try {
					publish(true);
					load(documentSet);
				} catch (Exception e) {
					LoggerFactory.log(this, Level.SEVERE, "Failed to load document set: "+documentSet.getName(), e); //$NON-NLS-1$
					return null;
				} finally {
					publish(false);
				}
			}

			// Load allocation if required
			if(allocation!=null && !allocation.isLoaded()) {
				try {
					publish(true);
					load(allocation);
				} catch (Exception e) {
					LoggerFactory.log(this, Level.SEVERE, "Failed to load allocation: "+allocation.getName(), e); //$NON-NLS-1$
					return null;
				} finally {
					publish(false);
				}
			}

			// Count sentence properties
			Counter sentenceCounter = getCachedCounter(documentSet.getId());
			if(sentenceCounter==null) {
				sentenceCounter = new Counter();
				DocumentSet dSet = documentSet.get();
				publish(new Range(dSet.size()), 0, true);
				for(int i=0; i<dSet.size(); i++) {
					PropertyUtils.countProperties(sentenceCounter, dSet.get(i));
					publish(i+1);
				}
				publish(false);
			}
			result.put("sentenceCounter", sentenceCounter); //$NON-NLS-1$

			// Process allocation if available
			if(allocation!=null) {
				CoreferenceAllocation alloc = allocation.get();
				String[] documentIds = alloc.getDocumentIds();
				publish(new Range(documentIds.length), 0, true);

				// Count span properties
				Counter spanCounter = getCachedCounter(allocation.getId()+"_spans"); //$NON-NLS-1$
				if(spanCounter==null) {
					spanCounter = new Counter();
					for(int i=0; i<documentIds.length; i++) {
						SpanSet spanSet = alloc.getSpanSet(documentIds[i]);
						if(spanSet!=null) {
							PropertyUtils.countProperties(spanCounter, spanSet);
						}
						publish(i+1);
					}
				}
				result.put("spanCounter", spanCounter); //$NON-NLS-1$

				// Count edge properties
				Counter edgeCounter = getCachedCounter(allocation.getId()+"_edges"); //$NON-NLS-1$
				if(edgeCounter==null) {
					edgeCounter = new Counter();
					for(int i=0; i<documentIds.length; i++) {
						EdgeSet edgeSet = alloc.getEdgeSet(documentIds[i]);
						if(edgeSet!=null) {
							PropertyUtils.countProperties(edgeCounter, edgeSet);
						}
						publish(i+1);
					}
				}
				result.put("edgeCounter", edgeCounter); //$NON-NLS-1$

				publish(false);
			}

			return result;
		}

		private void load(Loadable loadable) throws Exception {
			while(loadable.isLoading());

			if(!loadable.isLoaded()) {
				loadable.load();
			}
		}

		@Override
		protected void process(List<Object> chunks) {
			for(Object chunk : chunks) {
				if(chunk instanceof Range) {
					Range range = (Range) chunk;
					progressBar.setMinimum(range.getStart());
					progressBar.setMaximum(range.getEnd());
				} else if(chunk instanceof Integer) {
					progressBar.setValue((int) chunk);
				} else if(chunk instanceof Boolean) {
					progressBar.setIndeterminate((boolean) chunk);
				}
			}
		}

		@Override
		protected void done() {
			Counter sentenceCounter = null;
			Counter spanCounter = null;
			Counter edgeCounter = null;

			try {
				Options result = get();
				if(result==null) {
					return;
				}

				sentenceCounter = (Counter) result.get("sentenceCounter"); //$NON-NLS-1$
				spanCounter = (Counter) result.get("spanCounter"); //$NON-NLS-1$
				edgeCounter = (Counter) result.get("edgeCounter"); //$NON-NLS-1$

				cacheCounter(documentSet.getId(), sentenceCounter);
				if(allocation!=null) {
					cacheCounter(allocation.getId()+"_spans", spanCounter); //$NON-NLS-1$
					cacheCounter(allocation.getId()+"_edges", edgeCounter); //$NON-NLS-1$
				}
			} catch (CancellationException | InterruptedException e) {
				// ignore
			} catch (Exception e) {
				LoggerFactory.log(this, Level.SEVERE, "Failed to count properties for document set: "+documentSet.getName(), e); //$NON-NLS-1$

				Core.getCore().handleThrowable(e);
			} finally {
				progressBar.setVisible(false);
				progressBar.setIndeterminate(false);

				countFinished(sentenceCounter, spanCounter, edgeCounter);
			}
		}

	}
}
