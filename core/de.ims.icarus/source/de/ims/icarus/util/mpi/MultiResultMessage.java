/*
 * $Revision: 33 $
 * $Date: 2013-05-13 14:33:31 +0200 (Mo, 13 Mai 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/util/mpi/MultiResultMessage.java $
 *
 * $LastChangedDate: 2013-05-13 14:33:31 +0200 (Mo, 13 Mai 2013) $ 
 * $LastChangedRevision: 33 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.util.mpi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Markus Gärtner
 * @version $Id: MultiResultMessage.java 33 2013-05-13 12:33:31Z mcgaerty $
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
