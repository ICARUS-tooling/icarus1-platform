/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.search_tools.corpus;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;


import org.java.plugin.registry.Extension;

import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.language.AvailabilityObserver;
import de.ims.icarus.language.DataType;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.SentenceDataList;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchFactory;
import de.ims.icarus.search_tools.SearchManager;
import de.ims.icarus.search_tools.SearchQuery;
import de.ims.icarus.search_tools.annotation.AnnotationBuffer;
import de.ims.icarus.search_tools.annotation.ResultAnnotator;
import de.ims.icarus.search_tools.result.SearchResult;
import de.ims.icarus.search_tools.tree.AbstractTreeSearch;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;

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
			boolean doPlainUnify = ConfigRegistry.getGlobalRegistry().getBoolean(
					"plugins.searchTools.alwaysUnifyNonAggregatedConstraints"); //$NON-NLS-1$
			
			if(!doPlainUnify) {
				MutableBoolean check = new MutableBoolean(false);
				doPlainUnify = DialogFactory.getGlobalFactory().showCheckedConfirm(
						null, DialogFactory.CONTINUE_CANCEL_OPTION, check, 
						"plugins.searchTools.graphValidation.title",  //$NON-NLS-1$
						"config.alwaysUnifyNonAggregatedConstraints", //$NON-NLS-1$
						"plugins.searchTools.graphValidation.ununifiedGroups"); //$NON-NLS-1$
				
				if(check.getValue()) {
					ConfigRegistry.getGlobalRegistry().setValue(
							"plugins.searchTools.alwaysUnifyNonAggregatedConstraints",  //$NON-NLS-1$
							true);
				}
			}
			
			if(doPlainUnify) {
				groupConstraints = ConstraintUnifier.collectUnunifiedGroupConstraints(getSearchGraph());
			} else {
				return null;
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
			if(!DialogFactory.getGlobalFactory().showConfirm(null, DialogFactory.CONTINUE_CANCEL_OPTION, 
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
		 * @see de.ims.icarus.language.AvailabilityObserver#dataAvailable(int, de.ims.icarus.language.SentenceData)
		 */
		@Override
		public void dataAvailable(int index, SentenceData item) {
			offerItem(index, item);
		}
	}
}
