/*
 * $Revision: 7 $
 * $Date: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/CoreErrorDialog.java $
 *
 * $LastChangedDate: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $ 
 * $LastChangedRevision: 7 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

/**
 * @author Markus Gärtner
 * @version $Id: CoreErrorDialog.java 7 2013-02-27 13:18:56Z mcgaerty $
 *
 */
class CoreErrorDialog extends JFrame {

	private static final long serialVersionUID = 2706145795518241027L;

	CoreErrorDialog(Throwable t) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Container container = getContentPane();
        container.setLayout(new BorderLayout());
        JLabel header = new JLabel("Unexpected launcher error:"); //$NON-NLS-1$
        header.setBorder(new EmptyBorder(5, 10, 10, 10));
        container.add(header, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane();
        container.add(scrollPane, BorderLayout.CENTER);
        StringBuffer sb = new StringBuffer(t.toString());
        String nl = System.getProperty("line.separator"); //$NON-NLS-1$
        sb.append(nl).append(nl);
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
        scrollPane.setPreferredSize(new Dimension(400, 400));
        textArea.setCaretPosition(0);
        
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
}
