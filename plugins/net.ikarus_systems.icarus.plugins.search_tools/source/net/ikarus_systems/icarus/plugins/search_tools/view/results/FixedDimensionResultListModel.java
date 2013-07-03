/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.search_tools.view.results;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;

import net.ikarus_systems.icarus.search_tools.result.SearchResult;
import net.ikarus_systems.icarus.ui.NumberDisplayMode;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.Updatable;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class FixedDimensionResultListModel extends AbstractListModel<Object> 
		implements Updatable {
	
	private static final long serialVersionUID = 3932501248758779138L;


	protected SearchResult resultData;
	
	protected NumberDisplayMode displayMode = NumberDisplayMode.RAW;
	
	protected int dimension = 0;
	
	protected int[] rowTransform;
	
	protected final RowHeaderModel rowHeaderModel;
	
	public FixedDimensionResultListModel(SearchResult searchResult) {
		
		rowHeaderModel = new RowHeaderModel();
	}

	public ListModel<String> getRowHeaderModel() {
		return rowHeaderModel;
	}

	public SearchResult getResultData() {
		return resultData;
	}

	public void setResultData(SearchResult resultData) {
		if(resultData==null)
			throw new IllegalArgumentException("Invalid result data"); //$NON-NLS-1$
		if(resultData.getDimension()<1)
			throw new IllegalArgumentException("Unsupported result dimension: "+resultData.getDimension()); //$NON-NLS-1$
		
		this.resultData = resultData;

		update();
	}	
	
	public NumberDisplayMode getDisplayMode() {
		return displayMode;
	}

	public void setDisplayMode(NumberDisplayMode displayMode) {
		if(displayMode==null)
			throw new NullPointerException();
		
		if(this.displayMode==displayMode) {
			return;
		}
		
		this.displayMode = displayMode;
		
		updateEDT();
	}
	
	public int getDimension() {
		return dimension;
	}


	public void setDimension(int dimension) {
		if(this.dimension==dimension) {
			return;
		}
		
		this.dimension = dimension;
		
		update();
	}


	public boolean isSorted() {
		return rowTransform!=null;
	}

	public void fireModelChanged() {
		fireContentsChanged(this, 0, Math.max(0, getSize() - 1));
	}

	@Override
	public boolean update() {
		fireModelChanged();

		rowHeaderModel.update();

		return true;
	}
	
	private void updateEDT() {
		if(SwingUtilities.isEventDispatchThread()) {
			update();
		} else {
			UIUtil.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					update();
				}
			});
		}
	}

	/**
	 * @see javax.swing.ListModel#getSize()
	 */
	@Override
	public int getSize() {
		return resultData.getInstanceCount(dimension); 
	}
	
	protected int translateRowIndex(int index) {
		if(rowTransform!=null && rowTransform.length==getSize()) {
			index = rowTransform[index];
		}
		return index;
	}
	
	public String getRowName(int row) {
		return resultData.getInstanceLabel(dimension, 
				translateRowIndex(row)).toString();
	}

	/**
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	@Override
	public Object getElementAt(int index) {
		return resultData.getGroupMatchCount(
				dimension, translateRowIndex(index));
	}

	protected class RowHeaderModel extends AbstractListModel<String> implements Updatable {

		private static final long serialVersionUID = -7000236105602807901L;

		@Override
		public String getElementAt(int index) {
			String header = getRowName(index);
			if(displayMode==NumberDisplayMode.PERCENTAGE) {
				
				double p = (double)resultData.getGroupMatchCount(dimension, translateRowIndex(index)) 
						/ (double)resultData.getTotalMatchCount() * 100d;
				header = String.format("%s (%1.2f%%)", header, p); //$NON-NLS-1$
			}
			
			return header;
		}

		@Override
		public int getSize() {
			return FixedDimensionResultListModel.this.getSize();
		}

		public void fireModelChanged() {
			fireContentsChanged(this, 0, Math.max(0, getSize() - 1));
		}

		@Override
		public boolean update() {
			fireModelChanged();
			return true;
		}
	}
}
