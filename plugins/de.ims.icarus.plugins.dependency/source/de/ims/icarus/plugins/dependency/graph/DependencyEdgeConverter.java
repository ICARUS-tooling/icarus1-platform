/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.dependency.graph;

import de.ims.icarus.language.dependency.DependencyNodeData;
import de.ims.icarus.language.dependency.DependencyUtils;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.DefaultConstraint;
import de.ims.icarus.search_tools.standard.DefaultGraphEdge;
import de.ims.icarus.search_tools.util.SearchUtils;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.DataConversionException;
import de.ims.icarus.util.data.DataConverter;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyEdgeConverter implements DataConverter {

	public DependencyEdgeConverter() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.util.data.DataConverter#convert(java.lang.Object, de.ims.icarus.util.Options)
	 */
	@Override
	public Object convert(Object source, Options options)
			throws DataConversionException {
		
		DependencyNodeData data = (DependencyNodeData) source;
		SearchConstraint[] constraints = new SearchConstraint[4];
		
		SearchOperator operator = SearchOperator.getOperator("="); //$NON-NLS-1$
		
		constraints[0] = new DefaultConstraint("relation", data.getRelation(), operator); //$NON-NLS-1$
		constraints[1] = new DefaultConstraint("distance", DependencyUtils.getDistance(data), operator); //$NON-NLS-1$
		constraints[2] = new DefaultConstraint("direction", DependencyUtils.getDirection(data), operator); //$NON-NLS-1$
		constraints[3] = new DefaultConstraint("projectivity", data.isProjective(), operator); //$NON-NLS-1$
		
		DefaultGraphEdge result = new DefaultGraphEdge();
		result.setConstraints(constraints);
		
		return result;
	}

	/**
	 * @see de.ims.icarus.util.data.DataConverter#getInputType()
	 */
	@Override
	public ContentType getInputType() {
		return DependencyUtils.getDependencyNodeContentType();
	}

	/**
	 * @see de.ims.icarus.util.data.DataConverter#getResultType()
	 */
	@Override
	public ContentType getResultType() {
		return SearchUtils.getSearchEdgeContentType();
	}

	/**
	 * @see de.ims.icarus.util.data.DataConverter#getAccuracy()
	 */
	@Override
	public double getAccuracy() {
		return 1d;
	}

}
