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
