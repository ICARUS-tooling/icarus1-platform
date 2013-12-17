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
package de.ims.icarus.language.helper;

import java.awt.Component;

import javax.swing.JList;

import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.ui.list.TooltipListCellRenderer;
import de.ims.icarus.util.Installable;
import de.ims.icarus.util.StringUtil;
import de.ims.icarus.util.annotation.AnnotationController;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SentenceDataListCellRenderer extends TooltipListCellRenderer
		implements Installable {

	private static final long serialVersionUID = 6024979433069005270L;

	protected AnnotationController annotationSource;

	public SentenceDataListCellRenderer() {
		// no-op
	}

	/**
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList<? extends Object> list,
			Object value, int index, boolean isSelected, boolean cellHasFocus) {

		if(value instanceof SentenceData) {
			SentenceData data = (SentenceData) value;
			StringBuilder sb = new StringBuilder(data.length()*20);

			// Show index
			if(LanguageUtils.isShowIndex()) {
				sb.append(StringUtil.formatDecimal(index+1)+": "); //$NON-NLS-1$
			}

			// Show corpus index if available
			if(LanguageUtils.isShowCorpusIndex()) {
				sb.append("(").append(StringUtil.formatDecimal(data.getIndex()+1)).append(") "); //$NON-NLS-1$ //$NON-NLS-2$
			}

			for(int i=0; i<data.length(); i++) {
				if(i>0) {
					sb.append(" "); //$NON-NLS-1$
				}
				sb.append(data.getForm(i));
			}

			value = sb.toString();
		} else {
			if(value==null) {
				value = "loading..."; //$NON-NLS-1$
			}

			if(LanguageUtils.isShowIndex()) {
				value = StringUtil.formatDecimal(index+1)+": "+value; //$NON-NLS-1$
			}

		}

		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		return this;
	}

	/**
	 * @see de.ims.icarus.util.Installable#install(java.lang.Object)
	 */
	@Override
	public void install(Object target) {
		if(target instanceof AnnotationController) {
			if(this.annotationSource!=null && this.annotationSource!=target)
				throw new IllegalStateException("Cannot be assigned to multiple annotation controllers"); //$NON-NLS-1$

			this.annotationSource = (AnnotationController)target;
		} else {
			this.annotationSource = null;
		}
	}

	/**
	 * @see de.ims.icarus.util.Installable#uninstall(java.lang.Object)
	 */
	@Override
	public void uninstall(Object target) {
		this.annotationSource = null;
	}

}
