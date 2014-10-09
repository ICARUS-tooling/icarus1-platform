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
package de.ims.icarus.plugins.prosody.annotation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import de.ims.icarus.config.ConfigEvent;
import de.ims.icarus.config.ConfigListener;
import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.config.ConfigRegistry.Handle;
import de.ims.icarus.plugins.prosody.search.ProsodyTargetTree;
import de.ims.icarus.plugins.prosody.search.constraints.SyllableConstraint;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchManager;
import de.ims.icarus.search_tools.annotation.BitmaskHighlighting;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ProsodyHighlighting extends BitmaskHighlighting {


	private static ProsodyHighlighting instance;

	public static ProsodyHighlighting getInstance() {
		// Most times this will be called from the event dispatch thread
		// so we do not need to care much about synchronization.
		// In fact concurrent (re)creation of the shared instance
		// does not matter at all.
		if(instance==null) {
			instance = new ProsodyHighlighting();
		}

		return instance;
	}

	public ProsodyHighlighting() {
		super(DEFAULT_BLOCK_SIZE); // Use default block size of 5 digits

		// Register tokens
		registerToken("form", true, new Color(2807039)); //$NON-NLS-1$
//		registerToken("lemma", true, new Color(2807039)); //$NON-NLS-1$
//		registerToken("pos", true, new Color(2807039)); //$NON-NLS-1$
//		registerToken("features", true, new Color(2807039)); //$NON-NLS-1$
//		registerToken("projectivity", false, new Color(1677593)); //$NON-NLS-1$
//		registerToken("relation", false, new Color(1677593)); //$NON-NLS-1$
//		registerToken("distance", false, new Color(14748812)); //$NON-NLS-1$
//		registerToken("direction", false, new Color(10789924)); //$NON-NLS-1$

		registerToken("wordProperty", true, new Color(0xD49625)); //$NON-NLS-1$
		registerToken("syllableProperty", true, new Color(0xFFD400)); //$NON-NLS-1$
		registerToken("sylCount", true, new Color(0xAA2AFF)); //$NON-NLS-1$
		registerToken("accentShape", true, new Color(0xFF2A7F)); //$NON-NLS-1$
		registerToken("wordPos", true, new Color(0x149314)); //$NON-NLS-1$
		registerAlias("section", "wordPos"); //$NON-NLS-1$ //$NON-NLS-2$
		registerToken("painteIntegral", true, new Color(0x931493)); //$NON-NLS-1$
		registerAlias("painteCurve", "painteIntegral"); //$NON-NLS-1$ //$NON-NLS-2$
		registerAlias("painteAngle", "painteIntegral"); //$NON-NLS-1$ //$NON-NLS-2$
		registerAlias("painteDistance", "painteIntegral"); //$NON-NLS-1$ //$NON-NLS-2$
		registerAlias("painteChannel", "painteIntegral"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected ConfigListener configListener;

	/**
	 *
	 * @see de.ims.icarus.search_tools.annotation.BitmaskHighlighting#loadConfig()
	 */
	@Override
	public synchronized void loadConfig() {

		ConfigRegistry config = ConfigRegistry.getGlobalRegistry();
		Handle group = config.getHandle("plugins.prosody.highlighting"); //$NON-NLS-1$

		if(configListener==null) {
			configListener = new ConfigListener() {

				@Override
				public void invoke(ConfigRegistry sender, ConfigEvent event) {
					loadConfig();
				}
			};
			config.addGroupListener(group, configListener);
		}

		List<String> tokens = new ArrayList<>(tokenColors.keySet());
		for(String token : tokens) {
			Color col = new Color(config.getInteger(config.getChildHandle(group, token+"Highlight"))); //$NON-NLS-1$
			tokenColors.put(token, col);
		}

		transitiveHighlightColor = new Color(config.getInteger(config.getChildHandle(group, "transitiveHighlight"))); //$NON-NLS-1$
		nodeHighlightColor = new Color(config.getInteger(config.getChildHandle(group, "nodeHighlight"))); //$NON-NLS-1$
		edgeHighlightColor = new Color(config.getInteger(config.getChildHandle(group, "edgeHighlight"))); //$NON-NLS-1$
	}

	public long getHighlight(SearchConstraint[] constraints, ProsodyTargetTree targetTree, int sylIndex) {
		long highlight = 0L;

		if(constraints==null) {
			return highlight;
		}

		highlight |= GENERAL_HIGHLIGHT;

		long minGroup = 0L;

		for(SearchConstraint constraint : constraints) {
			if(constraint.isUndefined()) {
				continue;
			}

//			if(!(constraint instanceof SyllableConstraint)) {
//				continue;
//			}

			int offset = getOffset(constraint.getToken());

			if(SearchManager.isGroupingOperator(constraint.getOperator())) {
				long group = (int) constraint.getValue() + 1L;

				if(group>15)
					throw new IllegalArgumentException("Unable to highlight group id: "+group); //$NON-NLS-1$

				if(minGroup==0L) {
					minGroup = group;
				}

				highlight |= (group << (offset+1));
			} else if(!(constraint instanceof SyllableConstraint)
					|| !((SyllableConstraint)constraint).matches(targetTree, sylIndex)) {
				continue;
			}

			highlight |= (1L << offset);
		}

		if(minGroup!=0L) {
			highlight |= (minGroup << GENERAL_OFFSET);
		}

		return highlight;
	}
}
