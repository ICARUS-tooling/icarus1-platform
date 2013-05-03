/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.helper;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;

import net.ikarus_systems.icarus.language.MutableSentenceData;
import net.ikarus_systems.icarus.language.SentenceData;
import net.ikarus_systems.icarus.language.SentenceDataListener;
import net.ikarus_systems.icarus.ui.view.AWTPresenter;
import net.ikarus_systems.icarus.ui.view.UnsupportedPresentationDataException;
import net.ikarus_systems.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractSentenceTablePresenter<T extends SentenceData> 
		implements AWTPresenter, SentenceDataListener {
	
	protected JTable table;
	
	protected JPanel contentPanel;
	
	protected T data;

	protected AbstractSentenceTablePresenter() {
		// no-op
	}
	
	protected abstract JTable createTable();
	
	protected JToolBar createToolBar() {
		return null;
	}
	
	protected void buildPanel() {
		contentPanel = new JPanel(new BorderLayout());
		
		table = createTable();
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBorder(null);
		contentPanel.add(scrollPane, BorderLayout.CENTER);
		
		JToolBar toolBar = createToolBar();
		if(toolBar!=null) {
			contentPanel.add(toolBar, BorderLayout.NORTH);
		}
	}
	
	protected void setData(T data) {
		if(this.data!=null && this.data instanceof MutableSentenceData) {
			((MutableSentenceData)this.data).removeSentenceDataListener(this);
		}
		
		this.data = data;
		
		if(this.data!=null && this.data instanceof MutableSentenceData) {
			((MutableSentenceData)this.data).addSentenceDataListener(this);
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#present(java.lang.Object, net.ikarus_systems.icarus.util.Options)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void present(Object data, Options options)
			throws UnsupportedPresentationDataException {
		if(data==null)
			throw new IllegalArgumentException("Invalid data"); //$NON-NLS-1$
		
		if(data.equals(this.data)) {
			return;
		}
		
		setData((T) data);
		
		table.repaint();
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#clear()
	 */
	@Override
	public void clear() {
		if(data==null) {
			return;
		}
		
		if(data instanceof MutableSentenceData) {
			((MutableSentenceData)data).removeSentenceDataListener(this);
		}
		
		data = null;
		table.repaint();
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#close()
	 */
	@Override
	public void close() {
		
		if(data instanceof MutableSentenceData) {
			((MutableSentenceData)data).removeSentenceDataListener(this);
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.AWTPresenter#getPresentingComponent()
	 */
	@Override
	public Component getPresentingComponent() {
		if(contentPanel==null) {
			buildPanel();
		}
		return contentPanel;
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#getPresentedData()
	 */
	@Override
	public Object getPresentedData() {
		return data;
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#isPresenting()
	 */
	@Override
	public boolean isPresenting() {
		return data!=null;
	}

}
