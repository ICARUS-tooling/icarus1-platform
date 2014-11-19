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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.prosody.ui.view.sampa;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.TransferHandler;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.config.ConfigRegistry.Handle;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.core.View;
import de.ims.icarus.plugins.prosody.sampa.SampaIterator;
import de.ims.icarus.plugins.prosody.sampa.SampaMapper2;
import de.ims.icarus.plugins.prosody.sampa.SampaSet;
import de.ims.icarus.plugins.prosody.sampa.SampaValidator;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.list.FileListTransferHandler;
import de.ims.icarus.ui.list.ListUtils;
import de.ims.icarus.util.MutablePrimitives.MutableInteger;
import de.ims.icarus.util.ToolException;
import de.ims.icarus.util.classes.ClassUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SampaValidationView extends View {

	private JList<File> wordFilesList;
	private JList<File> syllableFilesList;

	private CallbackHandler callbackHandler;
	private Handler handler;

	private static final String configPath = "plugins.prosody.sampaValidation"; //$NON-NLS-1$

	public SampaValidationView() {
		// no-op
	}

	@Override
	public void init(JComponent container) {

		// Load actions
		if (!defaultLoadActions(SampaValidationView.class,
				"sampa-validation-view-actions.xml")) { //$NON-NLS-1$
			return;
		}

		// Init ui
		container.setLayout(new BorderLayout());

		FileListTransferHandler transferHandler = new FileListTransferHandler();
		FileListCellRenderer renderer = new FileListCellRenderer();

		wordFilesList = createFilesList(transferHandler, renderer);
		syllableFilesList = createFilesList(transferHandler, renderer);

		JPanel wordPanel = createListPanel(wordFilesList, "plugins.prosody.sampaValidationView.wordFileToolBarList"); //$NON-NLS-1$
		JPanel syllablePanel = createListPanel(syllableFilesList, "plugins.prosody.sampaValidationView.syllableFileToolBarList"); //$NON-NLS-1$

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, wordPanel, syllablePanel);
//		UIUtil.defaultHideSplitPaneDecoration(splitPane);
		splitPane.setResizeWeight(0.5);
		splitPane.setBorder(UIUtil.topLineBorder);

		container.add(splitPane, BorderLayout.CENTER);

		JToolBar toolBar = getDefaultActionManager().createToolBar(
				"plugins.prosody.sampaValidationView.toolBarList", null); //$NON-NLS-1$
		container.add(toolBar, BorderLayout.NORTH);

		registerActionCallbacks();

		refreshActions();
	}

	private JList<File> createFilesList(TransferHandler transferHandler, ListCellRenderer<?> renderer) {

		JList<File> list = new JList<>(new DefaultListModel<File>());
		list.setDragEnabled(true);
		list.setBorder(UIUtil.defaultContentBorder);
		list.setCellRenderer((ListCellRenderer<? super File>) renderer);
		list.setTransferHandler(transferHandler);
		list.addMouseListener(getHandler());
		list.addListSelectionListener(getHandler());
		list.getModel().addListDataListener(getHandler());

		return list;
	}

	private JPanel createListPanel(JList<File> list, String actionListId) {
		JPanel panel = new JPanel(new BorderLayout());

		JToolBar toolBar = getDefaultActionManager().createToolBar(actionListId, null);
		panel.add(toolBar, BorderLayout.NORTH);

		JScrollPane scrollPane = new JScrollPane(list);
		UIUtil.defaultSetUnitIncrement(scrollPane);
		scrollPane.setBorder(UIUtil.topLineBorder);

		panel.add(scrollPane, BorderLayout.CENTER);

		return panel;
	}

	private Handler getHandler() {
		if (handler == null) {
			handler = new Handler();
		}
		return handler;
	}

	private void registerActionCallbacks() {
		if (callbackHandler == null) {
			callbackHandler = new CallbackHandler();
		}

		ActionManager actionManager = getDefaultActionManager();

		actionManager.addHandler("plugins.prosody.sampaValidationView.openPreferencesAction", //$NON-NLS-1$
				callbackHandler, "openPreferences"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.sampaValidationView.clearAction", //$NON-NLS-1$
				callbackHandler, "clear"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.sampaValidationView.executeAction", //$NON-NLS-1$
				callbackHandler, "execute"); //$NON-NLS-1$

		actionManager.addHandler("plugins.prosody.sampaValidationView.clearWordFilesAction", //$NON-NLS-1$
				callbackHandler, "clearWordFiles"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.sampaValidationView.addWordFilesAction", //$NON-NLS-1$
				callbackHandler, "addWordFiles"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.sampaValidationView.removeWordFilesAction", //$NON-NLS-1$
				callbackHandler, "removeWordFiles"); //$NON-NLS-1$

		actionManager.addHandler("plugins.prosody.sampaValidationView.clearSyllableFilesAction", //$NON-NLS-1$
				callbackHandler, "clearSyllableFiles"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.sampaValidationView.addSyllableFilesAction", //$NON-NLS-1$
				callbackHandler, "addSyllableFiles"); //$NON-NLS-1$
		actionManager.addHandler("plugins.prosody.sampaValidationView.removeSyllableFilesAction", //$NON-NLS-1$
				callbackHandler, "removeSyllableFiles"); //$NON-NLS-1$
	}

	private void refreshActions() {

		ActionManager actionManager = getDefaultActionManager();

		boolean hasWordFiles = wordFilesList.getModel().getSize()>0;
		boolean hasSyllableFiles = syllableFilesList.getModel().getSize()>0;
		boolean hasFiles =  hasWordFiles || hasSyllableFiles;
		boolean hasWordFilesSelection = wordFilesList.getSelectedIndex()!=-1;
		boolean hasSyllableFilesSelection = syllableFilesList.getSelectedIndex()!=-1;
		boolean hasSelection = hasWordFilesSelection || hasSyllableFilesSelection;

		actionManager.setEnabled(hasFiles,
				"plugins.prosody.sampaValidationView.clearAction", //$NON-NLS-1$
				"plugins.prosody.sampaValidationView.executeAction"); //$NON-NLS-1$

		actionManager.setEnabled(hasWordFiles,
				"plugins.prosody.sampaValidationView.clearWordFilesAction"); //$NON-NLS-1$
		actionManager.setEnabled(hasWordFilesSelection,
				"plugins.prosody.sampaValidationView.removeWordFilesAction"); //$NON-NLS-1$

		actionManager.setEnabled(hasSyllableFiles,
				"plugins.prosody.sampaValidationView.clearSyllableFilesAction"); //$NON-NLS-1$
		actionManager.setEnabled(hasSyllableFilesSelection,
				"plugins.prosody.sampaValidationView.removeSyllableFilesAction"); //$NON-NLS-1$
	}

	//FIXME handle exceptions instead of the general throws clause!
	private void executeValidation() throws Exception {

		ConfigRegistry registry = ConfigRegistry.getGlobalRegistry();
		Handle handle = registry.getHandle(configPath);
		boolean useExternalSampaTable = registry.getBoolean(registry.getChildHandle(handle, "useExternalSampaTable")); //$NON-NLS-1$
		boolean pairFilesByName = registry.getBoolean(registry.getChildHandle(handle, "pairFilesByName")); //$NON-NLS-1$
		boolean verboseOutput = registry.getBoolean(registry.getChildHandle(handle, "verboseOutput")); //$NON-NLS-1$

		double minSyllableCoverage = registry.getDouble(registry.getChildHandle(handle, "minSyllableCoverage")); //$NON-NLS-1$
		boolean decodeEscapedCharacters = registry.getBoolean(registry.getChildHandle(handle, "decodeEscapedCharacters")); //$NON-NLS-1$
		String wordFilesEncoding = registry.getString(registry.getChildHandle(handle, "wordFilesEncoding")); //$NON-NLS-1$
		String syllableFilesEncoding = registry.getString(registry.getChildHandle(handle, "syllableFilesEncoding")); //$NON-NLS-1$

		SampaValidator validator = new SampaValidator();
		validator.setVerbose(verboseOutput);

		if(useExternalSampaTable) {
			Path sampaTableFile = registry.getFile(registry.getChildHandle(handle, "sampaTableFile")); //$NON-NLS-1$
			validator.setMapper(new SampaMapper2(sampaTableFile.toUri().toURL()));
		}

		List<File> wordFiles = ListUtils.asList(wordFilesList.getModel());
		List<File> syllableFiles = ListUtils.asList(syllableFilesList.getModel());

		if(wordFiles.size() != syllableFiles.size())
			throw new IllegalArgumentException("Numbers of word and syllable files do not match"); //$NON-NLS-1$

		if(pairFilesByName) {
			Collections.sort(wordFiles);

			Map<String, File> fileMap = new HashMap<>();

			for(File syllableFile : syllableFiles) {
				String name = name(syllableFile);

				if(fileMap.containsKey(name))
					throw new IllegalArgumentException("Cannot pair files by name - duplicate name extracted for syllable files: "+name); //$NON-NLS-1$

				fileMap.put(name, syllableFile);
			}

			for(int i=0; i<wordFiles.size(); i++) {
				File wordFile = wordFiles.get(i);
				String name = name(wordFile);

				File syllableFile = fileMap.get(name);
				if(syllableFile==null)
					throw new IllegalArgumentException("Cannot pair files by name - no matching syllable file for word file name: "+name); //$NON-NLS-1$

				syllableFiles.set(i, syllableFile);
			}
		}

		for(int i=0; i<wordFiles.size(); i++) {

			File wordFile = wordFiles.get(i);
			File syllableFile = syllableFiles.get(i);

			if(verboseOutput) {
				System.out.printf("Pairing files '%s' and '%s'", wordFile, syllableFile); //$NON-NLS-1$
			}

			@SuppressWarnings("resource")
			FilePairSampaIterator iterator = new FilePairSampaIterator(wordFile, syllableFile);

			iterator.decodeEscapedCharacters = decodeEscapedCharacters;
			iterator.minSyllableCoverage = minSyllableCoverage;
			iterator.wordFilesEncoding = wordFilesEncoding;
			iterator.syllableFilesEncoding = syllableFilesEncoding;

			validator.addSampaIterator(iterator);
		}

		validator.validate();

		//TODO move validation output to other stream, file or UI!

		System.out.printf("Validation result: %d words, %d syllables, %d errors\n", //$NON-NLS-1$
				validator.getWordCount(), validator.getSyllableCount(), validator.getErrorCount());

		for(int i=0; i<validator.getErrorCount(); i++) {
			SampaSet data = validator.getErroneousSetAt(i);
			System.out.println(data.getLocationInfo());
			System.out.println(" -> Failed to map /"+Arrays.deepToString(data.getSampaBlocks())+"/ to word '"+data.getWord()+"'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	private static String name(File file) {
		String name = file.getName();

		int idx = name.lastIndexOf('.');
		if(idx!=-1) {
			name = name.substring(0, idx);
		}

		return name;
	}

	private void clearList(JList<File> list) {
		DefaultListModel<File> model = (DefaultListModel<File>) list.getModel();
		model.removeAllElements();
	}

	private void removeSelectedFromList(JList<File> list) {
		List<File> selectedItems = list.getSelectedValuesList();
		if(selectedItems.isEmpty()) {
			return;
		}

		DefaultListModel<File> model = (DefaultListModel<File>) list.getModel();
		for(File file : selectedItems) {
			model.removeElement(file);
		}
	}

	private void addToList(JList<File> list) {
		//TODO open file chooser dialog and let user select files
	}

	private class Handler extends MouseAdapter implements ListDataListener, ListSelectionListener {

		/**
		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
		 */
		@Override
		public void valueChanged(ListSelectionEvent e) {
			refreshActions();
		}

		/**
		 * @see javax.swing.event.ListDataListener#intervalAdded(javax.swing.event.ListDataEvent)
		 */
		@Override
		public void intervalAdded(ListDataEvent e) {
			refreshActions();
		}

		/**
		 * @see javax.swing.event.ListDataListener#intervalRemoved(javax.swing.event.ListDataEvent)
		 */
		@Override
		public void intervalRemoved(ListDataEvent e) {
			refreshActions();
		}

		/**
		 * @see javax.swing.event.ListDataListener#contentsChanged(javax.swing.event.ListDataEvent)
		 */
		@Override
		public void contentsChanged(ListDataEvent e) {
			refreshActions();
		}

		// TODO add handler methods

	}

	public class CallbackHandler {

		private CallbackHandler() {
			// no-op
		}

		public void openPreferences(ActionEvent e) {

			try {
				UIUtil.openConfigDialog(configPath);
			} catch (Exception ex) {
				LoggerFactory.error(this, "Failed to open preferences for sampa vaildation view", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}

		public void clear(ActionEvent e) {
			try {
				//TODO add dialog to confirm removal of all entries!

				clearList(wordFilesList);
				clearList(syllableFilesList);
			} catch (Exception ex) {
				LoggerFactory.error(this, "Failed to clear all file lists", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}

		public void clearWordFiles(ActionEvent e) {
			try {
				clearList(wordFilesList);
			} catch (Exception ex) {
				LoggerFactory.error(this, "Failed to clear word files list", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}

		public void removeWordFiles(ActionEvent e) {
			try {
				removeSelectedFromList(wordFilesList);
			} catch (Exception ex) {
				LoggerFactory.error(this, "Failed to remove selected word files", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}

		public void addWordFiles(ActionEvent e) {
			try {
				addToList(wordFilesList);
			} catch (Exception ex) {
				LoggerFactory.error(this, "Failed to add new word files", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}

		public void clearSyllableFiles(ActionEvent e) {
			try {
				clearList(syllableFilesList);
			} catch (Exception ex) {
				LoggerFactory.error(this, "Failed to clear syllable files list", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}

		public void removeSyllableFiles(ActionEvent e) {
			try {
				removeSelectedFromList(syllableFilesList);
			} catch (Exception ex) {
				LoggerFactory.error(this, "Failed to remove selected syllable files", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}

		public void addSyllableFiles(ActionEvent e) {
			try {
				addToList(syllableFilesList);
			} catch (Exception ex) {
				LoggerFactory.error(this, "Failed to add new syllable files", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}

		public void execute(ActionEvent e) {
			try {
				executeValidation();
			} catch (Exception ex) {
				LoggerFactory.error(this, "Failed to execute SAMPA validation task", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}
	}

	private static class FileListCellRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = -116406446501685524L;

		@Override
		public Component getListCellRendererComponent(JList<?> list,
				Object value, int index, boolean isSelected,
				boolean cellHasFocus) {

			File file = (File) value;

			value = file.getName();

			super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);

			setToolTipText(UIUtil.toSwingTooltip(file.getAbsolutePath()));

			return this;
		}

	}

	private static class FilePairSampaSet implements SampaSet {

		private final String word;
		private final String[] sampaBlocks;
		private File wordFile, syllableFile;
		private int wordFileLineNumber, syllableFileLineNumber0, syllableFileLineNumber1;

		FilePairSampaSet(String word, String[] sampaBlocks) {
			this.word = word;
			this.sampaBlocks = sampaBlocks;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.sampa.SampaSet#getLocationInfo()
		 */
		@Override
		public String getLocationInfo() {
			if(wordFile==null || syllableFile==null) {
				return null;
			}

			StringBuilder sb = new StringBuilder();

			sb.append("word-file '").append(wordFile.getPath()).append("' (line ").append(wordFileLineNumber).append(") "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			sb.append("syllable-file '").append(syllableFile.getPath()).append("' (lines ").append(syllableFileLineNumber0).append(" to ").append(syllableFileLineNumber1).append(") "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

			return sb.toString();
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.sampa.SampaSet#getWord()
		 */
		@Override
		public String getWord() {
			return word;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.sampa.SampaSet#getSampaBlocks()
		 */
		@Override
		public String[] getSampaBlocks() {
			return sampaBlocks;
		}

		public void setWordInfo(File wordFile, int wordFileLineNumber) {
			this.wordFile = wordFile;
			this.wordFileLineNumber = wordFileLineNumber;
		}

		public void setSyllableInfo(File syllableFile, int syllableFileLineNumber0, int syllableFileLineNumber1) {
			this.syllableFile = syllableFile;
			this.syllableFileLineNumber0 = syllableFileLineNumber0;
			this.syllableFileLineNumber1 = syllableFileLineNumber1;
		}

		@Override
		public int hashCode() {
			int hc = word.hashCode()*sampaBlocks.hashCode();

			if(wordFile!=null) {
				hc *= wordFile.hashCode()*wordFileLineNumber;
			}

			if(syllableFile!=null) {
				hc *= syllableFile.hashCode()*syllableFileLineNumber0*syllableFileLineNumber1;
			}

			return hc;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof FilePairSampaSet) {
				FilePairSampaSet other = (FilePairSampaSet) obj;
				return word.equals(other.word)
						&& Arrays.deepEquals(sampaBlocks, other.sampaBlocks)
						&& ClassUtils.equals(wordFile, other.wordFile)
						&& wordFileLineNumber==other.wordFileLineNumber
						&& ClassUtils.equals(syllableFile, other.syllableFile)
						&& syllableFileLineNumber0==other.syllableFileLineNumber0
						&& syllableFileLineNumber1==other.syllableFileLineNumber1;
			}
			return false;
		}

		@Override
		public String toString() {
			return "'"+word+"' -> /"+Arrays.deepToString(sampaBlocks)+"/"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

	}

	private enum DataType {

		/**
		 * Regular content
		 */
		CONTENT,

		/**
		 * <p> or <P>
		 */
		BREAK,

		/**
		 * Words to be ignored
		 */
		FILLER,

		/**
		 * Symbol marked with separator character '|'
		 */
		SYLLABLE_END,
	}

	private static class DataPoint {
		final double timestamp, duration;
		final String content;
		final int lineNumber;
		final DataType type;

		public DataPoint(double timestamp, double duration, String content, DataType type, int lineNumber) {
			this.timestamp = timestamp;
			this.duration = duration;
			this.content = content;
			this.type = type;
			this.lineNumber = lineNumber;
		}

		@Override
		public String toString() {
			return "["+lineNumber+": "+timestamp+" "+content+"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
	}

	private static class FilePairSampaIterator implements SampaIterator {

		private final File wordFile, syllableFile;
		private MutableInteger wordCursor, syllableCursor;

		private List<String> syllableBuffer;
		private StringBuilder charBuffer;

		private List<DataPoint> words = new ArrayList<>(1000);
		private List<DataPoint> syllables = new ArrayList<>(1000);

		private final Pattern splitPattern = Pattern.compile("\\s+"); //$NON-NLS-1$

		private double minSyllableCoverage = 0.5;
		private boolean decodeEscapedCharacters = true;
		private String wordFilesEncoding = "UTF-8"; //$NON-NLS-1$
		private String syllableFilesEncoding = "UTF-8"; //$NON-NLS-1$

		public FilePairSampaIterator(File wordFile, File syllableFile) {
			if (wordFile == null)
				throw new NullPointerException("Invalid wordFile"); //$NON-NLS-1$
			if (syllableFile == null)
				throw new NullPointerException("Invalid syllableFile"); //$NON-NLS-1$

			this.wordFile = wordFile;
			this.syllableFile = syllableFile;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.sampa.SampaIterator#reset()
		 */
		@Override
		public void reset() throws ToolException {
			wordCursor = new MutableInteger(0);
			syllableCursor = new MutableInteger(0);

			words.clear();
			syllables.clear();

			syllableBuffer = new ArrayList<>();
			charBuffer = new StringBuilder(20);

			// Load words
			try {
				readData(wordFile, words, true, wordFilesEncoding);
			} catch (FileNotFoundException e) {
				throw new ToolException("Word file could not be found", e); //$NON-NLS-1$
			}  catch (IOException e) {
				throw new ToolException("Failed to scan word file for valid data points", e); //$NON-NLS-1$
			}

			// Load syllables
			try {
				readData(syllableFile, syllables, false, syllableFilesEncoding);
			} catch (FileNotFoundException e) {
				throw new ToolException("Syllable file could not be found", e); //$NON-NLS-1$
			}  catch (IOException e) {
				throw new ToolException("Failed to scan syllable file for valid data points", e); //$NON-NLS-1$
			}
		}

		private static final Matcher fillerMatcher = Pattern.compile("\\[[hf@tn]\\]").matcher(""); //$NON-NLS-1$ //$NON-NLS-2$
		private static final Matcher breakMatcher = Pattern.compile("<[pP]>").matcher(""); //$NON-NLS-1$ //$NON-NLS-2$

		private static final char STRESS_SYMBOL = '\"';
		private static final char END_SYMBOL = '|';

		private boolean isFiller(String s) {
			fillerMatcher.reset(s);
			return fillerMatcher.matches();
		}

		private boolean isBreak(String s) {
			breakMatcher.reset(s);
			return breakMatcher.matches();
		}

		private static final char ESCAPE_SYMBOL ='\"';

		private String unescape(String s) throws ToolException {
			charBuffer.setLength(0);
			boolean unescaped = false;

			boolean escapeNext = false;
			for(int i=0; i<s.length(); i++) {
				char c = s.charAt(i);

				if(escapeNext) {
					switch (c) {
					case 'a':
						c = 'ä';
						break;
					case 'A':
						c = 'Ä';
						break;
					case 'o':
						c = 'ö';
						break;
					case 'O':
						c = 'Ö';
						break;
					case 'u':
						c = 'ü';
						break;
					case 'U':
						c = 'Ü';
						break;
					case 's':
						c = 'ß';
						break;

					default:
						throw new ToolException("Unrecognized escaped character: "+c); //$NON-NLS-1$
					}
				}

				escapeNext = c==ESCAPE_SYMBOL;

				if(!escapeNext) {
					charBuffer.append(c);
				}

				unescaped |= escapeNext;
			}

			return unescaped ? charBuffer.toString() : s;
		}

		private void readData(File file, List<DataPoint> buffer, boolean isWordFile, String encoding) throws IOException, ToolException {
			String line;
			int lineNumber = -1;

			boolean hashFound = false;

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName(encoding)))) {

				DataPoint lastDataPoint = null;

				while((line=reader.readLine())!=null) {
					lineNumber++;

					line = line.trim();

					if(line.isEmpty()) {
						continue;
					}

					char c = line.charAt(0);

					if(hashFound && Character.isDigit(c)) {
						String[] parts = splitPattern.split(line);
						double timestamp = Double.parseDouble(parts[0]);
						String content = parts[2];

						double duration = lastDataPoint==null ? 0D : timestamp-lastDataPoint.timestamp;

						DataType type = DataType.CONTENT;

						if(isWordFile) {
							if(decodeEscapedCharacters) {
								content = unescape(content);
							}

							if(isBreak(content)) {
								type = DataType.BREAK;
							} else if(isFiller(content)) {
								type = DataType.FILLER;
							}
						} else {
							if(isBreak(content)) {
								type = DataType.BREAK;
							} else  {
								int from = 0;
								int to = content.length();
								if(content.charAt(from)==STRESS_SYMBOL) {
									from++;
								}
								if(content.charAt(to-1)==END_SYMBOL) {
									type = DataType.SYLLABLE_END;
									to--;
								}

								content = content.substring(from, to);
							}
						}

						DataPoint dataPoint = new DataPoint(timestamp, duration, content, type, lineNumber+1); // +1 for human readability

						buffer.add(dataPoint);

						lastDataPoint = dataPoint;
					}

					hashFound |= c=='#';
				}
			}
		}

		/**
		 * Skips all filler and break data points
		 */
		private DataPoint skipNonContent(MutableInteger cursor, List<DataPoint> list) {
			while(cursor.getValue()<list.size()) {
				DataPoint data = list.get(cursor.getValue());
				if(data.type==DataType.CONTENT
						|| data.type==DataType.SYLLABLE_END) {
					return data;
				}

				cursor.increment();
			}

			return null;
		}

		private DataPoint seekEnd(MutableInteger cursor, List<DataPoint> list) {
			while(cursor.getValue()<list.size()) {
				DataPoint data = list.get(cursor.getValue());
				if(data.type==DataType.SYLLABLE_END) {
					return data;
				}

				cursor.increment();
			}

			return null;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.sampa.SampaIterator#next()
		 */
		@Override
		public SampaSet next() throws ToolException {

			syllableBuffer.clear();
			charBuffer.setLength(0);

			DataPoint word = skipNonContent(wordCursor, words);

			if(word==null) {
				return null;
			}

			int syllablesBeginLineNumber = -1;
			int syllablesEndLineNumber = -1;

			while(syllableCursor.getValue()<syllables.size()) {

				// Scan for next syllable
				DataPoint sylBegin = skipNonContent(syllableCursor, syllables);
				if(sylBegin==null) {
//					throw new ToolException("Expected more syllable data for word (last syllable line: "+syllableCursor.getValue()+"): "+word+" in file "+wordFile); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					break;
				}
				if(syllablesBeginLineNumber==-1) {
					syllablesBeginLineNumber = sylBegin.lineNumber;
				}

				int firstSampa = syllableCursor.getValue();

				DataPoint sylEnd = seekEnd(syllableCursor, syllables);
				if(sylEnd==null)
					throw new ToolException("Expected more syllable data for word (missing end symbol for syllable "+sylBegin+"): "+word+" in file "+syllableFile); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				syllablesEndLineNumber = sylEnd.lineNumber;

				int lastSampa = syllableCursor.getValue();

				// Syllable overlaps word -> can't be a member of previous word
				if(sylEnd.timestamp>word.timestamp) {
					double surplus = sylEnd.timestamp-word.timestamp;

					// If syllable is not covered to a certain degree by the current word discard it
					double duration = sylEnd.timestamp-sylBegin.timestamp+sylBegin.duration;
					if(surplus>minSyllableCoverage*duration) {
						// Important: revert syllable cursor back to the syllable begin for next call
						syllableCursor.setValue(firstSampa);
						break;
					}
				}

				// Build sampa block for syllable (skip breaks)
				charBuffer.setLength(0);
				for(int i=firstSampa; i<=lastSampa; i++) {
					DataPoint data = syllables.get(i);
					if(data.type==DataType.BREAK) {
						continue;
					}
					charBuffer.append(data.content);
				}

				syllableBuffer.add(charBuffer.toString());
				syllableCursor.increment();
			}

			if(syllableBuffer.isEmpty())
				throw new ToolException("Unable to connect word "+word+" (last syllable line: "+syllableCursor.getValue()+" in file "+syllableFile+") in file "+wordFile); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

			String[] sampaBlocks = new String[syllableBuffer.size()];
			syllableBuffer.toArray(sampaBlocks);
			FilePairSampaSet result = new FilePairSampaSet(word.content, sampaBlocks);
			result.setWordInfo(wordFile, word.lineNumber);
			result.setSyllableInfo(syllableFile, syllablesBeginLineNumber, syllablesEndLineNumber);

			wordCursor.increment();

			return result;

//			String[] wordParts = splitPattern.split(lastWordLine);
//			double wordEndTs = Double.parseDouble(wordParts[0]);
//
//			FilePairSampaSet result = null;
//
//			while(lastSyllableLine!=null && result==null) {
//				String[] syllableParts = splitPattern.split(lastSyllableLine);
//
//				syllableBuffer.add(syllableParts[2]);
//
//				double syllableEndTs = Double.parseDouble(syllableParts[0]);
//
//				if(Math.abs(syllableEndTs-wordEndTs)<0.005) {
//					String[] sampaBlocks = new String[syllableBuffer.size()];
//					syllableBuffer.toArray(sampaBlocks);
//					result = new FilePairSampaSet(wordParts[2], sampaBlocks);
//					result.setWordInfo(wordFile, wordCursor.getValue());
//					result.setSyllableInfo(syllableFile, syllableCursor.getValue());
//
//					lastWordLine = nextWordLine();
//				}
//
//				lastSyllableLine = nextSyllableLine();
//			}
//
//			if(result==null && checkActive())
//				throw new ToolException("");
//
//			return result;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.sampa.SampaIterator#close()
		 */
		@Override
		public void close() {
			wordCursor = syllableCursor = null;
			words.clear();
			syllables.clear();
			syllableBuffer.clear();
		}

	}
}
