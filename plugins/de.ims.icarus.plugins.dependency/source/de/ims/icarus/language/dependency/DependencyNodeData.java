/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.language.dependency;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.ims.icarus.language.LanguageConstants;
import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.language.dependency.MutableDependencyData.DependencyDataEntry;
import de.ims.icarus.plugins.jgraph.cells.CompoundGraphNode;
import de.ims.icarus.util.CloneableObject;
import de.ims.icarus.util.Exceptions;


/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class DependencyNodeData implements CloneableObject, Cloneable,
		Serializable, CompoundGraphNode {

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
		head = LanguageUtils.DATA_UNDEFINED_VALUE;
		index = LanguageUtils.DATA_UNDEFINED_VALUE;
	}

	public DependencyNodeData(DependencyDataEntry source) {
		copyFrom(source);
	}

	public DependencyNodeData(DependencyNodeData source) {
		copyFrom(source);

		int childCount = source.getChildCount();
		if(childCount>0) {
			children = new ArrayList<>(childCount);
			for(int i=0; i<childCount; i++) {
				children.add(source.getChildAt(i).clone());
			}
		}
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
				LanguageUtils.getHeadLabel(head), relation);
	}

	public void clearHead() {
		head = LanguageUtils.DATA_UNDEFINED_VALUE;
		relation = ""; //$NON-NLS-1$
	}
	
	public boolean hasHead() {
		return head!=LanguageUtils.DATA_UNDEFINED_VALUE && head!=LanguageUtils.DATA_HEAD_ROOT;
	}

	@Override
	public DependencyNodeData clone() {
		return new DependencyNodeData(this);
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
		flags = source.flags;
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

	public String getForm2() {
		return hasChildren() ? form+" [...]" : form; //$NON-NLS-1$
	}

	public String getForm() {
		return form;
	}

	public void setForm(String form) {
		this.form = form;
	}

	public String getLemma2() {
		return hasChildren() ? lemma+" [...]" : lemma; //$NON-NLS-1$
	}

	public String getLemma() {
		return lemma;
	}

	public void setLemma(String lemma) {
		this.lemma = lemma;
	}

	public String getFeatures2() {
		return hasChildren() ? features+" [...]" : features; //$NON-NLS-1$
	}

	public String getFeatures() {
		return features;
	}

	public void setFeatures(String features) {
		this.features = features;
	}

	public String getPos2() {
		return hasChildren() ? pos+" [...]" : pos; //$NON-NLS-1$
	}

	public String getPos() {
		return pos;
	}

	public void setPos(String pos) {
		this.pos = pos;
	}

	public String getRelation() {
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
	
	public boolean isFlagSet(long flag) {
		return (flags & flag) == flag;
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
		return (int) (children==null ? 0 : children.size());
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
	
	public List<DependencyNodeData> getChildren(boolean sort) {
		if(!hasChildren()) {
			return Collections.emptyList();
		}
		
		List<DependencyNodeData> list = new ArrayList<>();
		feedChildren(list);
		
		if(sort) {
			Collections.sort(list, INDEX_SORTER);
		}
		
		return list;
	}

	
	public DependencyNodeData[] getChildrenArray() {
		List<DependencyNodeData> children = getChildren();
		if(children.isEmpty()) {
			return null;
		}
		DependencyNodeData[] buffer = new DependencyNodeData[children.size()];
		return children.toArray(buffer);
	}

	
	public List<DependencyNodeData> getChildren() {
		return getChildren(true);
	}
	
	public static Comparator<DependencyNodeData> INDEX_SORTER = new Comparator<DependencyNodeData>() {

		@Override
		public int compare(DependencyNodeData item1,
				DependencyNodeData item2) {
			
			return item1.getIndex()==item2.getIndex() ? 0 :
				item1.getIndex()>item2.getIndex() ? 1 : -1;
		}
		
	};
	
	protected void feedChildren(List<DependencyNodeData> list) {
		for(DependencyNodeData child : children) {
			if(child.hasChildren()) {
				child.feedChildren(list);
			}
			
			list.add(child);
		}
	}
	
	public boolean isRoot() {
		return head==LanguageUtils.DATA_HEAD_ROOT;
	}
	
	public boolean isProjective() {
		return isFlagSet(LanguageConstants.FLAG_PROJECTIVE);
	}
}
