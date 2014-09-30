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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.prosody.ui.table;

import de.ims.icarus.plugins.prosody.ProsodicSentenceData;
import de.ims.icarus.plugins.prosody.ProsodyConstants;
import de.ims.icarus.plugins.prosody.ui.view.SentenceInfo;
import de.ims.icarus.plugins.prosody.ui.view.SyllableInfo;
import de.ims.icarus.plugins.prosody.ui.view.WordInfo;
import de.ims.icarus.ui.treetable.AbstractTreeTableModel;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ProsodyTreeTableModel extends AbstractTreeTableModel implements ProsodyConstants {

	private SentenceInfo sentenceInfo;
	private Column[] columns = {
		Column.rootColumn,
		Column.labelColumn,
		new Column.PropertyColumn(POS_KEY, SYLLABLE_LABEL_KEY),
		Column.durationColumn,
	};

	public boolean isEmpty() {
		return sentenceInfo==null;
	}

	public SentenceInfo getSentenceInfo() {
		return sentenceInfo;
	}

	public ProsodicSentenceData getSentence() {
		return sentenceInfo==null ? null : sentenceInfo.getSentence();
	}

	public void clear() {
		sentenceInfo = null;

		fireNewRoot();
	}

	public void rebuild(ProsodicSentenceData sentence) {
		if (sentence == null)
			throw new NullPointerException("Invalid sentence"); //$NON-NLS-1$

		sentenceInfo = new SentenceInfo(sentence);

		fireNewRoot();
	}

	/**
	 * @see de.ims.icarus.ui.treetable.TreeTableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return columns.length;
	}

	public Column getColumn(int column) {
		return columns[column];
	}

	/**
	 * @see de.ims.icarus.ui.treetable.TreeTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {
		return columns[column].getName();
	}

	/**
	 * @see de.ims.icarus.ui.treetable.TreeTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int column) {
		return columns[column].getColumnClass();
	}

	/**
	 * @see de.ims.icarus.ui.treetable.TreeTableModel#getValueAt(java.lang.Object, int)
	 */
	@Override
	public Object getValueAt(Object node, int column) {
		Object result = null;

		if(node instanceof WordInfo) {
			result = columns[column].getValue((WordInfo)node);
		} else if(node instanceof SyllableInfo) {
			result = columns[column].getValue((SyllableInfo)node);
		}

		return result;
	}

	/**
	 * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
	 */
	@Override
	public Object getChild(Object parent, int index) {
		Object child = null;

		if(sentenceInfo!=null) {
			if(isRoot(parent)) {
				child = sentenceInfo.wordInfo(index);
			} else if(parent instanceof WordInfo) {
				child = ((WordInfo)parent).syllableInfo(index);
			}
		}

		return child;
	}

	/**
	 * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
	 */
	@Override
	public int getChildCount(Object parent) {
		int count = 0;

		if(sentenceInfo!=null) {
			if(isRoot(parent)) {
				count = sentenceInfo.wordCount();
			} else if(parent instanceof WordInfo) {
				count = ((WordInfo)parent).sylCount();
			}
		}

		return count;
	}

}
