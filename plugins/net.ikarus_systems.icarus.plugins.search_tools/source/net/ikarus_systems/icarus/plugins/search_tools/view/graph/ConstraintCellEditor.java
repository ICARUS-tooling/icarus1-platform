/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.search_tools.view.graph;

import java.awt.Insets;
import java.util.logging.Level;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

import net.ikarus_systems.icarus.language.LanguageUtils;
import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.jgraph.view.HeavyWeightCellEditor;
import net.ikarus_systems.icarus.plugins.search_tools.view.SearchUtilityListCellRenderer;
import net.ikarus_systems.icarus.search_tools.ConstraintFactory;
import net.ikarus_systems.icarus.search_tools.EdgeType;
import net.ikarus_systems.icarus.search_tools.SearchConstraint;
import net.ikarus_systems.icarus.search_tools.SearchOperator;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.dialog.ChoiceFormEntry;
import net.ikarus_systems.icarus.ui.dialog.ControlFormEntry;
import net.ikarus_systems.icarus.ui.dialog.FormBuilder;
import net.ikarus_systems.icarus.ui.dialog.FormBuilder.FormEntry;

import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.view.mxGraph;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ConstraintCellEditor extends HeavyWeightCellEditor {

	public ConstraintCellEditor(ConstraintGraphPresenter presenter) {
		super(presenter);
	}

	@Override
	public ConstraintGraphPresenter getPresenter() {
		return (ConstraintGraphPresenter) super.getPresenter();
	}

	@Override
	public FormBuilder getVertexEditor() {
		return (FormBuilder) vertexEditor;
	}

	@Override
	public FormBuilder getEdgeEditor() {
		return (FormBuilder) edgeEditor;
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.jgraph.view.HeavyWeightCellEditor#getEditorComponent(java.lang.Object)
	 */
	@Override
	protected JComponent getEditorComponent(Object editor) {
		if(editor instanceof FormBuilder) {
			return (JComponent) ((FormBuilder)editor).getContainer();
		} else 
			throw new IllegalArgumentException("Editor is not a form builder instance: "+editor); //$NON-NLS-1$
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.jgraph.view.HeavyWeightCellEditor#createVertexEditor()
	 */
	@Override
	protected Object createVertexEditor() {
		FormBuilder formBuilder = FormBuilder.newLocalizingBuilder();
		
		ConstraintFactory[] factories = getPresenter().getNodeConstraintFactories();
		
		if(factories!=null) {
			// NEGATED
			formBuilder.addToggleFormEntry("negated", "plugins.searchTools.labels.negated"); //$NON-NLS-1$ //$NON-NLS-2$
			
			// ROOT
			formBuilder.addToggleFormEntry("root", "plugins.searchTools.labels.negated"); //$NON-NLS-1$ //$NON-NLS-2$
			
			// CONSTRAINTS
			for(int i=0; i<factories.length; i++) {
				formBuilder.addEntry("constraint_"+i,  //$NON-NLS-1$
						new ConstraintFormEntry(factories[i]));
			}
		} else {
			LoggerFactory.log(this, Level.WARNING, "Missing constraint factories - skipped editor creation"); //$NON-NLS-1$
		}
		
		// BUTTONS
		formBuilder.addEntry("control", new ControlFormEntry( //$NON-NLS-1$
				cancelEditingAction, textSubmitAction));
		
		formBuilder.buildForm();
		formBuilder.pack();
		
		return formBuilder;
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.jgraph.view.HeavyWeightCellEditor#createEdgeEditor()
	 */
	@Override
	protected Object createEdgeEditor() {
		FormBuilder formBuilder = FormBuilder.newLocalizingBuilder();
		
		ConstraintFactory[] factories = getPresenter().getEdgeConstraintFactories();
		
		if(factories!=null) {
			// NEGATED
			formBuilder.addToggleFormEntry("negated", "plugins.searchTools.labels.negated"); //$NON-NLS-1$ //$NON-NLS-2$
			
			// EDGE TYPE
			ChoiceFormEntry entry = new ChoiceFormEntry(
					"plugins.searchTools.labels.edgeType",  //$NON-NLS-1$
					EdgeType.values());
			entry.setResizeMode(FormBuilder.RESIZE_REMAINDER);
			entry.getComboBox().setRenderer(sharedRenderer);
			UIUtil.resizeComponent(entry.getComboBox(), 100, 20);
			formBuilder.addEntry("edgeType", entry); //$NON-NLS-1$
			
			// CONSTRAINTS
			for(int i=0; i<factories.length; i++) {
				formBuilder.addEntry("constraint_"+i,  //$NON-NLS-1$
						new ConstraintFormEntry(factories[i]));
			}
		} else {
			LoggerFactory.log(this, Level.WARNING, "Missing constraint factories - skipped editor creation"); //$NON-NLS-1$
		}
		
		// BUTTONS
		formBuilder.addEntry("control", new ControlFormEntry( //$NON-NLS-1$
				cancelEditingAction, textSubmitAction));
		
		formBuilder.buildForm();
		formBuilder.pack();
		
		return formBuilder;
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.jgraph.view.HeavyWeightCellEditor#initVertexEditor(java.lang.Object)
	 */
	@Override
	protected void initVertexEditor(Object value) {
		ConstraintNodeData nodeData = (ConstraintNodeData) value;
		
		FormBuilder formBuilder = getVertexEditor();
		
		formBuilder.setValue("negated", nodeData.isNegated()); //$NON-NLS-1$
		formBuilder.setValue("root", nodeData.isRoot()); //$NON-NLS-1$
		
		SearchConstraint[] constraints = nodeData.getConstraints();
		for(int i=0; i<constraints.length; i++) {
			formBuilder.setValue("constraint_"+i, constraints[i]); //$NON-NLS-1$
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.jgraph.view.HeavyWeightCellEditor#initEdgeEditor(java.lang.Object)
	 */
	@Override
	protected void initEdgeEditor(Object value) {
		ConstraintEdgeData edgeData = (ConstraintEdgeData) value;
		
		FormBuilder formBuilder = getEdgeEditor();
		
		formBuilder.setValue("negated", edgeData.isNegated()); //$NON-NLS-1$
		formBuilder.setValue("edgeType", edgeData.getEdgeType()); //$NON-NLS-1$
		
		SearchConstraint[] constraints = edgeData.getConstraints();
		for(int i=0; i<constraints.length; i++) {
			formBuilder.setValue("constraint_"+i, constraints[i]); //$NON-NLS-1$
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.jgraph.view.HeavyWeightCellEditor#readVertexEditor(java.lang.Object)
	 */
	@Override
	protected void readVertexEditor(Object cell) {
		ConstraintFactory[] factories = getPresenter().getNodeConstraintFactories();
		if(factories==null || factories.length==0) {
			LoggerFactory.log(this, Level.WARNING, "Missing node constraint factories on constraint presenter"); //$NON-NLS-1$
			return;
		}
		
		ConstraintNodeData nodeData = new ConstraintNodeData(factories.length);
		FormBuilder formBuilder = getVertexEditor();
		
		nodeData.setNegated((boolean) formBuilder.getValue("negated")); //$NON-NLS-1$
		nodeData.setRoot((boolean) formBuilder.getValue("root")); //$NON-NLS-1$
		for(int i=0; i<factories.length; i++) {
			nodeData.setConstraint(i, (SearchConstraint) formBuilder.getValue("constraint_"+i)); //$NON-NLS-1$
		}

		mxGraph graph = getPresenter().getGraph();
		mxIGraphModel model = graph.getModel();
		
		model.beginUpdate();
		try {
			graph.cellLabelChanged(cell, nodeData, graph.isAutoSizeCell(cell));
		} finally {
			model.endUpdate();
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.jgraph.view.HeavyWeightCellEditor#readEdgeEditor(java.lang.Object)
	 */
	@Override
	protected void readEdgeEditor(Object cell) {
		ConstraintFactory[] factories = getPresenter().getNodeConstraintFactories();
		if(factories==null || factories.length==0) {
			LoggerFactory.log(this, Level.WARNING, "Missing node constraint factories on constraint presenter"); //$NON-NLS-1$
			return;
		}
		
		ConstraintEdgeData edgeData = new ConstraintEdgeData(factories.length);
		FormBuilder formBuilder = getEdgeEditor();
		
		// Negated state
		edgeData.setNegated((boolean) formBuilder.getValue("negated")); //$NON-NLS-1$
		
		// Edge style
		edgeData.setEdgeType((EdgeType) formBuilder.getValue("edgeType")); //$NON-NLS-1$
		
		// Constraints
		for(int i=0; i<factories.length; i++) {
			edgeData.setConstraint(i, (SearchConstraint) formBuilder.getValue("constraint_"+i)); //$NON-NLS-1$
		}

		mxGraph graph = getPresenter().getGraph();
		mxIGraphModel model = graph.getModel();
		
		model.beginUpdate();
		try {
			graph.cellLabelChanged(cell, edgeData, graph.isAutoSizeCell(cell));
		} finally {
			model.endUpdate();
		}

	}

	@SuppressWarnings("serial")
	protected static class NumberDocument extends PlainDocument {

		@Override
		public void insertString(int offset, String str, AttributeSet a)
				throws BadLocationException {
			super.insertString(offset, str, a);
			String newText = getText(0, getLength());
			if (!(newText.equals(LanguageUtils.DATA_UNDEFINED_LABEL) 
					|| newText.matches("^\\d*$"))) { //$NON-NLS-1$
				remove(offset, str.length());
			}
		}
	}
	
	private static SearchUtilityListCellRenderer sharedRenderer =
			new SearchUtilityListCellRenderer();
	
	private static class ConstraintFormEntry implements FormEntry {
		
		private JComboBox<SearchOperator> operatorSelect;
		private JComponent valueSelect;
		private JLabel label;
		
		private ConstraintFactory factory;
		
		ConstraintFormEntry(ConstraintFactory factory) {
			this.factory = factory;
			
			SearchOperator[] operators = factory.getSupportedOperators();
			
			operatorSelect = new JComboBox<>(operators);
			operatorSelect.setEditable(false);
			operatorSelect.setRenderer(sharedRenderer);
			operatorSelect.setMaximumRowCount(operators.length);
			UIUtil.resizeComponent(operatorSelect, 60, 20);
			
			Object[] labelSet = factory.getLabelSet();
			Class<?> valueClass = factory.getValueClass();
			Object defaultValue = factory.getDefaultValue();
			
			if(labelSet==null) {
				JTextField tf = new JTextField(15);
				
				if(Integer.class.equals(valueClass)) {
					tf.setDocument(new NumberDocument());
				}
				
				if(defaultValue!=null) {
					tf.setText(String.valueOf(defaultValue));
				}
				
				valueSelect = tf;
			} else {
				JComboBox<Object> cb = new JComboBox<>(labelSet);
				
				if(Integer.class.equals(valueClass)) {
					JTextComponent editor = (JTextComponent) cb.getEditor().getEditorComponent();
					editor.setDocument(new NumberDocument());
				}
				
				if(valueClass!=null) {
					cb.setEditable(true);
				}
				
				cb.setSelectedItem(defaultValue);
				
				valueSelect = cb;
			}
			
			UIUtil.resizeComponent(valueSelect, 100, 20);
			
			label = new JLabel(factory.getName());
		}

		/**
		 * @see net.ikarus_systems.icarus.ui.dialog.FormBuilder.FormEntry#setValue(java.lang.Object)
		 */
		@Override
		public ConstraintFormEntry setValue(Object value) {
			SearchConstraint constraint = (SearchConstraint)value;
			

			Object currentValue = factory.valueToLabel(constraint.getValue());
			
			operatorSelect.setSelectedItem(constraint.getOperator());
			if(valueSelect instanceof JTextField) {
				((JTextField)valueSelect).setText(String.valueOf(currentValue));
			} else {
				((JComboBox<?>)valueSelect).setSelectedItem(currentValue);
			}
			label.setText(factory.getName());
			label.setToolTipText(factory.getDescription());
			
			return this;
		}

		/**
		 * @see net.ikarus_systems.icarus.ui.dialog.FormBuilder.FormEntry#getValue()
		 */
		@Override
		public Object getValue() {
			
			SearchOperator operator = (SearchOperator) operatorSelect.getSelectedItem();
			Object value = null;
			
			if(valueSelect instanceof JTextField) {
				value = ((JTextField)valueSelect).getText();
			} else {
				value = ((JComboBox<?>)valueSelect).getSelectedItem();
			}
			
			value = factory.labelToValue(value);
			
			if(value instanceof String && Integer.class.equals(factory.getValueClass())) {
				value = Integer.parseInt((String)value);
			}
			
			return factory.createConstraint(value, operator);
		}

		/**
		 * @see net.ikarus_systems.icarus.ui.dialog.FormBuilder.FormEntry#clear()
		 */
		@Override
		public ConstraintFormEntry clear() {
			operatorSelect.setSelectedIndex(0);
			
			if(valueSelect instanceof JTextField) {
				((JTextField)valueSelect).setText(null);
			} else {
				JComboBox<?> cb = (JComboBox<?>) valueSelect;
				if(cb.isEditable()) {
					cb.setSelectedItem(null);
				} else {
					cb.setSelectedIndex(0);
				}
			}
			
			return this;
		}

		/**
		 * @see net.ikarus_systems.icarus.ui.dialog.FormBuilder.FormEntry#addToForm(net.ikarus_systems.icarus.ui.dialog.FormBuilder)
		 */
		@Override
		public ConstraintFormEntry addToForm(FormBuilder builder) {
			builder.feedComponent(label);
			builder.feedComponent(operatorSelect, new Insets(1, 4, 1, 4));
			builder.feedComponent(valueSelect, null, FormBuilder.RESIZE_HORIZONTAL);
			builder.newLine();
			
			return this;
		}
		
	}
}
