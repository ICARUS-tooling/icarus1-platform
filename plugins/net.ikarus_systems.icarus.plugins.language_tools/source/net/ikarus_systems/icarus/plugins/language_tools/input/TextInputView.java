/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.language_tools.input;

import java.awt.BorderLayout;

import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;

import net.ikarus_systems.icarus.plugins.core.View;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.util.mpi.Commands;
import net.ikarus_systems.icarus.util.mpi.Message;
import net.ikarus_systems.icarus.util.mpi.ResultMessage;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TextInputView extends View {
	
	protected JTextArea inputArea;
	
	protected JToolBar toolBar;
	
	public TextInputView() {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		inputArea = new JTextArea();
		inputArea.setLineWrap(true);
		inputArea.setWrapStyleWord(true);
		inputArea.setBorder(UIUtil.defaultContentBorder);
		UIUtil.createUndoSupport(inputArea, 20);
		UIUtil.addPopupMenu(inputArea, UIUtil.createDefaultTextMenu(inputArea, true));
				
		JScrollPane scrollPane = new JScrollPane(inputArea);
		scrollPane.setBorder(null);
		UIUtil.defaultSetUnitIncrement(scrollPane);

		ActionMap actionMap = inputArea.getActionMap();
		
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setRollover(true);
		
		toolBar.add(Box.createHorizontalGlue());
		toolBar.add(actionMap.get("undo")); //$NON-NLS-1$
		toolBar.add(actionMap.get("redo")); //$NON-NLS-1$
		toolBar.addSeparator();
		toolBar.add(actionMap.get(DefaultEditorKit.cutAction));
		toolBar.add(actionMap.get(DefaultEditorKit.copyAction));
		toolBar.add(actionMap.get(DefaultEditorKit.pasteAction));
		toolBar.addSeparator();
		toolBar.add(actionMap.get(DefaultEditorKit.selectAllAction));
		toolBar.add(actionMap.get("clear")); //$NON-NLS-1$
		
		container.setLayout(new BorderLayout());
		container.add(toolBar, BorderLayout.NORTH);
		container.add(scrollPane, BorderLayout.CENTER);
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#reset()
	 */
	@Override
	public void reset() {
		inputArea.setText(null);
		// TODO clear undo-history as well?
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#reset()
	 */
	@Override
	public void close() {
		inputArea.setText(null);
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#handleRequest(net.ikarus_systems.icarus.util.mpi.Message)
	 */
	@Override
	protected ResultMessage handleRequest(Message message) throws Exception {
		if(Commands.GET_TEXT.equals(message.getCommand())) {
			return message.successResult(this, inputArea.getText());
		} else if(Commands.SET_TEXT.equals(message.getCommand())) {
			inputArea.setText((String) message.getData());
			return message.successResult(this, null);
		} else if(Commands.APPEND.equals(message.getCommand())) {
			Document doc = inputArea.getDocument();
			String text = (String)message.getData();
			doc.insertString(doc.getLength(), text, null);
			return message.successResult(this, null);
		} else {
			return message.unknownRequestResult(this);
		}
	}
}
