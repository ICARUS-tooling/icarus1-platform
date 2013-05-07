/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.dependency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import net.ikarus_systems.icarus.language.AbstractMutableSentenceData;
import net.ikarus_systems.icarus.language.Grammar;
import net.ikarus_systems.icarus.language.MutableSentenceData;
import net.ikarus_systems.icarus.language.SentenceData;
import net.ikarus_systems.icarus.language.SentenceDataEvent;
import net.ikarus_systems.icarus.language.UnsupportedSentenceDataException;
import net.ikarus_systems.icarus.language.annotation.AnnotatedSentenceData;
import net.ikarus_systems.icarus.util.Exceptions;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlRootElement(name="DependencyData")
@XmlAccessorType(XmlAccessType.FIELD)
public class MutableDependencyData extends AbstractMutableSentenceData
		implements DependencyConstants, AnnotatedSentenceData, DependencyData {

	private static final long serialVersionUID = 8905987553461570238L;

	@XmlTransient
	protected transient final DependencyDataEvent event;

	@XmlTransient
	protected Object annotation = null; // TODO change to default value?

	@XmlElement(name="items")
	private List<DependencyDataEntry> items = new ArrayList<>();
	
	@XmlTransient
	private Map<String, Object> properties;

	public MutableDependencyData() {
		event = new DependencyDataEvent(this);
	}

	public MutableDependencyData(String[] forms, String[] lemmas, 
			String[] features, String[] poss, int[] heads,
			String[] depRels) {
		this();

		items.clear();

		for (int i = 0; i < forms.length; i++) {
			items.add(this.new DependencyDataEntry(
					forms[i], lemmas[i], features[i], poss[i], heads[i], depRels[i]));
		}
	}

	public void fireDataChanged() {
		event.set(SentenceDataEvent.CHANGE_EVENT, -1, -1);
		fireDataChanged(event);
	}

	public void fireItemsInserted(int startIndex, int endIndex) {
		event.set(SentenceDataEvent.INSERT_EVENT, startIndex, endIndex);
		fireDataChanged(event);
	}

	public void fireItemsRemoved(int startIndex, int endIndex) {
		event.set(SentenceDataEvent.REMOVE_EVENT, startIndex, endIndex);
		fireDataChanged(event);
	}

	public void fireItemsUpdated(int startIndex, int endIndex) {
		event.set(SentenceDataEvent.UPDATE_EVENT, startIndex, endIndex);
		fireDataChanged(event);
	}

	@Override
	public String[] getForms() {
		String[] forms = new String[items.size()];
		for (int i = 0; i < forms.length; i++)
			forms[i] = items.get(i).form;

		return forms;
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
		int index = items.size() - 1;
		item.index = index;
		item.form = "<empty>"; //$NON-NLS-1$
		fireItemsInserted(index, index);
		return item;
	}

	public void addItem(String form, String lemma, String features, String pos, int head, String depRel) {
		items.add(this.new DependencyDataEntry(
				form, lemma, features, pos, head, depRel));
		int index = items.size() - 1;
		fireItemsInserted(index, index);
	}

	public boolean addItem(DependencyDataEntry item) {
		if (!items.contains(item)) {
			items.add(item);
			int index = items.size() - 1;
			fireItemsInserted(index, index);
			return true;
		}
		return false;
	}

	public void insertItem(int index, String form, String lemma, String features, 
			String pos, int head, String depRel) {
		items.add(index, this.new DependencyDataEntry(
				form, lemma, features, pos, head, depRel));
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
		return items.indexOf(item);
	}

	public void removeItemAt(int index) {
		items.remove(index);
		fireItemsRemoved(index, index);
	}

	public boolean removeItem(DependencyDataEntry item) {
		int index = items.indexOf(item);
		if (index >= 0) {
			items.remove(index);
			fireItemsRemoved(index, index);
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
			throw new IllegalArgumentException("Invalid source"); //$NON-NLS-1$
		if (!(source instanceof DependencyData))
			throw new UnsupportedSentenceDataException("Unsupported type: "+source.getClass()); //$NON-NLS-1$
		
		DependencyData data = (DependencyData) source;
		items.clear();
		for (int i = 0; i < data.length(); i++) {
			items.add(this.new DependencyDataEntry(data.getForm(i),
					data.getLemma(i), data.getFeatures(i),
					data.getPos(i), data.getHead(i), data.getRelation(i)));
		}
		
		if(source instanceof AnnotatedSentenceData)
			annotation = ((AnnotatedSentenceData)source).getAnnotation();
		
		fireDataChanged();
	}

	@Override
	public MutableSentenceData clone() {
		MutableDependencyData data = new MutableDependencyData();
		data.copyFrom(this);

		return data;
	}

	@Override
	public Object getAnnotation() {
		return annotation;
	}
	
	@Override
	public void setAnnotation(Object annotation) {
		// TODO
	}

	@Override
	public String getForm(int index) {
		return items.get(index).getForm();
	}

	@Override
	public String getLemma(int index) {
		return items.get(index).getLemma();
	}

	@Override
	public String getFeatures(int index) {
		return items.get(index).getFeatures();
	}

	@Override
	public int getHead(int index) {
		return items.get(index).getHead();
	}

	@Override
	public String getPos(int index) {
		return items.get(index).getPos();
	}

	@Override
	public String getRelation(int index) {
		return items.get(index).getRelation();
	}

	@Override
	public int length() {
		return items.size();
	}

	@Override
	public boolean isFlagSet(int index, long flag) {
		return (items.get(index).getFlag() & flag) == flag;
	}

	public void setFlag(int index, long flag) {
		long value = items.get(index).getFlag();
		value |= flag;
		items.get(index).setFlag(value);
	}

	public void unsetFlag(int index, long flag) {
		long value = items.get(index).getFlag();
		value &= ~flag;
		items.get(index).setFlag(value);
	}
	
	@Override
	public long getFlags(int index) {
		return items.get(index).getFlag();
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
			if (item.head == DATA_HEAD_ROOT || item.head==DATA_UNDEFINED_VALUE) {
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

		int updateFrom = indexFrom;
		int updateTo = indexTo;

		for (int i = 0; i < items.size(); i++) {
			tmp = items.get(i);
			if (tmp.head == indexFrom) {
				tmp.head = indexTo;
				updateFrom = Math.min(updateFrom, i);
				updateTo = Math.max(updateTo, i);
			} else if (tmp.head == indexTo) {
				tmp.head = indexFrom;
				updateFrom = Math.min(updateFrom, i);
				updateTo = Math.max(updateTo, i);
			}
		}

		fireItemsUpdated(updateFrom, updateTo);
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceData#getSourceGrammar()
	 */
	@Override
	public Grammar getSourceGrammar() {
		return DependencyUtils.getDependencyGrammar();
	}

	/**
	 * @see net.ikarus_systems.icarus.language.MutableSentenceData#snapshot()
	 */
	@Override
	public SentenceData snapshot() {
		return new SimpleDependencyData(this);
	}

	/**
	 * @see net.ikarus_systems.icarus.language.MutableSentenceData#setProperty(java.lang.String, java.lang.Object)
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
	 * @see net.ikarus_systems.icarus.language.MutableSentenceData#getProperty(java.lang.String)
	 */
	@Override
	public Object getProperty(String key) {
		return properties==null ? null : properties.get(key);
	}

	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	@XmlRootElement
	public class DependencyDataEntry {

		@XmlAttribute(name="form")
		private String form;

		@XmlAttribute(name="lemma")
		private String lemma;

		@XmlAttribute(name="features")
		private String features;

		@XmlAttribute(name="pos")
		private String pos;

		@XmlAttribute(name="depRel")
		private String relation;

		@XmlAttribute(name="head")
		private int head;

		@XmlAttribute(name="index")
		private int index;
		
		@XmlAttribute(name="flag", required=false)
		private long flag;

		public DependencyDataEntry() {
			form = pos = relation = lemma = features = ""; //$NON-NLS-1$
			head = DATA_UNDEFINED_VALUE;
		}

		public DependencyDataEntry(String form, String lemma, String features, String pos, int head,
				String relation) {
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
		}

		public DependencyDataEntry(DependencyDataEntry source) {
			copyFrom(source);
		}

		protected void copyFrom(DependencyDataEntry source) {
			form = source.form;
			lemma = source.lemma;
			features = source.features;
			pos = source.pos;
			head = source.head;
			index = source.index;
			relation = source.relation;
		}

		@Override
		public String toString() {
			return String.format("%s %s (%s) [%d] %s %s [%s]",  //$NON-NLS-1$
					form, lemma, features, index + 1, pos,
					DependencyUtils.getHeadLabel(head), relation);
		}

		/**
		 * @return the flag
		 */
		public long getFlag() {
			return flag;
		}

		/**
		 * @param flag the flag to set
		 */
		public void setFlag(long flag) {
			this.flag = flag;
			
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
			return head == DATA_HEAD_ROOT;
		}

		public boolean hasHead() {
			return head != DATA_HEAD_ROOT && head != DATA_UNDEFINED_VALUE;
		}
	}
}
