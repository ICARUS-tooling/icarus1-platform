/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.ui.text;

import java.util.logging.Level;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;

import de.ims.icarus.logging.LoggerFactory;


/**
 * @author Markus Gärtner
 * @version $Id$
 * 
 */
public class LineLimitingDocumentListener implements DocumentListener {

	private int lineLimit;
	private boolean removeFromStart;

	public LineLimitingDocumentListener(int lineLimit, boolean removeFromStart) {
		setLineLimit(lineLimit);
		setRemoveFromStart(removeFromStart);
	}

	public LineLimitingDocumentListener(int lineLimit) {
		this(lineLimit, true);
	}

	public int getLineLimit() {
		return lineLimit;
	}

	public boolean isRemoveFromStart() {
		return removeFromStart;
	}

	public void setLineLimit(int lineLimit) {
		if (lineLimit < 1)
			throw new IllegalArgumentException(
					"Invalid line limit: " + lineLimit); //$NON-NLS-1$

		this.lineLimit = lineLimit;
	}

	public void setRemoveFromStart(boolean removeFromStart) {
		this.removeFromStart = removeFromStart;
	}

	/**
	 * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
	 */
	@Override
	public void insertUpdate(final DocumentEvent e) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				processEvent(e);
			}
		});
	}

	protected void processEvent(DocumentEvent e) {
		Document doc = e.getDocument();
		Element root = doc.getDefaultRootElement();

		while(root.getElementCount() > lineLimit) {
			if (removeFromStart) {
				removeFromStart(doc, root);
			} else {
				removeFromEnd(doc, root);
			}
		}
	}

	protected void removeFromStart(Document doc, Element root) {
		Element line = root.getElement(0);
		int end = line.getEndOffset();

		try {
			doc.remove(0, end);
		} catch (BadLocationException e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to remove line from document start", e); //$NON-NLS-1$
		}
	}

	protected void removeFromEnd(Document document, Element root) {
		Element line = root.getElement(root.getElementCount() - 1);
		int start = line.getStartOffset();
		int end = line.getEndOffset();

		try {
			// Remove line including line-feed character of previous line
			document.remove(start-1, end-start);
		} catch (BadLocationException e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to remove line from document end", e); //$NON-NLS-1$
		}
	}

	/**
	 * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
	 */
	@Override
	public void removeUpdate(DocumentEvent e) {
		// no-op
	}

	/**
	 * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
	 */
	@Override
	public void changedUpdate(DocumentEvent e) {
		// no-op
	}
}
