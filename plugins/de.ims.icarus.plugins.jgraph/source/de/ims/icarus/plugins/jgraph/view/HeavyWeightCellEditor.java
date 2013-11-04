/**
 * 
 */
package de.ims.icarus.plugins.jgraph.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.event.MouseInputListener;

import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.view.mxICellEditor;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxCellState;

import de.ims.icarus.resources.ResourceDomain;
import de.ims.icarus.resources.ResourceManager;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class HeavyWeightCellEditor implements mxICellEditor {

	protected static final String CANCEL_EDITING = "cancel-editing"; //$NON-NLS-1$

	protected static final String INSERT_BREAK = "insert-break"; //$NON-NLS-1$

	protected static final String SUBMIT_TEXT = "submit-text"; //$NON-NLS-1$

	protected final GraphPresenter presenter;

	protected transient Object editingCell;

	protected transient EventObject trigger;

	transient KeyStroke escapeKeystroke = KeyStroke.getKeyStroke("ESCAPE"); //$NON-NLS-1$

	transient KeyStroke enterKeystroke = KeyStroke.getKeyStroke("ENTER"); //$NON-NLS-1$

	protected transient Object vertexEditor;

	protected transient Object edgeEditor;

	protected transient Object currentEditor;

	protected AbstractAction cancelEditingAction = new AbstractAction() {

		private static final long serialVersionUID = 4299662825391540058L;

		public void actionPerformed(ActionEvent e) {
			presenter.stopEditing(true);
		}
	};

	protected AbstractAction textSubmitAction = new AbstractAction() {

		private static final long serialVersionUID = -6522329956804616380L;

		public void actionPerformed(ActionEvent e) {
			presenter.stopEditing(false);
		}
	};

	protected MouseInputListener movementHandler = new MouseInputListener() {

		private Point lastLocation;

		@Override
		public void mousePressed(MouseEvent e) {
			lastLocation = new Point(e.getPoint());
			e.consume();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			lastLocation = null;
			e.consume();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (lastLocation == null)
				lastLocation = new Point(e.getPoint());

			if (lastLocation.x != e.getX() || lastLocation.y != e.getY()) {
				JComponent comp = (JComponent) e.getSource();
				Rectangle bounds = comp.getBounds();
				bounds.x += e.getX() - lastLocation.x;
				bounds.y += e.getY() - lastLocation.y;
				
				checkBounds(bounds);

				comp.setBounds(bounds);
			}

			e.consume();
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			e.consume();
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			e.consume();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			e.consume();
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			e.consume();
		}
	};;

	public HeavyWeightCellEditor(GraphPresenter presenter) {
		if(presenter==null)
			throw new NullPointerException("Invalid presenter"); //$NON-NLS-1$

		this.presenter = presenter;
		
		ResourceDomain resourceDomain = ResourceManager.getInstance().getGlobalDomain();
		
		resourceDomain.prepareAction(cancelEditingAction, 
				"plugins.jgraph.editor.cancelAction.name",  //$NON-NLS-1$
				"plugins.jgraph.editor.cancelAction.description"); //$NON-NLS-1$
		resourceDomain.addAction(cancelEditingAction);

		resourceDomain.prepareAction(textSubmitAction, 
				"plugins.jgraph.editor.submitAction.name",  //$NON-NLS-1$
				"plugins.jgraph.editor.submitAction.description"); //$NON-NLS-1$
		resourceDomain.addAction(textSubmitAction);
		
		buildEditors();
	}
	
	protected void buildEditors() {
		vertexEditor = createVertexEditor();
		configEditorComponent(getEditorComponent(vertexEditor));

		edgeEditor = createEdgeEditor();
		configEditorComponent(getEditorComponent(edgeEditor));		
	}
	
	protected abstract JComponent getEditorComponent(Object editor);
	
	protected void configEditorComponent(JComponent editorComponent) {
		editorComponent.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createLineBorder(Color.black), BorderFactory
				.createEmptyBorder(5, 5, 5, 5)));
		editorComponent.addMouseListener(movementHandler);
		editorComponent.addMouseMotionListener(movementHandler);
		editorComponent.setCursor(Cursor.getDefaultCursor());
		installActions(editorComponent);
	}

	protected void installActions(JComponent editor) {
		editor.getInputMap().put(escapeKeystroke, CANCEL_EDITING);
		editor.getActionMap().put(CANCEL_EDITING, cancelEditingAction);
	}

	protected abstract Object createVertexEditor();

	protected abstract Object createEdgeEditor();
	
	protected void checkBounds(Rectangle bounds) {
		Rectangle box = presenter.getBounds();
		// FIXME right now we apply an offset of 30px so the panel 
		// does not shift over the bottom line
		box.y -= 30;

		if (bounds.x < box.x)
			bounds.x = box.x+2;

		if (bounds.y < box.y)
			bounds.y = box.y+2;

		if (bounds.x + bounds.width > box.x + box.width)
			bounds.x = box.x + box.width - bounds.width - 4;

		if (bounds.y + bounds.height > box.x + box.height)
			bounds.y = box.y + box.height - bounds.height - 4;		
	}
	
	public GraphPresenter getPresenter() {
		return presenter;
	}

	public Object getVertexEditor() {
		return vertexEditor;
	}

	public Object getEdgeEditor() {
		return edgeEditor;
	}

	/**
	 * 
	 * @see com.mxgraph.swing.view.mxICellEditor#getEditingCell()
	 */
	@Override
	public Object getEditingCell() {
		return editingCell;
	}

	/**
	 * 
	 * @see com.mxgraph.swing.view.mxICellEditor#startEditing(java.lang.Object,
	 * java.util.EventObject)
	 */
	@Override
	public void startEditing(Object cell, EventObject evt) {
		if(edgeEditor==null || vertexEditor==null) {
			return;
		}
		
		if (editingCell != null) {
			stopEditing(true);
		}

		mxCellState state = presenter.getGraph().getView().getState(cell);

		if (state != null) {
			editingCell = cell;
			trigger = evt;

			Object value = presenter.getGraph().getModel().getValue(cell);

			if (presenter.getGraph().getModel().isVertex(cell)) {
				currentEditor = vertexEditor;
				initVertexEditor(value);
			} else {
				currentEditor = edgeEditor;
				initEdgeEditor(value);
			}

			double scale = Math.max(1, presenter.getGraph().getView().getScale());
			
			JComponent editorComponent = getEditorComponent(currentEditor);
			
			Rectangle bounds = getCellBounds(state, scale);
			
			bounds = getEditorBounds(bounds);
			
			editorComponent.setBounds(bounds);
			editorComponent.setVisible(true);

			presenter.getGraphControl().add(editorComponent, 0);

			presenter.redraw(state);

			editorComponent.revalidate();
			editorComponent.requestFocusInWindow();
			editorComponent.repaint();

			// configureActionMaps();
		}
	}

	protected abstract void initVertexEditor(Object value);

	protected abstract void initEdgeEditor(Object value);

	/**
	 * 
	 * @see com.mxgraph.swing.view.mxICellEditor#stopEditing(boolean)
	 */
	@Override
	public void stopEditing(boolean cancel) {
		if(edgeEditor==null || vertexEditor==null) {
			return;
		}
		
		if (editingCell != null) {
			JComponent editorComponent = getEditorComponent(currentEditor);
			
			editorComponent.transferFocusUpCycle();

			Object cell = editingCell;
			editingCell = null;

			if (!cancel) {
				if (presenter.getGraph().getModel().isVertex(cell)) {
					readVertexEditor(cell);
				} else {
					readEdgeEditor(cell);
				}
			}

			trigger = null;

			mxCellState state = presenter.getGraph().getView().getState(
					cell);
			presenter.redraw(state);

			if (editorComponent.getParent() != null) {
				editorComponent.setVisible(false);
				editorComponent.getParent().remove(editorComponent);
			}
			
			currentEditor = null;

			presenter.requestFocusInWindow();
		}
	}

	protected abstract void readVertexEditor(Object cell);

	protected abstract void readEdgeEditor(Object cell);

	/**
	 * Returns true if the label bounds of the state should be used for the
	 * editor.
	 */
	protected boolean useLabelBounds(mxCellState state) {
		mxIGraphModel model = state.getView().getGraph().getModel();
		mxGeometry geometry = model.getGeometry(state.getCell());

		return ((geometry != null && geometry.getOffset() != null
				&& !geometry.isRelative() && (geometry.getOffset().getX() != 0 || geometry
				.getOffset().getY() != 0)) || model.isEdge(state.getCell()));
	}

	/**
	 * Returns the bounds to be used for the editor.
	 */
	public Rectangle getCellBounds(mxCellState state, double scale) {
		mxIGraphModel model = state.getView().getGraph().getModel();
		Rectangle bounds = null;

		if (useLabelBounds(state)) {
			bounds = state.getLabelBounds().getRectangle();
			bounds.height += 10;
		} else {
			bounds = state.getRectangle();
		}

		// Applies the horizontal and vertical label positions
		if (model.isVertex(state.getCell())) {
			String horizontal = mxUtils.getString(state.getStyle(),
					mxConstants.STYLE_LABEL_POSITION, mxConstants.ALIGN_CENTER);

			if (horizontal.equals(mxConstants.ALIGN_LEFT)) {
				bounds.x -= state.getWidth();
			} else if (horizontal.equals(mxConstants.ALIGN_RIGHT)) {
				bounds.x += state.getWidth();
			}

			String vertical = mxUtils.getString(state.getStyle(),
					mxConstants.STYLE_VERTICAL_LABEL_POSITION,
					mxConstants.ALIGN_MIDDLE);

			if (vertical.equals(mxConstants.ALIGN_TOP)) {
				bounds.y -= state.getHeight();
			} else if (vertical.equals(mxConstants.ALIGN_BOTTOM)) {
				bounds.y += state.getHeight();
			}
		}

		/*
		 * bounds.setSize((int) Math.max(bounds.getWidth(), Math
		 * .round(minimumWidth * scale)), (int) Math.max(bounds .getHeight(),
		 * Math.round(minimumHeight * scale)));
		 */

		return bounds;
	}

	public Rectangle getEditorBounds(Rectangle cellBounds) {
		Component editorComponent = getEditorComponent(currentEditor);
		Rectangle bounds = new Rectangle(editorComponent.getPreferredSize());

		Rectangle box = presenter.getGraphControl().getVisibleRect();		
		box.width -= presenter.getVerticalScrollBar().getWidth();
		box.height -= presenter.getHorizontalScrollBar().getHeight();
		
		// Place editor near cell
		
		bounds.x = cellBounds.x;
		bounds.y = cellBounds.y-bounds.height-1;
		
		// Check border constraints

		if (bounds.x < box.x) {
			bounds.x = box.x+4;
		}

		if (bounds.y < box.y) {
			bounds.y = box.y+4;
		}

		if (bounds.x + bounds.width > box.x + box.width) {
			bounds.x = box.x + box.width - bounds.width - 4;
		}

		if (bounds.y + bounds.height > box.x + box.height) {
			bounds.y = box.y + box.height - bounds.height - 4;
		}
		
		return bounds;
	}
}