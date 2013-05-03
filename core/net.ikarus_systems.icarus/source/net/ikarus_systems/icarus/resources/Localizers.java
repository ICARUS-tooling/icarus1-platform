/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.resources;

import java.awt.Dialog;
import java.awt.Frame;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.util.Exceptions;

/**
 * Collection of common {@code Localizer} instances used for
 * various objects.
 * 
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public final class Localizers {
	
	private Localizers() {
		// no-op
	}

	/**
	 * A {@code Localizer} that does nothing.
	 */
	public static class EmptyLocalizer implements Localizer {

		@Override
		public void localize(Object item) {
			// no-op
		}

	};
	
	public static final EmptyLocalizer emptyLocalizer = new EmptyLocalizer();

	/**
	 * A {@code Localizer} that does nothing.
	 */
	public static class EmptyRegisteringLocalizer implements RegisteringLocalizer {

		@Override
		public void localize(Object item) {
			// no-op
		}

		/**
		 * @see net.ikarus_systems.icarus.resources.RegisteringLocalizer#register(java.lang.Object, java.lang.Object)
		 */
		@Override
		public void register(Object item, Object data) {
			// no-op
		}

	};
	
	public static final RegisteringLocalizer emptyRegisteringLocalizer = new EmptyRegisteringLocalizer();
	
	public static abstract class DomainAwareLocalizer implements Localizer {
		protected final ResourceDomain domain;
		
		protected DomainAwareLocalizer(ResourceDomain domain) {
			Exceptions.testNullArgument(domain, "domain"); //$NON-NLS-1$
			
			this.domain = domain;
		}

		
		protected String tooltipString(String key) {
			String tooltip = domain.get(key, (String)null);
			if (tooltip == null || tooltip.isEmpty())
				return null;

			return tooltip;
		}
		
		protected String textString(String key) {
			return domain.get(key);
		}
	}

	/**
	 * A {@code Localizer} that localizes objects of type
	 * {@link JLabel} using the {@link JLabel#setText(String)}
	 * and {@link JComponent#setToolTipText(String)} methods. 
	 * Keys for both the {@code text} and {@code tooltip} 
	 * {@code String}s are obtained from the components
	 * {@link JComponent#getClientProperty(Object)} with
	 * {@link ResourceConstants#DEFAULT_TEXT_KEY} and 
	 * {@link ResourceConstants#DEFAULT_DESCRIPTION_KEY} as keys.
	 * 
	 * @see JComponent#putClientProperty(Object, Object)
	 * @see JLabel#setText(String)
	 */
	public static class LabelLocalizer extends DomainAwareLocalizer {

		/**
		 * @param domain
		 */
		public LabelLocalizer(ResourceDomain domain) {
			super(domain);
		}

		@Override
		public void localize(Object item) {
			JLabel label = (JLabel) item;
			String key = (String) label.getClientProperty(ResourceConstants.DEFAULT_TEXT_KEY);
			if (key != null)
				label.setText(textString(key));

			key = (String) label.getClientProperty(ResourceConstants.DEFAULT_DESCRIPTION_KEY);
			if (key != null)
				label.setToolTipText(tooltipString(key));
		}

	};
	
	public static final Localizer labelLocalizer = new LabelLocalizer(ResourceManager.getInstance().getGlobalDomain());

	/**
	 * A {@code Localizer} that localizes objects of type
	 * {@link AbstractButton} using the {@link AbstractButton#setText(String)}
	 * and {@link JComponent#setToolTipText(String)} methods. 
	 * Keys for both the {@code text} and {@code tooltip} 
	 * {@code String}s are obtained from the components
	 * {@link JComponent#getClientProperty(Object)} with
	 * {@link ResourceConstants#DEFAULT_TEXT_KEY} and 
	 * {@link ResourceConstants#DEFAULT_DESCRIPTION_KEY} as keys.
	 * 
	 * @see JComponent#putClientProperty(Object, Object)
	 * @see AbstractButton#setText(String)
	 * @see JComponent#setToolTipText(String)
	 */
	public static class ButtonLocalizer extends DomainAwareLocalizer {

		/**
		 * @param domain
		 */
		public ButtonLocalizer(ResourceDomain domain) {
			super(domain);
		}

		@Override
		public void localize(Object item) {
			AbstractButton button = (AbstractButton) item;
			String key = (String) button.getClientProperty(ResourceConstants.DEFAULT_TEXT_KEY);
			if (key != null)
				button.setText(textString(key));

			key = (String) button.getClientProperty(ResourceConstants.DEFAULT_DESCRIPTION_KEY);
			if (key != null)
				button.setToolTipText(tooltipString(key));
		}

	};
	
	public static final Localizer buttonLocalizer = new ButtonLocalizer(ResourceManager.getInstance().getGlobalDomain());

	/**
	 * A {@code Localizer} that localizes objects of type
	 * {@link JPopupMenu} using the {@link JPopupMenu#setLabel(String)}
	 * and {@link JComponent#setToolTipText(String)} methods. 
	 * Keys for both the {@code text} and {@code tooltip} 
	 * {@code String}s are obtained from the components
	 * {@link JComponent#getClientProperty(Object)} with
	 * {@link ResourceConstants#DEFAULT_TEXT_KEY} and 
	 * {@link ResourceConstants#DEFAULT_DESCRIPTION_KEY} as keys.
	 * 
	 * @see JComponent#putClientProperty(Object, Object)
	 * @see JPopupMenu#setLabel(String)
	 * @see JComponent#setToolTipText(String)
	 */
	public static class PopupLocalizer extends DomainAwareLocalizer {

		/**
		 * @param domain
		 */
		public PopupLocalizer(ResourceDomain domain) {
			super(domain);
		}

		@Override
		public void localize(Object item) {
			JPopupMenu menu = (JPopupMenu)item;
			String key = (String) menu.getClientProperty(ResourceConstants.DEFAULT_TEXT_KEY);
			if (key != null)
				menu.setLabel(textString(key));

			key = (String) menu.getClientProperty(ResourceConstants.DEFAULT_DESCRIPTION_KEY);
			if (key != null)
				menu.setToolTipText(tooltipString(key));
		}

	};
	
	public static final Localizer popupLocalizer = new PopupLocalizer(ResourceManager.getInstance().getGlobalDomain());

	/**
	 * A {@code Localizer} that localizes objects of type
	 * {@link JTextComponent} using the {@link JTextComponent#setLabel(String)}
	 * and {@link JComponent#setToolTipText(String)} methods. 
	 * Keys for both the {@code text} and {@code tooltip} 
	 * {@code String}s are obtained from the components
	 * {@link JComponent#getClientProperty(Object)} with
	 * {@link ResourceConstants#DEFAULT_TEXT_KEY} and 
	 * {@link ResourceConstants#DEFAULT_DESCRIPTION_KEY} as keys.
	 * 
	 * @see JComponent#putClientProperty(Object, Object)
	 * @see JTextComponent#setText(String)
	 * @see JComponent#setToolTipText(String)
	 */
	public static class TextComponentLocalizer extends DomainAwareLocalizer {

		/**
		 * @param domain
		 */
		public TextComponentLocalizer(ResourceDomain domain) {
			super(domain);
		}

		@Override
		public void localize(Object item) {
			JTextComponent comp = (JTextComponent) item;
			String key = (String) comp.getClientProperty(ResourceConstants.DEFAULT_TEXT_KEY);
			if (key != null)
				comp.setText(textString(key));

			key = (String) comp.getClientProperty(ResourceConstants.DEFAULT_DESCRIPTION_KEY);
			if (key != null)
				comp.setToolTipText(tooltipString(key));
		}

	};

	public static final Localizer textComponentLocalizer = new TextComponentLocalizer(ResourceManager.getInstance().getGlobalDomain());

	/**
	 * A {@code Localizer} that localizes objects of type
	 * {@link Action}. It uses the strings stored in the
	 * {@code Action} object for the keys {@link ResourceConstants#DEFAULT_TEXT_KEY}
	 * and {@link ResourceConstants#DEFAULT_DESCRIPTION_KEY} to fetch
	 * the localized text from the {@code ResourceManager} and stores those
	 * {@code String}s back into the {@code Action} using
	 * {@link Action#putValue(String, Object)} with 
	 * {@link Action#NAME} and {@link Action#SHORT_DESCRIPTION} as
	 * keys.
	 * 
	 * @see Action#getValue(String)
	 * @see Action#putValue(String, Object)
	 */
	public static class ActionLocalizer extends DomainAwareLocalizer {

		/**
		 * @param domain
		 */
		public ActionLocalizer(ResourceDomain domain) {
			super(domain);
		}

		@Override
		public void localize(Object item) {
			Action action = (Action) item;
			String key = (String) action.getValue(ResourceConstants.DEFAULT_TEXT_KEY);
			if (key != null)
				action.putValue(Action.NAME, textString(key));

			key = (String) action.getValue(ResourceConstants.DEFAULT_DESCRIPTION_KEY);
			if (key != null)
				action.putValue(Action.SHORT_DESCRIPTION, tooltipString(key));
		}

	};

	public static final Localizer actionLocalizer = new ActionLocalizer(ResourceManager.getInstance().getGlobalDomain());

	/**
	 * A {@code RegisteringLocalizer} that internally maps
	 * {@code Frame} objects to {@code String} keys that are used
	 * to fetch localized {@code String} objects when localizing
	 * a certain {@code Frame} via {@link Frame#setTitle(String)}.
	 * Note: Only weak references are stored within this {@code RegisteringLocalizer}
	 * so it is necessary to hold an external reference to a registered
	 * {@code Frame} in order to make the provided localization {@code data}
	 * persistent!
	 */
	public static class FrameLocalizer extends DomainAwareLocalizer implements RegisteringLocalizer {

		/**
		 * @param domain
		 */
		public FrameLocalizer(ResourceDomain domain) {
			super(domain);
		}

		private Map<Frame, String> frames = new WeakHashMap<>();

		@Override
		public void localize(Object item) {
			Frame frame = (Frame) item;
			String key = frames.get(item);
			if (key != null)
				frame.setTitle(textString(key));
		}

		@Override
		public void register(Object item, Object data) {
			frames.put((Frame) item, (String) data);
		}

	};

	public static final RegisteringLocalizer frameLocalizer = new FrameLocalizer(ResourceManager.getInstance().getGlobalDomain());

	/**
	 * A {@code RegisteringLocalizer} that internally maps
	 * {@code Dialog} objects to {@code String} keys that are used
	 * to fetch localized {@code String} objects when localizing
	 * a certain {@code Dialog} via {@link Dialog#setTitle(String)}.
	 * Note: Only weak references are stored within this {@code RegisteringLocalizer}
	 * so it is necessary to hold an external reference to a registered
	 * {@code Dialog} in order to make the provided localization {@code data}
	 * persistent!
	 */
	public static class DialogLocalizer extends DomainAwareLocalizer implements RegisteringLocalizer {

		/**
		 * @param domain
		 */
		public DialogLocalizer(ResourceDomain domain) {
			super(domain);
		}

		private Map<Dialog, String> dialogs = new WeakHashMap<>();

		@Override
		public void localize(Object item) {
			Dialog dialog = (Dialog) item;
			String key = dialogs.get(item);
			if (key != null)
				dialog.setTitle(textString(key));
		}

		@Override
		public void register(Object item, Object data) {
			dialogs.put((Dialog) item, (String) data);
		}

	};

	public static final RegisteringLocalizer dialogLocalizer = new DialogLocalizer(ResourceManager.getInstance().getGlobalDomain());

	/**
	 * Helper class to store access information for a certain
	 * object in terms of localization.
	 * 
	 * @author Markus Gärtner 
	 * @version $Id$
	 *
	 */
	public static class LocalizationAccessDescriptor {
		/**
		 * Key used to obtain a localized {@code name} property
		 */
		String nameKey = null;
		
		/**
		 * The identifier of the {@code setName} method 
		 */
		String nameMethodName = null;
		
		/**
		 * Key used to obtain a localized {@code description} property
		 */
		String descriptionkey = null;
		
		/**
		 * The identifier if the {@code setDescription} method
		 */
		String descriptionMethodName = null;
	}

	/**
	 * Special implementation of the {@code RegisteringLocalizer} interface
	 * that makes use of {@link LocalizationAccessDescriptor} instances to
	 * access registered objects in terms of localization. When told to
	 * localize a given {@code Object} it looks for a registered {@code LocalizationAccessDescriptor}
	 * and uses its information to obtain localized texts and call the
	 * appropriate method of the object being localized. Again only weak
	 * references to registered objects are stored. 
	 */
	public static class GenericLocalizer extends DomainAwareLocalizer implements RegisteringLocalizer {

		/**
		 * @param domain
		 */
		public GenericLocalizer(ResourceDomain domain) {
			super(domain);
		}

		private Map<Object, LocalizationAccessDescriptor> descriptors = new WeakHashMap<>();

		@Override
		public void localize(Object item) {
			LocalizationAccessDescriptor descriptor = descriptors.get(item);
			if(descriptor==null)
				return;
			
			set(item, descriptor.nameKey, descriptor.nameMethodName);
			set(item, descriptor.descriptionkey,
					descriptor.descriptionMethodName);
		}

		protected void set(Object item, String key, String methodName) {
			if (key == null || methodName == null)
				return;

			try {
				Method method = item.getClass().getDeclaredMethod(methodName,
						String.class);

				if (Modifier.isPublic(method.getModifiers()))
					method.invoke(item, textString(key));
			} catch (Exception e) {
				LoggerFactory.log(this, Level.WARNING, "Unable to localize object: "+item, e); //$NON-NLS-1$
			}
		}

		@Override
		public void register(Object item, Object data) {
			descriptors.put(item, (LocalizationAccessDescriptor) data);
		}

	};
	
	public static RegisteringLocalizer genericLocalizer = new GenericLocalizer(ResourceManager.getInstance().getGlobalDomain()); 
}
