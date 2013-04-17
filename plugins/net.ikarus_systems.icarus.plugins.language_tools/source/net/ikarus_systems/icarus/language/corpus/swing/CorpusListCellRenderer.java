/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.corpus.swing;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import net.ikarus_systems.icarus.language.corpus.Corpus;
import net.ikarus_systems.icarus.language.corpus.CorpusDescriptor;
import net.ikarus_systems.icarus.language.corpus.CorpusInfo;
import net.ikarus_systems.icarus.ui.UIUtil;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CorpusListCellRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = -2941175073501767602L;

	/**
	 * 
	 */
	public CorpusListCellRenderer() {
		UIUtil.disableHtml(this);
	}

	/**
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList<?> list,
			Object value, int index, boolean isSelected, boolean cellHasFocus) {
		
		setToolTipText(null);
		
		if(value instanceof Corpus) {
			value = ((Corpus)value).getName();
		} else if(value instanceof CorpusDescriptor) {
			value = ((CorpusDescriptor)value).getName();
		} else if(value instanceof CorpusInfo) {
			value = ((CorpusInfo)value).getCorpusName();
		}
		
		return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	}

}
