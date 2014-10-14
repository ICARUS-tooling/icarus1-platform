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
import de.ims.icarus.plugins.prosody.painte.PaIntEConstraintParams;
import de.ims.icarus.plugins.prosody.painte.PaIntEParamsWrapper;
import de.ims.icarus.plugins.prosody.search.constraints.AbstractProsodySyllableConstraint;
import de.ims.icarus.plugins.prosody.ui.view.editor.PaIntERegistry;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.util.ConstraintException;
import de.ims.icarus.util.id.UnknownIdentifierException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class BoundedSyllableConstraint extends AbstractProsodySyllableConstraint {

	private static final long serialVersionUID = -3749612494118909584L;

	protected transient double leftBorder, rightBorder;
	protected transient int resolution;

	protected BoundedSyllableConstraint(String token, Object value,
			SearchOperator operator, Object specifier) {
		super(token, value, operator, specifier);
	}

	/**
	 * @see de.ims.icarus.search_tools.standard.DefaultConstraint#init()
	 */
	@Override
	protected void init() {

		String configPath = getConfigPath();

		if(configPath==null) {
			leftBorder = 0.0;
			rightBorder = 1.0;
			resolution = 30;
			return;
		}

		ConfigRegistry registry = ConfigRegistry.getGlobalRegistry();
		Handle handle = registry.getHandle(configPath);

		leftBorder = registry.getDouble(registry.getChildHandle(handle, "leftBorder")); //$NON-NLS-1$
		rightBorder = registry.getDouble(registry.getChildHandle(handle, "rightBorder")); //$NON-NLS-1$
		resolution = registry.getInteger(registry.getChildHandle(handle, "resolution")); //$NON-NLS-1$
	}

	protected abstract String getConfigPath();

	public double getLeftBorder() {
		return leftBorder;
	}

	public double getRightBorder() {
		return rightBorder;
	}

	public int getResolution() {
		return resolution;
	}

	public void setLeftBorder(double leftBorder) {
		this.leftBorder = leftBorder;
	}

	public void setRightBorder(double rightBorder) {
		this.rightBorder = rightBorder;
	}

	public void setResolution(int resolution) {
		this.resolution = resolution;
	}

	public static void parseParams(String s, PaIntEConstraintParams constraints) {
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

	protected void parseConstraint(String s, PaIntEConstraintParams...constraints) {

		int constraintCount = constraints.length;

		String[] parts = s.split(";"); //$NON-NLS-1$

		if(parts.length<constraintCount)
			throw new IllegalArgumentException("Invalid channel parts - need at least "+constraintCount+" sets of painte parameters separated by semicolon: "+s); //$NON-NLS-1$ //$NON-NLS-2$

		for(int i=0; i<constraintCount; i++) {
			parseParams(parts[i], constraints[i]);
		}

		if(parts.length>constraintCount && !parts[constraintCount].isEmpty()) {
			leftBorder = Double.parseDouble(parts[constraintCount]);
		}

		constraintCount++;
		if(parts.length>constraintCount && !parts[constraintCount].isEmpty()) {
			rightBorder = Double.parseDouble(parts[constraintCount]);
		}

		constraintCount++;
		if(parts.length>constraintCount && !parts[constraintCount].isEmpty()) {
			resolution = Integer.parseInt(parts[constraintCount]);
		}

		if(leftBorder<-3)
			throw new ConstraintException("Left border if PaIntE-Channel constraint is too small (min -3): "+leftBorder); //$NON-NLS-1$

		if(rightBorder>3)
			throw new ConstraintException("Left border if PaIntE-Channel constraint is too big (max 3): "+rightBorder); //$NON-NLS-1$

		if(leftBorder>=rightBorder)
			throw new ConstraintException("Invalid interval defined for PaIntE-Channel constraint (left border must be less then right one): "+leftBorder+" to "+rightBorder); //$NON-NLS-1$ //$NON-NLS-2$

		if(resolution<10 || resolution>1000)
			throw new ConstraintException("Resolution is out of bounds (10 to 1000): "+resolution); //$NON-NLS-1$
	}

}
