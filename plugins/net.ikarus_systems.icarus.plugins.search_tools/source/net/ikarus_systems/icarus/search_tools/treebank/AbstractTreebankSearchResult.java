/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools.treebank;

import net.ikarus_systems.icarus.language.SentenceDataList;
import net.ikarus_systems.icarus.search_tools.ConstraintContext;
import net.ikarus_systems.icarus.search_tools.ConstraintFactory;
import net.ikarus_systems.icarus.search_tools.SearchConstraint;
import net.ikarus_systems.icarus.search_tools.SearchDescriptor;
import net.ikarus_systems.icarus.search_tools.result.ResultEntry;
import net.ikarus_systems.icarus.search_tools.result.SearchResult;
import net.ikarus_systems.icarus.search_tools.util.SearchUtils;
import net.ikarus_systems.icarus.util.SubstitutionSupport;
import net.ikarus_systems.icarus.util.data.ContentType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractTreebankSearchResult implements SearchResult {
	
	protected transient final SearchDescriptor descriptor;
	
	protected String[] groupTokens;
	protected SubstitutionSupport[] groupInstances;
	protected SearchConstraint[] groupConstraints;
	protected int[] groupIndexMap;
	
	protected boolean finalized = false;

	protected AbstractTreebankSearchResult(SearchDescriptor descriptor, SearchConstraint[] groupConstraints) {
		if(descriptor==null)
			throw new IllegalArgumentException("Invalid descriptor"); //$NON-NLS-1$
		if(!(descriptor.getTarget() instanceof SentenceDataList))
			throw new IllegalArgumentException("Invalid target: "+descriptor.getTarget()); //$NON-NLS-1$
		
		this.descriptor = descriptor;
		this.groupConstraints = SearchUtils.cloneSimple(groupConstraints);
		
		if(groupConstraints!=null && groupConstraints.length>0) {
			int size = groupConstraints.length;
			groupTokens = new String[size];
			groupInstances = new SubstitutionSupport[size];
			
			int maxIndex = 0;
			
			// Init token and instance mapping and find max group index
			for(int i=0; i<size; i++) {
				groupTokens[i] = groupConstraints[i].getToken();
				groupInstances[i] = new SubstitutionSupport();
				maxIndex = Math.max(maxIndex, (int)groupConstraints[i].getValue());
			}
			
			groupIndexMap = new int[maxIndex+1];

			// Fill reverse mapping from group id to id 
			// in current list of group constraints
			for(int i=0; i<size; i++) {
				int index = (int) groupConstraints[i].getValue();
				groupIndexMap[index] = i;
			}
		}
	}
	
	public SentenceDataList getTarget() {
		return (SentenceDataList) descriptor.getTarget();
	}

	@Override
	public boolean reorder(int[] permutation) {
		// Per default we do not support reordering
		return false;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getDimension()
	 */
	@Override
	public int getDimension() {
		return groupConstraints==null ? 0 : groupConstraints.length;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getSource()
	 */
	@Override
	public SearchDescriptor getSource() {
		return descriptor;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getInstanceCount(int)
	 */
	@Override
	public int getInstanceCount(int groupId) {
		return groupInstances[groupId].size();
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getGroupConstraint(int)
	 */
	@Override
	public SearchConstraint getGroupConstraint(int groupId) {
		return groupConstraints[groupId];
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return getTarget().getContentType();
	}
	
	public ConstraintContext getContext() {
		return getSource().getSearchFactory().getConstraintContext();
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getGroupLabel(int)
	 */
	@Override
	public Object getGroupLabel(int groupId) {
		String token = groupTokens[groupId];
		ConstraintFactory factory = getContext().getFactory(token);
		return factory.getName();
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getInstanceLabel(int, int)
	 */
	@Override
	public Object getInstanceLabel(int groupId, int index) {
		SubstitutionSupport sub = groupInstances[groupId];
		return sub.resubstitute(index);
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getIndexOf(int, java.lang.Object)
	 */
	@Override
	public int getIndexOf(int groupId, Object label) {
		SubstitutionSupport sub = groupInstances[groupId];
		return sub.getSubstitution((String) label);
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#getSubResult(int[])
	 */
	@Override
	public SearchResult getSubResult(int... groupInstances) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.result.SearchResult#isFinal()
	 */
	@Override
	public boolean isFinal() {
		return finalized;
	}
	
	public abstract GroupCache createCache();
	
	public abstract void commit(ResultEntry entry, GroupCache cache);
}
