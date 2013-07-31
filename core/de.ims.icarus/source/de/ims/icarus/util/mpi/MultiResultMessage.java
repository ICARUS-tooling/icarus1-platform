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
package de.ims.icarus.util.mpi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class MultiResultMessage extends ResultMessage {
	
	private List<ResultMessage> results;

	public MultiResultMessage(Object source, ResultType type, Message message, 
			Collection<ResultMessage> items) {
		super(source, type, message, null, null);
		
		results = new ArrayList<>(items);
	}

	public final int getResultCount() {
		return results.size();
	}
	
	public final ResultMessage getResultAt(int index) {
		return results.get(index);
	}
	
	public final ResultMessage[] getMessagesForType(ResultType type) {
		Collection<ResultMessage> messages = new LinkedList<>();
		
		for(ResultMessage message : results) {
			if(message.getType()==type) {
				messages.add(message);
			}
		}
		
		return messages.toArray(new ResultMessage[messages.size()]);
	}
}
