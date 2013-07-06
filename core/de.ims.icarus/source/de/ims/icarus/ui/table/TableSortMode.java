/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.ui.table;

import javax.swing.Icon;

import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.util.id.Identity;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum TableSortMode implements Identity {

	COLUMNS_ASCENDING_ALPHABETICALLY("sort_cols_asc_alph", true, true, false), //$NON-NLS-1$
	COLUMNS_DESCENDING_ALPHABETICALLY("sort_cols_desc_alph", true, false, false), //$NON-NLS-1$
	COLUMNS_ASCENDING_NUMERICALLY("sort_cols_asc_num", true, true, true), //$NON-NLS-1$
	COLUMNS_DESCENDING_NUMERICALLY("sort_cols_desc_num", true, false, true), //$NON-NLS-1$
	ROWS_ASCENDING_ALPHABETICALLY("sort_rows_asc_alph", false, true, false), //$NON-NLS-1$
	ROWS_DESCENDING_ALPHABETICALLY("sort_rows_desc_alph", false, false, false), //$NON-NLS-1$
	ROWS_ASCENDING_NUMERICALLY("sort_rows_asc_num", false, true, true), //$NON-NLS-1$
	ROWS_DESCENDING_NUMERICALLY("sort_rows_desc_num", false, false, true); //$NON-NLS-1$
	
	private final String key;
	private final boolean col;
	private final boolean asc;
	private final boolean num;
	
	private TableSortMode(String key, boolean col, boolean asc, boolean num) {
		this.key = key;
		this.col = col;
		this.asc = asc;
		this.num = num;
	}
	
	public static TableSortMode parseMode(String s) {
		if(s==null)
			throw new IllegalArgumentException("Invalid string"); //$NON-NLS-1$
		
		for(TableSortMode mode : values()) {
			if(mode.key.equals(s)) {
				return mode;
			}
		}
		
		throw new IllegalArgumentException("Unknown mode string: "+s); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getId()
	 */
	@Override
	public String getId() {
		return toString();
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getName()
	 */
	@Override
	public String getName() {
		return ResourceManager.getInstance().get(
				"tableSortMode."+key+".name"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getDescription()
	 */
	@Override
	public String getDescription() {
		return ResourceManager.getInstance().get(
				"tableSortMode."+key+".description"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getIcon()
	 */
	@Override
	public Icon getIcon() {
		return IconRegistry.getGlobalRegistry().getIcon(key);
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getOwner()
	 */
	@Override
	public Object getOwner() {
		return this;
	}

	public String getKey() {
		return key;
	}

	public boolean sortsColumns() {
		return col;
	}

	public boolean sortsNumbers() {
		return num;
	}

	public boolean sortsAscending() {
		return asc;
	}
}
