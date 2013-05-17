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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import net.ikarus_systems.icarus.language.Grammar;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SimpleDependencyData implements DependencyData {

	private static final long serialVersionUID = -6877590672027658180L;

	@XmlElement(name="form")
	protected String[] forms;

	@XmlElement(name="pos")
	protected String[] poss;

	@XmlElement(name="lemma")
	protected String[] lemmas;

	@XmlElement(name="feature")
	protected String[] features;

	@XmlElement(name="relation")
	protected String[] relations;

	@XmlElement(name="head")
	protected int[] heads;

	@XmlElement(name="flag", required=false)
	protected long[] flags;

	public SimpleDependencyData(String[] forms, String[] lemmas, 
			String[] features, String[] poss, String[] relations, int[] heads, long[] flags) {
		this.forms = forms;
		this.lemmas = lemmas;
		this.features = features;
		this.poss = poss;
		this.relations = relations;
		this.heads = heads;
		this.flags = flags;
	}

	public SimpleDependencyData(DependencyData source) {
		int size = source.length();

		forms = new String[size];
		lemmas = new String[size];
		features = new String[size];
		poss = new String[size];
		relations = new String[size];
		heads = new int[size];

		for(int index=0; index<size; index++) {
			forms[index] = source.getForm(index);
			lemmas[index] = source.getLemma(index);
			features[index] = source.getFeatures(index);
			poss[index] = source.getPos(index);
			relations[index] = source.getRelation(index);
			heads[index] = source.getHead(index);
			flags[index] = source.getFlags(index);
		}
	}

	public SimpleDependencyData() {
		// Creates the empty sentence

		forms = new String[0];
		lemmas = new String[0];
		features = new String[0];
		poss = new String[0];
		relations = new String[0];
		heads = new int[0];
	}

	@Override
	public int length() {
		return forms.length;
	}

	@Override
	public boolean isEmpty() {
		return forms == null || forms.length == 0;
	}

	@Override
	public String[] getForms() {
		return forms;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(200);

		for (int i = 0; i < forms.length; i++) {
			sb.append(i + 1).append(": ").append(forms[i]).append(" ").append( //$NON-NLS-1$ //$NON-NLS-2$
					DependencyUtils.getHeadLabel(heads[i])).append(" ").append( //$NON-NLS-1$
					poss[i]).append(" ").append(relations[i]).append(" ").append( //$NON-NLS-1$ //$NON-NLS-2$
					lemmas[i]).append(" ").append(features[i]); //$NON-NLS-1$
			if (i < forms.length - 1)
				sb.append("\n"); //$NON-NLS-1$
		}

		return sb.toString();
	}

	@Override
	public SimpleDependencyData clone() {
		return new SimpleDependencyData(this);
	}

	@Override
	public String getRelation(int index) {
		return relations[index];
	}

	@Override
	public String getForm(int index) {
		return forms[index];
	}

	@Override
	public int getHead(int index) {
		return heads[index];
	}

	@Override
	public String getPos(int index) {
		return poss[index];
	}

	@Override
	public String getFeatures(int index) {
		return features[index];
	}

	@Override
	public String getLemma(int index) {
		return lemmas[index];
	}
	
	public boolean isFlagSet(int index, long flag) {
		return flags!=null && (flags[index] & flag)==flag;
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceData#getSourceGrammar()
	 */
	@Override
	public Grammar getSourceGrammar() {
		return DependencyUtils.getDependencyGrammar();
	}

	/**
	 * @see net.ikarus_systems.icarus.language.dependency.DependencyData#getFlags(int)
	 */
	@Override
	public long getFlags(int index) {
		return flags==null ? 0 : flags[index];
	}
}
