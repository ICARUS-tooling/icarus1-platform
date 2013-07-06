/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.ui.text;

import java.awt.Component;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.Document;

import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.view.AWTPresenter;
import net.ikarus_systems.icarus.ui.view.UnsupportedPresentationDataException;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.ContentTypeRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DocumentPresenter implements AWTPresenter {
	
	private JTextArea textArea;
	private Document presentedData;
	private JScrollPane scrollPane;

	public DocumentPresenter() {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#supports(net.ikarus_systems.icarus.util.data.ContentType)
	 */
	@Override
	public boolean supports(ContentType type) {
		ContentType required = ContentTypeRegistry.getInstance().getTypeForClass(Document.class);
		return ContentTypeRegistry.isCompatible(required, type);
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#present(java.lang.Object, net.ikarus_systems.icarus.util.Options)
	 */
	@Override
	public void present(Object data, Options options)
			throws UnsupportedPresentationDataException {
		if(!(data instanceof Document))
			throw new UnsupportedPresentationDataException("Data not supported: "+data.getClass()); //$NON-NLS-1$
		
		presentedData = (Document) data;
		
		if(textArea!=null) {
			refresh();
		}
	}
	
	protected void refresh() {
		if(textArea==null) {
			return;
		}
		
		textArea.setDocument(presentedData);
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#clear()
	 */
	@Override
	public void clear() {
		// no-op;
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#close()
	 */
	@Override
	public void close() {
		// no-op
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

	/**
	 * @see net.ikarus_systems.icarus.ui.view.AWTPresenter#getPresentingComponent()
	 */
	@Override
	public Component getPresentingComponent() {
		if(textArea==null) {
			textArea = new JTextArea();
			textArea.setEditable(true);
			UIUtil.disableHtml(textArea);
			UIUtil.disableCaretScroll(textArea);
			UIUtil.addPopupMenu(textArea, UIUtil.createDefaultTextMenu(textArea, false));
			
			scrollPane = new JScrollPane(textArea);
			scrollPane.setBorder(UIUtil.emptyBorder);
			UIUtil.defaultSetUnitIncrement(scrollPane);
			
			if(presentedData!=null) {
				refresh();
			}
		}
		
		return scrollPane;
	}

}
