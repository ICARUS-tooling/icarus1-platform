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
package de.ims.icarus.ui.text;

import java.awt.Component;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.Document;

import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.view.AWTPresenter;
import de.ims.icarus.ui.view.UnsupportedPresentationDataException;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;


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
	 * @see de.ims.icarus.ui.view.Presenter#supports(de.ims.icarus.util.data.ContentType)
	 */
	@Override
	public boolean supports(ContentType type) {
		ContentType required = ContentTypeRegistry.getInstance().getTypeForClass(Document.class);
		return ContentTypeRegistry.isCompatible(required, type);
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#present(java.lang.Object, de.ims.icarus.util.Options)
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
	 * @see de.ims.icarus.ui.view.Presenter#clear()
	 */
	@Override
	public void clear() {
		// no-op;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#close()
	 */
	@Override
	public void close() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#isPresenting()
	 */
	@Override
	public boolean isPresenting() {
		return presentedData!=null;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#getPresentedData()
	 */
	@Override
	public Object getPresentedData() {
		return presentedData;
	}

	/**
	 * @see de.ims.icarus.ui.view.AWTPresenter#getPresentingComponent()
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
