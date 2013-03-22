/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.ikarus_systems.icarus.plugins.weblicht.WebserviceViewListModel;
import net.ikarus_systems.icarus.plugins.weblicht.webservice.Webservice;
import net.ikarus_systems.icarus.plugins.weblicht.webservice.WebserviceIOAttributes;
import net.ikarus_systems.icarus.plugins.weblicht.webservice.WebserviceProxy;
import net.ikarus_systems.icarus.plugins.weblicht.webservice.WebserviceRegistry;
import net.ikarus_systems.icarus.resources.ResourceDomain;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.UIDummies;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.util.Exceptions;
import net.ikarus_systems.icarus.util.KeyValuePair;
import net.ikarus_systems.icarus.util.MutablePrimitives.MutableBoolean;

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public final class DialogFactory {
	
	private static DialogFactory globalFactory;
	
	public static DialogFactory getGlobalFactory() {
		if(globalFactory==null) {
			synchronized (DialogFactory.class) {
				if(globalFactory==null)
					globalFactory= new DialogFactory(
							ResourceManager.getInstance().getGlobalDomain());
			}
		}
		
		return globalFactory;
	}
	
	protected final ResourceDomain resourceDomain;

	public DialogFactory(ResourceDomain resourceDomain) {
		Exceptions.testNullArgument(resourceDomain, "resourceDomain"); //$NON-NLS-1$
		
		this.resourceDomain = resourceDomain;
	}
	
	public ResourceDomain getResourceDomain() {
		return resourceDomain;
	}

	public void showError(Component parent, String title, 
			String message, Object...params) {
		
		BasicDialogBuilder builder = new BasicDialogBuilder(getResourceDomain());
		
		builder.setTitle(title);
		builder.setMessage(message, params);
		builder.setErrorType();
		builder.setOptions("ok"); //$NON-NLS-1$
		
		builder.showDialog(parent);
	}

	public void showDetailedError(Component parent, String title, 
			String message, Throwable t, Object...params) {
		
		JPanel dummy = new JPanel();
		UIDummies.createDefaultErrorOutput(dummy, t);
		
		BasicDialogBuilder builder = new BasicDialogBuilder(getResourceDomain());
		
		builder.setTitle(title);
		builder.setMessage(message, params);
		builder.addMessage(dummy);
		builder.setErrorType();
		builder.setOptions("ok"); //$NON-NLS-1$
		
		// TODO
		
		builder.showDialog(parent);
	}

	public void showInfo(Component parent, String title, 
			String message, Object... params) {
		
		BasicDialogBuilder builder = new BasicDialogBuilder(getResourceDomain());
		
		builder.setTitle(title);
		builder.setMessage(message, params);
		builder.setInfoType();
		builder.setOptions("ok"); //$NON-NLS-1$
		
		builder.showDialog(parent);
	}

	public void showWarning(Component parent, String title, 
			String message, Object... params) {
		
		BasicDialogBuilder builder = new BasicDialogBuilder(getResourceDomain());
		
		builder.setTitle(title);
		builder.setMessage(message, params);
		builder.setWarningType();
		builder.setOptions("ok"); //$NON-NLS-1$
		
		builder.showDialog(parent);
	}

	public boolean showConfirm(Component parent, String title, 
			String message, Object... params) {
		
		BasicDialogBuilder builder = new BasicDialogBuilder(getResourceDomain());
		
		builder.setTitle(title);
		builder.setMessage(message, params);
		builder.setQuestionType();
		builder.setOptions("yes", "no"); //$NON-NLS-1$ //$NON-NLS-2$
		
		builder.showDialog(parent);
		
		return builder.isYesValue();
	}

	public boolean showCheckedConfirm(Component parent, MutableBoolean output, 
			String title, String info, String message, Object... params) {

		if(info!=null && resourceDomain!=null)
			info = resourceDomain.get(info);
		JCheckBox checkBox = new JCheckBox(info);
		checkBox.setSelected(output.getValue());
		
		BasicDialogBuilder builder = new BasicDialogBuilder(getResourceDomain());
		
		builder.setTitle(title);
		builder.setMessage(message, params);
		builder.setQuestionType();
		builder.setOptions("yes", "no"); //$NON-NLS-1$ //$NON-NLS-2$
		
		builder.showDialog(parent);
		output.setValue(checkBox.isSelected());
		
		return builder.isYesValue();
	}

	public boolean showWarningConfirm(Component parent, String title, 
			String message, Object... params) {
		
		BasicDialogBuilder builder = new BasicDialogBuilder(getResourceDomain());
		
		builder.setTitle(title);
		builder.setMessage(message, params);
		builder.setWarningType();
		builder.setOptions("yes", "no"); //$NON-NLS-1$ //$NON-NLS-2$
		
		builder.showDialog(parent);
		
		return builder.isYesValue();
	}
	
	private JTextArea createTextArea() {
		JTextArea textArea = new JTextArea() {

			private static final long serialVersionUID = -3234388779826990121L;

			// force resizing according to the enclosing scroll pane's width
			@Override
			public boolean getScrollableTracksViewportWidth() {
				return true;
			}
		};
		textArea.setPreferredSize(new Dimension(320, 190));
		UIUtil.createUndoSupport(textArea, 75);
		UIUtil.addPopupMenu(textArea, UIUtil.createDefaultTextMenu(textArea, true));
		
		JScrollPane scrollPane = new JScrollPane(textArea);
		JPanel container = new JPanel(new BorderLayout());
		container.add(scrollPane, BorderLayout.CENTER);
		
		textArea.putClientProperty("container", container);		 //$NON-NLS-1$
		textArea.setText(null);
		textArea.setToolTipText(null);
		
		return textArea;
	}
	
	private Component getContainer(JComponent comp) {
		Object container = comp.getClientProperty("container"); //$NON-NLS-1$
		return container instanceof Component ? (Component)container : comp;
	}
	
	public String showTextInputDialog(Component parent, String title, 
			String message, Object...params) {

		JTextArea textArea = createTextArea();
		BasicDialogBuilder builder = new BasicDialogBuilder(getResourceDomain());
		
		builder.setTitle(title);
		builder.setMessage(message, params);
		builder.addMessage(getContainer(textArea));
		builder.setPlainType();
		builder.setOptions("ok", "cancel"); //$NON-NLS-1$ //$NON-NLS-2$
		
		builder.showDialog(parent);
		
		String text = textArea.getText();
		
		return builder.isYesValue() ? text : null;
	}
	
	public void showTextOutputDialog(Component parent, String title, 
			String message, String content, Object...params) {

		JTextArea textArea = createTextArea();
		BasicDialogBuilder builder = new BasicDialogBuilder(getResourceDomain());
		
		builder.setTitle(title);
		builder.setMessage(message, params);
		builder.addMessage(getContainer(textArea));
		builder.setPlainType();
		builder.setOptions("ok"); //$NON-NLS-1$
		
		builder.showDialog(parent);
	}
	
	public KeyValuePair<String, String> showPropertyEditDialog(
			Component parent, String title, String message, 
			String key, String value, Object params) {

		BasicDialogBuilder builder = new BasicDialogBuilder(getResourceDomain());
		
		JTextArea valueArea = createTextArea();
		valueArea.setText(value);
		JTextField keyField = new JTextField(30);
		keyField.setText(key);
		
		builder.setTitle(title);
		builder.setMessage(message, params);
		builder.addMessage("key"); //$NON-NLS-1$
		builder.addMessage(keyField);
		builder.addMessage("value"); //$NON-NLS-1$
		builder.addMessage(getContainer(valueArea));
		builder.setPlainType();
		builder.setOptions("ok", "cancel"); //$NON-NLS-1$ //$NON-NLS-2$
		
		builder.showDialog(parent);
		
		if(!builder.isYesValue()) {
			return null;
		}
		
		key = keyField.getText();
		value = valueArea.getText();
		
		return new KeyValuePair<String, String>(key, value);
	}
	
	public String showInputDialog(Component parent, String title, String message, 
			String text, Object params) {

		BasicDialogBuilder builder = new BasicDialogBuilder(getResourceDomain());
		
		JTextField textField = new JTextField(30);
		textField.setText(text);
		
		builder.setTitle(title);
		builder.setMessage(message, params);
		builder.addMessage(textField);
		builder.setPlainType();
		builder.setOptions("ok", "cancel"); //$NON-NLS-1$ //$NON-NLS-2$
		
		builder.showDialog(parent);
		
		if(!builder.isYesValue()) {
			return null;
		}
		
		text = textField.getText();
		
		return text;
	}
	
	
}
