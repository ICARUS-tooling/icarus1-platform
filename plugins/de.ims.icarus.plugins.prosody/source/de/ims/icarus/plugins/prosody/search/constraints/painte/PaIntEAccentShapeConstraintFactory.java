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
package de.ims.icarus.plugins.prosody.search.constraints.painte;

import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.config.ConfigRegistry.Handle;
import de.ims.icarus.language.LanguageConstants;
import de.ims.icarus.plugins.prosody.ProsodyConstants;
import de.ims.icarus.plugins.prosody.ProsodyUtils;
import de.ims.icarus.plugins.prosody.search.ProsodyTargetTree;
import de.ims.icarus.plugins.prosody.search.constraints.AbstractProsodySyllableConstraint;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.AbstractConstraintFactory;
import de.ims.icarus.search_tools.standard.DefaultSearchOperator;
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class PaIntEAccentShapeConstraintFactory extends AbstractConstraintFactory implements ProsodyConstants {

	public static final String TOKEN = "accentShape"; //$NON-NLS-1$

	private static final String CONFIG_PATH = "plugins.prosody.search.accentShape"; //$NON-NLS-1$

	public PaIntEAccentShapeConstraintFactory() {
		super(TOKEN, NODE_CONSTRAINT_TYPE,
				"plugins.prosody.constraints.accentShape.name",  //$NON-NLS-1$
				"plugins.prosody.constraints.accentShape.description"); //$NON-NLS-1$
	}

	@Override
	public Class<?> getValueClass(Object specifier) {
		return null;
	}

	@Override
	public SearchOperator[] getSupportedOperators() {
		return new SearchOperator[]{
				DefaultSearchOperator.EQUALS,
				DefaultSearchOperator.EQUALS_NOT,
				DefaultSearchOperator.GROUPING,
		};
	}

	@Override
	public Object getDefaultValue(Object specifier) {
		return LanguageConstants.DATA_UNDEFINED_VALUE;
	}

	@Override
	public Object[] getLabelSet(Object specifier) {
		return new Object[] {
			LanguageConstants.DATA_UNDEFINED_LABEL,
			PEAK_SHAPE_RISE_LABEL,
			PEAK_SHAPE_FALL_LABEL,
			PEAK_SHAPE_RISE_FALL_LABEL,
			PEAK_SHAPE_NO_PEAK_LABEL,
		};
	}

	@Override
	public Object labelToValue(Object label, Object specifier) {
		return ProsodyUtils.parsePeakShapeLabel((String) label);
	}

	@Override
	public Object valueToLabel(Object value, Object specifier) {
		return ProsodyUtils.getPeakShapeLabel((int) value);
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Object specifier, Options options) {
		return new ProsodyAccentShapeConstraint(value, operator);
	}

	private static class ProsodyAccentShapeConstraint extends AbstractProsodySyllableConstraint {

		private static final long serialVersionUID = 3545419475427701670L;

		private transient int delta, excursion;
		private transient double minBRise, maxBRise;
		private transient double minBFall, maxBFall;
		private transient double minBRiseFall, maxBRiseFall;

		public ProsodyAccentShapeConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator, null);
		}

		@Override
		protected void init() {

			ConfigRegistry registry = ConfigRegistry.getGlobalRegistry();
			Handle handle = registry.getHandle(CONFIG_PATH);

			excursion = registry.getInteger(registry.getChildHandle(handle, "excursion")); //$NON-NLS-1$
			delta = registry.getInteger(registry.getChildHandle(handle, "delta")); //$NON-NLS-1$
			minBRise = registry.getDouble(registry.getChildHandle(handle, "minBRise")); //$NON-NLS-1$
			maxBRise = registry.getDouble(registry.getChildHandle(handle, "maxBRise")); //$NON-NLS-1$
			minBFall = registry.getDouble(registry.getChildHandle(handle, "minBFall")); //$NON-NLS-1$
			maxBFall = registry.getDouble(registry.getChildHandle(handle, "maxBFall")); //$NON-NLS-1$
			minBRiseFall = registry.getDouble(registry.getChildHandle(handle, "minBRiseFall")); //$NON-NLS-1$
			maxBRiseFall = registry.getDouble(registry.getChildHandle(handle, "maxBRiseFall")); //$NON-NLS-1$
		}

		@Override
		public Object getInstance(ProsodyTargetTree tree, int sylIndex) {
			float b = tree.getPainteB(sylIndex);
			float c1 = tree.getPainteC1(sylIndex);
			float c2 = tree.getPainteC2(sylIndex);

			if(c1<excursion && c2<excursion) {
				return PEAK_SHAPE_NO_PEAK_VALUE;
			}

			if(Math.abs(c1-c2)<=delta) {
				return (b>=minBRiseFall && b<=maxBRiseFall) ? PEAK_SHAPE_RISE_FALL_VALUE : DATA_UNDEFINED_VALUE;
			} else if(c1>c2) {
				return (b>=minBRise && b<=maxBRise) ? PEAK_SHAPE_RISE_VALUE : DATA_UNDEFINED_VALUE;
			} else {
				return (b>=minBFall && b<=maxBFall) ? PEAK_SHAPE_FALL_VALUE : DATA_UNDEFINED_VALUE;
			}
		}

		@Override
		public Object getLabel(Object value) {
//			if((int)value == DATA_UNDEFINED_VALUE) {
//				return PEAK_SHAPE_NO_PEAK_LABEL;
//			}
			return ProsodyUtils.getPeakShapeLabel((int) value);
		}
	}
}
