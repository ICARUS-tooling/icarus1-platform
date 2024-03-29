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
package de.ims.icarus.resources;

import java.awt.Dialog;
import java.awt.Frame;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.logging.Level;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.resources.Localizers.ActionLocalizer;
import de.ims.icarus.resources.Localizers.ButtonLocalizer;
import de.ims.icarus.resources.Localizers.DialogLocalizer;
import de.ims.icarus.resources.Localizers.FrameLocalizer;
import de.ims.icarus.resources.Localizers.GenericLocalizer;
import de.ims.icarus.resources.Localizers.LabelLocalizer;
import de.ims.icarus.resources.Localizers.LocalizationAccessDescriptor;
import de.ims.icarus.resources.Localizers.PopupLocalizer;
import de.ims.icarus.resources.Localizers.TextComponentLocalizer;


/**
 * Provides a kind of 'private' domain of resources that will not be
 * accessible in a global manner via the shared {@code ResourceManager}
 * instance. All locations ({@code baseNames}) added to this domain
 * are wrapped into {@link ManagedResource} objects that are notified
 * by the {@code ResourceManager} when the current {@code Locale}
 * changes. All methods used to access localized data on the
 * {@code ResourceManager} are mirrored in this class with the back-end
 * being a list of aforementioned {@code ManagedResource}s that will be
 * traversed until an entry for a given {@code key} is found.
 * <p>
 * A typical usage for this implementation is the creation of a private
 * localization 'manager' for some entity like a plug-in. Note that
 * registering of {@code Localizable} objects is still handled by the global
 * {@code ResourceManager}.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ResourceDomain {

	protected List<ManagedResource> resources = new LinkedList<>();

	protected Map<String, Localizer> localizers = new HashMap<>();

	protected final ResourceDomain parent;

	protected final boolean returnKeyIfAbsent;

	public ResourceDomain() {
		this(null, true);
	}

	/**
	 *
	 */
	public ResourceDomain(ResourceDomain parent, boolean returnKeyIfAbsent) {
		this.parent = parent;
		this.returnKeyIfAbsent = returnKeyIfAbsent;

		init();
	}

	protected  void init() {
		localizers.put("label", new LabelLocalizer(this)); //$NON-NLS-1$
		localizers.put("button", new ButtonLocalizer(this)); //$NON-NLS-1$
		localizers.put("popup", new PopupLocalizer(this)); //$NON-NLS-1$
		localizers.put("textComponent", new TextComponentLocalizer(this)); //$NON-NLS-1$
		localizers.put("action", new ActionLocalizer(this)); //$NON-NLS-1$
		localizers.put("frame", new FrameLocalizer(this)); //$NON-NLS-1$
		localizers.put("dialog", new DialogLocalizer(this)); //$NON-NLS-1$
		localizers.put("generic", new GenericLocalizer(this)); //$NON-NLS-1$
	}

	protected Localizer getLocalizer(String key) {
		Localizer localizer = localizers.get(key);
		return localizer==null ? Localizers.emptyLocalizer : localizer;

	}

	protected RegisteringLocalizer getRegisteringLocalizer(String key) {
		Localizer localizer = localizers.get(key);
		return (RegisteringLocalizer) (localizer==null || !(localizer instanceof RegisteringLocalizer) ?
				Localizers.emptyRegisteringLocalizer : localizer);
	}

	public void clear() {
		for(ManagedResource resource : resources) {
			resource.clear();
		}
	}

	public ManagedResource addResource(String baseName, ResourceLoader loader) {
		ManagedResource resource = ResourceManager.getInstance().addManagedResource(baseName, loader);

		if(resource!=null) {
			synchronized (resources) {
				resources.add(resource);
			}
		}

		return resource;
	}

	public ManagedResource addResource(String baseName) {
		return addResource(baseName, null);
	}

	public void removeResource(String baseName) {
		ResourceManager.getInstance().removeManagedResource(baseName);
		synchronized (resources) {
			for(Iterator<ManagedResource> i = resources.iterator(); i.hasNext();) {
				ManagedResource resource = i.next();
				if(resource.getBaseName().equals(baseName)) {
					i.remove();
					break;
				}
			}
		}
	}

	public String getFormatted(String key, Object...args) {
		return String.format(get(key), args);
	}

	public String get(String key) {
		return get(key, key, (Object[])null);
	}

	public String get(String key, String defaultValue) {
		return get(key, null, defaultValue);
	}

	public String get(String key, String defaultValue, Object...params) {
		String value = getResource(key);

		// Applies default value if required
		if (value == null) {
			if(ResourceManager.isNotifyMissingResource()) {
				//TODO provide stack trace!
				LoggerFactory.log(this, Level.INFO, "No resource entry for key: "+key/*, new Throwable()*/); //$NON-NLS-1$
			}
			value = defaultValue;
		}

		// Replaces the placeholders with the values in the array
		if (value != null && (params != null || value.indexOf('{')!=-1)) {
			value = ResourceManager.format(value, params);
		}

		if(value==null && returnKeyIfAbsent) {
			value = key;
		}

		return value;
	}

	protected final String getResource(String key) {
		Iterator<ManagedResource> it = resources.iterator();

		while (it.hasNext()) {
			try {
				return it.next().getResource(key);
			} catch (MissingResourceException mrex) {
				// continue
			}
		}

		// delegate search to parent if we didn't find a matching entry
		return parent==null ? null : parent.getResource(key);
	}

	public void addItem(Object item, Localizer localizer, boolean init) {
		ResourceManager.getInstance().addLocalizableItem(item, localizer, init);
	}

	private final boolean DEFAULT_INIT = true;

	public void addItem(Object item, Localizer localizer) {
		addItem(item, localizer, DEFAULT_INIT);
	}

	public void addItem(Localizer item, boolean init) {
		addItem(item, item, init);
	}

	public void addItem(Localizer item) {
		addItem(item, item, DEFAULT_INIT);
	}

	public void addItem(Localizable item, boolean init) {
		addItem(item, Localizers.emptyLocalizer, init);
	}

	public void addItem(Localizable item) {
		addItem(item, Localizers.emptyLocalizer, DEFAULT_INIT);
	}

	public void removeItem(Object item) {
		ResourceManager.getInstance().removeLocalizableItem(item);
	}

	public Action addAction(Action action, boolean init) {
		addItem(action, getLocalizer("action"), init); //$NON-NLS-1$
		return action;
	}

	public Action addAction(Action action) {
		addItem(action, getLocalizer("action"), DEFAULT_INIT); //$NON-NLS-1$
		return action;
	}

	public Action prepareAction(Action action, String nameKey,
			String descKey) {
		action.putValue(ResourceConstants.DEFAULT_TEXT_KEY, nameKey);
		action.putValue(ResourceConstants.DEFAULT_DESCRIPTION_KEY, descKey);
		return action;
	}

	public void addComponent(JComponent comp, boolean init) {
		if (comp instanceof JLabel)
			addItem(comp, getLocalizer("label"), init); //$NON-NLS-1$
		else if (comp instanceof AbstractButton)
			addItem(comp, getLocalizer("button"), init); //$NON-NLS-1$
		else if (comp instanceof JTextComponent)
			addItem(comp, getLocalizer("textComponent"), init); //$NON-NLS-1$
		else if (comp instanceof JPopupMenu)
			addItem(comp, getLocalizer("popup"), init); //$NON-NLS-1$
		else if (comp instanceof Localizable)
			addItem((Localizable) comp, init);
		else
			throw new IllegalArgumentException(
					"No default localizer present for " + comp.toString()); //$NON-NLS-1$
	}

	public void addComponent(JComponent comp) {
		addComponent(comp, DEFAULT_INIT);
	}

	public JComponent prepareComponent(JComponent comp, String nameKey,
			String descKey) {
		comp.putClientProperty(ResourceConstants.DEFAULT_TEXT_KEY, nameKey);
		comp.putClientProperty(ResourceConstants.DEFAULT_DESCRIPTION_KEY, descKey);
		return comp;
	}

	public void addFrame(Frame frame, String nameKey, boolean init) {
		RegisteringLocalizer localizer = getRegisteringLocalizer("frame"); //$NON-NLS-1$
		localizer.register(frame, nameKey);
		addItem(frame, localizer, init);
	}

	public void addFrame(Frame frame, String nameKey) {
		addFrame(frame, nameKey, DEFAULT_INIT);
	}

	public void addDialog(Dialog frame, String nameKey, boolean init) {
		RegisteringLocalizer localizer = getRegisteringLocalizer("dialog"); //$NON-NLS-1$
		localizer.register(frame, nameKey);
		addItem(frame, localizer, init);
	}

	public void addDialog(Dialog frame, String nameKey) {
		addDialog(frame, nameKey, DEFAULT_INIT);
	}

	public void addGeneric(Object item, String nameKey,
			String nameMethodName, boolean init) {
		LocalizationAccessDescriptor descriptor = new LocalizationAccessDescriptor();
		descriptor.nameKey = nameKey;
		descriptor.nameMethodName = nameMethodName;

		RegisteringLocalizer localizer = getRegisteringLocalizer("generic"); //$NON-NLS-1$
		localizer.register(item, descriptor);
		addItem(item, localizer, init);
	}

	public void addGeneric(Object item, String nameKey,
			String nameMethodName) {
		addGeneric(item, nameKey, nameMethodName, DEFAULT_INIT);
	}

	public void addGeneric(Object item, String nameKey,
			String nameMethodName, String descriptionKey,
			String descriptionMethodName, boolean init) {
		LocalizationAccessDescriptor descriptor = new LocalizationAccessDescriptor();
		descriptor.nameKey = nameKey;
		descriptor.nameMethodName = nameMethodName;
		descriptor.descriptionkey = descriptionKey;
		descriptor.descriptionMethodName = descriptionMethodName;

		RegisteringLocalizer localizer = getRegisteringLocalizer("generic"); //$NON-NLS-1$
		localizer.register(item, descriptor);
		addItem(item, localizer, init);
	}

	public void addGeneric(Object item, String nameKey,
			String nameMethodName, String descriptionKey,
			String descriptionMethodName) {
		addGeneric(item, nameKey, nameMethodName, descriptionKey,
				descriptionMethodName, DEFAULT_INIT);
	}
}
