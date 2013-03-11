/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.config;

import java.awt.Color;
import java.awt.Component;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JColorChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

import net.ikarus_systems.icarus.config.ConfigRegistry.Handle;
import net.ikarus_systems.icarus.resources.ResourceDomain;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.GridBagUtil;
import net.ikarus_systems.icarus.util.Exceptions;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ConfigUtils implements ConfigConstants {

	private static final ExecutorService executor = Executors.newSingleThreadExecutor();
	
	private static final Timer timer = new Timer("Config-autosave-timer"); //$NON-NLS-1$
	
	public static void execute(Runnable job) {
		executor.execute(job);
	}
	
	public static void schedule(TimerTask task, long delay, long period) {
		timer.scheduleAtFixedRate(task, delay, period);
	}

	
	
	public static String localizeName(ConfigRegistry config, Handle handle){
		String name = (String) config.getProperty(handle, NAME_KEY);
		
		if (name == null) 
			name = "config." + config.getName(handle); //$NON-NLS-1$
		
		return ResourceManager.getInstance().get(name, null, config.getName(handle));
	}

	public static String localizeDescription(ConfigRegistry config,
			Handle handle) {
		String desc = (String) config.getProperty(handle, DESCRIPTION_KEY);

		if (desc == null)
			desc = "config.desc." + config.getName(handle); //$NON-NLS-1$
		
		return ResourceManager.getInstance().get(desc, null, null);
	}

	public static String localizeNote(ConfigRegistry config, Handle handle) {
		String note = (String) config.getProperty(handle, NOTE_KEY);

		if (note == null)
			note = "config.note." + config.getName(handle); //$NON-NLS-1$
		
		return ResourceManager.getInstance().get(note, null, null);
	}

	public static String localizeOption(ConfigRegistry config, Handle handle) {
		String name = (String) config.getProperty(handle, NAME_KEY);

		if (name == null)
			name = "config.options" + config.getName(handle); //$NON-NLS-1$
	
		return ResourceManager.getInstance().get(name, null, config.getName(handle));
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