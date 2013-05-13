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

import net.ikarus_systems.icarus.language.CompoundSentenceData;
import net.ikarus_systems.icarus.language.DataType;
import net.ikarus_systems.icarus.language.SentenceData;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CompoundDependencyData extends CompoundSentenceData implements
		DependencyData {

	/**
	 * 
	 */
	private static final long serialVersionUID = -689009503616872916L;

	/**
	 * 
	 */
	public CompoundDependencyData() {
		// no-op
	}

	/**
	 * @param systemData
	 */
	public CompoundDependencyData(DependencyData systemData) {
		super(systemData);
	}

	/**
	 * @param systemData
	 * @param goldData
	 * @param userData
	 */
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
	 * @see net.ikarus_systems.icarus.language.dependency.DependencyData#getForm(int)
	 */
	@Override
	public String getForm(int index) {
		DependencyData data = getFirstSet();
		return data==null ? null : data.getForm(index);
	}

	/**
	 * @see net.ikarus_systems.icarus.language.dependency.DependencyData#getPos(int)
	 */
	@Override
	public String getPos(int index) {
		DependencyData data = getFirstSet();
		return data==null ? null : data.getPos(index);
	}

	/**
	 * @see net.ikarus_systems.icarus.language.dependency.DependencyData#getRelation(int)
	 */
	@Override
	public String getRelation(int index) {
		DependencyData data = getFirstSet();
		return data==null ? null : data.getRelation(index);
	}

	/**
	 * @see net.ikarus_systems.icarus.language.dependency.DependencyData#getLemma(int)
	 */
	@Override
	public String getLemma(int index) {
		DependencyData data = getFirstSet();
		return data==null ? null : data.getLemma(index);
	}

	/**
	 * @see net.ikarus_systems.icarus.language.dependency.DependencyData#getFeatures(int)
	 */
	@Override
	public String getFeatures(int index) {
		DependencyData data = getFirstSet();
		return data==null ? null : data.getFeatures(index);
	}

	/**
	 * @see net.ikarus_systems.icarus.language.dependency.DependencyData#getHead(int)
	 */
	@Override
	public int getHead(int index) {
		DependencyData data = getFirstSet();
		return data==null ? DependencyConstants.DATA_UNDEFINED_VALUE : data.getHead(index);
	}

	/**
	 * @see net.ikarus_systems.icarus.language.dependency.DependencyData#isFlagSet(int, long)
	 */
	@Override
	public boolean isFlagSet(int index, long flag) {
		DependencyData data = getFirstSet();
		return data==null ? false : data.isFlagSet(index, flag);
	}

	/**
	 * @see net.ikarus_systems.icarus.language.dependency.DependencyData#getFlags(int)
	 */
	@Override
	public long getFlags(int index) {
		DependencyData data = getFirstSet();
		return data==null ? 0 : data.getFlags(index);
	}

}
