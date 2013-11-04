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
package de.ims.icarus.language.dependency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ims.icarus.language.AbstractMutableSentenceData;
import de.ims.icarus.language.Grammar;
import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.language.MutableSentenceData;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.SentenceDataEvent;
import de.ims.icarus.language.UnsupportedSentenceDataException;
import de.ims.icarus.language.annotation.AnnotatedSentenceData;
import de.ims.icarus.util.Exceptions;
import de.ims.icarus.util.annotation.Annotation;


/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class MutableDependencyData extends AbstractMutableSentenceData
		implements DependencyConstants, AnnotatedSentenceData, DependencyData {

	private static final long serialVersionUID = 8905987553461570238L;

	protected transient final DependencyDataEvent event;

	protected Annotation annotation = null; // TODO change to default value?

	private List<DependencyDataEntry> items = new ArrayList<>();
	
	private Map<String, Object> properties;

	public MutableDependencyData() {
		event = new DependencyDataEvent(this);
	}

	public MutableDependencyData(String[] forms, String[] lemmas, 
			String[] features, String[] poss, int[] heads,
			String[] relations, long[] flags) {
		this();

		items.clear();

		for (int i = 0; i < forms.length; i++) {
			items.add(this.new DependencyDataEntry(
					forms[i], lemmas[i], features[i], 
					poss[i], heads[i], relations[i], flags[i]));
		}
	}

	protected void fireDataChanged() {
		event.set(SentenceDataEvent.CHANGE_EVENT, (int)-1, (int)-1);
		fireDataChanged(event);
	}

	protected void fireItemsInserted(int startIndex, int endIndex) {
		event.set(SentenceDataEvent.INSERT_EVENT, startIndex, endIndex);
		fireDataChanged(event);
	}

	protected void fireItemsRemoved(int startIndex, int endIndex) {
		event.set(SentenceDataEvent.REMOVE_EVENT, startIndex, endIndex);
		fireDataChanged(event);
	}

	protected void fireItemsUpdated(int startIndex, int endIndex) {
		event.set(SentenceDataEvent.UPDATE_EVENT, startIndex, endIndex);
		fireDataChanged(event);
	}

	public DependencyDataEntry getItem(int index) {
		return items.get(index);
	}

	public int getItemCount() {
		return items.size();
	}

	@Override
	public boolean isEmpty() {
		return items.isEmpty();
	}

	public DependencyDataEntry addDummyItem() {
		DependencyDataEntry item = this.new DependencyDataEntry();
		items.add(item);
		int index = (int) (items.size() - 1);
		item.index = index;
		item.form = "<empty>"; //$NON-NLS-1$
		fireItemsInserted(index, index);
		return item;
	}

	public void addItem(String form, String lemma, String features, String pos, int head, String relation, long flags) {
		items.add(this.new DependencyDataEntry(
				form, lemma, features, pos, head, relation, flags));
		int index = (int) (items.size() - 1);
		fireItemsInserted(index, index);
	}

	public boolean addItem(DependencyDataEntry item) {
		if (!items.contains(item)) {
			items.add(item);
			int index = (int) (items.size() - 1);
			fireItemsInserted(index, index);
			return true;
		}
		return false;
	}

	public void insertItem(int index, String form, String lemma, String features, 
			String pos, int head, String relation, long flags) {
		items.add(index, this.new DependencyDataEntry(
				form, lemma, features, pos, head, relation, flags));
		fireItemsInserted(index, index);
	}

	public boolean insertItem(int index, DependencyDataEntry item) {
		if (!items.contains(item)) {
			items.add(index, item);
			fireItemsInserted(index, index);
			return true;
		}
		return false;
	}

	public int indexOf(DependencyDataEntry item) {
		return (int) items.indexOf(item);
	}

	public void removeItemAt(int index) {
		// Find children
		int indexFrom = Short.MAX_VALUE;
		int indexTo = -1;
		
		for(int i=0; i<items.size(); i++) {
			if(i==index) {
				continue;
			}
			
			DependencyDataEntry item = items.get(i);
			if(item.getHead()==index) {
				item.setHead0(LanguageUtils.DATA_UNDEFINED_VALUE);
				indexFrom = (int) Math.min(indexFrom, i);
				indexTo = (int) Math.max(indexTo, i);
			}
		}
		
		if(indexFrom<Short.MAX_VALUE && indexTo>-1) {
			fireItemsUpdated(indexFrom, indexTo);
		}
		
		items.remove(index);
		fireItemsRemoved(index, index);
	}

	public boolean removeItem(DependencyDataEntry item) {
		int index = (int) items.indexOf(item);
		if (index >= 0) {
			removeItemAt(index);
			return true;
		}

		return false;
	}

	public void removeItems(int startIndex, int endIndex) {
		items.subList(startIndex, endIndex).clear();
		fireItemsRemoved(startIndex, endIndex);
	}

	@Override
	public void clear() {
		items.clear();
		fireDataChanged();
	}

	@Override
	public void copyFrom(SentenceData source) {
		if(source==null)
			throw new NullPointerException("Invalid source"); //$NON-NLS-1$
		if (!(source instanceof DependencyData))
			throw new UnsupportedSentenceDataException("Unsupported type: "+source.getClass()); //$NON-NLS-1$
		
		DependencyData data = (DependencyData) source;
		items.clear();
		for (int i = 0; i < data.length(); i++) {
			items.add(this.new DependencyDataEntry(data, i));
		}
		
		if(source instanceof AnnotatedSentenceData) {
			annotation = ((AnnotatedSentenceData)source).getAnnotation();
		}
		
		fireDataChanged();
	}

	@Override
	public MutableSentenceData clone() {
		MutableDependencyData data = new MutableDependencyData();
		data.copyFrom(this);

		return data;
	}

	@Override
	public Annotation getAnnotation() {
		return annotation;
	}
	
	public void setAnnotation(Annotation annotation) {
		// TODO
	}

	@Override
	public String getForm(int index) {
		return items.get(index).getForm();
	}
	
	public void setForm(int index, String form) {
		items.get(index).setForm(form);
	}

	@Override
	public String getLemma(int index) {
		return items.get(index).getLemma();
	}
	
	public void setLemma(int index, String lemma) {
		items.get(index).setForm(lemma);
	}

	@Override
	public String getFeatures(int index) {
		return items.get(index).getFeatures();
	}
	
	public void setFeatures(int index, String features) {
		items.get(index).setForm(features);
	}

	@Override
	public int getHead(int index) {
		return items.get(index).getHead();
	}
	
	public void setHead(int index, int head) {
		items.get(index).setHead(head);
	}

	@Override
	public String getPos(int index) {
		return items.get(index).getPos();
	}
	
	public void setPos(int index, String pos) {
		items.get(index).setForm(pos);
	}

	@Override
	public String getRelation(int index) {
		return items.get(index).getRelation();
	}
	
	public void setRelation(int index, String relation) {
		items.get(index).setForm(relation);
	}

	@Override
	public int length() {
		return items.size();
	}

	@Override
	public boolean isFlagSet(int index, long flag) {
		return (items.get(index).getFlags() & flag) == flag;
	}

	public void setFlag(int index, long flag) {
		long value = items.get(index).getFlags();
		value |= flag;
		items.get(index).setFlags(value);
	}

	public void unsetFlag(int index, long flag) {
		long value = items.get(index).getFlags();
		value &= ~flag;
		items.get(index).setFlags(value);
	}
	
	@Override
	public long getFlags(int index) {
		return items.get(index).getFlags();
	}

	/**
	 * Checks basic constraints for this parse.
	 * 
	 * @return true, if and only if exactly {@code n-1} items have a valid head 
	 * where {@code n} is the total number of items and exactly one 
	 * item is marked as root. Items with undefined heads count towards
	 * the 'root' value.
	 */
	public boolean validate() {
		boolean[] visited = new boolean[items.size()];
		int[] counts = { 0, 0 }; // {heads, roots}

		for (int index = 0; index < items.size(); index++) {
			val(index, visited, counts);
		}

		return counts[0] == items.size() - 1 && counts[1] == 1;
	}

	private void val(int index, boolean[] visited, int[] counts) {
		if (!visited[index]) {
			DependencyDataEntry item = items.get(index);
			visited[index] = true;
			item.index = index;
			if (item.head == LanguageUtils.DATA_HEAD_ROOT 
					|| item.head==LanguageUtils.DATA_UNDEFINED_VALUE) {
				// 1 = root
				counts[1]++;
			} else if (item.head >= 0 && item.head < items.size()) {
				// 0 = valid non root head
				counts[0]++;

				val(item.head, visited, counts);
			}
		}
	}

	public void switchItems(int indexFrom, int indexTo) {
		if (indexFrom == indexTo)
			return;

		if (indexFrom > indexTo) {
			int tmp = indexFrom;
			indexFrom = indexTo;
			indexTo = tmp;
		}

		DependencyDataEntry itemA = items.get(indexFrom);
		DependencyDataEntry itemB = items.get(indexTo);
		DependencyDataEntry tmp = new DependencyDataEntry(itemA);

		itemA.copyFrom(itemB);
		itemB.copyFrom(tmp);
		
		headSwitch(indexFrom, indexTo);
	}
	
	protected void headSwitch(int oldHead, int newHead) {

		int updateFrom = (int) Math.min(oldHead, newHead);
		int updateTo = (int) Math.max(oldHead, newHead);

		for (int i = 0; i < items.size(); i++) {
			DependencyDataEntry tmp = items.get(i);
			if (tmp.head == oldHead) {
				tmp.head = newHead;
				updateFrom = (int) Math.min(updateFrom, i);
				updateTo = (int) Math.max(updateTo, i);
			} else if (tmp.head == newHead) {
				tmp.head = oldHead;
				updateFrom = (int) Math.min(updateFrom, i);
				updateTo = (int) Math.max(updateTo, i);
			}
		}

		fireItemsUpdated(updateFrom, updateTo);
	}

	/**
	 * @see de.ims.icarus.language.SentenceData#getSourceGrammar()
	 */
	@Override
	public Grammar getSourceGrammar() {
		return DependencyUtils.getDependencyGrammar();
	}

	/**
	 * @see de.ims.icarus.language.MutableSentenceData#snapshot()
	 */
	@Override
	public SentenceData snapshot() {
		return new SimpleDependencyData(this);
	}

	/**
	 * @see de.ims.icarus.language.MutableSentenceData#setProperty(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setProperty(String key, Object value) {
		if(properties==null) {
			properties = new HashMap<>();
		}
		
		if(value==null) {
			properties.remove(key);
		} else {
			properties.put(key, value);
		}
	}

	/**
	 * @see de.ims.icarus.language.MutableSentenceData#getProperty(java.lang.String)
	 */
	@Override
	public Object getProperty(String key) {
		return properties==null ? null : properties.get(key);
	}

	/**
	 * @see de.ims.icarus.ui.helper.TextItem#getText()
	 */
	@Override
	public String getText() {
		return LanguageUtils.combine(this);
	}

	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public class DependencyDataEntry {

		private String form;

		private String lemma;

		private String features;

		private String pos;

		private String relation;

		private int head;

		private int index;
		
		private long flags;

		public DependencyDataEntry() {
			form = pos = relation = lemma = features = ""; //$NON-NLS-1$
			head = LanguageUtils.DATA_UNDEFINED_VALUE;
			flags = 0;
		}

		public DependencyDataEntry(String form, String lemma, String features, String pos, int head,
				String relation, long flags) {
			Exceptions.testNullArgument(form, "form"); //$NON-NLS-1$
			Exceptions.testNullArgument(lemma, "lemma"); //$NON-NLS-1$
			Exceptions.testNullArgument(features, "features"); //$NON-NLS-1$
			Exceptions.testNullArgument(pos, "pos"); //$NON-NLS-1$
			Exceptions.testNullArgument(relation, "relation"); //$NON-NLS-1$

			this.form = form;
			this.lemma = lemma;
			this.features = features;
			this.pos = pos;
			this.head = head;
			this.relation = relation;
			this.flags = flags;
		}

		public DependencyDataEntry(DependencyDataEntry source) {
			copyFrom(source);
		}

		public DependencyDataEntry(DependencyData source, int index) {
			copyFrom(source, index);
		}

		protected void copyFrom(DependencyDataEntry source) {
			form = source.form;
			lemma = source.lemma;
			features = source.features;
			pos = source.pos;
			head = source.head;
			index = source.index;
			relation = source.relation;
			flags = source.flags;
		}

		protected void copyFrom(DependencyData source, int idx) {
			form = source.getForm(idx);
			lemma = source.getLemma(idx);
			features = source.getFeatures(idx);
			pos = source.getPos(idx);
			head = source.getHead(idx);
			index = idx;
			relation = source.getRelation(idx);
			flags = source.getFlags(idx);
		}
		
		public void copyFrom(DependencyNodeData nodeData) {
			boolean fireEvent = false;
			
			// FORM
			if(!form.equals(nodeData.getForm())) {
				fireEvent = true;
				form = nodeData.getForm();
			}
			
			// LEMMA
			if(!lemma.equals(nodeData.getLemma())) {
				fireEvent = true;
				lemma = nodeData.getLemma();
			}
			
			// FEATURES
			if(!features.equals(nodeData.getFeatures())) {
				fireEvent = true;
				features = nodeData.getFeatures();
			}
			
			// POS
			if(!pos.equals(nodeData.getPos())) {
				fireEvent = true;
				pos = nodeData.getPos();
			}
			
			// HEAD
			if(head!=nodeData.getHead()) {
				fireEvent = true;
				head = nodeData.getHead();
			}
			
			// RELATION
			if(!relation.equals(nodeData.getRelation())) {
				fireEvent = true;
				relation = nodeData.getRelation();
			}
			
			// INDEX
			if(index!=nodeData.getIndex()) {
				fireEvent = false;
				MutableDependencyData.this.switchItems(index, nodeData.getIndex());
			}
			
			if(fireEvent) {
				MutableDependencyData.this.fireItemsUpdated(index, index);
			}
		}

		@Override
		public String toString() {
			return String.format("%s %s (%s) [%d] %s %s [%s] <%d>",  //$NON-NLS-1$
					form, lemma, features, index + 1, pos,
					LanguageUtils.getHeadLabel(head), relation, flags);
		}

		/**
		 * @return the flag
		 */
		public long getFlags() {
			return flags;
		}

		/**
		 * @param flags the flag to set
		 */
		public void setFlags(long flags) {
			this.flags = flags;
			
			int index = MutableDependencyData.this.indexOf(this);
			MutableDependencyData.this.fireItemsUpdated(index, index);
		}

		public String getForm() {
			return form;
		}

		public void setForm(String form) {
			Exceptions.testNullArgument(form, "form"); //$NON-NLS-1$
			if (!form.equals(this.form)) {
				this.form = form;
				int index = MutableDependencyData.this.indexOf(this);
				MutableDependencyData.this.fireItemsUpdated(index, index);
			}
		}

		public String getLemma() {
			return lemma;
		}

		public void setLemma(String lemma) {
			Exceptions.testNullArgument(lemma, "lemma"); //$NON-NLS-1$
			if (!lemma.equals(this.lemma)) {
				this.lemma = lemma;
				int index = MutableDependencyData.this.indexOf(this);
				MutableDependencyData.this.fireItemsUpdated(index, index);
			}
		}

		public String getFeatures() {
			return features;
		}

		public void setFeatures(String features) {
			Exceptions.testNullArgument(features, "features"); //$NON-NLS-1$
			if (!features.equals(this.features)) {
				this.features = features;
				int index = MutableDependencyData.this.indexOf(this);
				MutableDependencyData.this.fireItemsUpdated(index, index);
			}
		}

		public String getPos() {
			return pos;
		}

		public void setPos(String pos) {
			Exceptions.testNullArgument(pos, "pos"); //$NON-NLS-1$
			if (!pos.equals(this.pos)) {
				this.pos = pos;
				int index = MutableDependencyData.this.indexOf(this);
				MutableDependencyData.this.fireItemsUpdated(index, index);
			}
		}

		public int getHead() {
			return head;
		}

		public void setHead(int head) {
			if (head != this.head) {
				this.head = head;
				int index = MutableDependencyData.this.indexOf(this);
				MutableDependencyData.this.fireItemsUpdated(index, index);
			}
		}
		
		void setHead0(int head) {
			this.head = head;
		}

		public String getRelation() {
			return relation;
		}

		public void setRelation(String relation) {
			Exceptions.testNullArgument(relation, "relation"); //$NON-NLS-1$
			if (!relation.equals(this.relation)) {
				this.relation = relation;
				int index = MutableDependencyData.this.indexOf(this);
				MutableDependencyData.this.fireItemsUpdated(index, index);
			}
		}

		public int getIndex() {
			return index;
		}

		public void setIndex(int value) {
			MutableDependencyData.this.switchItems(index, value);
		}

		public MutableDependencyData getData() {
			return MutableDependencyData.this;
		}

		public boolean isRoot() {
			return head == LanguageUtils.DATA_HEAD_ROOT;
		}

		public boolean hasHead() {
			return head != LanguageUtils.DATA_HEAD_ROOT 
					&& head != LanguageUtils.DATA_UNDEFINED_VALUE;
		}
	}
}
