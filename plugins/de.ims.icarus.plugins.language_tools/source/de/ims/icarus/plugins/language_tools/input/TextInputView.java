/*
 * $Revision: 41 $
 * $Date: 2013-05-21 00:46:47 +0200 (Di, 21 Mai 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.language_tools/source/net/ikarus_systems/icarus/plugins/language_tools/input/TextInputView.java $
 *
 * $LastChangedDate: 2013-05-21 00:46:47 +0200 (Di, 21 Mai 2013) $ 
 * $LastChangedRevision: 41 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.plugins.language_tools.input;

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

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.core.View;
import de.ims.icarus.ui.UIDummies;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.mpi.Commands;
import de.ims.icarus.util.mpi.Message;
import de.ims.icarus.util.mpi.ResultMessage;


/**
 * @author Markus Gärtner
 * @version $Id: TextInputView.java 41 2013-05-20 22:46:47Z mcgaerty $
 *
 */
public class TextInputView extends View {
	
	protected JTextArea inputArea;
	
	protected JToolBar toolBar;
	
	public TextInputView() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.plugins.core.View#init(javax.swing.JComponent)
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
	 * @see de.ims.icarus.plugins.core.View#reset()
	 */
	@Override
	public void reset() {
		inputArea.setText(null);
		// TODO clear undo-history as well?
	}

	/**
	 * @see de.ims.icarus.plugins.core.View#reset()
	 */
	@Override
	public void close() {
		inputArea.setText(null);
	}

	/**
	 * @see de.ims.icarus.plugins.core.View#handleRequest(de.ims.icarus.util.mpi.Message)
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
