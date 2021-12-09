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
package de.ims.icarus.plugins.coref.view.text;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.java.plugin.registry.Extension;

import de.ims.icarus.language.coref.CoreferenceAllocation;
import de.ims.icarus.language.coref.CoreferenceUtils;
import de.ims.icarus.language.coref.DocumentData;
import de.ims.icarus.language.coref.Span;
import de.ims.icarus.language.coref.SpanSet;
import de.ims.icarus.language.coref.annotation.AnnotatedCoreferenceDocumentData;
import de.ims.icarus.language.coref.annotation.CoreferenceDocumentAnnotationManager;
import de.ims.icarus.language.coref.text.CoreferenceDocument;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.plugins.coref.view.CoreferenceDocumentDataPresenter;
import de.ims.icarus.util.Filter;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.annotation.AnnotatedData;
import de.ims.icarus.util.data.ContentType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferenceDocumentPresenter extends AbstractCoreferenceTextPresenter {

	protected DocumentData data;

	protected CoreferenceDocumentDataPresenter.PresenterMenu presenterMenu;

	public CoreferenceDocumentPresenter() {

		presenterMenu = createPresenterMenu();
	}

	protected CoreferenceDocumentDataPresenter.PresenterMenu createPresenterMenu() {
		return new CoreferenceDocumentDataPresenter.PresenterMenu(this, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String uid = e.getActionCommand();
				Extension extension = PluginUtil.getExtension(uid);
				if(extension!=null) {
					togglePresenter(extension);
				}
			}
		});
	}


	/**
	 * @see de.ims.icarus.plugins.coref.view.text.AbstractCoreferenceTextPresenter#createPopupOptions()
	 */
	@Override
	protected Options createPopupOptions() {
		Options options = super.createPopupOptions();

		if(presenterMenu!=null) {
			options.put("showInMenu", presenterMenu); //$NON-NLS-1$
		}

		return options;
	}

	protected void togglePresenter(Extension extension) {
		if(extension==null)
			throw new NullPointerException("Invalid extension"); //$NON-NLS-1$

		if(parent==null) {
			return;
		}
		if(pendingFilter==null) {
			return;
		}

		try {
			Options options = new Options();
			options.put("filter", pendingFilter); //$NON-NLS-1$

			if(!options.isEmpty()) {
				parent.togglePresenter(extension, options);
			}
		} catch(Exception e) {
			LoggerFactory.log(this, Level.SEVERE,
					"Failed to switch to presennter: "+extension.getUniqueId(), e); //$NON-NLS-1$
		}
	}

	@Override
	public ContentType getContentType() {
		return CoreferenceUtils.getCoreferenceDocumentContentType();
	}

	@Override
	protected void setData(Object data) {
		this.data = (DocumentData) data;

		if(data instanceof AnnotatedCoreferenceDocumentData) {
			getAnnotationManager().setAnnotation(((AnnotatedData)data).getAnnotation());
		}
	}

	@Override
	protected boolean buildDocument(CoreferenceDocument doc) throws Exception {
		if(data==null) {
			return false;
		}

		/*if(createInitialFilter && data instanceof AnnotatedCoreferenceDocumentData && allocation!=null) {
			doc.setFilter(new AnnotatedSpanFilter((AnnotatedCoreferenceDocumentData) data));
			createInitialFilter = false;
		}*/

		Filter filter = (Filter) options.get("filter"); //$NON-NLS-1$
		doc.setFilter(filter);

		doc.setAnnotationManager(getAnnotationManager());
		doc.appendBatchDocumentData(data, getAllocation(), getGoldAllocation());

		doc.applyBatchUpdates(0);

		return true;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#isPresenting()
	 */
	@Override
	public boolean isPresenting() {
		return data!=null;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#getPresentedData()
	 */
	@Override
	public DocumentData getPresentedData() {
		return data;
	}

	@Override
	protected CoreferenceDocument getDocument() {
		// Make sure our components are created
		if(textPane==null) {
			getPresentingComponent();
		}

		return (CoreferenceDocument) textPane.getDocument();
	}

	protected class AnnotatedSpanFilter implements Filter {

		protected Set<Span> lut;
		protected final AnnotatedCoreferenceDocumentData data;

		public AnnotatedSpanFilter(AnnotatedCoreferenceDocumentData data) {
			if(data==null)
				throw new NullPointerException("Invalid annotated data"); //$NON-NLS-1$

			this.data = data;

			buildLookup();
		}

		protected void buildLookup() {
			CoreferenceAllocation allocation = getAllocation();

			if(allocation==null) {
				return;
			}

			CoreferenceDocumentAnnotationManager annotationManager = getAnnotationManager();

			if(!annotationManager.hasAnnotation()) {
				return;
			}

			SpanSet spanSet = allocation.getSpanSet(data.getId());
			if(spanSet==null) {
				return;
			}

			lut = new HashSet<>();
			for(int i=0; i<spanSet.size(); i++) {
				if(annotationManager.isHighlighted(i)) {
					lut.add(spanSet.get(i));
				}
			}
		}

		/**
		 * @see de.ims.icarus.util.Filter#accepts(java.lang.Object)
		 */
		@Override
		public boolean accepts(Object obj) {
			return lut==null || lut.contains(obj);
		}

	}
}
