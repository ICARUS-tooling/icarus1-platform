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
package de.ims.icarus.util.transfer;

import java.awt.event.ActionEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JMenu;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.dialog.DialogDispatcher;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ConsumerMenu extends JMenu {

	private static final long serialVersionUID = -7179805061342913830L;

	private Reference<Object> ref;
	private final Object source;
	private boolean isBatch;
	private Options options;

	public ConsumerMenu(Object source) {
		if (source == null)
			throw new NullPointerException("Invalid source"); //$NON-NLS-1$

		this.source = source;

		ResourceManager.getInstance().getGlobalDomain().prepareComponent(this,
				"consumerMenu.name", //$NON-NLS-1$
				"consumerMenu.description"); //$NON-NLS-1$
		ResourceManager.getInstance().getGlobalDomain().addComponent(this);
	}

	private void rebuild(List<Consumer> consumers) {
		removeAll();

		ResourceManager.getInstance().getGlobalDomain().prepareComponent(this,
				"consumerMenu.name", //$NON-NLS-1$
				"consumerMenu.description"); //$NON-NLS-1$
		ResourceManager.getInstance().getGlobalDomain().addComponent(this);

		if(consumers.isEmpty()) {
			add(new JLabel(ResourceManager.getInstance().get("consumerMenu.notAvailable"))); //$NON-NLS-1$
			return;
		}

		Collections.sort(consumers, Identity.COMPARATOR);

		for(Consumer consumer : consumers) {
			add(new ConsumerAction(consumer));
		}
	}

	private void setData(Object data) {
		if (data == null)
			throw new NullPointerException("Invalid data");

		ref = new WeakReference<Object>(data);
	}

	public void clear() {
		List<Consumer> tmp = Collections.emptyList();
		ref = null;
		rebuild(tmp);
	}

	public void refresh(ContentType contentType, Object data) {
		setData(data);
		rebuild(ConsumerRegistry.getInstance().getConsumers(contentType));
	}

	public void refreshBatch(ContentType contentType, Object[] data) {
		setData(data);
		rebuild(ConsumerRegistry.getInstance().getConsumers(contentType));
	}

	private Object getData() {
		return ref.get();
	}

	/**
	 * @return the source
	 */
	public Object getSource() {
		return source;
	}

	/**
	 * @return the isBatch
	 */
	public boolean isBatch() {
		return isBatch;
	}

	/**
	 * @return the options
	 */
	public Options getOptions() {
		return options;
	}

	/**
	 * @param options the options to set
	 */
	public void setOptions(Options options) {
		this.options = options;
	}

	private class ConsumerAction extends AbstractAction {

		private static final long serialVersionUID = 1361653438099216797L;

		private final Consumer consumer;

		ConsumerAction(Consumer consumer) {
			if (consumer == null)
				throw new NullPointerException("Invalid consumer"); //$NON-NLS-1$

			this.consumer = consumer;

			putValue(Action.NAME, consumer.getName());
			putValue(Action.SHORT_DESCRIPTION, consumer.getDescription());
			putValue(Action.SMALL_ICON, consumer.getIcon());

			if(isBatch) {
				setEnabled(consumer.supportsBatch());
			}
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			Object data = getData();
			if(data==null || !isEnabled()) {
				return;
			}

			Options options = getOptions();
			if(options==null) {
				options = Options.emptyOptions;
			} else {
				options = options.clone();
			}

			try {
				if(isBatch) {
					consumer.processBatch((Object[]) data, source, options);
				} else {
					consumer.process(data, source, options);
				}
			} catch(Exception ex) {
				LoggerFactory.error(this, "Consumer '"+consumer.getName()+"' failed to process request", ex); //$NON-NLS-1$ //$NON-NLS-2$

				new DialogDispatcher(null,
					"dialogs.error",  //$NON-NLS-1$
					"dialogs.consumerFail.message",  //$NON-NLS-1$
					consumer.getName()).showAsError();
			}
		}
	}
}
