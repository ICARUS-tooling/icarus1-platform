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
package de.ims.icarus.plugins.search_tools.view.results;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import de.ims.icarus.search_tools.result.SearchResult;
import de.ims.icarus.ui.NumberDisplayMode;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.Updatable;
import de.ims.icarus.ui.table.TableSortMode;
import de.ims.icarus.util.StringUtil;
import de.ims.icarus.util.collections.CollectionUtils;


/**
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SearchResultTableModel extends AbstractTableModel
		implements PropertyChangeListener, Updatable {

	private static final long serialVersionUID = -2154403981551519210L;

	protected SearchResult resultData;

	protected NumberDisplayMode displayMode = NumberDisplayMode.RAW;

	protected final RowHeaderModel rowHeaderModel;

	protected final ColumnModel columnModel;

	protected Integer[] rowTransform;
	protected Integer[] columnTransform;

	public static final int DEFAULT_ROW_DIMENSION = 0;
	public static final int DEFAULT_COLUMN_DIMENSION = 1;

	protected boolean ommitDimensionCheck = false;

	protected int rowDimension = 0;
	protected int columnDimension = 1;
	public SearchResultTableModel(SearchResult resultData) {
		this(resultData, false);
	}

	public SearchResultTableModel(SearchResult resultData, boolean ommitDimensionCheck) {
		setOmmitDimensionCheck(ommitDimensionCheck);

		rowHeaderModel = new RowHeaderModel();
		columnModel = new ColumnModel();

		setResultData(resultData);
	}

	public boolean isOmmitDimensionCheck() {
		return ommitDimensionCheck;
	}

	public void setOmmitDimensionCheck(boolean ommitDimensionCheck) {
		this.ommitDimensionCheck = ommitDimensionCheck;
	}

	public int getSupportedDimensions() {
		return 2;
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

	public void flip() {
		rowTransform = columnTransform = null;

		int tmp = rowDimension;
		rowDimension = columnDimension;
		columnDimension = tmp;

		update();
	}

	public boolean isFlipped() {
		return rowDimension!=DEFAULT_ROW_DIMENSION;
	}

	public boolean isSorted() {
		return rowTransform!=null || columnTransform!=null;
	}

	public void reset() {
		if(isFlipped()) {
			rowDimension = DEFAULT_ROW_DIMENSION;
			columnDimension = DEFAULT_COLUMN_DIMENSION;

			update();
		}
	}

	public ListModel<String> getRowHeaderModel() {
		return rowHeaderModel;
	}

	public TableColumnModel getColumnModel() {
		return columnModel;
	}

	public SearchResult getResultData() {
		return resultData;
	}

	public void setResultData(SearchResult resultData) {
		if(resultData==null)
			throw new NullPointerException("Invalid result data"); //$NON-NLS-1$
		if(!isOmmitDimensionCheck() && resultData.getDimension()!=getSupportedDimensions())
			throw new IllegalArgumentException("Unsupported result dimension: "+resultData.getDimension()); //$NON-NLS-1$

		this.resultData = resultData;

		columnModel.rebuild();

		fireTableDataChanged();
		rowHeaderModel.fireModelChanged();
	}

	@Override
	public int getColumnCount() {
		return resultData.getInstanceCount(columnDimension);
	}

	@Override
	public String getColumnName(int column) {
		return resultData.getInstanceLabel(columnDimension,
				translateColumnIndex(column)).toString();
	}

	public String getRowName(int row) {
		return resultData.getInstanceLabel(rowDimension,
				translateRowIndex(row)).toString();
	}

	@Override
	public int getRowCount() {
		return resultData.getInstanceCount(rowDimension);
	}

	@Override
	public Class<?> getColumnClass(int column) {
		return Integer.class;
	}

	public int translateRowIndex(int rowIndex) {
		if(rowTransform!=null && rowTransform.length==getRowCount())
			rowIndex = rowTransform[rowIndex];
		return rowIndex;
	}

	public int translateColumnIndex(int columnIndex) {
		if(columnTransform!=null && columnTransform.length==getColumnCount())
			columnIndex = columnTransform[columnIndex];
		return columnIndex;
	}

	public int translateRowIndex(int row, int column) {
		return translateRowIndex(isFlipped() ? column : row);
	}

	public int translateColumnIndex(int row, int column) {
		return translateColumnIndex(isFlipped() ? row : column);
	}

	@Override
	public Integer getValueAt(int row, int column) {
		return resultData.getMatchCount(
				translateRowIndex(row, column),
				translateColumnIndex(row, column));
	}

	public SearchResult getSubResultAt(int row, int column) {
		return resultData.getSubResult(
				translateRowIndex(row, column),
				translateColumnIndex(row, column));
	}

	@Override
	public boolean update() {
		if (!columnModel.update()) {
			fireTableDataChanged();
		}

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

	public void clear(boolean clearRows, boolean clearColumns) {
		if(clearRows)
			rowTransform = null;

		if(clearColumns)
			columnTransform = null;

		if(clearRows || clearColumns) {
			update();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		displayMode = (NumberDisplayMode) evt.getNewValue();
		columnModel.update();
		rowHeaderModel.update();
	}

	public synchronized void sort(TableSortMode mode) throws InterruptedException {
		if(mode.sortsNumbers()) {
			sortContent(!mode.sortsColumns(), mode.sortsAscending(),
					mode.sortsColumns(), mode.sortsAscending());
		} else {
			sortHeader(!mode.sortsColumns(), mode.sortsAscending(),
					mode.sortsColumns(), mode.sortsAscending());
		}
	}

	public synchronized void sortContent(boolean sortRows, final boolean rowsAscending,
			boolean sortColumns, final boolean columnsAscending) throws InterruptedException {
		if(!sortRows && !sortColumns)
			return;

		/*System.out.printf("sorting content: sortRows=%b rowsAsc=%b sortCols=%b colsAsc=%b\n",
				sortRows, rowsAscending, sortColumns, columnsAscending);*/

		int rowCount = getRowCount();
		int columnCount = getColumnCount();

		if(sortRows) {
			if(rowTransform==null || rowTransform.length!=rowCount) {
				rowTransform = new Integer[rowCount];
			}
			CollectionUtils.fillAscending(rowTransform);
			try {
				Arrays.sort(rowTransform, new Comparator<Integer>(){
					int result = rowsAscending ? -1 : 1;
					int dimension = rowDimension;

					@Override
					public int compare(Integer o1, Integer o2) {
						if(Thread.currentThread().isInterrupted())
							throw new IllegalStateException();

						return result * ( resultData.getGroupMatchCount(dimension, o1)
								- resultData.getGroupMatchCount(dimension, o2));
					}});
			} catch(IllegalStateException e) {
				throw new InterruptedException();
			}
		}

		if(sortColumns) {
			if(columnTransform==null || columnTransform.length!=columnCount) {
				columnTransform = new Integer[columnCount];
			}
			CollectionUtils.fillAscending(columnTransform);
			try {
				Arrays.sort(columnTransform, new Comparator<Integer>(){
					int result = columnsAscending ? 1 : -1;
					int dimension = columnDimension;

					@Override
					public int compare(Integer o1, Integer o2) {
						if(Thread.currentThread().isInterrupted())
							throw new IllegalStateException();

						return result * ( resultData.getGroupMatchCount(dimension, o1)
								- resultData.getGroupMatchCount(dimension, o2));
					}});
			} catch(IllegalStateException e) {
				throw new InterruptedException();
			}
		}

		updateEDT();
	}

	public synchronized void sortHeader(boolean sortRows, final boolean rowsAscending,
			boolean sortColumns, final boolean columnsAscending) throws InterruptedException {
		if(!sortRows && !sortColumns)
			return;

		int rowCount = getRowCount();
		int columnCount = getColumnCount();

		if(sortRows) {
			if(rowTransform==null || rowTransform.length!=rowCount) {
				rowTransform = new Integer[rowCount];
			}

			Integer[] rowTransform = this.rowTransform;
			this.rowTransform = null;
			CollectionUtils.fillAscending(rowTransform);

			try {
				Arrays.sort(rowTransform, new Comparator<Integer>(){
					@Override
					public int compare(Integer o1, Integer o2) {
						if(Thread.currentThread().isInterrupted())
							throw new IllegalStateException();

						int result = StringUtil.compareNumberAwareIgnoreCase(
								getRowName(o1), getRowName(o2));

						return rowsAscending ? -result : result;
					}});
			} catch(IllegalStateException e) {
				throw new InterruptedException();
			}

			this.rowTransform = rowTransform;
		}

		if(sortColumns) {
			if(columnTransform==null || columnTransform.length!=columnCount) {
				columnTransform = new Integer[columnCount];
			}

			Integer[] columnTransform = this.columnTransform;
			this.columnTransform = null;
			CollectionUtils.fillAscending(columnTransform);

			try {
				Arrays.sort(columnTransform, new Comparator<Integer>(){
					@Override
					public int compare(Integer o1, Integer o2) {
						if(Thread.currentThread().isInterrupted())
							throw new IllegalStateException();

						int result = StringUtil.compareNumberAwareIgnoreCase(
								getColumnName(o1), getColumnName(o2));

						return rowsAscending ? result : -result;
					}});
			} catch(IllegalStateException e) {
				throw new InterruptedException();
			}

			this.columnTransform = columnTransform;
		}

		updateEDT();
	}

	public synchronized void invert() {
		if(rowTransform!=null)
			CollectionUtils.reverse(rowTransform, 0, -1);

		if(columnTransform!=null)
			CollectionUtils.reverse(columnTransform, 0, -1);

		if(rowTransform!=null || columnTransform!=null)
			fireTableDataChanged();
	}

	/**
	 *
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	class RowHeaderModel extends AbstractListModel<String> implements Updatable {

		private static final long serialVersionUID = -2856284447051029646L;

		@Override
		public String getElementAt(int index) {
			String header = getRowName(index);
			if(displayMode==NumberDisplayMode.PERCENTAGE && resultData.getDimension()>rowDimension) {

				double p = (double)resultData.getGroupMatchCount(rowDimension, translateRowIndex(index))
						/ (double)resultData.getTotalMatchCount() * 100d;
				header = String.format("%s (%1.2f%%)", header, p); //$NON-NLS-1$
			}

			return header;
		}

		@Override
		public int getSize() {
			return getRowCount();
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

	/**
	 *
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	class ColumnModel extends DefaultTableColumnModel implements Updatable {

		private static final long serialVersionUID = -4605417985019659721L;

		ColumnModel() {
			setColumnSelectionAllowed(false);
		}

		public boolean rebuild() {
			int columnCount = SearchResultTableModel.this.getColumnCount();

			TableColumn column;

			// System.out.println(columnCount);

			boolean changed = false, added = false;

			for (int i = 0; i < columnCount; i++) {
				String header = getColumnName(i);
				if(displayMode==NumberDisplayMode.PERCENTAGE && resultData.getDimension()>columnDimension) {

					double p = (double)resultData.getGroupMatchCount(
							columnDimension, SearchResultTableModel.this.translateColumnIndex(i))
							/ (double)resultData.getTotalMatchCount() * 100d;
					header = String.format("%s (%1.2f%%)", header, p); //$NON-NLS-1$
				}

				if (i < getColumnCount()) {
					column = getColumn(i);
					if(column.getHeaderValue()==null || !column.getHeaderValue().equals(header)) {
						column.setHeaderValue(header);
						changed = true;
					}
				} else {
					column = new TableColumn(i, 75);
					// TODO check if this could cause problems (resizable=true)
					column.setResizable(true);
					column.setHeaderValue(header);
					addColumn(column);
					added = true;
				}
			}

			int currentColumnCount = getColumnCount();
			if(currentColumnCount>columnCount) {
				while(getColumnCount()>columnCount)
					tableColumns.remove(tableColumns.size()-1);

				fireColumnRemoved(new TableColumnModelEvent(
						this, columnCount, currentColumnCount));
			}

			if(changed)
				fireColumnAdded(new TableColumnModelEvent(
						this, 0, getColumnCount()-1));

			return added;
		}

		@Override
		public boolean update() {
			return rebuild();
		}
	}
}
