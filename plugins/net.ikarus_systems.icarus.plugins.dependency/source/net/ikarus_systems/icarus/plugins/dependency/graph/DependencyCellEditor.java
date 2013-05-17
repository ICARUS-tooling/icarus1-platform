/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.dependency.graph;

import javax.swing.JComponent;
import javax.swing.JPanel;

import net.ikarus_systems.icarus.plugins.jgraph.view.HeavyWeightCellEditor;
import net.ikarus_systems.icarus.ui.dialog.FormBuilder;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyCellEditor extends HeavyWeightCellEditor {

	public DependencyCellEditor(DependencyGraphPresenter presenter) {
		super(presenter);
	}

	@Override
	public DependencyGraphPresenter getPresenter() {
		return (DependencyGraphPresenter) super.getPresenter();
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
		FormBuilder formBuilder = FormBuilder.newBuilder(new JPanel());
		
		return formBuilder;
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.jgraph.view.HeavyWeightCellEditor#createEdgeEditor()
	 */
	@Override
	protected Object createEdgeEditor() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.jgraph.view.HeavyWeightCellEditor#initVertexEditor(java.lang.Object)
	 */
	@Override
	protected void initVertexEditor(Object value) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.jgraph.view.HeavyWeightCellEditor#initEdgeEditor(java.lang.Object)
	 */
	@Override
	protected void initEdgeEditor(Object value) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.jgraph.view.HeavyWeightCellEditor#readVertexEditor(java.lang.Object)
	 */
	@Override
	protected void readVertexEditor(Object cell) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.jgraph.view.HeavyWeightCellEditor#readEdgeEditor(java.lang.Object)
	 */
	@Override
	protected void readEdgeEditor(Object cell) {
		// TODO Auto-generated method stub

	}

}
