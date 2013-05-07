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

import net.ikarus_systems.icarus.language.SentenceDataEvent;
import net.ikarus_systems.icarus.language.SentenceDataListener;
import net.ikarus_systems.icarus.language.dependency.DependencyData;
import net.ikarus_systems.icarus.language.dependency.DependencyGraph;
import net.ikarus_systems.icarus.language.dependency.DependencyNodeData;
import net.ikarus_systems.icarus.language.dependency.DependencyUtils;
import net.ikarus_systems.icarus.language.dependency.MutableDependencyData;
import net.ikarus_systems.icarus.plugins.jgraph.ui.GraphPresenter;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.data.ContentType;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.view.mxGraph;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyGraphPresenter extends GraphPresenter {

	private static final long serialVersionUID = -8262542126697438425L;
	
	protected MutableDependencyData data;

	public DependencyGraphPresenter(boolean editable) {
		super(editable);
	}

	/**
	 * 
	 * @see net.ikarus_systems.icarus.plugins.jgraph.ui.GraphPresenter#createGraph()
	 */
	@Override
	protected mxGraph createGraph() {
		return new DependencyGraph();
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#isPresenting()
	 */
	@Override
	public boolean isPresenting() {
		return data!=null;
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#getPresentedData()
	 */
	@Override
	public Object getPresentedData() {
		return data;
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.jgraph.ui.GraphPresenter#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return DependencyUtils.getDependencyContentType();
	}
	
	public MutableDependencyData getData() {
		return data;
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.jgraph.ui.GraphPresenter#setData(java.lang.Object, net.ikarus_systems.icarus.util.Options)
	 */
	@Override
	protected void setData(Object data, Options options) {
		MutableDependencyData newData = null;
		if(data instanceof MutableDependencyData) {
			newData = (MutableDependencyData)data;
		} else if(data!=null) {
			newData = new MutableDependencyData();
			newData.copyFrom((DependencyData)data);
		}
		
		MutableDependencyData oldData = getData();
		if(oldData!=null) {
			oldData.removeSentenceDataListener((SentenceDataListener) getHandler());
		}
		
		this.data = newData;
		
		if(newData!=null) {
			newData.addSentenceDataListener((SentenceDataListener) getHandler());
		}
	}

	protected mxCell newVertex(Object item, String id, double x, double y) {
		mxCell cell = new mxCell(item);
		cell.setId(id);
		
		cell.setGeometry(new mxGeometry(x, y, 50, 24));
		cell.setVertex(true);
		cell.setConnectable(true);

		return cell;
	}

	protected mxCell newEdge(Object value, String id, Object source, Object target) {
		mxCell cell = new mxCell();
		cell.setEdge(true);
		cell.setValue(value);
		cell.setId(id);
		
		mxGeometry geo = new mxGeometry();
		geo.setRelative(true);
		cell.setGeometry(geo);

		return cell;
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.jgraph.ui.GraphPresenter#syncToGraph()
	 */
	@Override
	protected void syncToGraph() {
		
		mxIGraphModel model = graph.getModel();
		model.beginUpdate();
		try {
			Object parent = graph.getDefaultParent();
			
			// Clear graph
			Object[] cells = mxGraphModel.getChildren(model, parent);
			for(Object cell : cells) {
				model.remove(cell);
			}
			
			// Just abort if nothing to display
			if(data==null || data.isEmpty()) {
				return;
			}
			
			Object[] vertices = new Object[data.length()];
			double x = 2*graph.getGridSize();
			double y = 2*graph.getGridSize();
			
			// Add vertices
			for(int i=0; i<data.length(); i++) {
				DependencyNodeData nodeData = new DependencyNodeData(data, i);
				vertices[i] = newVertex(nodeData, "node"+i, x, y); //$NON-NLS-1$
				graph.addCell(vertices[i]);
				
				x += model.getGeometry(vertices[1]).getWidth()+ graph.getGridSize();
			}
			
			// Add edges
			for(int i=0; i<data.length(); i++) {
				int head = data.getHead(i);
				if(DependencyUtils.isUndefined(head) || DependencyUtils.isRoot(head)) {
					continue;
				}
				
				Object source = vertices[head];
				Object target = vertices[i];
				
				Object edge = newEdge(data.getRelation(i), "head"+i,  //$NON-NLS-1$
						model.getValue(source), model.getValue(target));
				
				graph.addEdge(edge, parent, source, target, null);
			}
			
			// Apply styles
			refreshStyles();
			
			// Apply layout
			refreshLayout();
		} finally {
			model.endUpdate();
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.jgraph.ui.GraphPresenter#syncToData()
	 */
	@Override
	protected void syncToData() {
		// TODO Auto-generated method stub

	}

	@Override
	protected Handler createHandler() {
		return new DHandler();
	}

	protected class DHandler extends Handler implements SentenceDataListener {

		/**
		 * @see net.ikarus_systems.icarus.language.SentenceDataListener#dataChanged(net.ikarus_systems.icarus.language.SentenceDataEvent)
		 */
		@Override
		public void dataChanged(SentenceDataEvent event) {
			if(isIgnoringChanges()) {
				return;
			}
			
			// TODO
		}
	}
}