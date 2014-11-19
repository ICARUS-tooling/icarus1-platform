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
package de.ims.icarus.plugins.prosody.search.constraints.edge;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.config.ConfigRegistry.Handle;
import de.ims.icarus.plugins.prosody.ProsodyConstants;
import de.ims.icarus.plugins.prosody.ProsodyUtils;
import de.ims.icarus.plugins.prosody.search.ProsodyTargetTree;
import de.ims.icarus.plugins.prosody.search.constraints.ValueHandler;
import de.ims.icarus.plugins.prosody.search.constraints.painte.AggregationMode;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.AbstractConstraintFactory;
import de.ims.icarus.search_tools.standard.DefaultConstraint;
import de.ims.icarus.search_tools.standard.DefaultSearchOperator;
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id: DependencyRelationConstraintFactory.java 269 2014-07-07 22:09:53Z mcgaerty $
 *
 */
public class SyllableDifferenceConstraintFactory extends AbstractConstraintFactory implements ProsodyConstants {

	public static final String TOKEN = "sylDif"; //$NON-NLS-1$

	private static final String CONFIG_PATH = "plugins.prosody.search.sylDif"; //$NON-NLS-1$

	private static final Map<Object, ValueHandler> propertyClassMap = new HashMap<>();
	static {
		propertyClassMap.put(SYLLABLE_DURATION_KEY, ValueHandler.floatHandler);
		propertyClassMap.put(SYLLABLE_ENDPITCH_KEY, ValueHandler.floatHandler);
		propertyClassMap.put(SYLLABLE_MIDPITCH_KEY, ValueHandler.floatHandler);
		propertyClassMap.put(SYLLABLE_OFFSET_KEY, ValueHandler.floatHandler);
		propertyClassMap.put(SYLLABLE_STARTPITCH_KEY, ValueHandler.floatHandler);
		propertyClassMap.put(SYLLABLE_TIMESTAMP_KEY, ValueHandler.floatHandler);
		propertyClassMap.put(CODA_SIZE_KEY, ValueHandler.integerHandler);
		propertyClassMap.put(VOWEL_DURATION_KEY, ValueHandler.floatHandler);
		propertyClassMap.put(ONSET_SIZE_KEY, ValueHandler.integerHandler);
		propertyClassMap.put(PAINTE_A1_KEY, ValueHandler.floatHandler);
		propertyClassMap.put(PAINTE_A2_KEY, ValueHandler.floatHandler);
		propertyClassMap.put(PAINTE_B_KEY, ValueHandler.floatHandler);
		propertyClassMap.put(PAINTE_C1_KEY, ValueHandler.floatHandler);
		propertyClassMap.put(PAINTE_C2_KEY, ValueHandler.floatHandler);
		propertyClassMap.put(PAINTE_D_KEY, ValueHandler.floatHandler);
	}

	private static ValueHandler getHandler(Object key) {
		if(key==null) {
			return ValueHandler.stringHandler;
		}

		String s = (String) key;
		int sep = s.indexOf(';');
		if(sep!=-1) {
			key = s.substring(0, sep);
		}
		ValueHandler handler = propertyClassMap.get(key);
		if(handler==null)
			throw new IllegalArgumentException("Not a number property: "+key); //$NON-NLS-1$

		return handler;
	}

	public SyllableDifferenceConstraintFactory() {
		super(TOKEN, EDGE_CONSTRAINT_TYPE,
				"plugins.prosody.constraints.sylDif.name",  //$NON-NLS-1$
				"plugins.prosody.constraints.sylDif.description"); //$NON-NLS-1$
	}

	@Override
	public SearchOperator[] getSupportedOperators() {
		return DefaultSearchOperator.numerical();
	}

	@Override
	public Object[] getSupportedSpecifiers() {
		return ProsodyUtils.getDefaultNumericalSyllablePropertyKeys();
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
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Object specifier, Options options) {
		return new SyllableComparisonConstraint(value, operator, specifier);
	}

	private static final AggregationMode DEFAULT_AGGREGATION_MODE = AggregationMode.maxValue;

	public static AggregationMode getAggregationMode(String id, ValueHandler handler) {
		if("-".equals(id)) { //$NON-NLS-1$
			return DEFAULT_AGGREGATION_MODE;
		}

		try {
			int index = Integer.parseInt(id);
			return new AggregationMode.SingletonAggregation(index-1);
		} catch(NumberFormatException e) {
			// ignore
		}

		switch (id.toLowerCase()) {
		case "min": //$NON-NLS-1$
			return AggregationMode.minValue;
		case "max": //$NON-NLS-1$
			return AggregationMode.maxValue;
		case "first": //$NON-NLS-1$
			return AggregationMode.firstValue;
		case "last": //$NON-NLS-1$
			return AggregationMode.lastValue;
		case "avg": { //$NON-NLS-1$
			if(handler==ValueHandler.doubleHandler) {
				return AggregationMode.avgDoubleValue;
			} else if(handler==ValueHandler.floatHandler) {
				return AggregationMode.avgFloatValue;
			} else if(handler==ValueHandler.integerHandler) {
				return AggregationMode.avgIntegerValue;
			} else if(handler==ValueHandler.longHandler) {
				return AggregationMode.avgLongValue;
			}
		}

			//$FALL-THROUGH$
		default:
			throw new IllegalArgumentException("Not a valid aggregation mode for this property: "+id); //$NON-NLS-1$
		}
	}

	private static class SyllableComparisonConstraint extends DefaultConstraint {

		private static final long serialVersionUID = -2819824788436110048L;

		private transient AggregationMode sourceAggregationMode, targetAggregationMode;
		private transient int fromIndex, toIndex;
		private transient String key;
		private transient boolean reverseFrom, reverseTo;
		private transient ValueHandler valueHandler;
		protected transient boolean ignoreUnstressed = false;

		public SyllableComparisonConstraint(Object value, SearchOperator operator, Object specifier) {
			super(TOKEN, value, operator, specifier);
		}

		/**
		 * @see de.ims.icarus.search_tools.standard.DefaultConstraint#init()
		 */
		@Override
		protected void init() {

			String configPath = getConfigPath();

			ConfigRegistry registry = ConfigRegistry.getGlobalRegistry();
			Handle handle = registry.getHandle(configPath);

			ignoreUnstressed = registry.getBoolean(registry.getChildHandle(handle, "ignoreUnstressedSyllables")); //$NON-NLS-1$
		}

		protected String getConfigPath() {
			return CONFIG_PATH;
		}

		@Override
		public void prepare() {
			super.prepare();

			String s = (String)getSpecifier();
			if(s!=null && !s.isEmpty()) {
				parseSpecifier(s);
			}
		}

		protected void parseSpecifier(String s) {
			String[] parts = s.split(";"); //$NON-NLS-1$

			if(parts.length<1)
				throw new IllegalArgumentException("Invalid specifier - need at least a property id"); //$NON-NLS-1$

			// Ensure numerical property
			valueHandler = getHandler(parts[0]);

			key = parts[0];

			// Get source aggregation mode
			if(parts.length>1) {
				sourceAggregationMode = getAggregationMode(parts[1], valueHandler);
			} else {
				// Max is default!
				sourceAggregationMode = DEFAULT_AGGREGATION_MODE;
			}

			// Get target aggregation mode
			if(parts.length>2) {
				targetAggregationMode = getAggregationMode(parts[2], valueHandler);
			} else {
				// If only 1 custom aggregation is defined, use it for both source AND target
				targetAggregationMode = sourceAggregationMode;
			}

			// Get begin index
			if(parts.length>3) {
				fromIndex = Integer.parseInt(parts[3]);
				reverseFrom = fromIndex<0;
				if(!reverseFrom) fromIndex--;
			} else {
				fromIndex = 0;
				reverseFrom = false;
			}

			// Get end index
			if(parts.length>4) {
				toIndex = Integer.parseInt(parts[4]);
				reverseTo = toIndex<0;
				if(!reverseTo) toIndex--;
			} else {
				toIndex = -1;
				reverseTo = false;
			}
		}

		protected Object getAggregatedInstance(ProsodyTargetTree tree, AggregationMode aggregationMode) {
			if(!tree.hasSyllables()) {
				return null;
			}

			int maxIndex = tree.getSyllableCount()-1;

			int fromIndex = this.fromIndex;
			if(fromIndex<0 && reverseFrom) {
				fromIndex = maxIndex+fromIndex+1;
			}

			int toIndex = this.toIndex;
			if(toIndex<0) {
				if(reverseTo) {
					toIndex = maxIndex+toIndex+1;
				} else {
					toIndex = maxIndex;
				}
			}

			// Maybe give some error message or hint to the user?
			if(fromIndex>toIndex || fromIndex<0 || toIndex<0) {
				return null;
			}

			return aggregationMode.getAggregatedValue(tree, key, fromIndex, toIndex, ignoreUnstressed);
		}

		@Override
		public Object getInstance(Object value) {
			ProsodyTargetTree tree = (ProsodyTargetTree) value;

			int targetIndex = tree.getNodeIndex();

			Object targetValue = getAggregatedInstance(tree, targetAggregationMode);
			if(targetValue==null) {
				return valueHandler.getDefaultValue();
			}

			tree.viewParent();
			Object sourceValue = getAggregatedInstance(tree, sourceAggregationMode);
			if(sourceValue==null) {
				return valueHandler.getDefaultValue();
			}

			tree.viewNode(targetIndex);

			return valueHandler.substract(sourceValue, targetValue);
		}

		@Override
		public SearchConstraint clone() {
			return new SyllableComparisonConstraint(getValue(), getOperator(), getSpecifier());
		}

		@Override
		public Object getLabel(Object value) {
			return (value instanceof Float || value instanceof Double) ?
					String.format(Locale.ENGLISH, "%.02f", value) : super.getLabel(value); //$NON-NLS-1$
		}
	}
}
