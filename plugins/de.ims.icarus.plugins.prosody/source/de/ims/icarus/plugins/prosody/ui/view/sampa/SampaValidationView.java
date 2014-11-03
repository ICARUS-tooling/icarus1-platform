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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

import de.ims.icarus.io.IOUtil;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.core.View;
import de.ims.icarus.plugins.prosody.sampa.SampaIterator;
import de.ims.icarus.plugins.prosody.sampa.SampaSet;
import de.ims.icarus.plugins.prosody.sampa.SampaValidator;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.list.FileListTransferHandler;
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

		JPanel wordPanel = createListPanel(wordFilesList, "plugins.prosody.sampaValidationView.wordFileToolBarList");
		JPanel syllablePanel = createListPanel(syllableFilesList, "plugins.prosody.sampaValidationView.syllableFileToolBarList");

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

		File wordFile = wordFilesList.getModel().getElementAt(0);
		File syllableFile = syllableFilesList.getModel().getElementAt(0);

		@SuppressWarnings("resource")
		FilePairSampaIterator iterator = new FilePairSampaIterator(wordFile, syllableFile);

		SampaValidator validator = new SampaValidator();
		validator.addSampaIterator(iterator);

		validator.validate();

		System.out.printf("Validation result: %d words, %d syllables, %d errors\n", //$NON-NLS-1$
				validator.getWordCount(), validator.getSyllableCount(), validator.getErrorCount());

		for(int i=0; i<validator.getErrorCount(); i++) {
			SampaSet data = validator.getErroneousSetAt(i);
			System.out.println(data.getLocationInfo());
			System.out.println(" -> Failed to map /"+Arrays.deepToString(data.getSampaBlocks())+"/ to word '"+data.getWord()+"'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
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
				//TODO
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

	private static class DataPoint {
		final double timestamp, duration;
		final String content;
		final int lineNumber;

		public DataPoint(double timestamp, double duration, String content, int lineNumber) {
			this.timestamp = timestamp;
			this.duration = duration;
			this.content = content;
			this.lineNumber = lineNumber;
		}

		@Override
		public String toString() {
			return "["+lineNumber+": "+timestamp+" "+content+"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
	}

	private static class FilePairSampaIterator implements SampaIterator {

		private final File wordFile, syllableFile;
		private int wordCursor, syllableCursor;

		private List<String> syllableBuffer;

		private List<DataPoint> words = new ArrayList<>(1000);
		private List<DataPoint> syllables = new ArrayList<>(1000);
		private boolean eof = false;

		private final Pattern splitPattern = Pattern.compile("\\s+"); //$NON-NLS-1$

		private final double minSyllableCoverage = 0.5;

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
			wordCursor = 0;
			syllableCursor = 0;

			words.clear();
			syllables.clear();
			eof = false;

			syllableBuffer = new ArrayList<>();

			// Load words
			try {
				readData(wordFile, words);
			} catch (FileNotFoundException e) {
				throw new ToolException("Word file could not be found", e); //$NON-NLS-1$
			}  catch (IOException e) {
				throw new ToolException("Failed to scan word file for valid data points", e); //$NON-NLS-1$
			}

			// Load syllables
			try {
				readData(syllableFile, syllables);
			} catch (FileNotFoundException e) {
				throw new ToolException("Syllable file could not be found", e); //$NON-NLS-1$
			}  catch (IOException e) {
				throw new ToolException("Failed to scan syllable file for valid data points", e); //$NON-NLS-1$
			}
		}

		private void readData(File file, List<DataPoint> buffer) throws IOException {
			String line;
			int lineNumber = -1;

			boolean hashFound = false;

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), IOUtil.UTF8_CHARSET))) {

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

						DataPoint dataPoint = new DataPoint(timestamp, duration, content, lineNumber+1); // +1 for human readability

						buffer.add(dataPoint);

						lastDataPoint = dataPoint;
					}

					hashFound |= c=='#';
				}
			}
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.sampa.SampaIterator#next()
		 */
		@Override
		public SampaSet next() throws ToolException {
			boolean wordsFininshed = wordCursor>=words.size();
			boolean syllablesFinished = syllableCursor>=syllables.size();

			if(wordsFininshed!=syllablesFinished)
				throw new ToolException("Inconsistent end of data: wordsFinished="+wordsFininshed+" syllablesFinished="+syllablesFinished); //$NON-NLS-1$ //$NON-NLS-2$

			if(wordsFininshed && syllablesFinished) {
				return null;
			}

			syllableBuffer.clear();

			DataPoint word = words.get(wordCursor);

			FilePairSampaSet result = null;
			int syllableBeginLineNumber = -1;
			int syllableEndLineNumber = -1;

			// Check current syllable to determine what word last syllable should be assigned to
			while(syllableCursor<syllables.size()) {
				DataPoint syllable = syllables.get(syllableCursor);

				if(syllableBeginLineNumber==-1) {
					syllableBeginLineNumber = syllable.lineNumber;
				}

				// Syllable overlaps word -> can't be a member of previous word
				if(syllable.timestamp>word.timestamp) {
					double surplus = syllable.timestamp-word.timestamp;

					// If syllable is not covered to a certain degree by the current word
					// discard it
					if(surplus>minSyllableCoverage*syllable.duration) {
						break;
					}
				}

				syllableEndLineNumber = syllable.lineNumber;

				syllableBuffer.add(syllable.content);

				syllableCursor++;
			}

			if(syllableBuffer.isEmpty())
				throw new ToolException("Unable to connect word "+word); //$NON-NLS-1$

			String[] sampaBlocks = new String[syllableBuffer.size()];
			syllableBuffer.toArray(sampaBlocks);
			result = new FilePairSampaSet(word.content, sampaBlocks);
			result.setWordInfo(wordFile, word.lineNumber);
			result.setSyllableInfo(syllableFile, syllableBeginLineNumber, syllableEndLineNumber);

			wordCursor++;

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
			wordCursor = syllableCursor = -1;
			words.clear();
			syllables.clear();
			syllableBuffer.clear();
		}

	}
}
