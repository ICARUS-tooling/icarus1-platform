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
package de.ims.icarus.plugins.matetools;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import de.ims.icarus.config.ConfigBuilder;
import de.ims.icarus.config.ConfigConstants;
import de.ims.icarus.config.ConfigRegistry.EntryType;
import de.ims.icarus.config.EntryHandler;
import de.ims.icarus.plugins.matetools.parser.ModelStorage;
import de.ims.icarus.ui.dialog.FormBuilder;
import de.ims.icarus.util.location.Location;
import de.ims.icarus.util.location.Locations;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class MatetoolsPreferences {

	public MatetoolsPreferences() {
		ConfigBuilder builder = new ConfigBuilder();

		// PLUGINS GROUP
		builder.addGroup("plugins", true); //$NON-NLS-1$
		// MATETOOLS GROUP
		builder.addGroup("matetools", true); //$NON-NLS-1$
		// PARSER GROUP
		builder.addGroup("parser", true); //$NON-NLS-1$
		
		builder.addBooleanEntry("verbose", false); //$NON-NLS-1$
		builder.addBooleanEntry("useParser", true); //$NON-NLS-1$
		builder.addBooleanEntry("useTagger", true); //$NON-NLS-1$
		builder.addBooleanEntry("useLemmatizer", true); //$NON-NLS-1$
		builder.addBooleanEntry("useMorphTagger", true); //$NON-NLS-1$
		builder.addBooleanEntry("doUppercaseLemmas", false); //$NON-NLS-1$
		builder.addBooleanEntry("fastRelease", false); //$NON-NLS-1$
		builder.setProperties(builder.addIntegerEntry("maxCores", 0),  //$NON-NLS-1$
				ConfigConstants.NOTE_KEY, "config.matetools.maxCores.note"); //$NON-NLS-1$
		builder.setProperties(builder.addListEntry(
				"models", EntryType.CUSTOM), //$NON-NLS-1$
				ConfigConstants.HANDLER, sharedStorageEditor,
				ConfigConstants.RENDERER, modelStorageRenderer);
		
//		builder.addGroup("conll09reader", true); //$NON-NLS-1$
//		builder.addOptionsEntry("inputType", 0, //$NON-NLS-1$
//				"gold", "system"); //$NON-NLS-1$ //$NON-NLS-2$
	
		// TODO add config items for readers/writers?
	}

	
	public static final ListCellRenderer<?> modelStorageRenderer = new DefaultListCellRenderer() {

		private static final long serialVersionUID = 3600419316946236085L;

		@Override
		public Component getListCellRendererComponent(JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
			
			if(value instanceof ModelStorage) {
				value = ((ModelStorage)value).getLanguage();
			}
			
			return super.getListCellRendererComponent(
					list, value, index, isSelected, cellHasFocus);
		}		
	};
	
	public static final ModelStorageEditor sharedStorageEditor = new ModelStorageEditor();
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public static class ModelStorageEditor implements EntryHandler {
		protected ModelStorage entry;		
		protected FormBuilder formBuilder;

		protected void buildPanel() {
			
			// LANGUAGE
			formBuilder.addInputFormEntry("language",  //$NON-NLS-1$
					"plugins.matetools.parserModelEditor.languageLabel"); //$NON-NLS-1$
			// PARSER MODEL
			formBuilder.addLocationFormEntry("parserModel",  //$NON-NLS-1$
					"plugins.matetools.parserModelEditor.parserModelLabel"); //$NON-NLS-1$
			// TAGGER MODEL
			formBuilder.addLocationFormEntry("taggerModel",  //$NON-NLS-1$
					"plugins.matetools.parserModelEditor.taggerModelLabel"); //$NON-NLS-1$
			// LEMMATIZER MODEL
			formBuilder.addLocationFormEntry("lemmatizerModel",  //$NON-NLS-1$
					"plugins.matetools.parserModelEditor.lemmatizerModelLabel"); //$NON-NLS-1$
			// MORPH_TAGGER MODEL
			formBuilder.addLocationFormEntry("morphTaggerModel",  //$NON-NLS-1$
					"plugins.matetools.parserModelEditor.morphTaggerModelLabel"); //$NON-NLS-1$
			
			formBuilder.buildForm();
		}

		@Override
		public void setValue(Object value) {
			entry = (ModelStorage) value;
			refresh();
		}
		
		protected void refresh() {
			if(entry!=null && formBuilder!=null) {
				formBuilder.setValue("language", entry.getLanguage()); //$NON-NLS-1$
				formBuilder.setValue("parserModel",  //$NON-NLS-1$
						Locations.getFileLocation(entry.getParserModelPath())); 
				formBuilder.setValue("taggerModel",  //$NON-NLS-1$
						Locations.getFileLocation(entry.getTaggerModelPath())); 
				formBuilder.setValue("lemmatizerModel",  //$NON-NLS-1$
						Locations.getFileLocation(entry.getLemmatizerModelPath())); 
				formBuilder.setValue("morphTaggerModel",  //$NON-NLS-1$
						Locations.getFileLocation(entry.getMorphTaggerModelPath())); 
			}
		}

		@Override
		public Object getValue() {
			if(entry!=null && formBuilder!=null) {
				read();
			}
			
			return entry;
		}
		
		protected void read() {
			entry.setLanguage((String) formBuilder.getValue("language")); //$NON-NLS-1$
			entry.setParserModelPath(Locations.getPath(
					(Location) formBuilder.getValue("parserModel"))); //$NON-NLS-1$
			entry.setTaggerModelPath(Locations.getPath(
					(Location) formBuilder.getValue("taggerModel"))); //$NON-NLS-1$
			entry.setLemmatizerModelPath(Locations.getPath(
					(Location) formBuilder.getValue("lemmatizerModel"))); //$NON-NLS-1$
			entry.setMorphTaggerModelPath(Locations.getPath(
					(Location) formBuilder.getValue("morphTaggerModel"))); //$NON-NLS-1$
		}

		@Override
		public Component getComponent() {
			if(formBuilder==null) {
				formBuilder = FormBuilder.newLocalizingBuilder();
				buildPanel();
				refresh();
			}
			return formBuilder.getContainer();
		}

		@Override
		public boolean isValueEditable() {
			return true;
		}

		@Override
		public boolean isValueValid() {
			return true;
		}

		@Override
		public Object newEntry() {
			return new ModelStorage();
		}		
	}
}
