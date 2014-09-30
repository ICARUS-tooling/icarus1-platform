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
package de.ims.icarus.plugins.prosody.ui.view.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.ims.icarus.config.ConfigEvent;
import de.ims.icarus.config.ConfigListener;
import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.config.ConfigRegistry.Handle;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.core.View;
import de.ims.icarus.plugins.prosody.params.PaIntEParams;
import de.ims.icarus.plugins.prosody.params.PaIntEParamsWrapper;
import de.ims.icarus.plugins.prosody.ui.geom.Axis;
import de.ims.icarus.plugins.prosody.ui.geom.PaIntEGraph;
import de.ims.icarus.plugins.prosody.ui.list.PaIntEParamsListCellRenderer;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.ui.NumberDocument;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.util.classes.ClassUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class PaIntEEditorView extends View {

	private CallbackHandler callbackHandler;
	private Handler handler;

	private GraphComponent graphComponent;

	private final PaIntEParams painteParams = new PaIntEParams();

	private JList<PaIntEParamsWrapper> paramsHistoryList;
	private DefaultListModel<PaIntEParamsWrapper> paramsHistoryListModel;
	private ParamComponents[] paramComponents;

	private static final int PARAM_A1 = 0;
	private static final int PARAM_A2 = 1;
	private static final int PARAM_B = 2;
	private static final int PARAM_C1 = 3;
	private static final int PARAM_C2 = 4;
	private static final int PARAM_D = 5;
	private static final int PARAM_ALIGNMENT = 6;

	private static final String[] paramIds = {
		"a1", //$NON-NLS-1$
		"a2", //$NON-NLS-1$
		"b", //$NON-NLS-1$
		"c1", //$NON-NLS-1$
		"c2", //$NON-NLS-1$
		"d", //$NON-NLS-1$
		"alignment", //$NON-NLS-1$
	};

	private static final String configPath = "plugins.prosody.appearance.painteEditor"; //$NON-NLS-1$

	public PaIntEEditorView() {
		// no-op
	}

	@Override
	public void init(JComponent container) {

		// Load actions
		if (!defaultLoadActions(PaIntEEditorView.class,
				"painte-editor-view-actions.xml")) { //$NON-NLS-1$
			return;
		}

		// Init ui
		container.setLayout(new BorderLayout());

		graphComponent = new GraphComponent();
		graphComponent.setBorder(new EmptyBorder(10, 10, 10, 10));

		paramComponents = new ParamComponents[] {
				new ParamComponents("a1"), //$NON-NLS-1$
				new ParamComponents("a2"), //$NON-NLS-1$
				new ParamComponents("b"), //$NON-NLS-1$
				new ParamComponents("c1"), //$NON-NLS-1$
				new ParamComponents("c2"), //$NON-NLS-1$
				new ParamComponents("d"), //$NON-NLS-1$
				new ParamComponents("alignment"), //$NON-NLS-1$
		};

		FormLayout layout = new FormLayout(
				"fill:pref, 5dlu, fill:pref, 3dlu, pref, 1dlu, fill:max(100dlu;pref), 1dlu, pref, 2dlu, pref", //$NON-NLS-1$
				"pref, 10dlu, pref, pref, pref, pref, pref, pref, pref"); //$NON-NLS-1$
		layout.setRowGroups(new int[][]{{3,4,5,6,7,8,9}});

		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		JLabel title = new JLabel();
		ResourceManager.getInstance().getGlobalDomain().prepareComponent(
				title, "plugins.prosody.painteEditorView.labels.params", null); //$NON-NLS-1$
		ResourceManager.getInstance().getGlobalDomain().addComponent(title);
		builder.add(title, CC.rchw(1, 1, 1, 7));
		builder.nextLine();

		for(int i=0; i<paramComponents.length; i++) {
			ParamComponents paramComps = paramComponents[i];
			int row = 3+i;
			builder.add(paramComps.titleLabel, CC.rc(row, 1));
			builder.add(paramComps.textField, CC.rc(row, 3));
			builder.add(paramComps.minLabel, CC.rc(row, 5));
			builder.add(paramComps.slider, CC.rc(row, 7));
			builder.add(paramComps.maxLabel, CC.rc(row, 9));
			builder.add(paramComps.button, CC.rc(row, 11));
		}

		JPanel paramsPanel = builder.getPanel();
		paramsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JSplitPane upperSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, graphComponent, paramsPanel);
		upperSplitPane.setBorder(null);
		upperSplitPane.setResizeWeight(0);
		upperSplitPane.setDividerLocation(300);

		JScrollPane upperScrollPane = new JScrollPane(upperSplitPane);
		upperScrollPane.setBorder(null);
		UIUtil.defaultSetUnitIncrement(upperScrollPane);
		upperScrollPane.setMinimumSize(paramsPanel.getPreferredSize());

		paramsHistoryListModel = new DefaultListModel<>();
		paramsHistoryList = new JList<>(paramsHistoryListModel);
		paramsHistoryList.setCellRenderer(new PaIntEParamsListCellRenderer());
		paramsHistoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		paramsHistoryList.addMouseListener(getHandler());
		paramsHistoryList.addListSelectionListener(getHandler());

		JScrollPane lowerScrollPane = new JScrollPane(paramsHistoryList);
		lowerScrollPane.setBorder(null);
		UIUtil.defaultSetUnitIncrement(lowerScrollPane);

		JSplitPane globalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, upperScrollPane, lowerScrollPane);
		globalSplitPane.setBorder(null);
//		globalSplitPane.setResizeWeight(0.7);

		container.add(globalSplitPane, BorderLayout.CENTER);

		JToolBar toolBar = createToolBar();
		if(toolBar!=null) {
			container.add(toolBar, BorderLayout.NORTH);
		}

		registerActionCallbacks();

		refreshActions();

		ConfigRegistry.getGlobalRegistry().addGroupListener(configPath, getHandler());

		reloadConfig();
	}

	public void refresh() {
		refreshParamComponents();
	}

	private JToolBar createToolBar() {
		return getDefaultActionManager().createToolBar(
				"plugins.prosody.painteEditorView.toolBarList", null); //$NON-NLS-1$
	}

	private Handler getHandler() {
		if (handler == null) {
			handler = new Handler();
		}
		return handler;
	}

	private void registerActionCallbacks() {
		if (callbackHandler == null) {
			callbackHandler = new CallbackHandler();
		}

		ActionManager actionManager = getDefaultActionManager();

		actionManager.addHandler("plugins.prosody.painteEditorView.openPreferencesAction", //$NON-NLS-1$
				callbackHandler, "openPreferences"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.addPainteParamsAction", //$NON-NLS-1$
				callbackHandler, "addPainteParams"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.removePainteParamsAction", //$NON-NLS-1$
				callbackHandler, "removePainteParams"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.renamePainteParamsAction", //$NON-NLS-1$
				callbackHandler, "renamePainteParams"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.usePainteParamsAction", //$NON-NLS-1$
				callbackHandler, "usePainteParams"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.clearParamsHistoryAction", //$NON-NLS-1$
				callbackHandler, "clearParamsHistory"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.importParamsHistoryAction", //$NON-NLS-1$
				callbackHandler, "importParamsHistory"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.painteEditorView.exportParamsHistoryAction", //$NON-NLS-1$
				callbackHandler, "exportParamsHistory"); //$NON-NLS-1$
	}

	private void refreshActions() {
		ActionManager actionManager = getDefaultActionManager();

		int index = paramsHistoryList.getSelectedIndex();
		int historySize = paramsHistoryListModel.getSize();

		boolean hasSelection = index!=-1;
		boolean hasHistory = historySize>0;

		actionManager.setEnabled(hasSelection,
				"plugins.prosody.painteEditorView.removePainteParamsAction", //$NON-NLS-1$
				"plugins.prosody.painteEditorView.renamePainteParamsAction", //$NON-NLS-1$
				"plugins.prosody.painteEditorView.usePainteParamsAction"); //$NON-NLS-1$
		actionManager.setEnabled(hasHistory,
				"plugins.prosody.painteEditorView.exportParamsHistoryAction", //$NON-NLS-1$
				"plugins.prosody.painteEditorView.clearParamsHistoryAction"); //$NON-NLS-1$
	}

	private void reloadConfig() {
		ConfigRegistry registry = ConfigRegistry.getGlobalRegistry();

		Handle handle = registry.getHandle(configPath);

		for(int i=0; i<paramComponents.length; i++) {
			ParamComponents paramComps = paramComponents[i];
			String id = paramComps.id;

			double min = registry.getDouble(registry.getChildHandle(handle, id+"LowerBound")); //$NON-NLS-1$
			double max = registry.getDouble(registry.getChildHandle(handle, id+"UpperBound")); //$NON-NLS-1$
			double value = registry.getDouble(registry.getChildHandle(handle, id+"Default")); //$NON-NLS-1$

			paramComps.setMinMax(min, max);
			paramComps.setValue(value);
		}

		refreshGraph();
	}

	private void loadSelectedParams() {
		PaIntEParamsWrapper params = paramsHistoryList.getSelectedValue();
		if(params==null) {
			return;
		}

		painteParams.setParams(params.get());

		refreshParamComponents();
	}

	private void showBoundsDialog(ParamComponents paramComps) {
		//TODO
	}

	private void refreshGraph() {
		if(paramComponents==null) {
			// Prevent nasty nullpointer during init phase
			return;
		}

		double[] params = new double[7];

		for(int i=0; i<params.length; i++) {
			params[i] = paramComponents[i].getValue();
		}

		painteParams.setParams(params);

		graphComponent.refresh();
	}

	private void refreshParamComponents() {
		if(paramComponents==null) {
			// Prevent nasty nullpointer during init phase
			return;
		}

		double[] params = new double[7];

		painteParams.getParams(params);

		for(int i=0; i<params.length; i++) {
			paramComponents[i].setValue((float) params[i]);
		}

		graphComponent.refresh();
	}

	private class Handler extends MouseAdapter implements ConfigListener, ListSelectionListener {

		private void maybeShowPopupMenu(MouseEvent e) {
			if(e.isPopupTrigger()) {
				//TODO show popup menu for params history list
			}
		}

		/**
		 * @see de.ims.icarus.config.ConfigListener#invoke(de.ims.icarus.config.ConfigRegistry, de.ims.icarus.config.ConfigEvent)
		 */
		@Override
		public void invoke(ConfigRegistry sender, ConfigEvent event) {
			reloadConfig();
		}

		/**
		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent e) {
			if(e.getClickCount()==2) {
				loadSelectedParams();
			}
		}

		/**
		 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			maybeShowPopupMenu(e);
		}

		/**
		 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopupMenu(e);
		}

		/**
		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
		 */
		@Override
		public void valueChanged(ListSelectionEvent e) {
			refreshActions();
		}

		// TODO add handler methods

	}

	public class CallbackHandler {

		private CallbackHandler() {
			// no-op
		}

		public void openPreferences(ActionEvent e) {
			try {
				UIUtil.openConfigDialog(configPath);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to open preferences", ex); //$NON-NLS-1$

				UIUtil.beep();
			}
		}

		public void refresh(ActionEvent e) {
			try {
				PaIntEEditorView.this.refresh();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to refresh presenter", ex); //$NON-NLS-1$

				UIUtil.beep();
			}
		}

		public void addPainteParams(ActionEvent e) {
			try {
				paramsHistoryListModel.addElement(new PaIntEParamsWrapper(painteParams.clone()));

				paramsHistoryList.clearSelection();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to add painte parameters to history", ex); //$NON-NLS-1$

				UIUtil.beep();
			}

			refreshActions();
		}

		public void removePainteParams(ActionEvent e) {
			try {
				int index = paramsHistoryList.getSelectedIndex();
				if(index==-1) {
					return;
				}
				paramsHistoryListModel.remove(index);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to remove painte parameters from history", ex); //$NON-NLS-1$

				UIUtil.beep();
			}

			refreshActions();
		}

		public void usePainteParams(ActionEvent e) {
			try {
				PaIntEParamsWrapper params = paramsHistoryList.getSelectedValue();
				if(params==null) {
					return;
				}

				painteParams.setParams(params.get());

				refreshParamComponents();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to select painte parameters for editing", ex); //$NON-NLS-1$

				UIUtil.beep();
			}

			refreshActions();
		}

		public void renamePainteParams(ActionEvent e) {
			try {
				PaIntEParamsWrapper params = paramsHistoryList.getSelectedValue();
				if(params==null) {
					return;
				}


				String currentName = params.getLabel();
				String newName = DialogFactory.getGlobalFactory().showInputDialog(getFrame(),
						"plugins.prosody.painteEditorView.dialogs.renameParams.title",  //$NON-NLS-1$
						"plugins.prosody.painteEditorView.dialogs.renameParams.message",  //$NON-NLS-1$
						currentName);

				// Cancelled by user
				if(newName==null) {
					return;
				}

				// No changes
				if(ClassUtils.equals(currentName, newName)) {
					return;
				}

				params.setLabel(newName);

				paramsHistoryList.repaint();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to rename painte parameters", ex); //$NON-NLS-1$

				UIUtil.beep();
			}

			refreshActions();
		}

		public void clearParamsHistory(ActionEvent e) {
			try {
				paramsHistoryListModel.removeAllElements();
				paramsHistoryList.clearSelection();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to clear params history", ex); //$NON-NLS-1$

				UIUtil.beep();
			}

			refreshActions();
		}

		public void exportParamsHistory(ActionEvent e) {
			try {

				if(paramsHistoryListModel.getSize()==0) {
					return;
				}

				// Obtain destination file (factory handles the 'overwrite' dialog)
				Path file = DialogFactory.getGlobalFactory().showDestinationFileDialog(
						getFrame(),
						"plugins.prosody.painteEditorView.dialogs.exportHistory.title",  //$NON-NLS-1$
						null);

				if(file==null) {
					return;
				}

				// Collect history elements
				ParamsHistory history = new ParamsHistory();
				for(int i=0; i<paramsHistoryListModel.getSize(); i++) {
					history.items.add(paramsHistoryListModel.get(i));
				}

				JAXBContext context = JAXBContext.newInstance(ParamsHistory.class);
				Marshaller marshaller = context.createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

				marshaller.marshal(history, Files.newOutputStream(file));

				paramsHistoryList.clearSelection();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to export params history", ex); //$NON-NLS-1$

				UIUtil.beep();
			}

			refreshActions();
		}

		public void importParamsHistory(ActionEvent e) {
			try {

				// Obtain source file
				Path file = DialogFactory.getGlobalFactory().showSourceFileDialog(
						getFrame(),
						"plugins.prosody.painteEditorView.dialogs.importHistory.title",  //$NON-NLS-1$
						null);

				if(file==null || Files.notExists(file)) {
					return;
				}

				JAXBContext context = JAXBContext.newInstance(ParamsHistory.class);
				Unmarshaller unmarshaller = context.createUnmarshaller();

				ParamsHistory history = (ParamsHistory) unmarshaller.unmarshal(Files.newInputStream(file));

				if(history.items.isEmpty()) {
					return;
				}

				for(PaIntEParamsWrapper params : history.items) {
					paramsHistoryListModel.addElement(params);
				}

				paramsHistoryList.clearSelection();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to import params history", ex); //$NON-NLS-1$

				UIUtil.beep();
			}

			refreshActions();
		}
	}

	@XmlRootElement(name="params-history")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class ParamsHistory {
		@XmlElement(name="entry")
		public List<PaIntEParamsWrapper> items = new ArrayList<>();
	}

	private class ParamComponents extends MouseAdapter implements ActionListener, ChangeListener {
		private final String id;

		private final JLabel titleLabel, minLabel, maxLabel;
		private final JTextField textField;
		private final JSlider slider;
		private final JButton button;

		private double min, max;

		public ParamComponents(String id) {
			if (id == null)
				throw new NullPointerException("Invalid id"); //$NON-NLS-1$

			this.id = id;

			titleLabel = new JLabel();
			titleLabel.setText(ResourceManager.getInstance().get("plugins.prosody.painteEditorView.labels."+id+".name")); //$NON-NLS-1$ //$NON-NLS-2$

			minLabel = new JLabel("-"); //$NON-NLS-1$
			maxLabel = new JLabel("+"); //$NON-NLS-1$

			textField = new JTextField(6);
			textField.setDocument(new NumberDocument(true));
			textField.addActionListener(this);

			slider = new JSlider(SwingConstants.HORIZONTAL);
			slider.addChangeListener(this);
			slider.setMinorTickSpacing(1);
			slider.setMajorTickSpacing(10);
			slider.setMaximum(1000);
//			slider.setPaintTicks(true);
			slider.addMouseWheelListener(this);

			button = new JButton();
			button.setIcon(IconRegistry.getGlobalRegistry().getIcon("settings.gif")); //$NON-NLS-1$
			button.addActionListener(this);
			UIUtil.resizeComponent(button, 18, 18);
		}

		private String toLabel(double value) {
			return String.format(Locale.ENGLISH, "%.02f", value); //$NON-NLS-1$
		}

		public void setValue(double value) {
			textField.setText(toLabel(value));

			double relValue = (value-min)/(max-min);
			int newValue = slider.getMinimum()+(int)((slider.getMaximum()-slider.getMinimum()) * relValue);

			slider.setValue(newValue);
		}

		public void setMinMax(double newMin, double newMax) {
			min = newMin;
			max = newMax;

			minLabel.setText(toLabel(min));
			maxLabel.setText(toLabel(max));
		}

		public double getValue() {
//			float value = slider.getValue();
//			float range = (float)slider.getMaximum()-(float)slider.getMinimum();
//
//			return min + (max-min)*(value/range);
			String label = textField.getText();
			return (label==null || label.isEmpty()) ? min :Double.parseDouble(label);
		}

		/**
		 * @see java.awt.event.MouseAdapter#mouseWheelMoved(java.awt.event.MouseWheelEvent)
		 */
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if(e.getWheelRotation()<0) {
				// Away from user => scroll right
				if(slider.getValue()<slider.getMaximum()); {
					slider.setValue(slider.getValue()+1);
				}
			} else {
				if(slider.getValue()>slider.getMinimum()); {
					slider.setValue(slider.getValue()-1);
				}
			}
		}

		/**
		 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
		 */
		@Override
		public void stateChanged(ChangeEvent e) {
			double value = slider.getValue();
			double range = (double)slider.getMaximum()-(double)slider.getMinimum();

			value = min + (max-min)*(value/range);
			textField.setText(toLabel(value));

//			if(!slider.getValueIsAdjusting()) {
//			}
			refreshGraph();
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource()==button) {
				showBoundsDialog(this);
			} else if(e.getSource()==textField) {
				float value = Float.parseFloat(textField.getText());
				setValue(value);
			}


			refreshGraph();
		}
	}

	private class GraphComponent extends JComponent {

		private static final long serialVersionUID = -3436549345676825118L;

		private final PaIntEGraph graph;

		public GraphComponent() {
			graph = new PaIntEGraph();
			graph.getCurve().setMaxSampleCount(200);
		}

		public void refresh() {
			if(paramComponents!=null) {

				double dMax = paramComponents[PARAM_D].max;
				double dMin = paramComponents[PARAM_D].min;

				double cMax = Math.max(paramComponents[PARAM_C1].max, paramComponents[PARAM_C2].max);

				dMin = Math.min(dMin, dMin-cMax);

				// Make sure y axis doesn't go into negative space!
				dMin = Math.max(dMin, 0F);

				Axis.Integer yAxis = (Axis.Integer) graph.getYAxis();
				yAxis.setMinValue((int) Math.floor(dMin));
				yAxis.setMaxValue((int) Math.ceil(dMax));
			}

			repaint();
		}

		/**
		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
		 */
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			Dimension size = getSize();
			Insets insets = getInsets();

			Rectangle area = new Rectangle(insets.left, insets.top,
					size.width-insets.left-insets.right, size.height-insets.top-insets.bottom);

			graph.paint(g, painteParams, area);
		}

		/**
		 * @see javax.swing.JComponent#getPreferredSize()
		 */
		@Override
		public Dimension getPreferredSize() {
			// TODO Auto-generated method stub
			return super.getPreferredSize();
		}

		/**
		 * @see javax.swing.JComponent#getMinimumSize()
		 */
		@Override
		public Dimension getMinimumSize() {
			return new Dimension(300, 300);
		}

	}
}
