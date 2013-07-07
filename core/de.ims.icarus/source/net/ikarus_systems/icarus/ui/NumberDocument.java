/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.ui;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class NumberDocument extends PlainDocument {

	private static final long serialVersionUID = -7247806020719831949L;

	@Override
	public void insertString(int offset, String str, AttributeSet a)
			throws BadLocationException {
		super.insertString(offset, str, a);
		String newText = getText(0, getLength());
		if (!newText.matches("^\\d*$")) { //$NON-NLS-1$
			remove(offset, str.length());
		}
	}
}