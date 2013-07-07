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
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;

import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.core.View;
import net.ikarus_systems.icarus.ui.UIDummies;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.actions.ActionManager;
import net.ikarus_systems.icarus.util.CorruptedStateException;
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
		
		// Load actions
		URL actionLocation = TextInputView.class.getResource("text-input-view-actions.xml"); //$NON-NLS-1$
		if(actionLocation==null)
			throw new CorruptedStateException("Missing resources: text-input-view-actions.xml"); //$NON-NLS-1$
		
		try {
			getDefaultActionManager().loadActions(actionLocation);
		} catch (IOException e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to load actions from file", e); //$NON-NLS-1$
			UIDummies.createDefaultErrorOutput(container, e);
			return;
		}		
		
		inputArea = new JTextArea();
		inputArea.setLineWrap(true);
		inputArea.setWrapStyleWord(true);
		inputArea.setBorder(UIUtil.defaultContentBorder);
		UIUtil.createUndoSupport(inputArea, 20);
		UIUtil.addPopupMenu(inputArea, UIUtil.createDefaultTextMenu(inputArea, true));
		
		ActionManager actionManager = getDefaultActionManager();
		ActionMap actionMap = inputArea.getActionMap();
		actionManager.addAction("plugins.languageTools.textInputView.undoAction", actionMap.get("undo")); //$NON-NLS-1$ //$NON-NLS-2$
		actionManager.addAction("plugins.languageTools.textInputView.redoAction", actionMap.get("redo")); //$NON-NLS-1$ //$NON-NLS-2$
		actionManager.addAction("plugins.languageTools.textInputView.clearAction", actionMap.get("clear")); //$NON-NLS-1$ //$NON-NLS-2$
		actionManager.addAction("plugins.languageTools.textInputView.selectAllAction", actionMap.get(DefaultEditorKit.selectAllAction)); //$NON-NLS-1$
		actionManager.addAction("plugins.languageTools.textInputView.cutAction", actionMap.get(DefaultEditorKit.cutAction)); //$NON-NLS-1$
		actionManager.addAction("plugins.languageTools.textInputView.copyAction", actionMap.get(DefaultEditorKit.copyAction)); //$NON-NLS-1$
		actionManager.addAction("plugins.languageTools.textInputView.pasteAction", actionMap.get(DefaultEditorKit.pasteAction)); //$NON-NLS-1$
				
		JScrollPane scrollPane = new JScrollPane(inputArea);
		scrollPane.setBorder(null);
		UIUtil.defaultSetUnitIncrement(scrollPane);
		
		toolBar = createToolBar();
		
		container.setLayout(new BorderLayout());
		container.add(toolBar, BorderLayout.NORTH);
		container.add(scrollPane, BorderLayout.CENTER);
		
		registerActionCallbacks();
		
		refreshActions();
	}
	
	protected void refreshActions() {
		// no-op
	}
	
	protected void registerActionCallbacks() {
		// for subclasses
	}
	
	protected JToolBar createToolBar() {
		return getDefaultActionManager().createToolBar(
				"plugins.languageTools.textInputView.toolBarList", null); //$NON-NLS-1$
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
