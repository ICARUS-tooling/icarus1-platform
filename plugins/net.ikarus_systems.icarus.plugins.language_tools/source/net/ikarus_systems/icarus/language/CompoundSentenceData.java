/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CompoundSentenceData implements SentenceData {

	private static final long serialVersionUID = 4260253017719158901L;
	
	private SentenceData[] items;

	public CompoundSentenceData() {
		// no-op
	}
	
	public CompoundSentenceData(SentenceData systemData) {
		setData(DataType.SYSTEM, systemData);
	}
	
	public CompoundSentenceData(SentenceData systemData, SentenceData goldData, SentenceData userData) {
		setData(DataType.SYSTEM, systemData);
		setData(DataType.GOLD, goldData);
		setData(DataType.USER, userData);
	}
	
	public void setData(DataType type, SentenceData data) {
		if(type==null)
			throw new IllegalArgumentException("Invalid type"); //$NON-NLS-1$
		
		if(items==null) {
			items = new SentenceData[DataType.values().length];
		}
		
		items[type.ordinal()] = data;
	}
	
	public SentenceData getData(DataType type) {
		if(type==null)
			throw new IllegalArgumentException("Invalid type"); //$NON-NLS-1$
		
		return items==null ? null : items[type.ordinal()];
	}
	
	protected SentenceData getFirstSet() {
		if(items==null) {
			return null;
		}
		
		for(SentenceData data : items) {
			if(data!=null) {
				return data;
			}
		}
		
		return null;
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceData#getForms()
	 */
	@Override
	public String getForm(int index) {
		SentenceData data = getFirstSet();
		return data==null ? null : data.getForm(index);
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceData#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		SentenceData data = getFirstSet();
		return data==null ? true : data.isEmpty();
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceData#length()
	 */
	@Override
	public int length() {
		SentenceData data = getFirstSet();
		return data==null ? 0 : data.length();
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceData#getSourceGrammar()
	 */
	@Override
	public Grammar getSourceGrammar() {
		SentenceData data = getFirstSet();
		return data==null ? null : data.getSourceGrammar();
	}

	public CompoundSentenceData clone() {
		// Fetch data
		SentenceData systemData = getData(DataType.SYSTEM);
		SentenceData goldData = getData(DataType.GOLD);
		SentenceData userData = getData(DataType.USER);
		
		// Clone data
		if(systemData!=null) {
			systemData = systemData.clone();
		}
		if(goldData!=null) {
			goldData = systemData.clone();
		}
		if(userData!=null) {
			userData = systemData.clone();
		}
		
		return new CompoundSentenceData(systemData, goldData, userData);
	}
}
