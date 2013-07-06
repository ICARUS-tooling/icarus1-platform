/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.ui.text;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;

import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import net.ikarus_systems.icarus.logging.LoggerFactory;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DocumentStream extends ByteArrayOutputStream implements ActionListener {

	protected static final String EOL = System.getProperty("line.separator"); //$NON-NLS-1$
	
	protected PrintStream alternateStream;
	protected BatchDocument document;
	protected MutableAttributeSet defaultStyle;
	protected boolean append;
	
	private static final int DEFAULT_UPDATE_INTERVAL = 500;
	
	private Timer timer;
	
	public DocumentStream(BatchDocument document, PrintStream alternateStream,
			Color color) {
		setDocument(document);
		setAlternateStream(alternateStream);
		if(color!=null) {
			defaultStyle = new SimpleAttributeSet();
			StyleConstants.setForeground(defaultStyle, color);
		}
	}
	
	public DocumentStream(BatchDocument document, PrintStream alternateStream) {
		this(document, alternateStream, null);
	}
	
	public DocumentStream(BatchDocument document) {
		this(document, null, null);
	}

	public PrintStream getAlternateStream() {
		return alternateStream;
	}

	public BatchDocument getDocument() {
		return document;
	}

	public MutableAttributeSet getDefaultStyle() {
		return defaultStyle;
	}

	public boolean isAppend() {
		return append;
	}

	public void setAlternateStream(PrintStream alternateStream) {
		this.alternateStream = alternateStream;
	}

	public void setDocument(BatchDocument document) {
		if(document==null)
			throw new IllegalArgumentException("Invalid document"); //$NON-NLS-1$
		
		this.document = document;
	}

	public void setDefaultStyle(MutableAttributeSet defaultStyle) {
		this.defaultStyle = defaultStyle;
	}

	public void setAppend(boolean append) {
		this.append = append;
	}
	
	protected void addBatch(String s) {
		if(EOL.equals(s)) {
			document.appendBatchLineFeed(defaultStyle);
		} else {
			document.appendBatchString(s, defaultStyle);
		}
	}
	
	protected final Timer getTimer() {
		if(timer==null) {
			timer = new Timer(DEFAULT_UPDATE_INTERVAL, this);
		}
		
		return timer;
	}

	@Override
	public void flush() {
		String s = toString();

		if(s.length()==0) { 
			return;
		}
		
		addBatch(s);
		
		Timer timer = getTimer();
		if(!timer.isRunning()) {
			timer.start();
		}

		reset();
	}

	@Override
	public void close() throws IOException {
		super.close();
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if(document.pendingBatchCount()==0) {
			getTimer().stop();
			return;
		}
		
		int offset = append ? document.getLength() : 0;
		try {
			document.applyBatchUpdates(offset);
		} catch (BadLocationException ex) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to apply batch update on document", ex); //$NON-NLS-1$
		}
	}
}
