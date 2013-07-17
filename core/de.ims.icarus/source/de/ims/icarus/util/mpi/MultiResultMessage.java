/*
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
