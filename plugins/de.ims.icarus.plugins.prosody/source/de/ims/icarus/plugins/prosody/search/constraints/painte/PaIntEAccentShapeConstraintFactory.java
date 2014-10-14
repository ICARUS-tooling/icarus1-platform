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
 * @version $Id: DependencyRelationConstraintFactory.java 269 2014-07-07 22:09:53Z mcgaerty $
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
			ACCENT_SHAPE_RISE_LABEL,
			ACCENT_SHAPE_FALL_LABEL,
			ACCENT_SHAPE_RISE_FALL_LABEL,
		};
	}

	@Override
	public Object labelToValue(Object label, Object specifier) {
		return ProsodyUtils.parseAccentShapeLabel((String) label);
	}

	@Override
	public Object valueToLabel(Object value, Object specifier) {
		return ProsodyUtils.getAccentShapeLabel((int) value);
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
		private transient double minB, maxB;

		public ProsodyAccentShapeConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator, null);
		}

		@Override
		protected void init() {

			ConfigRegistry registry = ConfigRegistry.getGlobalRegistry();
			Handle handle = registry.getHandle(CONFIG_PATH);

			excursion = registry.getInteger(registry.getChildHandle(handle, "excursion")); //$NON-NLS-1$
			delta = registry.getInteger(registry.getChildHandle(handle, "delta")); //$NON-NLS-1$
			minB = registry.getDouble(registry.getChildHandle(handle, "minB")); //$NON-NLS-1$
			maxB = registry.getDouble(registry.getChildHandle(handle, "maxB")); //$NON-NLS-1$
		}

		@Override
		public Object getInstance(ProsodyTargetTree tree, int sylIndex) {
			float b = tree.getPainteB(sylIndex);
			if(b<minB || b>maxB) {
				return DATA_UNDEFINED_VALUE;
			}

			float c1 = tree.getPainteC1(sylIndex);
			float c2 = tree.getPainteC2(sylIndex);

			if(c1<excursion && c2<excursion) {
				return DATA_UNDEFINED_VALUE;
			}

			if(Math.abs(c1-c2)<=delta) {
				return ACCENT_SHAPE_RISE_FALL_VALUE;
			} else if(c1>c2) {
				return ACCENT_SHAPE_RISE_VALUE;
			} else {
				return ACCENT_SHAPE_FALL_VALUE;
			}
		}

		@Override
		public Object getLabel(Object value) {
			if((int)value == DATA_UNDEFINED_VALUE) {
				return ACCENT_SHAPE_UNDEFINED_LABEL;
			}
			return ProsodyUtils.getAccentShapeLabel((int) value);
		}
	}
}
