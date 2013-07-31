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
package de.ims.icarus.ui.text;

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class BatchDocument extends DefaultStyledDocument {

	private static final long serialVersionUID = 2567714909070703645L;

	protected static final char[] EOL_ARRAY = { '\n' };

	protected List<ElementSpec> batch = new ArrayList<>();

	public BatchDocument() {
		// no-op
	}
	
	public int appendBatchString(String str, AttributeSet attr) {
        batch.add(new ElementSpec(attr, ElementSpec.ContentType, str.toCharArray(), 0, str.length()));
        return str.length();
	}
	
	public int appendBatchLineFeed(AttributeSet attr) {
        batch.add(new ElementSpec(attr, ElementSpec.ContentType, EOL_ARRAY, 0, 1));

        // Then add attributes for element start/end tags. Ideally 
        // we'd get the attributes for the current position, but we 
        // don't know what those are yet if we have unprocessed 
        // batch inserts. Alternatives would be to get the last 
        // paragraph element (instead of the first), or to process 
        // any batch changes when a line-feed is inserted.
        Element paragraph = getParagraphElement(0);
        AttributeSet pattr = paragraph.getAttributes();
        batch.add(new ElementSpec(null, ElementSpec.EndTagType));
        batch.add(new ElementSpec(pattr, ElementSpec.StartTagType));
        
        return EOL_ARRAY.length;
	}

    public void applyBatchUpdates(int offset) throws BadLocationException {
    	if(batch.isEmpty()) {
    		return;
    	}
    	
        ElementSpec[] inserts = new ElementSpec[batch.size()];
        batch.toArray(inserts);
        batch.clear();

        // Process all of the inserts in bulk
        insert(offset, inserts);
    }
    
    public void clear() {
    	try {
			remove(0, getLength());
		} catch (BadLocationException e) {
			// ignore
		}
    }
    
    public void discardBatch() {
    	batch.clear();
    }
    
    public int pendingBatchCount() {
    	return batch.size();
    }
}
