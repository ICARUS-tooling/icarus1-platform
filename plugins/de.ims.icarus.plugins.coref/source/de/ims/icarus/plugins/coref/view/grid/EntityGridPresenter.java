/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.coref.view.grid;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToolBar;

import de.ims.icarus.language.coref.CoreferenceAllocation;
import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.CoreferenceUtils;
import de.ims.icarus.plugins.coref.view.CoreferenceCellRenderer;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.table.TablePresenter;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.data.ContentType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class EntityGridPresenter extends TablePresenter {
	
	protected CoreferenceDocumentData document;
	protected CoreferenceAllocation allocation;
	protected CoreferenceAllocation goldAllocation;
	
	protected boolean showGoldNodes = false;
	protected boolean markFalseNodes = true;
	protected boolean filterSingletons = true;
	
	protected EntityGridTableModel gridModel = new EntityGridTableModel();
	protected CoreferenceCellRenderer outline;
	protected EntityGridCellRenderer cellRenderer;

	public EntityGridPresenter() {
		// no-op
	}

	@Override
	protected void init() {
		cellRenderer = new EntityGridCellRenderer();
	}

	@Override
	protected JToolBar createToolBar() {
		// TODO
		return null;
	}

	@Override
	protected void buildPanel() {
		super.buildPanel();
		
		outline = new CoreferenceCellRenderer();
		outline.setBorder(UIUtil.defaultContentBorder);
		
		JPanel footer = new JPanel(new BorderLayout());
		footer.add(outline, BorderLayout.CENTER);
		footer.setBorder(UIUtil.topLineBorder);
		contentPanel.add(BorderLayout.SOUTH, footer);
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#clear()
	 */
	@Override
	public void clear() {
		setData(null, null);
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#isPresenting()
	 */
	@Override
	public boolean isPresenting() {
		return document!=null;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#getPresentedData()
	 */
	@Override
	public CoreferenceDocumentData getPresentedData() {
		return document;
	}

	/**
	 * @see de.ims.icarus.ui.table.TablePresenter#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return CoreferenceUtils.getCoreferenceDocumentContentType();
	}

	/**
	 * @see de.ims.icarus.ui.table.TablePresenter#createTable()
	 */
	@Override
	protected JTable createTable() {
		JTable table = new JTable(gridModel, gridModel.getColumnModel());
		
		table.setDefaultRenderer(EntityGridNode.class, cellRenderer);
		
		return table;
	}

	/**
	 * @see de.ims.icarus.ui.table.TablePresenter#setData(java.lang.Object, de.ims.icarus.util.Options)
	 */
	@Override
	protected void setData(Object data, Options options) {
		document = (CoreferenceDocumentData) data;
		
		if(options==null) {
			options = Options.emptyOptions;
		}
		allocation = (CoreferenceAllocation) options.get("allocation"); //$NON-NLS-1$
		goldAllocation = (CoreferenceAllocation) options.get("goldAllocation"); //$NON-NLS-1$
		
		gridModel.setDocument(document);
		gridModel.reload(allocation, goldAllocation, filterSingletons, showGoldNodes);
	}

}
