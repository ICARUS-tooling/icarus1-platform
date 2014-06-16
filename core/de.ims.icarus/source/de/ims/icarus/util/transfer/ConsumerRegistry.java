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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeCollection;
import de.ims.icarus.util.data.ContentTypeRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ConsumerRegistry {

	private volatile static ConsumerRegistry instance;

	public static ConsumerRegistry getInstance() {
		ConsumerRegistry result = instance;

		if (result == null) {
			synchronized (ConsumerRegistry.class) {
				result = instance;

				if (result == null) {
					instance = new ConsumerRegistry();
					result = instance;
				}
			}
		}

		return result;
	}

	private Set<Consumer> consumers = new HashSet<>();

	private ConsumerRegistry() {
		// no-op
	}

	public void register(Consumer consumer) {
		if (consumer == null)
			throw new NullPointerException("Invalid consumer"); //$NON-NLS-1$

		if(!consumers.add(consumer))
			throw new IllegalArgumentException("Consumer already present: "+consumer); //$NON-NLS-1$
	}

	private void collectConsumers(ContentType contentType, Set<Consumer> out) {
		for(Consumer consumer : consumers) {
			if(consumer.supports(contentType)) {
				out.add(consumer);
			}
		}
	}

	public List<Consumer> getConsumers(Object data) {
		if (data == null)
			throw new NullPointerException("Invalid data"); //$NON-NLS-1$

		ContentTypeCollection contentTypes = ContentTypeRegistry.getInstance().getEnclosingTypes(data);
		return getConsumers(contentTypes);
	}

	public List<Consumer> getConsumers(ContentTypeCollection contentTypes) {
		if (contentTypes == null)
			throw new NullPointerException("Invalid contentTypes"); //$NON-NLS-1$

		Set<Consumer> out = new HashSet<>();
		for(ContentType contentType : contentTypes.getContentTypes()) {
			collectConsumers(contentType, out);
		}

		return new ArrayList<>(out);
	}

	public List<Consumer> getConsumers(ContentType contentType) {
		if (contentType == null)
			throw new NullPointerException("Invalid contentType"); //$NON-NLS-1$

		Set<Consumer> out = new HashSet<>();
		collectConsumers(contentType, out);

		return new ArrayList<>(out);
	}
}
