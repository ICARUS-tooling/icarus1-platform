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
package de.ims.icarus.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import de.ims.icarus.resources.ResourceDomain;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.UIDummies;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.helper.Editor;
import de.ims.icarus.util.Exceptions;
import de.ims.icarus.util.KeyValuePair;
import de.ims.icarus.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.strings.StringUtil;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class DialogFactory {

	private static DialogFactory globalFactory;

	public static final int DEFAULT_TEXT_WIDTH = 300;

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

	private static final Map<Integer, Object[]> _options = new HashMap<>();

	public static Object[] getOptions(int id) {
		return _options.get(id).clone();
	}

	public synchronized static int registerOptions(Object...options) {
		if(options==null || options.length==0)
			throw new NullPointerException("Invalid options"); //$NON-NLS-1$
		int id = _options.size();

		_options.put(id, options);

		return id;
	}

	public static final int YES_NO_OPTION = registerOptions("yes", "no"); //$NON-NLS-1$ //$NON-NLS-2$;
	public static final int YES_NO_CANCEL_OPTION = registerOptions("yes", "no", "cancel"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$;
	public static final int YES_OPTION = registerOptions("yes"); //$NON-NLS-1$
	public static final int CONTINUE_OPTION = registerOptions("constinue"); //$NON-NLS-1$
	public static final int CONTINUE_CANCEL_OPTION = registerOptions("continue", "cancel"); //$NON-NLS-1$ //$NON-NLS-2$
	public static final int OK_OPTION = registerOptions("ok"); //$NON-NLS-1$
	public static final int OK_CANCEL_OPTION = registerOptions("ok", "cancel"); //$NON-NLS-1$ //$NON-NLS-2$

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
		showError(parent, OK_OPTION, title, message, params);
	}

	public void showError(Component parent, int options, String title,
			String message, Object...params) {

		BasicDialogBuilder builder = newBuilder();

		builder.setTitle(title);
		builder.setMessage(message, params);
		builder.setErrorType();
		builder.setOptions(getOptions(options));

		builder.showDialog(parent);
	}

	public void showDetailedError(Component parent, String title,
			String message, Throwable t, Object...params) {
		showDetailedError(parent, OK_OPTION, title, message, t, params);
	}

	public void showDetailedError(Component parent, int options, String title,
			String message, Throwable t, Object...params) {

		JPanel dummy = new JPanel();
		UIDummies.createDefaultErrorOutput(dummy, t);

		BasicDialogBuilder builder = new BasicDialogBuilder(getResourceDomain());

		builder.setTitle(title);
		builder.setMessage(message, params);
		builder.addMessage(dummy);
		builder.setErrorType();
		builder.setOptions(getOptions(options));

		// TODO

		builder.showDialog(parent);
	}

	public int showPlain(Component parent, String title,
			String message, Object... params) {
		return showPlain(parent, OK_OPTION, title, message, params);
	}

	public int showPlain(Component parent, int options, String title,
			String message, Object... params) {

		BasicDialogBuilder builder = new BasicDialogBuilder(getResourceDomain());

		builder.setTitle(title);
		builder.setMessage(message, params);
		builder.setPlainType();
		builder.setOptions(getOptions(options));

		builder.showDialog(parent);
		return builder.getValue();
	}

	public int showInfo(Component parent, String title,
			String message, Object... params) {
		return showInfo(parent, OK_OPTION, title, message, params);
	}

	public int showInfo(Component parent, int options, String title,
			String message, Object... params) {

		BasicDialogBuilder builder = new BasicDialogBuilder(getResourceDomain());

		builder.setTitle(title);
		builder.setMessage(message, params);
		builder.setInfoType();
		builder.setOptions(getOptions(options));

		builder.showDialog(parent);
		return builder.getValue();
	}

	public int showWarning(Component parent, String title,
			String message, Object... params) {
		return showWarning(parent, OK_OPTION, title, message, params);
	}

	public int showWarning(Component parent, int options, String title,
			String message, Object... params) {

		BasicDialogBuilder builder = new BasicDialogBuilder(getResourceDomain());

		builder.setTitle(title);
		builder.setMessage(message, params);
		builder.setWarningType();
		builder.setOptions(getOptions(options));

		builder.showDialog(parent);
		return builder.getValue();
	}

	public boolean showConfirm(Component parent, String title,
			String message, Object... params) {
		return showConfirm(parent, YES_NO_OPTION, title, message, params);
	}

	public boolean showConfirm(Component parent, int options, String title,
			String message, Object... params) {

		BasicDialogBuilder builder = new BasicDialogBuilder(getResourceDomain());

		builder.setTitle(title);
		builder.setMessage(message, params);
		builder.setQuestionType();
		builder.setOptions(getOptions(options));

		builder.showDialog(parent);

		return builder.isYesValue();
	}

	public boolean showCheckedConfirm(Component parent, MutableBoolean output,
			String title, String info, String message, Object... params) {
		return showCheckedConfirm(parent, YES_NO_OPTION, output, title, info, message, params);
	}

	public boolean showCheckedConfirm(Component parent, int options, MutableBoolean output,
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
		builder.setOptions(getOptions(options));

		builder.showDialog(parent);
		output.setValue(checkBox.isSelected());

		return builder.isYesValue();
	}

	public boolean showWarningConfirm(Component parent, String title,
			String message, Object... params) {
		return showWarningConfirm(parent, YES_NO_OPTION, title, message, params);
	}

	public boolean showWarningConfirm(Component parent, int options, String title,
			String message, Object... params) {

		BasicDialogBuilder builder = new BasicDialogBuilder(getResourceDomain());

		builder.setTitle(title);
		builder.setMessage(message, params);
		builder.setWarningType();
		builder.setOptions(getOptions(options));

		builder.showDialog(parent);

		return builder.isYesValue();
	}
	public boolean showGenericDialog(Component parent, int options, String title,
			String message, Component comp, boolean resizable) {
		return showGenericDialog(parent, title, message, comp, resizable, getOptions(options));
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

	public boolean showOverwriteFileDialog(Component parent, Path file) {
		return showOverwriteFileDialog(parent, YES_NO_OPTION, file);
	}

	public boolean showOverwriteFileDialog(Component parent, int options, Path file) {
		String path = file.toString();
		path = StringUtil.fit(path, 50);

		BasicDialogBuilder builder = new BasicDialogBuilder(getResourceDomain());

		builder.setTitle("dialogs.overwriteFile.title"); //$NON-NLS-1$
		builder.addMessage("dialogs.overwriteFile.message", path); //$NON-NLS-1$
		builder.setPlainType();
		builder.setOptions(getOptions(options));

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
		return showTextInputDialog(parent, OK_CANCEL_OPTION, title, message, params);
	}

	public String showTextInputDialog(Component parent, int options, String title,
			String message, Object...params) {

		JTextArea textArea = createTextArea();
		BasicDialogBuilder builder = new BasicDialogBuilder(getResourceDomain());

		builder.setTitle(title);
		builder.setMessage(message, params);
		builder.addMessage(getContainer(textArea));
		builder.setPlainType();
		builder.setOptions(getOptions(options));

		builder.showDialog(parent);

		String text = textArea.getText();

		return builder.isYesValue() ? text : null;
	}

	public void showTextOutputDialog(Component parent, String title,
			String message, String content, Object...params) {
		showTextOutputDialog(parent, YES_OPTION, title, message, content, params);
	}

	public void showTextOutputDialog(Component parent, int options, String title,
			String message, String content, Object...params) {

		JTextArea textArea = createTextArea();
		BasicDialogBuilder builder = new BasicDialogBuilder(getResourceDomain());

		builder.setTitle(title);
		builder.setMessage(message, params);
		builder.addMessage(getContainer(textArea));
		builder.setPlainType();
		builder.setOptions(getOptions(options));

		builder.showDialog(parent);
	}

	public KeyValuePair<String, String> showPropertyEditDialog(
			Component parent, String title, String message,
			String key, String value, Object...params) {
		return showPropertyEditDialog(parent, OK_CANCEL_OPTION,
				title, message,	key, value, params);
	}

	public KeyValuePair<String, String> showPropertyEditDialog(
			Component parent, int options, String title, String message,
			String key, String value, Object...params) {

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
		builder.setOptions(getOptions(options));

		builder.showDialog(parent);

		if(!builder.isYesValue()) {
			return null;
		}

		key = keyField.getText();
		value = valueArea.getText();

		return new KeyValuePair<String, String>(key, value);
	}

	public String showInputDialog(Component parent, String title, String message,
			String text, Object...params) {
		return showInputDialog(parent, OK_CANCEL_OPTION, title, message, text, params);
	}

	public String showInputDialog(Component parent, int options, String title,
			String message,	String text, Object...params) {

		BasicDialogBuilder builder = new BasicDialogBuilder(getResourceDomain());

		JTextField textField = new JTextField(30);
		textField.setText(text);

		builder.setTitle(title);
		builder.setMessage(message, params);
		builder.addMessage(textField);
		builder.setPlainType();
		builder.setOptions(getOptions(options));

		builder.showDialog(parent);

		if(!builder.isYesValue()) {
			return null;
		}

		text = textField.getText();

		return text;
	}

	public Path showDestinationFileDialog(Component parent, String title,
			Path directory) throws IOException {
		return showDestinationFileDialog(parent, title, directory, null);
	}

	public Path showDestinationFileDialog(Component parent, String title,
			Path directory, FileFilter filter) throws IOException {
		JFileChooser fileChooser = getFileChooser();
		fileChooser.setCurrentDirectory(directory.toFile());
		fileChooser.setSelectedFile(null);
		fileChooser.setDialogTitle(resourceDomain.get(title));
		fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		fileChooser.setApproveButtonText(resourceDomain.get("select")); //$NON-NLS-1$
		fileChooser.setFileFilter(filter);

		while(true) {
			int result = fileChooser.showDialog(parent, null);
			if(result==JFileChooser.CANCEL_OPTION) {
				return null;
			}

			Path file = fileChooser.getSelectedFile().toPath();

			if(file==null) {
				continue;
			}

			if(Files.notExists(file) || Files.size(file)==0) {
				return file;
			}

			if(Files.exists(file) && Files.size(file)>0 &&
					showOverwriteFileDialog(parent, file)) {
				return file;
			}
		}
	}

	public Path showSourceFileDialog(Component parent, String title,
			Path directory) {
		return showSourceFileDialog(parent, title, directory, null);
	}

	public Path showSourceFileDialog(Component parent, String title,
			Path directory, FileFilter filter) {
		JFileChooser fileChooser = getFileChooser();
		fileChooser.setCurrentDirectory(directory.toFile());
		fileChooser.setSelectedFile(null);
		fileChooser.setDialogTitle(resourceDomain.get(title));
		fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
		fileChooser.setApproveButtonText(resourceDomain.get("select")); //$NON-NLS-1$
		fileChooser.setFileFilter(filter);

		int result = fileChooser.showDialog(parent, null);
		if(result==JFileChooser.CANCEL_OPTION) {
			return null;
		}

		return fileChooser.getSelectedFile().toPath();
	}

	public <T extends Object> boolean showEditorDialog(Component parent,
			T data, Editor<T> editor, String title) {
		return showEditorDialog(parent, OK_CANCEL_OPTION, data, editor, title);
	}

	public <T extends Object> boolean showEditorDialog(Component parent, int options,
			T data, Editor<T> editor, String title) {

		editor.setEditingItem(data);

		BasicDialogBuilder builder = new BasicDialogBuilder(getResourceDomain());

		builder.setTitle(title);
		builder.setMessage(editor.getEditorComponent());
		builder.setPlainType();
		builder.setOptions(getOptions(options));

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
