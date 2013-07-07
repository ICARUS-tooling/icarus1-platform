/*
 * $Revision: 49 $
 * $Date: 2013-06-22 00:08:17 +0200 (Sa, 22 Jun 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/ui/dialog/DialogFactory.java $
 *
 * $LastChangedDate: 2013-06-22 00:08:17 +0200 (Sa, 22 Jun 2013) $ 
 * $LastChangedRevision: 49 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import de.ims.icarus.resources.ResourceDomain;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.UIDummies;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.helper.Editor;
import de.ims.icarus.util.Exceptions;
import de.ims.icarus.util.KeyValuePair;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.StringUtil;
import de.ims.icarus.util.MutablePrimitives.MutableBoolean;


/**
 * @author Markus Gärtner 
 * @version $Id: DialogFactory.java 49 2013-06-21 22:08:17Z mcgaerty $
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
	
	private JFileChooser sharedFileChooser;
	
	protected final ResourceDomain resourceDomain;

	public DialogFactory(ResourceDomain resourceDomain) {
		Exceptions.testNullArgument(resourceDomain, "resourceDomain"); //$NON-NLS-1$
		
		this.resourceDomain = resourceDomain;
	}
	
	private JFileChooser getFileChooser() {
		if(sharedFileChooser==null) {
			synchronized (this) {
				if(sharedFileChooser==null) {
					sharedFileChooser = new JFileChooser();
				}
			}
		}
		return sharedFileChooser;
	}
	
	public ResourceDomain getResourceDomain() {
		return resourceDomain;
	}
	
	public BasicDialogBuilder newBuilder() {
		return new BasicDialogBuilder(getResourceDomain());
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

	public void showPlain(Component parent, String title, 
			String message, Object... params) {
		
		BasicDialogBuilder builder = new BasicDialogBuilder(getResourceDomain());
		
		builder.setTitle(title);
		builder.setMessage(message, params);
		builder.setPlainType();
		builder.setOptions("ok"); //$NON-NLS-1$
		
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

		if(info!=null && resourceDomain!=null) {
			info = resourceDomain.get(info);
		}
		
		if(info==null) {
			info = ResourceManager.getInstance().get("dialogs.rememberDecision"); //$NON-NLS-1$
		}
		
		JCheckBox checkBox = new JCheckBox(info);
		checkBox.setSelected(output.getValue());
		
		BasicDialogBuilder builder = new BasicDialogBuilder(getResourceDomain());
		
		builder.setTitle(title);
		builder.setMessage(message, params);
		builder.addMessage(checkBox);
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
	
	public boolean showGenericDialog(Component parent, String title,
			String message, Component comp, boolean resizable, Object...options) {
		
		BasicDialogBuilder builder = new BasicDialogBuilder(getResourceDomain());
		
		builder.setTitle(title);
		builder.setMessage(message);
		builder.addMessage(comp);
		builder.setPlainType();
		builder.setOptions(options);
		
		Options opts = new Options();
		opts.put(DialogBuilder.RESIZABLE_OPTION, resizable);
		
		builder.showDialog(parent, opts);
		
		return builder.isYesValue();
	}
	
	public boolean showOverwriteFileDialog(Component parent, File file) {
		String path = file.getAbsolutePath();
		path = StringUtil.fit(path, 50);
		
		BasicDialogBuilder builder = new BasicDialogBuilder(getResourceDomain());
		
		builder.setTitle("dialogs.overwriteFile.title"); //$NON-NLS-1$
		builder.addMessage("dialogs.overwriteFile.message", path); //$NON-NLS-1$
		builder.setPlainType();
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
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		
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
	
	public File showDestinationFileDialog(Component parent, String title, 
			File directory) {
		JFileChooser fileChooser = getFileChooser();
		fileChooser.setCurrentDirectory(directory);
		fileChooser.setSelectedFile(null);
		fileChooser.setDialogTitle(resourceDomain.get(title));
		fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		fileChooser.setApproveButtonText(resourceDomain.get("select")); //$NON-NLS-1$
		
		while(true) {
			int result = fileChooser.showDialog(parent, null);
			if(result==JFileChooser.CANCEL_OPTION) {
				return null;
			}
			
			File file = fileChooser.getSelectedFile();
			
			if(file==null) {
				continue;
			}
			
			if(!file.exists() || file.length()==0) {
				return file;
			}
			
			if(file.exists() && file.length()>0 && 
					showOverwriteFileDialog(parent, file)) {
				return file;
			}
		}
	}
	
	public File showSourceFileDialog(Component parent, String title, 
			File directory) {
		JFileChooser fileChooser = getFileChooser();
		fileChooser.setCurrentDirectory(directory);
		fileChooser.setSelectedFile(null);
		fileChooser.setDialogTitle(resourceDomain.get(title));
		fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
		fileChooser.setApproveButtonText(resourceDomain.get("select")); //$NON-NLS-1$
		
		int result = fileChooser.showDialog(parent, null);
		if(result==JFileChooser.CANCEL_OPTION) {
			return null;
		}
			
		return fileChooser.getSelectedFile();
	}
	
	public <T extends Object> boolean showEditorDialog(Component parent, 
			T data, Editor<T> editor, String title) {

		editor.setEditingItem(data);

		BasicDialogBuilder builder = new BasicDialogBuilder(getResourceDomain());
		
		builder.setTitle(title);
		builder.setMessage(editor.getEditorComponent());
		builder.setPlainType();
		builder.setOptions("ok", "cancel"); //$NON-NLS-1$ //$NON-NLS-2$
		
		builder.showDialog(parent);
		
		if(!builder.isYesValue()) {
			return false;
		}
		
		if(editor.hasChanges()) {
			editor.applyEdit();
		}
		
		return true;
	}
}
