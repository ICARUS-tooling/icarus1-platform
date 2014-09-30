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

 * $Revision: 270 $
 * $Date: 2014-07-08 13:44:07 +0200 (Di, 08 Jul 2014) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.coref/source/de/ims/icarus/plugins/coref/search/constraints/CoreferenceSentencePropertyConstraintFactory.java $
 *
 * $LastChangedDate: 2014-07-08 13:44:07 +0200 (Di, 08 Jul 2014) $
 * $LastChangedRevision: 270 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.plugins.prosody.search.constraints;

import java.util.HashMap;
import java.util.Map;

import de.ims.icarus.plugins.prosody.ProsodyConstants;
import de.ims.icarus.plugins.prosody.ProsodyUtils;
import de.ims.icarus.plugins.prosody.search.ProsodyTargetTree;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.AbstractConstraintFactory;
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id: CoreferenceSentencePropertyConstraintFactory.java 270 2014-07-08 11:44:07Z mcgaerty $
 *
 */
public class ProsodySyllablePropertyConstraintFactory extends AbstractConstraintFactory implements ProsodyConstants {

	public static final String TOKEN = "syllableProperty"; //$NON-NLS-1$

	private static final Map<Object, ValueHandler> propertyClassMap = new HashMap<>();
	static {
		propertyClassMap.put(SYLLABLE_DURATION_KEY, ValueHandler.floatHandler);
		propertyClassMap.put(SYLLABLE_ENDPITCH_KEY, ValueHandler.floatHandler);
		propertyClassMap.put(SYLLABLE_LABEL_KEY, ValueHandler.stringHandler);
		propertyClassMap.put(SYLLABLE_MIDPITCH_KEY, ValueHandler.floatHandler);
		propertyClassMap.put(SYLLABLE_OFFSET_KEY, ValueHandler.floatHandler);
		propertyClassMap.put(SYLLABLE_STARTPITCH_KEY, ValueHandler.floatHandler);
		propertyClassMap.put(SYLLABLE_STRESS_KEY, ValueHandler.booleanHandler);
		propertyClassMap.put(SYLLABLE_TIMESTAMP_KEY, ValueHandler.floatHandler);
		propertyClassMap.put(SYLLABLE_VOWEL_KEY, ValueHandler.stringHandler);
		propertyClassMap.put(CODA_SIZE_KEY, ValueHandler.integerHandler);
		propertyClassMap.put(CODA_TYPE_KEY, ValueHandler.stringHandler);
		propertyClassMap.put(VOWEL_DURATION_KEY, ValueHandler.floatHandler);
		propertyClassMap.put(ONSET_SIZE_KEY, ValueHandler.integerHandler);
		propertyClassMap.put(ONSET_TYPE_KEY, ValueHandler.stringHandler);
		propertyClassMap.put(PHONEME_COUNT_KEY, ValueHandler.integerHandler);
		propertyClassMap.put(PAINTE_A1_KEY, ValueHandler.floatHandler);
		propertyClassMap.put(PAINTE_A2_KEY, ValueHandler.floatHandler);
		propertyClassMap.put(PAINTE_B_KEY, ValueHandler.floatHandler);
		propertyClassMap.put(PAINTE_C1_KEY, ValueHandler.floatHandler);
		propertyClassMap.put(PAINTE_C2_KEY, ValueHandler.floatHandler);
		propertyClassMap.put(PAINTE_D_KEY, ValueHandler.floatHandler);
	}

	private static ValueHandler getHandler(Object key) {
		ValueHandler handler = propertyClassMap.get(key);
		return handler==null ? ValueHandler.stringHandler : handler;
	}

	public ProsodySyllablePropertyConstraintFactory() {
		super(TOKEN, NODE_CONSTRAINT_TYPE,
				"plugins.prosody.constraints.syllableProperty.name",  //$NON-NLS-1$
				"plugins.prosody.constraints.syllableProperty.description"); //$NON-NLS-1$
	}

	@Override
	public Object[] getSupportedSpecifiers() {
		return ProsodyUtils.getDefaultSyllablePropertyKeys();
	}

	@Override
	public Class<?> getValueClass(Object specifier) {
		return getHandler(specifier).getValueClass();
	}

	@Override
	public Object getDefaultValue(Object specifier) {
		return getHandler(specifier).getDefaultValue();
	}

	@Override
	public Object labelToValue(Object label, Object specifier) {
		return getHandler(specifier).labelToValue(label);
	}

	@Override
	public Object valueToLabel(Object value, Object specifier) {
		return getHandler(specifier).valueToLabel(value);
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator, java.lang.Object, de.ims.icarus.util.Options)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Object specifier, Options options) {
		if(options.get(SEARCH_CASESENSITIVE, DEFAULT_SEARCH_CASESENSITIVE))
			return new ProsodySyllablePropertyConstraint(value, operator, specifier);
		else
			return new ProsodySyllablePropertyCIConstraint(value, operator, specifier);
	}

	private static class ProsodySyllablePropertyConstraint extends AbstractProsodySyllableConstraint {

		private static final long serialVersionUID = -2250947975211835769L;

		public ProsodySyllablePropertyConstraint(Object value, SearchOperator operator, Object specifier) {
			super(TOKEN, value, operator, specifier);
		}

		public String getKey() {
			return getSpecifier().toString();
		}

		@Override
		protected Object getInstance(ProsodyTargetTree tree, int syllable) {
			return tree.getProperty(getKey(), syllable);
		}

		@Override
		public SearchConstraint clone() {
			return new ProsodySyllablePropertyConstraint(getValue(), getOperator(), getSpecifier());
		}
	}

	private static class ProsodySyllablePropertyCIConstraint extends AbstractProsodySyllableCaseInsensitiveConstraint {

		private static final long serialVersionUID = 2216387895482510250L;

		public ProsodySyllablePropertyCIConstraint(Object value, SearchOperator operator, Object specifier) {
			super(TOKEN, value, operator, specifier);
		}

		public String getKey() {
			return getSpecifier().toString();
		}

		@Override
		protected Object getInstance(ProsodyTargetTree tree, int syllable) {
			return tree.getProperty(getKey(), syllable);
		}

		@Override
		public SearchConstraint clone() {
			return new ProsodySyllablePropertyCIConstraint(getValue(), getOperator(), getSpecifier());
		}
	}
}
