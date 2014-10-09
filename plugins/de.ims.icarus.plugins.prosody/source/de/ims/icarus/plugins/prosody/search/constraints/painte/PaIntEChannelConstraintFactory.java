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
import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.plugins.prosody.painte.PaIntEConstraintParams;
import de.ims.icarus.plugins.prosody.painte.PaIntEParamsWrapper;
import de.ims.icarus.plugins.prosody.painte.PaIntEUtils;
import de.ims.icarus.plugins.prosody.search.ProsodyTargetTree;
import de.ims.icarus.plugins.prosody.search.constraints.AbstractProsodySyllableConstraint;
import de.ims.icarus.plugins.prosody.ui.view.editor.PaIntERegistry;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.AbstractConstraintFactory;
import de.ims.icarus.search_tools.standard.DefaultSearchOperator;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.id.UnknownIdentifierException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class PaIntEChannelConstraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "painteChannel"; //$NON-NLS-1$

	private static final String CONFIG_PATH = "plugins.prosody.search.painteChannel"; //$NON-NLS-1$

	public PaIntEChannelConstraintFactory() {
		super(TOKEN, NODE_CONSTRAINT_TYPE,
				"plugins.prosody.constraints.painteChannel.name",  //$NON-NLS-1$
				"plugins.prosody.constraints.painteChannel.description"); //$NON-NLS-1$
	}

	@Override
	public Object[] getSupportedSpecifiers() {
		return new Object[0];
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
	public Object labelToValue(Object label, Object specifier) {
		return LanguageUtils.parseBooleanLabel((String)label);
	}

	@Override
	public Object valueToLabel(Object value, Object specifier) {
		return LanguageUtils.getBooleanLabel((int)value);
	}

	@Override
	public Object[] getLabelSet(Object specifier) {
		return new Object[]{
				LanguageConstants.DATA_UNDEFINED_LABEL,
				LanguageUtils.getBooleanLabel(LanguageConstants.DATA_YES_VALUE),
				LanguageUtils.getBooleanLabel(LanguageConstants.DATA_NO_VALUE),
		};
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator, java.lang.Object, de.ims.icarus.util.Options)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Object specifier, Options options) {
		return new PaIntEChannelConstraint(value, operator, specifier);
	}

	private static void parseParams(String s, PaIntEConstraintParams constraints) {
		if(s.startsWith("$")) { //$NON-NLS-1$
			String name = s.substring(1);
			PaIntEParamsWrapper wrapper = PaIntERegistry.getInstance().getParams(name);

			if(wrapper==null)
				throw new UnknownIdentifierException("No such painte parameter set available: "+name); //$NON-NLS-1$

			constraints.setParams(wrapper.getParams());
		} else {
			constraints.setParams(s);
		}
	}

	private static class PaIntEChannelConstraint extends AbstractProsodySyllableConstraint {

		private static final long serialVersionUID = 6887748634037055630L;

		private double leftBorder, rightBorder;
		private int resolution;

		protected final PaIntEConstraintParams valueParams = new PaIntEConstraintParams();
		protected PaIntEConstraintParams lowerParams;
		protected PaIntEConstraintParams upperParams;

		public PaIntEChannelConstraint(Object value, SearchOperator operator, Object specifier) {
			super(TOKEN, value, operator, specifier);


			ConfigRegistry registry = ConfigRegistry.getGlobalRegistry();
			Handle handle = registry.getHandle(CONFIG_PATH);

			leftBorder = registry.getDouble(registry.getChildHandle(handle, "leftBorder")); //$NON-NLS-1$
			rightBorder = registry.getDouble(registry.getChildHandle(handle, "rightBorder")); //$NON-NLS-1$
			resolution = registry.getInteger(registry.getChildHandle(handle, "resolution")); //$NON-NLS-1$
		}

		@Override
		public void setSpecifier(Object specifier) {
			super.setSpecifier(specifier);

			String s = (String)specifier;
			if(s!=null && !LanguageConstants.DATA_UNDEFINED_LABEL.equals(s)) {
				if(lowerParams==null) {
					lowerParams = new PaIntEConstraintParams();
				}
				if(upperParams==null) {
					upperParams = new PaIntEConstraintParams();
				}

				String[] parts = s.split(";"); //$NON-NLS-1$

				if(parts.length<2)
					throw new IllegalArgumentException("Invalid channel parts - need at least 2 sets of painte parameters separated by semicolon: "+s); //$NON-NLS-1$

				parseParams(parts[0], lowerParams);
				parseParams(parts[1], upperParams);

				if(parts.length>2 && !parts[2].isEmpty()) {
					leftBorder = Double.parseDouble(parts[2]);
				}

				if(parts.length>3 && !parts[3].isEmpty()) {
					rightBorder = Double.parseDouble(parts[3]);
				}

				if(parts.length>4 && !parts[4].isEmpty()) {
					resolution = Integer.parseInt(parts[4]);
				}
			}
		}

		@Override
		public SearchConstraint clone() {
			return new PaIntEChannelConstraint(getValue(), getOperator(), getSpecifier());
		}

		@Override
		protected Object getConstraint() {
			return getValue().equals(LanguageConstants.DATA_YES_VALUE);
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.search.constraints.AbstractProsodySyllableConstraint#getInstance(de.ims.icarus.plugins.prosody.search.ProsodyTargetTree, int)
		 */
		@Override
		protected Object getInstance(ProsodyTargetTree tree, int syllable) {

			valueParams.setParams(tree.getSource(), tree.getNodeIndex(), syllable);

			double stepSize = (rightBorder-leftBorder)/resolution;

			double x = leftBorder;

			//TODO make the constraint recognize inside/above/below/crossing

			while(x<=rightBorder) {
				double yTarget = PaIntEUtils.calcY(x, valueParams);
				double yUpper = PaIntEUtils.calcY(x, upperParams);
				double yLower = PaIntEUtils.calcY(x, lowerParams);

				if(yTarget>yUpper || yTarget <yLower) {
					return false;
				}

				x += stepSize;
			}

			return true;
		}
	}

}
