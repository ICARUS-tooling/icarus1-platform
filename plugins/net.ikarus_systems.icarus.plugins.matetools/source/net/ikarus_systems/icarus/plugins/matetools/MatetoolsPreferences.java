/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.matetools;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import net.ikarus_systems.icarus.config.ConfigBuilder;
import net.ikarus_systems.icarus.config.ConfigConstants;
import net.ikarus_systems.icarus.config.ConfigRegistry.EntryType;
import net.ikarus_systems.icarus.config.EntryHandler;
import net.ikarus_systems.icarus.plugins.matetools.parser.ModelStorage;
import net.ikarus_systems.icarus.ui.dialog.FormBuilder;
import net.ikarus_systems.icarus.util.location.Location;
import net.ikarus_systems.icarus.util.location.Locations;

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
