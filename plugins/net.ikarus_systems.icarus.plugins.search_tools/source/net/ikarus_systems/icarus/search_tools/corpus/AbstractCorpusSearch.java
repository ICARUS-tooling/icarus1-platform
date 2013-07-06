/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools.corpus;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import net.ikarus_systems.icarus.language.AvailabilityObserver;
import net.ikarus_systems.icarus.language.DataType;
import net.ikarus_systems.icarus.language.SentenceData;
import net.ikarus_systems.icarus.language.SentenceDataList;
import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.search_tools.SearchConstraint;
import net.ikarus_systems.icarus.search_tools.SearchFactory;
import net.ikarus_systems.icarus.search_tools.SearchManager;
import net.ikarus_systems.icarus.search_tools.SearchQuery;
import net.ikarus_systems.icarus.search_tools.annotation.AnnotationBuffer;
import net.ikarus_systems.icarus.search_tools.annotation.ResultAnnotator;
import net.ikarus_systems.icarus.search_tools.result.SearchResult;
import net.ikarus_systems.icarus.search_tools.tree.AbstractTreeSearch;
import net.ikarus_systems.icarus.ui.dialog.DialogFactory;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.ContentTypeRegistry;

import org.java.plugin.registry.Extension;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractCorpusSearch extends AbstractTreeSearch {
	
	public static final int ANNOTATION_BUFFER_SIZE = 300;
	
	protected AvailabilityObserver observer;
	protected final DataType dataType;
	
	protected AbstractCorpusSearch(SearchFactory factory, SearchQuery query, 
			Options parameters,	Object target) {
		super(factory, query, parameters, target);
		
		observer = createObserver();
		dataType = getDefaultDataType();
	}
	
	protected DataType getDefaultDataType() {
		return DataType.SYSTEM;
	}
	
	protected abstract ResultAnnotator createAnnotator();
	
	protected AbstractCorpusSearchResult createResult() {
		List<SearchConstraint> groupConstraints = null;
		
		try {
			// Try to unify group constraints
			groupConstraints = new ConstraintUnifier(getSearchGraph()).getGroupConstraints();
		} catch(Exception e) {
			LoggerFactory.log(this, Level.WARNING, 
					"Aggregation of group-constraints failed", e); //$NON-NLS-1$
		}
		
		/* If unifying the group constraints failed allow user
		 * to manually override and switch to raw collection of
		 * all existing group constraints (ignoring duplicates)
		 * 
		 * 'ok' will cause collection of all group constraints 
		 * without aggregation check
		 */
		if(groupConstraints==null) {
			if(DialogFactory.getGlobalFactory().showConfirm(null, 
					"plugins.searchTools.graphValidation.title",  //$NON-NLS-1$
					"plugins.searchTools.graphValidation.ununifiedGroups")) { //$NON-NLS-1$
				groupConstraints = ConstraintUnifier.collectUnunifiedGroupConstraints(getSearchGraph());
			}
		}
		
		if(groupConstraints==null) {
			groupConstraints = Collections.emptyList();
		}
		
		ContentType entryType = ContentTypeRegistry.getEntryType(getTarget());
		
		/* Allow user to run search with a dimension that is not
		 * covered by a specialized result presenter.
		 * 
		 * 'ok' will cause search to ignore group count limits
		 */
		boolean forceFallback = false;
		int dimension = groupConstraints.size();
		List<Extension> presenters = SearchManager.getResultPresenterExtensions(
				entryType, dimension);
		if(presenters==null || presenters.isEmpty()) {
			if(!DialogFactory.getGlobalFactory().showConfirm(null, 
					"plugins.searchTools.graphValidation.title",  //$NON-NLS-1$
					"plugins.searchTools.graphValidation.groupLimitExceeded",  //$NON-NLS-1$
					dimension)) {
				return null;
			}
			forceFallback = true;
		}
		
		AbstractCorpusSearchResult result;
		
		/* Only distinguish between 0D and ND where N>0 since 0D
		 * can be implemented efficiently by using a simple list storage.
		 */
		if(groupConstraints.isEmpty()) {
			result = new CorpusSearchResult0D(this);
		} else {
			result = new CorpusSearchResultND(this, 
					groupConstraints.toArray(new SearchConstraint[0]));
		}
		
		if(forceFallback) {
			result.setProperty(SearchResult.FORCE_SIMPLE_OUTLINE_PROPERTY, forceFallback);
		}
		
		ResultAnnotator annotator = createAnnotator();
		if(annotator!=null) {
			AnnotationBuffer annotationBuffer = new AnnotationBuffer(
					result, annotator, ANNOTATION_BUFFER_SIZE);
			result.setAnnotationBuffer(annotationBuffer);
		}
		
		return result;
	}
	
	protected AvailabilityObserver createObserver() {
		return new CorpusObserver();
	}

	@Override
	protected abstract SentenceDataList createSource(Object target);

	@Override
	protected Object getTargetItem(int index) {
		return ((SentenceDataList)source).get(index, dataType, observer);
	}
	
	protected class CorpusObserver implements AvailabilityObserver {

		/**
		 * @see net.ikarus_systems.icarus.language.AvailabilityObserver#dataAvailable(int, net.ikarus_systems.icarus.language.SentenceData)
		 */
		@Override
		public void dataAvailable(int index, SentenceData item) {
			offerItem(index, item);
		}
	}
}
