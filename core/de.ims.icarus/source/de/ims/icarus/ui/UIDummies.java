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
package de.ims.icarus.ui;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public final class UIDummies {

	private UIDummies() {
		// no-ops
	}

	public static JComponent createDefaultErrorOutput(JComponent container, Throwable t) {
		if(container==null) {
			container = new JPanel();
		}
		
		container.removeAll();
		if(!(container.getLayout() instanceof BorderLayout)) {
			container.setLayout(new BorderLayout());
		}
        container.add(new JLabel("Error:"), BorderLayout.NORTH); //$NON-NLS-1$
        JScrollPane scrollPane = new JScrollPane();
        container.add(scrollPane, BorderLayout.CENTER);
        StringBuffer sb = new StringBuffer();
        String nl = System.getProperty("line.separator"); //$NON-NLS-1$
        sb.append(t.toString()).append(nl).append(nl);
        Throwable err = t;
        while (err != null) {
            if (err != t) {
                sb.append(nl).append("Caused by " + err).append(nl).append(nl); //$NON-NLS-1$
            }
            StackTraceElement[] stackTrace = err.getStackTrace();
            for (int i = 0; i < stackTrace.length; i++) {
                sb.append(stackTrace[i].toString()).append(nl);
            }
            err = err.getCause();
        }
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setBackground(container.getBackground());
        textArea.setEditable(false);
        scrollPane.setViewportView(textArea);
        textArea.setCaretPosition(0);
        
        return container;
	}
}
