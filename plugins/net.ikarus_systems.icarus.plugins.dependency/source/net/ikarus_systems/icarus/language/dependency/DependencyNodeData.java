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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import net.ikarus_systems.icarus.language.dependency.MutableDependencyData.DependencyDataEntry;
import net.ikarus_systems.icarus.util.CloneableObject;
import net.ikarus_systems.icarus.util.Exceptions;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlRootElement
public class DependencyNodeData implements DependencyConstants, CloneableObject, Cloneable,
		Serializable {

	private static final long serialVersionUID = -2234983923334922206L;

	@XmlAttribute(name="form", required=false)
	protected String form;

	@XmlAttribute(name="lemma", required=false)
	protected String lemma;

	@XmlAttribute(name="features", required=false)
	protected String features;

	@XmlAttribute(name="pos", required=false)
	protected String pos;

	@XmlAttribute(name="relation", required=false)
	protected String relation;

	@XmlAttribute(name="head", required=false)
	protected int head;

	@XmlAttribute(name="index", required=false)
	protected int index;

	@XmlAttribute(name="flags", required=false)
	protected long flags = 0;
	
	@XmlTransient
	protected List<DependencyNodeData> children;

	public DependencyNodeData() {
		form = "<empty>"; //$NON-NLS-1$
		pos = lemma = features = relation = ""; //$NON-NLS-1$
		head = DATA_UNDEFINED_VALUE;
		index = DATA_UNDEFINED_VALUE;
	}

	public DependencyNodeData(DependencyDataEntry source) {
		copyFrom(source);
	}

	public DependencyNodeData(DependencyNodeData source) {
		copyFrom(source);
	}

	public DependencyNodeData(DependencyData source, int index) {
		Exceptions.testNullArgument(source, "source"); //$NON-NLS-1$
		
		this.index = index; 
		form = source.getForm(index);
		lemma = source.getLemma(index);
		features = source.getFeatures(index);
		pos = source.getPos(index);
		relation = source.getRelation(index);
		head = source.getHead(index);
	}

	@Override
	public String toString() {
		return String.format("%s [%d] %s %s [%s]", form, index + 1, pos, //$NON-NLS-1$
				DependencyUtils.getHeadLabel(head), relation);
	}

	public void clearHead() {
		head = DATA_UNDEFINED_VALUE;
		relation = ""; //$NON-NLS-1$
	}

	public DependencyDataEntry toEntry(MutableDependencyData owner) {
		return owner.new DependencyDataEntry(form, lemma, features, pos, head, relation);
	}

	public void addTo(MutableDependencyData target) {
		if (index < target.getItemCount())
			target.insertItem(index, toEntry(target));
		else
			target.addItem(toEntry(target));
	}

	@Override
	public DependencyNodeData clone() {
		return new DependencyNodeData(this);
	}

	public int copyTo(DependencyDataEntry item) {
		int fieldMask = 0;

		/*
		 * if(index!=item.getIndex()) { hasChanged = true; item.setIndex(index);
		 * }
		 */

		if (!form.equals(item.getForm())) {
			fieldMask = fieldMask | DependencyConstants.DATA_FIELD_FORM;
			item.setForm(form);
		}

		if (!lemma.equals(item.getLemma())) {
			fieldMask = fieldMask | DependencyConstants.DATA_FIELD_LEMMA;
			item.setLemma(lemma);
		}

		if (!features.equals(item.getFeatures())) {
			fieldMask = fieldMask | DependencyConstants.DATA_FIELD_FEATURES;
			item.setFeatures(features);
		}

		if (!pos.equals(item.getPos())) {
			fieldMask = fieldMask | DependencyConstants.DATA_FIELD_POS;
			item.setPos(pos);
		}

		if (!relation.equals(item.getRelation())) {
			fieldMask = fieldMask | DependencyConstants.DATA_FIELD_RELATION;
			item.setRelation(relation);
		}

		if (head != item.getHead()) {
			fieldMask = fieldMask | DependencyConstants.DATA_FIELD_HEAD;
			item.setHead(head);
		}

		return fieldMask;
	}

	public void copyFrom(DependencyDataEntry source) {
		form = source.getForm();
		pos = source.getPos();
		lemma = source.getLemma();
		features = source.getFeatures();
		relation = source.getRelation();
		head = source.getHead();
		index = source.getIndex();
	}

	public void copyFrom(DependencyNodeData source) {
		form = source.form;
		lemma = source.lemma;
		features = source.features;
		pos = source.pos;
		relation = source.relation;
		head = source.head;
		index = source.index;
		flags = source.getFlags();
	}

	/** 
	 * Check for differences.
	 * <p>
	 * Note that a difference in flags does not affect the result of this method
	 */
	public boolean checkDifference(DependencyDataEntry source) {
		return index != source.getIndex() 
				|| head != source.getHead()
				|| !form.equals(source.getForm())
				|| !lemma.equals(source.getLemma())
				|| !features.equals(source.getFeatures())
				|| !pos.equals(source.getPos())
				|| !relation.equals(source.getRelation());
	}

	/** 
	 * Check for differences.
	 * <p>
	 * Note that a difference in flags does not affect the result of this method
	 */
	public boolean checkDifference(DependencyNodeData source) {
		return index != source.index 
				|| head != source.head
				|| !form.equals(source.form)
				|| !lemma.equals(source.lemma)
				|| !features.equals(source.features) 
				|| !pos.equals(source.pos)
				|| !relation.equals(source.relation);
	}

	public String getForm() {
		return hasChildren() ? form+" [...]" : form; //$NON-NLS-1$
	}

	public String getSoleForm() {
		return form;
	}

	public void setForm(String form) {
		this.form = form;
	}

	public String getLemma() {
		return hasChildren() ? lemma+" [...]" : lemma; //$NON-NLS-1$
	}

	public String getSoleLemma() {
		return lemma;
	}

	public void setLemma(String lemma) {
		this.lemma = lemma;
	}

	public String getFeatures() {
		return hasChildren() ? features+" [...]" : features; //$NON-NLS-1$
	}

	public String getSoleFeatures() {
		return features;
	}

	public void setFeatures(String features) {
		this.features = features;
	}

	public String getPos() {
		return hasChildren() ? pos+" [...]" : pos; //$NON-NLS-1$
	}

	public String getSolePos() {
		return pos;
	}

	public void setPos(String pos) {
		this.pos = pos;
	}

	public String getRelation() {
		return hasChildren() ? relation+" [...]" : relation; //$NON-NLS-1$
	}

	public String getSoleRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	public int getHead() {
		return head;
	}

	public void setHead(int head) {
		this.head = head;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	public long getFlags() {
		return flags;
	}
	
	public void setFlags(long flags) {
		this.flags = flags;
	}
	
	public void addChild(DependencyNodeData child) {
		if(children==null) {
			children = new ArrayList<>(4);
		}
		children.add(child);
	}
	
	public boolean hasChildren() {
		return children!=null && !children.isEmpty();
	}
	
	public int getChildCount() {
		return children==null ? 0 : children.size();
	}
	
	public DependencyNodeData getChildAt(int index) {
		return children==null ? null : children.get(index);
	}
	
	public boolean isNeighbor(DependencyNodeData item) {
		if(Math.abs(getIndex()-item.getIndex())==1)
			return true;
		
		if(hasChildren()) {
			for(DependencyNodeData child : children)
				if(item.isNeighbor(child))
					return true;
		}
		
		return false;
	}
	
	public DependencyNodeData[] getChildren() {
		if(!hasChildren())
			return null;
		
		List<DependencyNodeData> list = new Vector<DependencyNodeData>();
		feedChildren(list);
		
		Collections.sort(list, childSorter);
		
		return list.toArray(new DependencyNodeData[list.size()]);
	}
	
	protected static Comparator<DependencyNodeData> childSorter = new Comparator<DependencyNodeData>() {

		@Override
		public int compare(DependencyNodeData item1,
				DependencyNodeData item2) {
			
			return item1.getIndex()==item2.getIndex() ? 0 :
				item1.getIndex()>item2.getIndex() ? 1 : -1;
		}
		
	};
	
	protected void feedChildren(List<DependencyNodeData> list) {
		for(DependencyNodeData child : children) {
			if(child.hasChildren())
				child.feedChildren(list);
			
			list.add(child);
		}
	}
	
	public boolean isRoot() {
		return head==DATA_HEAD_ROOT;
	}
}
