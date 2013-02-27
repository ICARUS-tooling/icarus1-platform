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

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.ikarus_systems.icarus.plugins.core.View;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.util.id.DefaultIdentity;
import net.ikarus_systems.icarus.util.id.Identity;
import net.ikarus_systems.icarus.util.opi.Commands;
import net.ikarus_systems.icarus.util.opi.Message;
import net.ikarus_systems.icarus.util.opi.ResultMessage;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TextInputView extends View {

	private Identity identity;
	
	private JTextArea inputArea;
	
	public TextInputView() {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identifiable#getIdentity()
	 */
	@Override
	public Identity getIdentity() {
		if(identity==null) {
			// Create identity object
			DefaultIdentity id = new DefaultIdentity(getExtension().getId(), this);
			id.setNameKey("plugins.languageTools.textInputView.identity.name"); //$NON-NLS-1$
			id.setDescriptionKey("plugins.languageTools.textInputView.identity.description"); //$NON-NLS-1$
			id.setIconLocation(TextInputView.class.getResource("text-input-view.gif")); //$NON-NLS-1$
			id.lock();
			
			identity = id;
		}
		
		return identity;
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
				
		JScrollPane scrollPane = new JScrollPane(inputArea);
		scrollPane.setBorder(null);
		UIUtil.defaultSetUnitIncrement(scrollPane);
		
		container.setLayout(new BorderLayout());
		container.add(scrollPane, BorderLayout.CENTER);
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#close()
	 */
	@Override
	public void close() {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#isClosable()
	 */
	@Override
	public boolean isClosable() {
		return true;
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#reset()
	 */
	@Override
	public void reset() {
		inputArea.setText(null);
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#handleRequest(net.ikarus_systems.icarus.util.opi.Message)
	 */
	@Override
	protected ResultMessage handleRequest(Message message) throws Exception {
		if(Commands.GET_TEXT.equals(message.getCommand())) {
			return message.successResult(inputArea.getText());
		} else if(Commands.SET_TEXT.equals(message.getCommand())) {
			inputArea.setText((String) message.getData());
			return message.successResult(null);
		} else {
			return message.unknownRequestResult();
		}
	}

	private class Handler {
	}
	
	public final class CallbackHandler {
		
		private CallbackHandler() {
			// no-op
		}
	}
}
