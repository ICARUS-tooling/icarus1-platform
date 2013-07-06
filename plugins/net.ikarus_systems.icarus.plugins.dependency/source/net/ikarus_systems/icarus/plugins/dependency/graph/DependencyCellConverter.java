/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.dependency.graph;

import net.ikarus_systems.icarus.language.LanguageConstants;
import net.ikarus_systems.icarus.language.dependency.DependencyNodeData;
import net.ikarus_systems.icarus.language.dependency.DependencyUtils;
import net.ikarus_systems.icarus.search_tools.NodeType;
import net.ikarus_systems.icarus.search_tools.SearchConstraint;
import net.ikarus_systems.icarus.search_tools.SearchOperator;
import net.ikarus_systems.icarus.search_tools.standard.DefaultConstraint;
import net.ikarus_systems.icarus.search_tools.standard.DefaultGraphEdge;
import net.ikarus_systems.icarus.search_tools.standard.DefaultGraphNode;
import net.ikarus_systems.icarus.search_tools.util.SearchUtils;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.DataConversionException;
import net.ikarus_systems.icarus.util.data.DataConverter;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyCellConverter implements DataConverter {

	public DependencyCellConverter() {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.util.data.DataConverter#convert(java.lang.Object, net.ikarus_systems.icarus.util.Options)
	 */
	@Override
	public Object convert(Object source, Options options)
			throws DataConversionException {
		
		if(options==null) {
			options = Options.emptyOptions;
		}
		
		DependencyNodeData data = (DependencyNodeData) source;
		SearchConstraint[] constraints = new SearchConstraint[4];
		
		SearchOperator operator = SearchOperator.getOperator("="); //$NON-NLS-1$
		
		if(options.get("isVertex", true)) {			 //$NON-NLS-1$
			constraints[0] = new DefaultConstraint("form", data.getForm(), operator); //$NON-NLS-1$
			constraints[1] = new DefaultConstraint("lemma", data.getLemma(), operator); //$NON-NLS-1$
			constraints[2] = new DefaultConstraint("pos", data.getPos(), operator); //$NON-NLS-1$
			constraints[3] = new DefaultConstraint("features", data.getFeatures(), operator); //$NON-NLS-1$
			
			DefaultGraphNode result = new DefaultGraphNode();
			result.setConstraints(constraints);
			
			if(data.isRoot()) {
				result.setNodeType(NodeType.ROOT);
			}
			
			return result;
		} else {
			int distance = LanguageConstants.DATA_UNDEFINED_VALUE;
			int direction = LanguageConstants.DATA_UNDEFINED_VALUE;
			int projective = LanguageConstants.DATA_UNDEFINED_VALUE;
			
			if(data.hasHead()) {
				distance = Math.abs(data.getHead()-data.getIndex());
				direction = data.getHead()>data.getIndex() ?
						LanguageConstants.DATA_LEFT_VALUE : LanguageConstants.DATA_RIGHT_VALUE;
			}
			if(data.isProjective()) {
				projective = LanguageConstants.DATA_YES_VALUE;
			}
			
			constraints[0] = new DefaultConstraint("relation", data.getRelation(), operator); //$NON-NLS-1$
			constraints[1] = new DefaultConstraint("distance", distance, operator); //$NON-NLS-1$
			constraints[2] = new DefaultConstraint("direction", direction, operator); //$NON-NLS-1$
			constraints[3] = new DefaultConstraint("projectivity", projective, operator); //$NON-NLS-1$
			
			DefaultGraphEdge result = new DefaultGraphEdge();
			result.setConstraints(constraints);
			
			return result;
		}
		
	}

	/**
	 * @see net.ikarus_systems.icarus.util.data.DataConverter#getInputType()
	 */
	@Override
	public ContentType getInputType() {
		return DependencyUtils.getDependencyNodeContentType();
	}

	/**
	 * @see net.ikarus_systems.icarus.util.data.DataConverter#getResultType()
	 */
	@Override
	public ContentType getResultType() {
		return SearchUtils.getConstraintCellContentType();
	}

	/**
	 * @see net.ikarus_systems.icarus.util.data.DataConverter#getAccuracy()
	 */
	@Override
	public double getAccuracy() {
		return 1d;
	}

}
