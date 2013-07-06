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

import java.awt.Color;
import java.io.PrintStream;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class Console {
	
	private static final PrintStream defaultOut = System.out;
	private static final PrintStream defaultErr = System.err;
	
	private BatchDocument errDoc;
	private BatchDocument outDoc;
	
	private DocumentStream errStream;
	private DocumentStream outStream;
	
	private boolean mergeStreams = true;
	
	private static LineLimitingDocumentListener lineLimiter;
	
	private static int lineLimit = 200;
	
	public static void setLineLimit(int limit) {
		if(limit<100)
			throw new IllegalArgumentException("Line limit must not be less than 100"); //$NON-NLS-1$
		
		lineLimit = limit;
		if(lineLimiter!=null) {
			lineLimiter.setLineLimit(lineLimit);
		}
	}
	
	private static Console instance;
	
	public static Console getInstance() {
		if (instance==null) {
			synchronized (Console.class) {
				if(instance==null) {
					instance = new Console();
				}
			}
		}
		
		return instance;
	}

	private Console() {
		// no-op
	}
	
	private static LineLimitingDocumentListener getLineLimiter() {
		if(lineLimiter==null) {
			lineLimiter = new LineLimitingDocumentListener(lineLimit);
		}
		
		return lineLimiter;
	}

	private void redirectOut() {
		if(outStream!=null || outDoc==null) {
			return;
		}
		outStream = new DocumentStream(outDoc, defaultOut);
		System.setOut(new PrintStream(outStream, true));
	}
	
	public synchronized BatchDocument getOutputDocument() {
		if(outDoc==null) {
			outDoc = new BatchDocument();
			outDoc.addDocumentListener(getLineLimiter());
			redirectOut();
			
			if(isMergeStreams()) {
				redirectErr();
				errStream.setDocument(outDoc);
			}
		}
		
		return outDoc;
	}

	private void redirectErr() {
		BatchDocument doc = mergeStreams ? outDoc : errDoc;
		if(errStream!=null || doc==null) {
			return;
		}
		errStream = new DocumentStream(doc, defaultErr, Color.red);
		System.setErr(new PrintStream(errStream, true));
	}
	
	public synchronized BatchDocument getErrorDocument() {
		if(errDoc==null) {
			errDoc = new BatchDocument();
			errDoc.addDocumentListener(getLineLimiter());
			
			if(!isMergeStreams()) {
				redirectErr();
			}
		}
		
		return errDoc;
	}

	public boolean isMergeStreams() {
		return mergeStreams;
	}

	public void setMergeStreams(boolean mergeStreams) {
		if(mergeStreams==this.mergeStreams) {
			return;
		}
		
		this.mergeStreams = mergeStreams;
		
		if(outDoc==null || errDoc==null) {
			return;
		}
		
		redirectOut();
		redirectErr();
		
		if(mergeStreams) {
			errStream.setDocument(outDoc);
		} else {
			errStream.setDocument(errDoc);
		}
	}
}
