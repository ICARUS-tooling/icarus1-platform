/*
 * $Revision: 7 $
 * $Date: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/ui/GridBagUtil.java $
 *
 * $LastChangedDate: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $ 
 * $LastChangedRevision: 7 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.ui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;

/**
 * 
 * @author Markus Gärtner 
 * @version $Id: GridBagUtil.java 7 2013-02-27 13:18:56Z mcgaerty $
 *
 */
public final class GridBagUtil {
	
	private GridBagUtil() {
		// no-op
	}

	/** */
	private static GridBagLayout layout = new GridBagLayout();

	/** */
	public static GridBagLayout getLayout() {
		return layout;
	}

	/** */
	public static void setLayout(GridBagLayout layout) {
		GridBagUtil.layout = layout;
	}

	/** */
	public static final GridBagConstraints getDefaultConstraints() {
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 1.0;
		c.weighty = 1.0;

		return c;
	}

	public static final GridBagConstraints gbcRight(GridBagConstraints gbc,
			int weightx) {
		gbc.weightx = weightx;
		gbc.anchor = GridBagConstraints.EAST;
		return gbc;
	}

	public static final GridBagConstraints gbcLeft(GridBagConstraints gbc,
			int weightx) {
		gbc.weightx = weightx;
		gbc.anchor = GridBagConstraints.WEST;
		return gbc;
	}

	public static final GridBagConstraints gbcTop(GridBagConstraints gbc,
			int weighty) {
		gbc.weighty = weighty;
		gbc.anchor = GridBagConstraints.NORTH;
		return gbc;
	}

	public static final GridBagConstraints gbcBottom(GridBagConstraints gbc,
			int weighty) {
		gbc.weighty = weighty;
		gbc.anchor = GridBagConstraints.SOUTH;
		return gbc;
	}

	public static final GridBagConstraints gbcCenter(GridBagConstraints gbc) {
		gbc.weightx = 100;
		gbc.anchor = GridBagConstraints.CENTER;
		return gbc;
	}

	/**
	 * Makes a <tt>GridBagConstraints</tt> object that has the provided grid
	 * properties, an inset of 1 in every direction and an <tt>NORTHWEST</tt>
	 * anchor.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param fill
	 * @return
	 */
	public static final GridBagConstraints makeGbc(int x, int y, int width,
			int height, int fill) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = width;
		gbc.gridheight = height;
		gbc.fill = fill;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(1, 1, 1, 1);
		return gbc;
	}

	/**
	 * Makes a <tt>GridBagConstraints</tt> object that does not resize.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return
	 */
	public static final GridBagConstraints makeGbc(int x, int y, int width,
			int height) {
		return makeGbc(x, y, width, height, GridBagConstraints.NONE);
	}
	
	public static final GridBagConstraints makeGbc(int x, int y) {
		return makeGbc(x, y, 1, 1, GridBagConstraints.NONE);
	}

	/**
	 * Makes a <tt>GridBagConstraints</tt> object that has no insets and does
	 * not resize.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return
	 */
	public static final GridBagConstraints makeGbcN(int x, int y, int width,
			int height) {
		GridBagConstraints gbc = makeGbc(x, y, width, height);
		gbc.insets = new Insets(0, 0, 0, 0);
		return gbc;

	}

	/**
	 * Makes a <tt>GridBagConstraints</tt> object that has no insets and resizes
	 * as specified.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param fill
	 * @return
	 */
	public static final GridBagConstraints makeGbcN(int x, int y, int width,
			int height, int fill) {
		GridBagConstraints gbc = makeGbc(x, y, width, height, fill);
		gbc.insets = new Insets(0, 0, 0, 0);
		return gbc;

	}

	/**
	 * Makes a <tt>GridBagConstraints</tt> object that has weight 100 for both x
	 * and y and resizes in no direction.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return
	 */
	public static final GridBagConstraints makeGbcW(int x, int y, int width,
			int height) {
		return makeGbcW(x, y, width, height, GridBagConstraints.NONE);
	}

	/**
	 * Makes a <tt>GridBagConstraints</tt> object that has weight of 100 for
	 * both x and y.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param fill
	 * @return
	 */
	public static final GridBagConstraints makeGbcW(int x, int y, int width,
			int height, int fill) {
		GridBagConstraints gbc = makeGbc(x, y, width, height, fill);
		gbc.weightx = 100;
		gbc.weighty = 100;
		return gbc;
	}

	/**
	 * Makes a <tt>GridBagConstraints</tt> object that resizes vertically with a
	 * weight of 100 and insets of 1.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return
	 */
	public static final GridBagConstraints makeGbcV(int x, int y, int width,
			int height) {
		GridBagConstraints gbc = makeGbc(x, y, width, height);
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.weighty = 100;
		return gbc;
	}

	/**
	 * Makes a <tt>GridBagConstraints</tt> object that resizes vertically with a
	 * weight of 100 and no insets.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return
	 */
	public static final GridBagConstraints makeGbcVN(int x, int y, int width,
			int height) {
		GridBagConstraints gbc = makeGbcN(x, y, width, height);
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.weighty = 100;
		return gbc;
	}

	/**
	 * Makes a <tt>GridBagConstraints</tt> object that resizes horizontally with
	 * a weight of 100 and insets of 1.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return
	 */
	public static final GridBagConstraints makeGbcH(int x, int y, int width,
			int height) {
		GridBagConstraints gbc = makeGbc(x, y, width, height,
				GridBagConstraints.HORIZONTAL);
		gbc.weightx = 100;
		return gbc;
	}

	/**
	 * Makes a <tt>GridBagConstraints</tt> object that resizes horizontally with
	 * a weight of 100 and no insets.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return
	 */
	public static final GridBagConstraints makeGbcHN(int x, int y, int width,
			int height) {
		GridBagConstraints gbc = makeGbcN(x, y, width, height,
				GridBagConstraints.HORIZONTAL);
		gbc.weightx = 100;
		return gbc;
	}

	/**
	 * Makes a <tt>GridBagConstraints</tt> object that resizes in all directions
	 * with a weight of 100 each and insets of 1.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return
	 */
	public static final GridBagConstraints makeGbcR(int x, int y, int width,
			int height) {
		return makeGbcW(x, y, width, height, GridBagConstraints.BOTH);
	}

	/**
	 * Makes a <tt>GridBagConstraints</tt> object that resizes in all directions
	 * with a weight of 100 each and no insets.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return
	 */
	public static final GridBagConstraints makeGbcRN(int x, int y, int width,
			int height) {
		GridBagConstraints gbc = makeGbcW(x, y, width, height,
				GridBagConstraints.BOTH);
		gbc.insets = new Insets(0, 0, 0, 0);
		return gbc;
	}

	public static final void attachComponent(Component compSrc,
			JComponent compDst, GridBagConstraints c) {
		if (compSrc == null)
			throw new IllegalArgumentException("Invalid source component."); //$NON-NLS-1$
		if (compDst == null)
			throw new IllegalArgumentException("Invalid destination component."); //$NON-NLS-1$
		if (c == null)
			throw new IllegalArgumentException("Invalid GridBagConstraints."); //$NON-NLS-1$

		// First removing the provided component
		layout.removeLayoutComponent(compSrc);
		compDst.remove(compSrc);

		// Now again add component
		layout.setConstraints(compSrc, c);
		compDst.add(compSrc);
	}

	public static final void attachComponent(Component compSrc,
			JComponent compDst) {
		attachComponent(compSrc, compDst, GridBagUtil.getDefaultConstraints());
	}
}
