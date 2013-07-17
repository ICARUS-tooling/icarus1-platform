/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.ui.dialog;

import java.awt.Component;
import java.util.Stack;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import de.ims.icarus.Core;
import de.ims.icarus.resources.ResourceDomain;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.util.Exceptions;
import de.ims.icarus.util.Options;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class BasicDialogBuilder extends DialogBuilder {
	
	protected JOptionPane optionPane;
	
	protected String title = ""; //$NON-NLS-1$
	
	private static Stack<JTextArea> labelPool;
	
	private static final Object indicatorValue = new Object();
	
	private static synchronized JTextArea getLabel() {
		JTextArea label = null ;
		if(labelPool!=null && !labelPool.isEmpty()) {
			label = labelPool.pop();
		}
		
		if(label==null) {
			label = new JTextArea();
			label.setEditable(false);
			label.setFont(UIManager.getFont("Label.font")); //$NON-NLS-1$
			label.setForeground(UIManager.getColor("Label.foreground")); //$NON-NLS-1$
			label.setBackground(UIManager.getColor("Label.background")); //$NON-NLS-1$
			label.setBorder(UIUtil.defaultContentBorder);
			label.setLineWrap(true);
			label.setWrapStyleWord(true);
			label.putClientProperty("builder", indicatorValue); //$NON-NLS-1$
			label.setSize(DialogFactory.DEFAULT_TEXT_WIDTH, 100);
		}
		
		return label;
	}
	
	private static synchronized void recycleLabel(JTextArea label) {
		if(label==null)
			throw new IllegalArgumentException("Invalid label"); //$NON-NLS-1$
		
		if(!indicatorValue.equals(label.getClientProperty("builder"))); //$NON-NLS-1$
		
		if(labelPool==null) {
			labelPool = new Stack<>();
		}
		
		labelPool.push(label);
	}
	
	private static Object createMessage(DialogBuilder builder, 
			Object message, Object...params) {
		if(message instanceof String) {
			JTextArea label = getLabel();
			
			if(builder.getResourceDomain()!=null) {
				message = builder.getResourceDomain().get((String) message, params);
			}
			
			label.setText((String) message);
			
			//label.setSize(label.getPreferredSize());
			
			return label;
		} else {
			return message;
		}
	}

	/**
	 * @param resourceDomain
	 */
	public BasicDialogBuilder(ResourceDomain resourceDomain) {
		super(resourceDomain);
		initOptionPane();
	}

	/**
	 * 
	 */
	public BasicDialogBuilder() {
		super();
		initOptionPane();
	}
	
	protected void initOptionPane() {
		optionPane = new JOptionPane(null);
		optionPane.setOptionType(JOptionPane.DEFAULT_OPTION);
		optionPane.setMessageType(JOptionPane.PLAIN_MESSAGE);
	}
	
	public JOptionPane getOptionPane() {
		return optionPane;
	}
	
	public void setTitle(String title) {
		Exceptions.testNullArgument(title, "title"); //$NON-NLS-1$
		if(resourceDomain!=null)
			title = resourceDomain.get(title);
		
		this.title = title;
	}
	
	public void setText(String text) {
		setText(text, (Object[])null);
	}
	
	public void setText(String text, Object...params) {
		Exceptions.testNullArgument(text, "text"); //$NON-NLS-1$
		
		optionPane.setMessage(createMessage(this, text, params));
	}
	
	public void setOptions(Object...options) {
		if(resourceDomain!=null) {
			for(int i=0; i<options.length; i++) {
				if(options[i] instanceof String)
					options[i] = resourceDomain.get((String)options[i]);
			}
		}
		
		optionPane.setOptions(options);
	}
	
	public void setMessage(Object message, Object...params) {
		optionPane.setMessage(createMessage(this, message, params));
	}
	
	public void addMessage(Object message, Object...params) {
		
		message = createMessage(this, message, params);
		
		Object currentMessage = optionPane.getMessage();
		Object[] newMessage = null;
		if(currentMessage==null) {
			// No message set so far
			newMessage = new Object[]{message};
		} else if(currentMessage instanceof Object[]) {
			// Already multiple messages present in the pane
			// -> append our new option object
			int length = ((Object[])currentMessage).length;
			newMessage = new Object[length+1];
			System.arraycopy(currentMessage, 0, newMessage, 0, length);
			newMessage[length] = message;
		} else {
			// Exactly one message defined so far
			newMessage = new Object[]{currentMessage, message};
		}
		
		optionPane.setMessage(newMessage);
	}
	
	public void setIcon(Icon icon) {
		optionPane.setIcon(icon);
	}
	
	public void setInitialValue(Object value) {
		optionPane.setInitialValue(value);
	}
	
	public void setInitialSelectionValue(Object value) {
		optionPane.setInitialSelectionValue(value);
	}
	
	public void setOptionType(int type) {
		optionPane.setOptionType(type);
	}
	
	public void setMessageType(int type) {
		optionPane.setMessageType(type);
	}
	
	public void setWarningType() {
		setMessageType(JOptionPane.WARNING_MESSAGE);
	}
	
	public void setErrorType() {
		setMessageType(JOptionPane.ERROR_MESSAGE);
	}
	
	public void setInfoType() {
		setMessageType(JOptionPane.INFORMATION_MESSAGE);
	}
	
	public void setQuestionType() {
		setMessageType(JOptionPane.QUESTION_MESSAGE);
	}
	
	public void setPlainType() {
		setMessageType(JOptionPane.PLAIN_MESSAGE);
	}

	/**
	 * @see de.ims.icarus.ui.dialog.DialogBuilder#showDialog(java.awt.Window)
	 */
	@Override
	public void showDialog(Component parent, Options options) { 
		if(options==null) {
			options = Options.emptyOptions;
		}
		
		JDialog dialog = optionPane.createDialog(parent, title);
		dialog.setIconImages(Core.getIconImages());
		
		// Apply options
		//dialog.setResizable(options.get(RESIZABLE_OPTION, false));
		dialog.setResizable(true);
		//dialog.setMinimumSize(dialog.getPreferredSize());
		
		dialog.setVisible(true);
		dialog.dispose();
		
		// Recycle used labels
		Object message = optionPane.getMessage();
		if(message instanceof Object[]) {
			Object[] messages = (Object[]) message;
			for(Object item : messages) {
				if(item instanceof JTextArea) {
					recycleLabel((JTextArea) item);
				}
			}
		} else if(message instanceof JTextArea) {
			recycleLabel((JTextArea) message);
		}
	}

	public void showDialog(Component parent) {
		showDialog(parent, null);
	}
	
	public Object getSelectedValue() {
		return optionPane.getValue();
	}
	
	public int getValue() {
		Object selectedValue = optionPane.getValue();		
		Object[] options = optionPane.getOptions();

        if(selectedValue == null)
            return JOptionPane.CLOSED_OPTION;
        if(options == null) {
            if(selectedValue instanceof Integer)
                return ((Integer)selectedValue).intValue();
            return JOptionPane.CLOSED_OPTION;
        }
        for(int counter = 0, maxCounter = options.length;
            counter < maxCounter; counter++) {
            if(options[counter].equals(selectedValue))
                return counter;
        }
        return JOptionPane.CLOSED_OPTION;
	}
	
	public Object[] getSelectionValues() {
		return optionPane.getSelectionValues();
	}
	
	public boolean isOkValue() {
		return JOptionPane.OK_OPTION==getValue();
	}
	
	public boolean isYesValue() {
		return JOptionPane.YES_OPTION==getValue();
	}
	
	public boolean isNoValue() {
		return JOptionPane.NO_OPTION==getValue();
	}
	
	public boolean isCancelValue() {
		return JOptionPane.CANCEL_OPTION==getValue();
	}
}
