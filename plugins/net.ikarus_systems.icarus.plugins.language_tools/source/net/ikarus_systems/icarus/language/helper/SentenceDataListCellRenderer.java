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

import java.awt.Component;

import javax.swing.JList;

import net.ikarus_systems.icarus.language.SentenceData;
import net.ikarus_systems.icarus.ui.helper.TooltipListCellRenderer;
import net.ikarus_systems.icarus.util.Installable;
import net.ikarus_systems.icarus.util.annotation.AnnotationController;

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
			String[] tokens = ((SentenceData)value).getForms();
			StringBuilder sb = new StringBuilder(tokens.length*20);
			
			sb.append(index+1).append(": "); //$NON-NLS-1$
			
			for(int i=0; i<tokens.length; i++) {
				if(i>0) {
					sb.append(" "); //$NON-NLS-1$
				}
				sb.append(tokens[i]);
			}
			
			value = sb.toString();
		} else {
			value = (index+1)+": "+value; //$NON-NLS-1$
		}
		
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		
		return this;
	}

	/**
	 * @see net.ikarus_systems.icarus.util.Installable#install(java.lang.Object)
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
	 * @see net.ikarus_systems.icarus.util.Installable#uninstall(java.lang.Object)
	 */
	@Override
	public void uninstall(Object target) {
		this.annotationSource = null;
	}

}
