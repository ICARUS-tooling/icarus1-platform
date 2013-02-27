/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util.opi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class MultiResultMessage extends ResultMessage {
	
	private List<ResultMessage> results;

	public MultiResultMessage(ResultType type, Message message, 
			Collection<ResultMessage> items) {
		super(type, message, null, null);
		
		results = new ArrayList<>(items);
	}

	public final int getResultCount() {
		return results.size();
	}
	
	public final ResultMessage getResultAt(int index) {
		return results.get(index);
	}
}
