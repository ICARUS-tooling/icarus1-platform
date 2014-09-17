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
 * $Revision: 269 $
 * $Date: 2014-07-08 00:09:53 +0200 (Di, 08 Jul 2014) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.dependency/source/de/ims/icarus/language/dependency/search/constraints/DependencyRelationConstraintFactory.java $
 *
 * $LastChangedDate: 2014-07-08 00:09:53 +0200 (Di, 08 Jul 2014) $
 * $LastChangedRevision: 269 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.plugins.prosody.search.constraints;

import de.ims.icarus.plugins.prosody.search.ProsodyTargetTree;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.AbstractConstraintFactory;
import de.ims.icarus.search_tools.standard.DefaultCaseInsensitiveConstraint;
import de.ims.icarus.search_tools.standard.DefaultConstraint;
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id: DependencyRelationConstraintFactory.java 269 2014-07-07 22:09:53Z mcgaerty $
 *
 */
public class ProsodySpeakerFeaturesConstraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "speakerFeatures"; //$NON-NLS-1$

	public ProsodySpeakerFeaturesConstraintFactory() {
		super(TOKEN, NODE_CONSTRAINT_TYPE,
				"plugins.prosody.constraints.speakerFeatures.name",  //$NON-NLS-1$
				"plugins.prosody.constraints.speakerFeatures.description"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Object specifier, Options options) {
		if(options.get(SEARCH_CASESENSITIVE, DEFAULT_SEARCH_CASESENSITIVE))
			return new ProsodySpeakerFeaturesConstraint(value, operator);
		else
			return new ProsodySpeakerFeaturesCIConstraint(value, operator);
	}

	private static class ProsodySpeakerFeaturesConstraint extends DefaultConstraint {

		public ProsodySpeakerFeaturesConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		public Object getInstance(Object value) {
			return ((ProsodyTargetTree)value).getSpeakerFeatures();
		}

		@Override
		public SearchConstraint clone() {
			return new ProsodySpeakerFeaturesConstraint(getValue(), getOperator());
		}
	}

	private static class ProsodySpeakerFeaturesCIConstraint extends DefaultCaseInsensitiveConstraint {

		public ProsodySpeakerFeaturesCIConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		public Object getInstance(Object value) {
			return ((ProsodyTargetTree)value).getSpeakerFeatures().toLowerCase();
		}

		@Override
		public ProsodySpeakerFeaturesCIConstraint clone() {
			return new ProsodySpeakerFeaturesCIConstraint(getValue(), getOperator());
		}
	}
}
