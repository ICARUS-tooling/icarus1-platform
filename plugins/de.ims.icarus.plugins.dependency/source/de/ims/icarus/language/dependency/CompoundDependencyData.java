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

import de.ims.icarus.language.CompoundSentenceData;
import de.ims.icarus.language.DataType;
import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.language.SentenceData;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CompoundDependencyData extends CompoundSentenceData implements
		DependencyData {

	private static final long serialVersionUID = -689009503616872916L;

	public CompoundDependencyData() {
		// no-op
	}

	public CompoundDependencyData(DependencyData systemData) {
		super(systemData);
	}

	public CompoundDependencyData(DependencyData systemData,
			DependencyData goldData, DependencyData userData) {
		super(systemData, goldData, userData);
	}

	@Override
	public void setData(DataType type, SentenceData data) {
		if(!(data instanceof DependencyData))
			throw new IllegalArgumentException("Sentence data type not supported: "+data.getClass()); //$NON-NLS-1$
		
		super.setData(type, data);
	}

	@Override
	public DependencyData getData(DataType type) {
		return (DependencyData) super.getData(type);
	}

	@Override
	protected DependencyData getFirstSet() {
		return (DependencyData) super.getFirstSet();
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencyData#getForm(int)
	 */
	@Override
	public String getForm(int index) {
		DependencyData data = getFirstSet();
		return data==null ? null : data.getForm(index);
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencyData#getPos(int)
	 */
	@Override
	public String getPos(int index) {
		DependencyData data = getFirstSet();
		return data==null ? null : data.getPos(index);
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencyData#getRelation(int)
	 */
	@Override
	public String getRelation(int index) {
		DependencyData data = getFirstSet();
		return data==null ? null : data.getRelation(index);
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencyData#getLemma(int)
	 */
	@Override
	public String getLemma(int index) {
		DependencyData data = getFirstSet();
		return data==null ? null : data.getLemma(index);
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencyData#getFeatures(int)
	 */
	@Override
	public String getFeatures(int index) {
		DependencyData data = getFirstSet();
		return data==null ? null : data.getFeatures(index);
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencyData#getHead(int)
	 */
	@Override
	public int getHead(int index) {
		DependencyData data = getFirstSet();
		return data==null ? LanguageUtils.DATA_UNDEFINED_VALUE : data.getHead(index);
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencyData#isFlagSet(int, long)
	 */
	@Override
	public boolean isFlagSet(int index, long flag) {
		DependencyData data = getFirstSet();
		return data==null ? false : data.isFlagSet(index, flag);
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencyData#getFlags(int)
	 */
	@Override
	public long getFlags(int index) {
		DependencyData data = getFirstSet();
		return data==null ? 0 : data.getFlags(index);
	}

}
