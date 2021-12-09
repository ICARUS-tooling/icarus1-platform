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
package de.ims.icarus.plugins.prosody.ui.view.editor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.table.AbstractTableModel;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import de.ims.icarus.Core;
import de.ims.icarus.Core.NamedRunnable;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.prosody.painte.PaIntEParams;
import de.ims.icarus.plugins.prosody.painte.PaIntEParamsWrapper;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.events.EventListener;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.ui.events.EventSource;
import de.ims.icarus.ui.events.Events;
import de.ims.icarus.util.Installable;
import de.ims.icarus.util.classes.ClassUtils;
import de.ims.icarus.util.collections.CollectionUtils;
import de.ims.icarus.util.intern.Interner;
import de.ims.icarus.util.strings.StringUtil;
import de.ims.icarus.xml.jaxb.JAXBGate;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class PaIntERegistry implements Interner<PaIntEParamsWrapper> {

	private List<PaIntEParamsWrapper> items = new ArrayList<>();
	private Map<String, PaIntEParamsWrapper> idMap = new HashMap<>();
	private final ParamsGate paramsGate;

	private static final String FILE_NAME = "painteParams.xml"; //$NON-NLS-1$

	private volatile static PaIntERegistry instance;

	private EventSource eventSource;

	public static PaIntERegistry getInstance() {
		PaIntERegistry result = instance;

		if (result == null) {
			synchronized (PaIntERegistry.class) {
				result = instance;

				if (result == null) {
					result = new PaIntERegistry();
					result.init();
					instance = result;
				}
			}
		}

		return result;
	}

	private PaIntERegistry() {
		eventSource = new EventSource(this);

		Path file = Core.getCore().getDataFolder().resolve(FILE_NAME);
		paramsGate = new ParamsGate(file);
	}

	private void init() {
		load();

		Core.getCore().addShutdownHook(new ShutdownHook());
	}

	private void save() {
		try {
			eventSource.fireEvent(new EventObject(Events.SAVE));

			paramsGate.saveBuffer();

			eventSource.fireEvent(new EventObject(Events.SAVED));
		} catch (Exception e) {
			LoggerFactory.error(this, "Failed to forward save call", e); //$NON-NLS-1$
		}
	}

	private void load() {
		try {
			eventSource.fireEvent(new EventObject(Events.LOAD));

			paramsGate.loadBuffer();

			eventSource.fireEvent(new EventObject(Events.LOADED));
		} catch (Exception e) {
			LoggerFactory.error(this, "Failed to forward load call", e); //$NON-NLS-1$
		}
	}

	/**
	 * @see de.ims.icarus.ui.events.EventSource#addListener(java.lang.String, de.ims.icarus.ui.events.EventListener)
	 */
	public void addListener(String eventName, EventListener listener) {
		eventSource.addListener(eventName, listener);
	}

	/**
	 * @see de.ims.icarus.ui.events.EventSource#removeEventListener(de.ims.icarus.ui.events.EventListener)
	 */
	public void removeListener(EventListener listener) {
		eventSource.removeEventListener(listener);
	}

	/**
	 * @see de.ims.icarus.ui.events.EventSource#removeEventListener(de.ims.icarus.ui.events.EventListener, java.lang.String)
	 */
	public void removeListener(EventListener listener, String eventName) {
		eventSource.removeEventListener(listener, eventName);
	}

	public void addParams(PaIntEParamsWrapper wrapper) {
		addParams0(wrapper);

		save();
	}

	private void addParams0(PaIntEParamsWrapper wrapper) {
		if (wrapper == null)
			throw new NullPointerException("Invalid wrapper"); //$NON-NLS-1$

		if(idMap.containsValue(wrapper))
			throw new IllegalArgumentException("Item already present in registry: "+wrapper); //$NON-NLS-1$

		String name = wrapper.getName();

		if(!isLegalName(name))
			throw new IllegalArgumentException("Invalid name for painte parameters set: "+name); //$NON-NLS-1$

		if(idMap.containsKey(name)) {
			name = getUniqueName(name);
			wrapper.setLabel(name);
		}

		int index = items.size();

		items.add(wrapper);
		idMap.put(name, wrapper);

		eventSource.fireEvent(new EventObject(Events.ADDED,
				"wrapper", wrapper, "name", name, "index", index)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void removeParams(PaIntEParamsWrapper wrapper) {
		if (wrapper == null)
			throw new NullPointerException("Invalid wrapper"); //$NON-NLS-1$

		if(!idMap.containsValue(wrapper))
			throw new IllegalArgumentException("Item not present in registry: "+wrapper); //$NON-NLS-1$

		String name = wrapper.getName();

		int index = items.indexOf(wrapper);

		items.remove(index);
		idMap.remove(name);

		eventSource.fireEvent(new EventObject(Events.REMOVED,
				"wrapper", wrapper, "name", name, "index", index)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		save();
	}

	public void removeAllParams() {
		items.clear();
		idMap.clear();

		eventSource.fireEvent(new EventObject(Events.CLEAR));
	}

	public void renameParams(PaIntEParamsWrapper wrapper, String newName) {
		if (wrapper == null)
			throw new NullPointerException("Invalid wrapper"); //$NON-NLS-1$
		if(!isLegalName(newName))
			throw new IllegalArgumentException("Invalid name for painte parameters set: "+newName); //$NON-NLS-1$

		String oldName = wrapper.getName();
		if(oldName.equals(newName)) {
			return;
		}

		String uniqueName = getUniqueName(newName);

		if(oldName.equals(uniqueName)) {
			return;
		}

		wrapper.setLabel(uniqueName);

		idMap.remove(oldName);
		idMap.put(uniqueName, wrapper);

		paramsChanged(wrapper);
	}

	public void editParamsDescription(PaIntEParamsWrapper wrapper, String description) {
		if (wrapper == null)
			throw new NullPointerException("Invalid wrapper"); //$NON-NLS-1$

		String oldDescription = wrapper.getDescription();

		if(ClassUtils.equals(description, oldDescription)) {
			return;
		}

		wrapper.setDescription(description);

		paramsChanged(wrapper);
	}

	public boolean setParams(PaIntEParamsWrapper wrapper, PaIntEParams newParams) {
		wrapper.getParams().setParams(newParams);

		boolean isRegistered = containsParams(wrapper);

		if(isRegistered) {
			paramsChanged(wrapper);
		}

		return isRegistered;
	}

	public boolean setCompact(PaIntEParamsWrapper wrapper, boolean compact) {
		if(compact==wrapper.isCompact()) {
			return false;
		}

		wrapper.setCompact(compact);

		boolean isRegistered = containsParams(wrapper);

		if(isRegistered) {
			paramsChanged(wrapper);
		}

		return isRegistered;
	}

	public void paramsChanged(PaIntEParamsWrapper wrapper) {
		if (wrapper == null)
			throw new NullPointerException("Invalid wrapper"); //$NON-NLS-1$

		int index = items.indexOf(wrapper);

		if(index==-1)
			throw new IllegalArgumentException("Item not present in registry: "+wrapper); //$NON-NLS-1$

		eventSource.fireEvent(new EventObject(Events.CHANGED,
				"wrapper", wrapper, "index", index)); //$NON-NLS-1$ //$NON-NLS-2$

		save();
	}

	public boolean containsParams(PaIntEParamsWrapper wrapper) {
		return idMap.containsKey(wrapper.getName())
				&& idMap.get(wrapper.getName())==wrapper;
	}

	public boolean containsName(String name) {
		return idMap.containsKey(name);
	}

	public PaIntEParamsWrapper getParams(String name) {
		if (name == null)
			throw new NullPointerException("Invalid name"); //$NON-NLS-1$

		return idMap.get(name);
	}

	public String getUniqueName(String baseName) {
		Set<String> usedNames = new HashSet<>(items.size());
		for(PaIntEParamsWrapper wrapper : items) {
			usedNames.add(wrapper.getName());
		}

		return StringUtil.getUniqueName(baseName, usedNames);
	}

	public static boolean isLegalName(String name) {
		return name!=null && name.length()>=3;
	}

	public void importParams(Path file) throws Exception {
		if (file == null)
			throw new NullPointerException("Invalid file"); //$NON-NLS-1$

		paramsGate.load(file);

		save();
	}

	public void exportParams(Path file) throws Exception {
		if (file == null)
			throw new NullPointerException("Invalid file"); //$NON-NLS-1$

		paramsGate.save(file, true);
	}

	@Override
	public PaIntEParamsWrapper intern(PaIntEParamsWrapper item) {
		PaIntEParamsWrapper savedWrapper = PaIntERegistry.getInstance().getParams(item.getLabel());
		return savedWrapper!=null ? savedWrapper : item;
	}

	private class ParamsGate extends JAXBGate<ParamsBuffer> {

		public ParamsGate(Path file) {
			super(file, ParamsBuffer.class);
		}

		/**
		 * @see de.ims.icarus.xml.jaxb.JAXBGate#readBuffer(java.lang.Object)
		 */
		@Override
		protected void readBuffer(ParamsBuffer buffer) throws Exception {
			for(PaIntEParamsWrapper wrapper : buffer.items) {
				try {
					addParams0(wrapper);
				} catch(Exception e) {
					LoggerFactory.error(this, "Error during load up of PaIntE registry", e); //$NON-NLS-1$
				}
			}
		}

		/**
		 * @see de.ims.icarus.xml.jaxb.JAXBGate#createBuffer()
		 */
		@Override
		protected ParamsBuffer createBuffer() throws Exception {
			return new ParamsBuffer(items);
		}

	}

	@XmlRootElement(name="params-buffer")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class ParamsBuffer {

		@XmlElement(name="entry")
		public List<PaIntEParamsWrapper> items = new ArrayList<>();

		public ParamsBuffer() {
			// no-op
		}

		public ParamsBuffer(Collection<PaIntEParamsWrapper> items) {
			this.items.addAll(items);
		}
	}

	public void moveParams(int index0, int index1) {
		@SuppressWarnings("unused")
		PaIntEParamsWrapper wrapper1 = items.get(index1);
		PaIntEParamsWrapper wrapper0 = items.remove(index0);

		items.add(index1, wrapper0);

		eventSource.fireEvent(new EventObject(Events.CHANGED));
	}

	public List<PaIntEParamsWrapper> getParams() {
		return CollectionUtils.getListProxy(items);
	}

	public Set<String> getParamNames() {
		return CollectionUtils.getSetProxy(idMap.keySet());
	}

	public PaIntERegistryTableModel createTableModel() {
		return new PaIntERegistryTableModel();
	}

	private static final String[] columnKeys = {
		"label", //$NON-NLS-1$
		"curve", //$NON-NLS-1$
		"compact", //$NON-NLS-1$
		"description", //$NON-NLS-1$
		"a1", //$NON-NLS-1$
		"a2", //$NON-NLS-1$
		"b", //$NON-NLS-1$
		"c1", //$NON-NLS-1$
		"c2", //$NON-NLS-1$
		"d", //$NON-NLS-1$
		"alignment" //$NON-NLS-1$
	};

	public class PaIntERegistryTableModel extends AbstractTableModel implements EventListener, Installable {

		private static final long serialVersionUID = 6859442635080501760L;

		@Override
		public void install(Object target) {
			addListener(Events.ADDED, this);
			addListener(Events.REMOVED, this);
			addListener(Events.CHANGED, this);
			addListener(Events.CLEAR, this);
		}

		@Override
		public void uninstall(Object target) {
			removeListener(this);
		}

		public PaIntEParamsWrapper getItem(int rowIndex) {
			return items.get(rowIndex);
		}

		@Override
		public String getColumnName(int column) {
			return ResourceManager.getInstance().get(
					"plugins.prosody.painteRegistryTable.columns."+columnKeys[column]+".name");  //$NON-NLS-1$//$NON-NLS-2$
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
			case 0:
			case 3:
				return String.class;

			case 1:
				return PaIntEParamsWrapper.class;

			case 2:
				return Boolean.class;

			default:
				return Double.class;
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex!=1 && columnIndex!=2 && columnIndex!=3;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			PaIntEParamsWrapper wrapper = getItem(rowIndex);

			switch (columnIndex) {
			case 0:
				if(isLegalName((String)aValue)) {
					wrapper.setLabel((String)aValue);
				}
				break;

			case 2:
				wrapper.setCompact((Boolean) aValue);
				break;

			case 3:
				wrapper.setDescription((String) aValue);
				break;

			case 4:
				wrapper.getParams().setA1((Double)aValue);
				break;
			case 5:
				wrapper.getParams().setA2((Double)aValue);
				break;
			case 6:
				wrapper.getParams().setB((Double)aValue);
				break;
			case 7:
				wrapper.getParams().setC1((Double)aValue);
				break;
			case 8:
				wrapper.getParams().setC2((Double)aValue);
				break;
			case 9:
				wrapper.getParams().setD((Double)aValue);
				break;
			case 10:
				wrapper.getParams().setAlignment((Double)aValue);
				break;

			default:
				throw new IndexOutOfBoundsException("No such column: "+columnIndex); //$NON-NLS-1$
			}

			paramsChanged(wrapper);
		}

		/**
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		@Override
		public int getRowCount() {
			return items.size();
		}

		/**
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		@Override
		public int getColumnCount() {
			return columnKeys.length;
		}

		/**
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			PaIntEParamsWrapper wrapper = getItem(rowIndex);

			switch (columnIndex) {
			case 0:
				return wrapper.getName();
			case 1:
				return wrapper;
			case 2:
				return wrapper.isCompact();
			case 3:
				return wrapper.getDescription();
			case 4:
				return wrapper.getParams().getA1();
			case 5:
				return wrapper.getParams().getA2();
			case 6:
				return wrapper.getParams().getB();
			case 7:
				return wrapper.getParams().getC1();
			case 8:
				return wrapper.getParams().getC2();
			case 9:
				return wrapper.getParams().getD();
			case 10:
				return wrapper.getParams().getAlignment();

			default:
				throw new IndexOutOfBoundsException("No such column: "+columnIndex); //$NON-NLS-1$
			}
		}

		/**
		 * @see de.ims.icarus.ui.events.EventListener#invoke(java.lang.Object, de.ims.icarus.ui.events.EventObject)
		 */
		@Override
		public void invoke(Object sender, EventObject event) {

			if(Events.CLEAR.equals(event.getName()) || event.getProperty("index")==null) { //$NON-NLS-1$
				fireTableDataChanged();
				return;
			}

			int index = (int) event.getProperty("index"); //$NON-NLS-1$

			if(Events.ADDED.equals(event.getName())) {
				fireTableRowsInserted(index, index);
			} else if(Events.REMOVED.equals(event.getName())) {
				fireTableRowsDeleted(index, index);
			}  else if(Events.CHANGED.equals(event.getName())) {
				fireTableRowsUpdated(index, index);
			}
		}

	}

	private class ShutdownHook implements NamedRunnable {

		/**
		 * @see de.ims.icarus.Core.NamedRunnable#getName()
		 */
		@Override
		public String getName() {
			return ResourceManager.getInstance().get(
					"plugins.prosody.painteSaveTask.title"); //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus.Core.NamedRunnable#run()
		 */
		@Override
		public void run() throws Exception {
			paramsGate.saveBufferNow();
		}
	}
}
