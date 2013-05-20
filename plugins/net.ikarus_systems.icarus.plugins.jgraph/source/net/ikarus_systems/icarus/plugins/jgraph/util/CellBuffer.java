/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.jgraph.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import net.ikarus_systems.icarus.util.CloneableObject;
import net.ikarus_systems.icarus.util.Exceptions;
import net.ikarus_systems.icarus.util.Filter;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlRootElement(name="graph")
@XmlAccessorType(XmlAccessType.FIELD)
public class CellBuffer implements CloneableObject {
	
	@XmlElements({
		@XmlElement(name="vertex", type=Vertex.class),
		@XmlElement(name="edge", type=Edge.class)
	})
	public Cell[] cells;
	
	@XmlAttribute
	public String graphType;
	
	private static Map<Cell, Cell> cloneMap = new HashMap<>();
	
	private static AtomicInteger idCounter = new AtomicInteger();
	
	public boolean isEmpty() {
		return cells==null || cells.length==0;
	}
	
	public static Object[] buildCells(CellBuffer buffer) {
		Exceptions.testNullArgument(buffer, "buffer"); //$NON-NLS-1$
		
		List<Object> cells = new ArrayList<>();
		Map<Cell, mxCell> cellMap = new HashMap<>();
		
		for(Cell cell : buffer.cells) {
			feedCell(cells, cell, cellMap);
		}
		
		return cells.toArray();
	}
	
	private static void feedCell(List<Object> cells, Cell cell, Map<Cell, mxCell> cellMap) {
		cells.add(buildCell(cell, cellMap));
		
		if(cell.children!=null) {
			for(Cell c : cell.children) {
				feedCell(cells, c, cellMap);
			}
		}
	}
	
	public static mxGeometry buildGeometry(Geometry geometry) {
		if(geometry==null)
			return new mxGeometry();
		
		mxGeometry geo = new mxGeometry();
		geo.setX(geometry.x);
		geo.setY(geometry.y);
		geo.setWidth(geometry.width);
		geo.setHeight(geometry.height);
		
		return geo;
	}
	
	public static mxICell buildCell(Cell cell, Map<Cell, mxCell> cellMap) {
		if(cell==null)
			return null;
		
		mxCell c = cellMap.get(cell);
		
		if(c!=null)
			return c;
		
		c = new mxCell();
		cellMap.put(cell, c);
		
		c.setValue(cell.value);
		c.setId(cell.id);
		c.setStyle(cell.style);
		
		if(cell instanceof Vertex) {
			c.setGeometry(buildGeometry(((Vertex)cell).geometry));
			c.setVertex(true);
			c.setConnectable(true);
		} else if(cell instanceof Edge) {
			c.setTerminal(buildCell(((Edge)cell).source, cellMap), true);
			c.setTerminal(buildCell(((Edge)cell).target, cellMap), false);
			c.setEdge(true);
			c.setGeometry(new mxGeometry());
			c.getGeometry().setRelative(true);
		}
		
		return c;
	}
	
	public static Comparator<Cell> cellSorter = new Comparator<Cell>() {

		@Override
		public int compare(Cell o1, Cell o2) {
			return o1 instanceof Vertex ? 
					(o1.getClass()==o2.getClass() ? 1 : -1) : 1;
		}
	};
	
	public static CellBuffer createBuffer(mxIGraphModel model, Filter filter, String type) {
		Exceptions.testNullArgument(model, "model"); //$NON-NLS-1$
		Exceptions.testNullArgument(type, "type"); //$NON-NLS-1$
		
		Object[] sources = GraphUtils.getAllCells(model, filter);
		
		return createBuffer(sources, model, type);
	}
	
	public static CellBuffer createBuffer(Object[] sources, mxIGraphModel model, String type) {
		Exceptions.testNullArgument(model, "model"); //$NON-NLS-1$
		Exceptions.testNullArgument(sources, "sources"); //$NON-NLS-1$
		Exceptions.testNullArgument(type, "type"); //$NON-NLS-1$
		
		if(sources.length==0) {
			return null;
		}
		
		idCounter.set(0);
		
		CellBuffer buffer = new CellBuffer();
		buffer.graphType = type;
		
		Map<Object, Cell> cache = new HashMap<>();
		
		for(Object cell : sources) {
			createCell(cell, model, cache);
		}
		
		Collection<Cell> cells = cache.values();
		Cell[] tmp = new Cell[cells.size()];
		buffer.cells = cells.toArray(tmp);
		
		Arrays.sort(buffer.cells, cellSorter);
		
		return buffer;
	}
	
	public static Geometry createGeometry(mxGeometry geo) {
		if(geo==null)
			return null;
		
		Geometry geometry = new Geometry();
		geometry.x = geo.getX();
		geometry.y = geo.getY();
		geometry.width = geo.getWidth();
		geometry.height = geo.getHeight();
		
		return geometry;
	}
	
	public static Cell createCell(Object source, mxIGraphModel model, 
			Map<Object, Cell> cache) {
		Cell cell = cache.get(source);
		if(cell!=null)
			return cell;
		
		if(model.isVertex(source)) {
			Vertex vertex = new Vertex();
			vertex.geometry = createGeometry(model.getGeometry(source));
			
			cell = vertex;
		} else if(model.isEdge(source)) {
			Edge edge = new Edge();
			edge.source = (Vertex) createCell(
					model.getTerminal(source, true), model, cache);
			edge.target = (Vertex) createCell(
					model.getTerminal(source, false), model, cache);
			
			cell = edge;
		} else
			throw new IllegalArgumentException("Cell is neighter vertex nor edge: "+source); //$NON-NLS-1$
		
		cell.style = model.getStyle(source);
		cell.value = model.getValue(source);
		cell.id = source instanceof mxICell ? ((mxICell)source).getId() : "cell_"+idCounter.getAndIncrement(); //$NON-NLS-1$
		
		int childCount = model.getChildCount(source);
		
		if(childCount>0) {
			Cell[] tmp = new Cell[childCount];
			for(int i=0; i<childCount; i++) {
				tmp[i] = createCell(model.getChildAt(source, i), model, cache);
			}
			cell.children = tmp;
		}
		
		cache.put(source, cell);
		
		return cell;
	}
	
	@SuppressWarnings("unchecked")
	static <C extends Cell> C clone(C cell) {
		synchronized (cloneMap) {
			Cell clone = cloneMap.get(cell);
			if(clone==null) {
				clone = cell.clone();
				cloneMap.put(cell, clone);
			}
			return (C) clone;
		}
	}
	
	@Override
	public CellBuffer clone() {
		synchronized (cloneMap) {
			CellBuffer buffer = new CellBuffer();
			try {
				// make sure we start with a clean table
				cloneMap.clear();
				
				buffer.graphType = graphType;
				
				if(!isEmpty()) {
					int size = cells.length;
					Cell[] tmp = new Cell[size];
					for(int i=0; i<size; i++) {
						tmp[i] = clone(cells[i]);
					}
					
					buffer.cells = tmp;
				}
			} finally {
				cloneMap.clear();
			}
			
			return buffer;
		}
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public abstract static class Cell implements CloneableObject {
		@XmlElement
		public Object value;
		
		@XmlAttribute(required=false)
		public String style;
		
		@XmlID
		@XmlAttribute(required=false)
		public String id;

		@XmlElement
		public Cell[] children;
		
		@Override
		public abstract Cell clone();
		
		protected void copyFrom(Cell cell) {
			synchronized (cloneMap) {
				if(cell.value instanceof CloneableObject)
					value = ((CloneableObject)cell.value).clone();
				id = cell.id;
				style = cell.style;
				
				if(cell.children!=null) {
					int size = cell.children.length;
					Cell[] tmp = new Cell[size];
					for(int i=0; i<size; i++) {
						tmp[i] = CellBuffer.clone(cell.children[i]);
					}
					children = tmp;
				}
			}
		}
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	@XmlRootElement(name="vertex")
	public static class Vertex extends Cell {
		
		@XmlElement(name="geomentry", required=false)
		public Geometry geometry;

		@Override
		public Cell clone() {
			synchronized (cloneMap) {
				Vertex vertex = new Vertex();
				vertex.copyFrom(this);
				vertex.geometry = geometry==null ? null : geometry.clone();
				
				return vertex;
			}
		}
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	@XmlRootElement(name="edge")
	public static class Edge extends Cell {
		@XmlIDREF
		@XmlElement
		public Vertex source;
		
		@XmlIDREF
		@XmlElement
		public Vertex target;

		@Override
		public Edge clone() {
			synchronized (cloneMap) {
				Edge edge = new Edge();
				edge.copyFrom(this);
				edge.source = CellBuffer.clone(source);
				edge.target = CellBuffer.clone(target);
			
				return edge;
			}
		}
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	@XmlRootElement(name="geometry")
	public static class Geometry implements CloneableObject {
		
		@XmlAttribute
		public double x;
		
		@XmlAttribute
		public double y;
		
		@XmlAttribute(required=false)
		public double width;
		
		@XmlAttribute(required=false)
		public double height;
		
		@Override
		public Geometry clone() {
			Geometry geo = new Geometry();
			geo.x = x;
			geo.y = y;
			geo.width = width;
			geo.height = height;
			
			return geo;
		}
	}
}
