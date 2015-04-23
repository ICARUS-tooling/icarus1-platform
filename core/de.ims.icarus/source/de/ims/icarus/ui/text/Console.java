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

	private static volatile LineLimitingDocumentListener lineLimiter;

	private static int lineLimit = 200;

	public static void setLineLimit(int limit) {
		if(limit<100)
			throw new IllegalArgumentException("Line limit must not be less than 100"); //$NON-NLS-1$

		lineLimit = limit;
		if(lineLimiter!=null) {
			lineLimiter.setLineLimit(lineLimit);
		}
	}

	private static volatile Console instance;

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
				if(errStream!=null) {
					errStream.setDocument(outDoc);
				}
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
