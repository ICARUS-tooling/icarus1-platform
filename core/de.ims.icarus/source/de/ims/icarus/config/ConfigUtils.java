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
package de.ims.icarus.config;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JColorChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

import de.ims.icarus.config.ConfigRegistry.Handle;
import de.ims.icarus.resources.ResourceDomain;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.GridBagUtil;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.tasks.TaskManager;
import de.ims.icarus.util.Exceptions;
import de.ims.icarus.util.collections.CollectionUtils;


/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ConfigUtils implements ConfigConstants {
	
	private static final Timer timer = new Timer("Config-autosave-timer"); //$NON-NLS-1$
	
	public static void execute(Runnable job) {
		TaskManager.getInstance().execute(job);
	}
	
	public static void schedule(TimerTask task, long delay, long period) {
		timer.scheduleAtFixedRate(task, delay, period);
	}
	
	public static String localizeName(ConfigRegistry config, Handle handle){
		String name = (String) config.getProperty(handle, NAME_KEY);
		
		if (name == null) 
			name = "config." + config.getName(handle); //$NON-NLS-1$
		
		return ResourceManager.getInstance().get(name, config.getName(handle));
	}

	public static String localizeDescription(ConfigRegistry config,
			Handle handle) {
		String desc = (String) config.getProperty(handle, DESCRIPTION_KEY);

		if (desc == null)
			desc = "config.desc." + config.getName(handle); //$NON-NLS-1$
		
		return ResourceManager.getInstance().get(desc, (Object[])null);
	}

	public static String localizeNote(ConfigRegistry config, Handle handle) {
		String note = (String) config.getProperty(handle, NOTE_KEY);

		if (note == null)
			note = "config.note." + config.getName(handle); //$NON-NLS-1$
		
		return ResourceManager.getInstance().get(note, (Object[])null);
	}

	public static String localizeOption(ConfigRegistry config, Handle handle) {
		String name = (String) config.getProperty(handle, NAME_KEY);

		if (name == null)
			name = "config.options" + config.getName(handle); //$NON-NLS-1$
	
		return ResourceManager.getInstance().get(name, config.getName(handle));
	}
	
	public static String[] localizeOptions(ConfigRegistry config, Handle handle, List<?> options) {
		String[] localizedOptions = new String[options.size()];

		@SuppressWarnings("unchecked")
		List<String> optionsKeys = (List<String>) config.getProperty(handle,
				OPTIONS_KEYS);

		String loca;
		for (int i = 0; i < localizedOptions.length; i++) {
			loca = optionsKeys == null ? null : optionsKeys.get(i);
			if (loca == null) {
				loca = options.get(i) instanceof String ? (String) options
						.get(i) : String.valueOf(options.get(i));
				
				localizedOptions[i] = ResourceManager.getInstance().get(
						"config." + loca, null, loca); //$NON-NLS-1$
				continue;
			}
			localizedOptions[i] = ResourceManager.getInstance().get(loca);
		}
		
		return localizedOptions;

	}
	
	public static ListCellRenderer<Object> localizingListCellRenderer = new DefaultListCellRenderer() {

		private static final long serialVersionUID = -3923648933046558508L;

		@Override
		public Component getListCellRendererComponent(JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

			if(!(value instanceof String)) {
				value = String.valueOf(value);
			}
			value = ResourceManager.getInstance().get((String) value);
			
			return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		}
	};
	
	public static void buildFontFamilyOption(ConfigBuilder builder, String defaultFont) {
		if(defaultFont==null) {
			defaultFont = "Dialog"; //$NON-NLS-1$
		}

		Object[] fonts = UIUtil.getFontNames();
		int index = CollectionUtils.indexOf(fonts, defaultFont);
		
		if(index==-1) {
			defaultFont = "Dialog"; //$NON-NLS-1$
			index = CollectionUtils.indexOf(fonts, defaultFont);
//			throw new IllegalArgumentException("Unknown font: "+defaultFont); //$NON-NLS-1$
		}
		
		builder.setProperties(builder.addOptionsEntry("fontFamily", index, fonts), //$NON-NLS-1$
					ConfigConstants.RENDERER, ConfigUtils.fontFamilyRenderer);
	}
	
	public static void buildDefaultFontConfig(ConfigBuilder builder, String defaultFont) {
		buildFontFamilyOption(builder, defaultFont);
		builder.addIntegerEntry("fontSize", 12, 5, 35); //$NON-NLS-1$
		builder.addColorEntry("fontColor", Color.black.getRGB()); //$NON-NLS-1$
		builder.addBooleanEntry("bold", false); //$NON-NLS-1$
		builder.addBooleanEntry("italic", false); //$NON-NLS-1$
	}
	
	public static Font defaultReadFont(Handle handle) {
		ConfigRegistry config = handle.getSource();
		String fontName = config.getString(config.getChildHandle(handle, "fontFamily")); //$NON-NLS-1$
		int fontSize = config.getInteger(config.getChildHandle(handle, "fontSize")); //$NON-NLS-1$
		boolean bold = config.getBoolean(config.getChildHandle(handle, "bold")); //$NON-NLS-1$
		boolean italic = config.getBoolean(config.getChildHandle(handle, "italic")); //$NON-NLS-1$
		
		String style = ""; //$NON-NLS-1$
		if(bold) {
			style += "BOLD"; //$NON-NLS-1$
		} 
		if(italic) {
			style = "ITALIC"; //$NON-NLS-1$
		}
		if(style.isEmpty()) {
			style = "PLAIN"; //$NON-NLS-1$
		}
		
		return Font.decode(String.format("%s-%s-%d", fontName, style, fontSize)); //$NON-NLS-1$
	}

	public static ListCellRenderer<Object> fontFamilyRenderer = new DefaultListCellRenderer() {

		private static final long serialVersionUID = -333044504694220350L;
		
		private Map<String, Font> fonts;
		
		private Font getFont(String name) {
			if(fonts==null)
				fonts = new Hashtable<String, Font>();
			
			Font font = fonts.get(name);
			
			if(font==null) {
				font = getFont();
				font = new Font(name, font.getStyle(), font.getSize());
				fonts.put(name, font);
			}
			
			return font;
		}

		@Override
		public Component getListCellRendererComponent(JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			setFont(getFont((String) value));
			
			return this;
		}
	};
	
	public static final ColorEntryHandler sharedColorEntryHandler = 
		new ColorEntryHandler();
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public static class ColorEntryHandler implements EntryHandler {
		
		protected JColorChooser colorChooser;
		
		protected void buildComponent() {
			colorChooser = new JColorChooser();
		}

		@Override
		public Component getComponent() {
			return colorChooser;
		}

		@Override
		public Object getValue() {
			return colorChooser==null ? null : colorChooser.getColor().getRGB();
		}

		@Override
		public boolean isValueEditable() {
			return true;
		}

		@Override
		public boolean isValueValid() {
			return colorChooser!=null && colorChooser.getColor()!=null;
		}

		@Override
		public void setValue(Object value) {
			Color col = new Color((Integer)value);
			
			if(colorChooser==null)
				buildComponent();
			colorChooser.setColor(col);
		}

		@Override
		public Object newEntry() {
			return 0;
		}
	}
	
	public static final StringEntryHandler sharedStringEntryHandler = 
		new StringEntryHandler();
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public static class StringEntryHandler implements EntryHandler {

		protected boolean valid = false;
		protected JTextField valueField;
		
		protected void buildComponent() {
			valueField = new JTextField(20);
		}

		@Override
		public Component getComponent() {
			return valueField;
		}

		@Override
		public Object getValue() {
			return valid && valueField!=null ? valueField.getText() : null;
		}

		@Override
		public boolean isValueEditable() {
			return true;
		}

		@Override
		public boolean isValueValid() {
			return valid;
		}

		@Override
		public Object newEntry() {
			return ""; //$NON-NLS-1$
		}

		@Override
		public void setValue(Object value) {
			valid = (value instanceof String);
			if(valueField==null)
				buildComponent();
			
			valueField.setText(valid ? (String)value : null);
		}		
	}
	
	public static final NumberEntryHandler<Integer> sharedIntegerEntryHandler =
		new NumberEntryHandler<Integer>(new Integer(0));
	
	public static final NumberEntryHandler<Double> sharedDoubleEntryHandler =
		new NumberEntryHandler<Double>(new Double(0d));
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 * @param <N>
	 */
	public static class NumberEntryHandler<N extends Number> implements EntryHandler {

		protected JFormattedTextField valueField;
		protected final N defaultValue;
		protected boolean valid;
		
		public NumberEntryHandler(N defaultValue) {
			Exceptions.testNullArgument(defaultValue, "defaultValue"); //$NON-NLS-1$
			
			this.defaultValue = defaultValue;
		}
		
		protected void buildComponent() {
			valueField = new JFormattedTextField(defaultValue);
		}

		@Override
		public Component getComponent() {
			return valueField;
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
			return defaultValue;
		}

		@Override
		public Object getValue() {
			return valid && valueField!=null ? valueField.getValue() : null;
		}

		@Override
		public void setValue(Object value) {
			valid = defaultValue.getClass().isAssignableFrom(value.getClass());
			if(valueField==null)
				buildComponent();
			
			valueField.setValue(valid ? value : null);
		}		
	}
	
	public static final MapHandler dummyHandler = new MapHandler() {
		
		@Override
		public void setValue(Object value) {
			// do nothing
		}
		
		@Override
		public Object newEntry() {
			return null;
		}
		
		@Override
		public boolean isValueValid() {
			return true;
		}
		
		@Override
		public boolean isValueEditable() {
			return true;
		}
		
		@Override
		public Object getValue() {
			return null;
		}
		
		@Override
		public Component getComponent() {
			return null;
		}

		@Override
		public String getKey() {
			return null;
		}

		@Override
		public void setKey(String key) {
			// no-op
		}
	};
	
	public static final StringMapHandler sharedStringMapHandler =
		new StringMapHandler();
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public static class StringMapHandler extends StringEntryHandler implements MapHandler {
		
		protected JPanel panel;
		protected JTextField keyField;
		
		@Override
		protected void buildComponent() {
			super.buildComponent();
			ResourceDomain resourceDomain = ResourceManager.getInstance().getGlobalDomain();
			
			panel = new JPanel(GridBagUtil.getLayout());
			
			JLabel label = new JLabel();
			resourceDomain.prepareComponent(label, "key", null); //$NON-NLS-1$
			resourceDomain.addComponent(label);
			GridBagUtil.attachComponent(label, panel, GridBagUtil.makeGbc(0, 0));

			keyField = new JTextField(20);
			GridBagUtil.attachComponent(keyField, panel, GridBagUtil.makeGbcH(1, 0, 1, 1));
			
			label = new JLabel();
			resourceDomain.prepareComponent(label, "value", null); //$NON-NLS-1$
			resourceDomain.addComponent(label);
			GridBagUtil.attachComponent(label, panel, GridBagUtil.makeGbc(0, 1));

			GridBagUtil.attachComponent(valueField, panel, GridBagUtil.makeGbcH(1, 1, 1, 1));
			
			panel.setSize(panel.getPreferredSize());
		}

		@Override
		public String getKey() {
			return keyField.getText();
		}

		@Override
		public void setKey(String key) {
			keyField.setText(key);
		}

		@Override
		public Component getComponent() {
			return panel;
		}	
	}
	
	public static final NumberMapHandler<Integer> sharedIntegerMapHandler =
		new NumberMapHandler<Integer>(new Integer(0));

	public static final NumberMapHandler<Double> sharedDoubleMapHandler =
		new NumberMapHandler<Double>(new Double(0d));
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 * @param <N>
	 */
	public static class NumberMapHandler<N extends Number> extends 
			NumberEntryHandler<N> implements MapHandler {

		protected JPanel panel;
		protected JTextField keyField;
		
		public NumberMapHandler(N defaultValue) {
			super(defaultValue);
		}
		
		@Override
		protected void buildComponent() {
			super.buildComponent();
			ResourceDomain resourceDomain = ResourceManager.getInstance().getGlobalDomain();
			
			panel = new JPanel(GridBagUtil.getLayout());
			
			JLabel label = new JLabel();
			resourceDomain.prepareComponent(label, "key", null); //$NON-NLS-1$
			resourceDomain.addComponent(label);
			GridBagUtil.attachComponent(label, panel, GridBagUtil.makeGbc(0, 0));
			
			keyField = new JTextField(20);
			GridBagUtil.attachComponent(keyField, panel, GridBagUtil.makeGbcH(1, 0, 1, 1));
			
			label = new JLabel();
			resourceDomain.prepareComponent(label, "value", null); //$NON-NLS-1$
			resourceDomain.addComponent(label);
			GridBagUtil.attachComponent(label, panel, GridBagUtil.makeGbc(0, 1));
			
			GridBagUtil.attachComponent(valueField, panel, GridBagUtil.makeGbcH(1, 1, 1, 1));
			
			panel.setSize(panel.getPreferredSize());
		}

		@Override
		public String getKey() {
			return keyField.getText();
		}

		@Override
		public void setKey(String key) {
			keyField.setText(key);
		}

		@Override
		public Component getComponent() {
			return panel;
		}	
	}
	
	public static final ColorMapHandler sharedColorMapHandler = 
		new ColorMapHandler();
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public static class ColorMapHandler extends ColorEntryHandler implements MapHandler {


		protected JPanel panel;
		protected JTextField keyField;
		
		@Override
		protected void buildComponent() {
			super.buildComponent();
			ResourceDomain resourceDomain = ResourceManager.getInstance().getGlobalDomain();
			
			panel = new JPanel(GridBagUtil.getLayout());
			
			JLabel label = new JLabel();
			resourceDomain.prepareComponent(label, "key", null); //$NON-NLS-1$
			resourceDomain.addComponent(label);
			GridBagUtil.attachComponent(label, panel, GridBagUtil.makeGbc(0, 0));
			
			keyField = new JTextField(20);
			GridBagUtil.attachComponent(keyField, panel, GridBagUtil.makeGbcH(1, 0, 1, 1));
			
			label = new JLabel();
			resourceDomain.prepareComponent(label, "value", null); //$NON-NLS-1$
			resourceDomain.addComponent(label);
			GridBagUtil.attachComponent(label, panel, GridBagUtil.makeGbc(0, 1));
			
			GridBagUtil.attachComponent(colorChooser, panel, GridBagUtil.makeGbcH(1, 1, 1, 1));
			
			panel.setSize(panel.getPreferredSize());
		}

		@Override
		public String getKey() {
			return keyField.getText();
		}

		@Override
		public void setKey(String key) {
			keyField.setText(key);
		}

		@Override
		public Component getComponent() {
			return panel;
		}		
	}
}
