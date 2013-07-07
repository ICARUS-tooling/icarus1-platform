/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools.result;

import java.util.List;

import net.ikarus_systems.icarus.search_tools.ConstraintFactory;
import net.ikarus_systems.icarus.search_tools.Search;
import net.ikarus_systems.icarus.search_tools.SearchConstraint;
import net.ikarus_systems.icarus.search_tools.standard.GroupCache;
import net.ikarus_systems.icarus.util.annotation.AnnotatedData;
import net.ikarus_systems.icarus.util.annotation.AnnotationContainer;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.DataList;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface SearchResult extends AnnotationContainer {
	
	public static final String DUMMY_INSTANCE = "<none>"; //$NON-NLS-1$
	
	/**
	 * Marks a result that should be visualized without any
	 * special presenter and be displayed only as a plain
	 * list of matches.
	 */
	public static final String FORCE_SIMPLE_OUTLINE_PROPERTY = "forceSimpleOutline"; //$NON-NLS-1$
	
	void setProperty(String key, Object value);
	
	Object getProperty(String key);

	/**
	 * Returns the total number of groupings in the result
	 */
	int getDimension();
	
	/**
	 * Returns the {@code Search} that wraps all the information
	 * about the origin of this result. 
	 */
	Search getSource();

	/**
	 * Returns the total count of reported matches in this result  
	 */
	int getTotalMatchCount();

	/**
	 * Returns the total count of reported hits in this result.
	 * <p>
	 * Note that an arbitrary number of hits can be wrapped into a single
	 * match!  
	 */
	int getTotalHitCount();
	
	/**
	 * Returns the number of matches reported for the given combination
	 * of instances.
	 */
	int getMatchCount(int...groupIndices);
	
	/**
	 * Returns the number of matches with an instance of the given
	 * group in them.
	 */
	int getGroupMatchCount(int groupId, int index);
	
	/**
	 * Returns the number of instances that were reported for the
	 * given {@code groupId}.
	 */
	int getInstanceCount(int groupId);
	
	/**
	 * Returns the {@code SearchConstraint} that declared the grouping
	 * for the given index
	 */
	SearchConstraint getGroupConstraint(int index);
	
	/**
	 * Returns the {@code ContentType} representing the entries in this
	 * result. For treebanks this would be directly forwarded to the
	 * treebank the search was targeted at.
	 */
	ContentType getContentType();
	
	/**
	 * Returns the label to be used as title for the grouping at index
	 * {@code groupId}. Typically this will be the localized string as
	 * returned by {@link ConstraintFactory#getName()} by the factory
	 * that created the {@code SearchConstraint} at the specified index.
	 */
	Object getGroupLabel(int groupId);
	
	/**
	 * Returns the label to be used for the instance at {@code index} in
	 * group {@code groupId}. Typically this will be the actual value as
	 * encountered by the search without any localization or other modification
	 * outside the <i>value-to-label</i>-conversion performed by
	 * {@link ConstraintFactory#valueToLabel(Object)}.
	 */
	Object getInstanceLabel(int groupId, int index);
	
	/**
	 * Returns the index of the given {@code label} object within the list
	 * that holds all the encountered labels for the specified group.
	 */
	int getIndexOf(int groupId, Object label);
	
	/**
	 * Returns the entry object at the specified index.
	 * @throws IndexOutOfBoundsException if {@code index} &lt; 0 or
	 * {@code index} &ge; {@link #getTotalMatchCount()}
	 */
	Object getEntry(int index);
	
	Object getPlainEntry(ResultEntry entry);
	
	ResultEntry getRawEntry(int index);
	
	AnnotatedData getAnnotatedEntry(ResultEntry entry);
	
	/**
	 * Applies the given {@code permutation} array to the internal order
	 * of groups and reorders all affected entries in this result.
	 * <p>
	 * Returns {@code true} in case the reordering was successful
	 */
	boolean reorder(int[] permutation);
	
	boolean canReorder();
	
	/**
	 * Returns a list-oriented view of all the entries for the
	 * specified combination of group-instances.
	 */
	DataList<? extends Object> getEntryList(int...groupIndices);
	
	List<ResultEntry> getRawEntryList(int...groupIndices);
	
	/**
	 * Shorthand method for accessing an entry within the list-view
	 * that is represented by {@link #getEntryList(int...)}
	 * 
	 * @throws IndexOutOfBoundsException if one of the indices is outside
	 * the boundaries for its specific list.
	 * @throws IllegalStateException if the result is not final and the
	 * implementation does not support entry retrieval during a running search
	 */
	Object getEntryAt(int index, int...groupIndices);
	
	ResultEntry getRawEntryAt(int index, int...groupIndices);
	
	/**
	 * Creates and returns a new {@code SearchResult} that is backed by this
	 * result object and presents a view of a lesser dimension with some
	 * group variables set to the specified instances.
	 * 
	 * @throws IndexOutOfBoundsException if one of the indices is outside
	 * the boundaries for its specific list.
	 * @throws IllegalStateException if the result is not final and the
	 * implementation does not support creation of sub-results during a 
	 * running search
	 */
	SearchResult getSubResult(int...groupInstances);
	
	/**
	 * Returns  {@code true} if and only if the search constructing this
	 * result is completed and no more modifications are to be expected.
	 * Since construction of specialized views on a complex search-result can
	 * be quite expensive, visualization facilities are adviced to check this
	 * method before attempting to present such views while the search is still
	 * in progress or before allowing the user to do so.
	 * <p>
	 * Note that implementations are allowed to throw {@code IllegalStateException}
	 * when they do not support certain operations during a running search.
	 */
	boolean isFinal();
	
	GroupCache createCache();
	
	/**
	 * Deletes the content of this result.
	 * <p>
	 * An implementation should throw an {@link IllegalStateException} in case
	 * the result is still under construction and not final.
	 * 
	 * @throws IllegalStateException
	 */
	void clear();
	
	/**
	 * Attempts to finalize this result so that future modifications
	 * will be discarded.
	 * <p>
	 * An implementation should throw an {@link IllegalStateException} in case
	 * the result is already final.
	 * 
	 * @throws IllegalStateException
	 */
	void finish();
}
