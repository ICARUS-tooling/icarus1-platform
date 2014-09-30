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
package de.ims.icarus.model.standard.rasterization;

import javax.swing.Icon;

import de.ims.icarus.model.api.layer.FragmentLayer;
import de.ims.icarus.model.api.members.Markable;
import de.ims.icarus.model.api.raster.Axis;
import de.ims.icarus.model.api.raster.Position;
import de.ims.icarus.model.api.raster.Rasterizer;
import de.ims.icarus.model.standard.elements.Positions.Position1D;
import de.ims.icarus.resources.ResourceManager;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CharacterRasterizer1D implements Rasterizer {

	private static final Axis sharedAxis = new CharacterAxis();
	private final boolean invertAxis;

	public CharacterRasterizer1D(boolean invertAxis) {
		this.invertAxis = invertAxis;
	}

	/**
	 * @return the invertAxis
	 */
	public boolean isInvertAxis() {
		return invertAxis;
	}

	private void checkAxis(int axis) {
		if(axis!=0)
			throw new IllegalArgumentException("Invalid sharedAxis: "+axis); //$NON-NLS-1$
	}

	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Position o1, Position o2) {
		if(invertAxis) {
			return -((Position1D)o1).compareTo((Position1D)o2);
		} else {
			return ((Position1D)o1).compareTo((Position1D)o2);
		}
	}

	/**
	 * @see de.ims.icarus.model.api.raster.Rasterizer#getAxisCount()
	 */
	@Override
	public int getAxisCount() {
		return 1;
	}

	/**
	 * @see de.ims.icarus.model.api.raster.Rasterizer#getAxisAt(int)
	 */
	@Override
	public Axis getAxisAt(int index) {
		checkAxis(index);

		return sharedAxis;
	}

	/**
	 * @see de.ims.icarus.model.api.raster.Rasterizer#getRasterSize(de.ims.icarus.model.api.members.Markable, de.ims.icarus.model.api.layer.FragmentLayer, java.lang.Object, int)
	 */
	@Override
	public long getRasterSize(Markable markable, FragmentLayer layer,
			Object value, int axis) {
		checkAxis(axis);

		CharSequence s = (CharSequence) value;

		return s==null ? 0 : s.length();
	}

	/**
	 * @see de.ims.icarus.model.api.raster.Rasterizer#getGranularity(int)
	 */
	@Override
	public long getGranularity(int axis) {
		checkAxis(axis);

		return 1;
	}

	/**
	 * @see de.ims.icarus.model.api.raster.Rasterizer#createPosition(long[])
	 */
	@Override
	public Position createPosition(long... values) {
		return new Position1D(values[0]);
	}

//	/**
//	 * @see de.ims.icarus.model.api.raster.Rasterizer#getFragmentLayer()
//	 */
//	@Override
//	public FragmentLayer getFragmentLayer() {
//		// TODO Auto-generated method stub
//		return null;
//	}

	private static class CharacterAxis implements Axis {

		/**
		 * @see de.ims.icarus.util.id.Identity#getId()
		 */
		@Override
		public String getId() {
			return getClass().getName();
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getName()
		 */
		@Override
		public String getName() {
			return ResourceManager.getInstance().get(
					"plugins.model.characterRasterizer.axis.name"); //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getDescription()
		 */
		@Override
		public String getDescription() {
			return ResourceManager.getInstance().get(
					"plugins.model.characterRasterizer.axis.description"); //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getIcon()
		 */
		@Override
		public Icon getIcon() {
			return null;
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getOwner()
		 */
		@Override
		public Object getOwner() {
			return this;
		}

		/**
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(Axis o) {
			if(o instanceof CharacterAxis) {
				return 0;
			}

			throw new IllegalArgumentException("Cannot compare to foreign axis"); //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus.model.api.raster.Axis#getMaxValue()
		 */
		@Override
		public long getMaxValue() {
			return Long.MAX_VALUE;
		}

		/**
		 * @see de.ims.icarus.model.api.raster.Axis#getMinValue()
		 */
		@Override
		public long getMinValue() {
			return 0;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return getName();
		}

	}
}
