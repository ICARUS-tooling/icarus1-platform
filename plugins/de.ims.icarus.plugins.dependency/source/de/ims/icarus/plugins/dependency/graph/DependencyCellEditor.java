/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.dependency.graph;

import javax.swing.JComponent;


import com.mxgraph.model.mxIGraphModel;

import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.language.dependency.DependencyData;
import de.ims.icarus.language.dependency.DependencyNodeData;
import de.ims.icarus.language.dependency.MutableDependencyData;
import de.ims.icarus.language.dependency.MutableDependencyData.DependencyDataEntry;
import de.ims.icarus.plugins.jgraph.view.HeavyWeightCellEditor;
import de.ims.icarus.ui.dialog.ChoiceFormEntry;
import de.ims.icarus.ui.dialog.ControlFormEntry;
import de.ims.icarus.ui.dialog.FormBuilder;
import de.ims.icarus.ui.dialog.NavigationFormEntry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyCellEditor extends HeavyWeightCellEditor {
	
	protected static String[] headOptions = {
		LanguageUtils.DATA_ROOT_LABEL,
		LanguageUtils.DATA_UNDEFINED_LABEL, 
	};

	public DependencyCellEditor(DependencyGraphPresenter presenter) {
		super(presenter);
	}

	@Override
	public DependencyGraphPresenter getPresenter() {
		return (DependencyGraphPresenter) super.getPresenter();
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
	 * @see de.ims.icarus.plugins.jgraph.view.HeavyWeightCellEditor#getEditorComponent(java.lang.Object)
	 */
	@Override
	protected JComponent getEditorComponent(Object editor) {
		if(editor instanceof FormBuilder) {
			return (JComponent) ((FormBuilder)editor).getContainer();
		} else 
			throw new IllegalArgumentException("Editor is not a form builder instance: "+editor); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.view.HeavyWeightCellEditor#createVertexEditor()
	 */
	@Override
	protected Object createVertexEditor() {
		FormBuilder formBuilder = FormBuilder.newLocalizingBuilder();
		
		int columns = 20;
		
		// INDEX
		formBuilder.addEntry("index", new NavigationFormEntry( //$NON-NLS-1$
				"plugins.dependency.labels.index").setResizeMode(FormBuilder.RESIZE_HORIZONTAL)); //$NON-NLS-1$
		// FORM
		formBuilder.addInputFormEntry("form", "plugins.dependency.labels.form", columns); //$NON-NLS-1$ //$NON-NLS-2$
		// LEMMA
		formBuilder.addInputFormEntry("lemma", "plugins.dependency.labels.lemma", columns); //$NON-NLS-1$ //$NON-NLS-2$
		// FEATURES
		formBuilder.addInputFormEntry("features", "plugins.dependency.labels.features", columns); //$NON-NLS-1$ //$NON-NLS-2$
		// POS
		formBuilder.addInputFormEntry("pos", "plugins.dependency.labels.pos", columns); //$NON-NLS-1$ //$NON-NLS-2$
		// HEAD
		formBuilder.addEntry("head", new ChoiceFormEntry( //$NON-NLS-1$
				"plugins.dependency.labels.head", headOptions, true)); //$NON-NLS-1$
		// RELATION
		formBuilder.addInputFormEntry("relation", "plugins.dependency.labels.relation", columns); //$NON-NLS-1$ //$NON-NLS-2$
		
		// TODO add editor entries for flags!
		
		// BUTTONS
		formBuilder.addEntry("control", new ControlFormEntry( //$NON-NLS-1$
				cancelEditingAction, textSubmitAction));
		
		formBuilder.buildForm();
		formBuilder.pack();
		
		return formBuilder;
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.view.HeavyWeightCellEditor#createEdgeEditor()
	 */
	@Override
	protected Object createEdgeEditor() {
		FormBuilder formBuilder = FormBuilder.newLocalizingBuilder();
		
		int columns = 20;
		
		// RELATION
		formBuilder.addInputFormEntry("relation", "plugins.dependency.labels.relation", columns); //$NON-NLS-1$ //$NON-NLS-2$
		
		// BUTTONS
		formBuilder.addEntry("control", new ControlFormEntry( //$NON-NLS-1$
				cancelEditingAction, textSubmitAction));
		
		formBuilder.buildForm();
		formBuilder.pack();
		
		return formBuilder;
	}
	

	/**
	 * @see de.ims.icarus.plugins.jgraph.view.HeavyWeightCellEditor#initVertexEditor(java.lang.Object)
	 */
	@Override
	protected void initVertexEditor(Object value) {
		if(value instanceof DependencyNodeData) {
		
			FormBuilder formBuilder = getVertexEditor();
			DependencyNodeData nodeData = (DependencyNodeData) value;
			DependencyData data = getPresenter().getData();
			
			NavigationFormEntry indexEntry = (NavigationFormEntry) formBuilder.getEntry("index"); //$NON-NLS-1$
			indexEntry.setMinimumValue(1);
			indexEntry.setMaximumValue(data.length());
			
			formBuilder.setValue("index", nodeData.getIndex()+1); //$NON-NLS-1$
			formBuilder.setValue("form", nodeData.getForm()); //$NON-NLS-1$
			formBuilder.setValue("lemma", nodeData.getLemma()); //$NON-NLS-1$
			formBuilder.setValue("features", nodeData.getFeatures()); //$NON-NLS-1$
			formBuilder.setValue("pos", nodeData.getPos()); //$NON-NLS-1$
			formBuilder.setValue("head", LanguageUtils.getHeadLabel(nodeData.getHead())); //$NON-NLS-1$
			formBuilder.setValue("relation", nodeData.getRelation()); //$NON-NLS-1$
			
			// TODO handle flags
		}
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.view.HeavyWeightCellEditor#initEdgeEditor(java.lang.Object)
	 */
	@Override
	protected void initEdgeEditor(Object value) {
		if(value instanceof String) {
			getEdgeEditor().setValue("relation", value); //$NON-NLS-1$
		}
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.view.HeavyWeightCellEditor#readVertexEditor(java.lang.Object)
	 */
	@Override
	protected void readVertexEditor(Object cell) {
		mxIGraphModel model = getPresenter().getGraph().getModel();
		Object value = model.getValue(cell);
		if(value instanceof DependencyNodeData) {
			FormBuilder formBuilder = getVertexEditor();
			
			DependencyNodeData nodeData = (DependencyNodeData) value;
			MutableDependencyData data = getPresenter().getData();
			DependencyDataEntry entry = data.getItem(nodeData.getIndex());
			
			nodeData = new DependencyNodeData();
			nodeData.setIndex((short) ((int) formBuilder.getValue("index") -1)); //$NON-NLS-1$
			nodeData.setForm((String) formBuilder.getValue("form")); //$NON-NLS-1$
			nodeData.setLemma((String) formBuilder.getValue("lemma")); //$NON-NLS-1$
			nodeData.setFeatures((String) formBuilder.getValue("features")); //$NON-NLS-1$
			nodeData.setPos((String) formBuilder.getValue("pos")); //$NON-NLS-1$
			nodeData.setHead(LanguageUtils.parseHeadLabel((String) formBuilder.getValue("head"))); //$NON-NLS-1$
			nodeData.setRelation((String) formBuilder.getValue("relation")); //$NON-NLS-1$

			if (nodeData.checkDifference(entry)) {
				entry.copyFrom(nodeData);
			}
		}
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.view.HeavyWeightCellEditor#readEdgeEditor(java.lang.Object)
	 */
	@Override
	protected void readEdgeEditor(Object cell) {
		mxIGraphModel model = getPresenter().getGraph().getModel();

		Object vertex = model.getTerminal(cell, false);
		Object value = model.getValue(vertex);
		if (value instanceof DependencyNodeData) {
			DependencyNodeData nodeData = (DependencyNodeData) value;
			DependencyDataEntry entry = getPresenter().getData().getItem(nodeData.getIndex());
			String relation = (String)getEdgeEditor().getValue("relation"); //$NON-NLS-1$

			if (!entry.getRelation().equals(relation)) {
				entry.setRelation(relation);
			}
		}
	}

}
