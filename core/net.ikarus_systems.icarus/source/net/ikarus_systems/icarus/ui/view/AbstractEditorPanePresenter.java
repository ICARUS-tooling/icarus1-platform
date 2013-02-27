/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.ui.view;

import java.awt.Component;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractEditorPanePresenter<T extends Object> implements AWTPresenter {
	
	protected JEditorPane contentPane;
	protected JScrollPane scrollPane;
	protected T presentedData;
	protected Options options;

	protected AbstractEditorPanePresenter() {
		// no-op
	}
	
	protected abstract String getDefaultText();
	
	protected static final String NONE = "-"; //$NON-NLS-1$
	protected static final String BR = "<br>"; //$NON-NLS-1$
	
	protected static String noneOrNonempty(Object obj) {
		if(obj==null) {
			return NONE;
		}
		String s = obj.toString();
		return (s==null || s.isEmpty()) ? NONE : s;
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#clear()
	 */
	@Override
	public void clear() {
		if(contentPane!=null) {
			contentPane.setText(getDefaultText());
			presentedData = null;
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#isPresenting()
	 */
	@Override
	public boolean isPresenting() {
		return presentedData!=null;
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#getPresentedData()
	 */
	@Override
	public Object getPresentedData() {
		return presentedData;
	}
	
	protected abstract void refresh();

	/**
	 * @see net.ikarus_systems.icarus.ui.view.AWTPresenter#getPresentingComponent()
	 */
	@Override
	public Component getPresentingComponent() {
		if(contentPane==null) {
			contentPane = new JEditorPane("text/html", getDefaultText()); //$NON-NLS-1$
			contentPane.setEditable(false);
			contentPane.setBorder(UIUtil.defaultContentBorder);
			
			scrollPane = new JScrollPane(contentPane);
			scrollPane.setBorder(null);
			UIUtil.defaultSetUnitIncrement(scrollPane);
			
			if(presentedData!=null) {
				refresh();
			}
		}
		
		return scrollPane;
	}

	/**
	 * Per default we do not store resources that need to
	 * be released (e.g. localized components). Localization
	 * of content is done once at the time the data is being
	 * presented and so we do not store links to localizers
	 * or the like. Subclasses might require different
	 * handling and are advised to override this method if need be!
	 * 
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#close()
	 */
	@Override
	public void close() {
		// no-op
	}
}
