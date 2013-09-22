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
package de.ims.icarus.language.coref.annotation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import de.ims.icarus.config.ConfigEvent;
import de.ims.icarus.config.ConfigListener;
import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.config.ConfigRegistry.Handle;
import de.ims.icarus.search_tools.annotation.BitmaskHighlighting;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferenceDocumentHighlighting extends BitmaskHighlighting {

	private static CoreferenceDocumentHighlighting instance;
	
	public static CoreferenceDocumentHighlighting getInstance() {
		// Most times this will be called from the event dispatch thread
		// so we do not need to care much about synchronization.
		// In fact concurrent (re)creation of the shared instance
		// does not matter at all.
		if(instance==null) {
			instance = new CoreferenceDocumentHighlighting();
		}
		
		return instance;
	}

	public CoreferenceDocumentHighlighting() {
		super(DEFAULT_BLOCK_SIZE); // Use default block size of 5 digits
		
		// Register tokens
		registerToken("begin", true, new Color(2807039)); //$NON-NLS-1$
		registerToken("end", true, new Color(2807039)); //$NON-NLS-1$
		registerToken("range", true, new Color(2807039)); //$NON-NLS-1$
		registerToken("id", true, new Color(2807039)); //$NON-NLS-1$
		registerToken("direction", false, new Color(10789924)); //$NON-NLS-1$
		registerToken("spanProperty", true, new Color(1677593)); //$NON-NLS-1$
		registerToken("sentenceProperty", true, new Color(2677593)); //$NON-NLS-1$
		registerToken("edgeProperty", false, new Color(3677593)); //$NON-NLS-1$
	}
	
	protected ConfigListener configListener;
	
	/**
	 * 
	 * @see de.ims.icarus.search_tools.annotation.BitmaskHighlighting#loadConfig()
	 */
	@Override
	public synchronized void loadConfig() {
		
		ConfigRegistry config = ConfigRegistry.getGlobalRegistry();
		Handle group = config.getHandle("plugins.coref.highlighting"); //$NON-NLS-1$
		
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
}
