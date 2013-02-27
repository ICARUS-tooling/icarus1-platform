package net.ikarus_systems.icarus.ui;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import net.ikarus_systems.icarus.resources.ResourceManager;

/*
 *  This class will merge individual edits into a single larger edit.
 *  That is, characters entered sequentially will be grouped together and
 *  undone as a group. Any attribute changes will be considered as part
 *  of the group and will therefore be undone when the group is undone.
 */
public class CompoundUndoManager extends UndoManager implements
		UndoableEditListener, DocumentListener, PropertyChangeListener {
	
	private static final long serialVersionUID = 7580553726318303697L;
	
	private UndoManager undoManager;
	private CompoundEdit compoundEdit;
	private JTextComponent textComponent;
	private UndoAction undoAction;
	private UndoAction redoAction;

	// These fields are used to help determine whether the edit is an
	// incremental edit. The offset and length should increase by 1 for
	// each character added or decrease by 1 for each character removed.

	private int lastOffset;
	private int lastLength;

	public CompoundUndoManager(JTextComponent textComponent) {
		this.textComponent = textComponent;
		undoManager = this;
		undoAction = new UndoAction(true);
		redoAction = new UndoAction(false);
		textComponent.getDocument().addUndoableEditListener(this);
		textComponent.addPropertyChangeListener("document", this); //$NON-NLS-1$
	}

	/*
	 * Add a DocumentLister before the undo is done so we can position the
	 * Caret correctly as each edit is undone.
	 */
	public void undo() {
		textComponent.getDocument().addDocumentListener(this);
		super.undo();
		textComponent.getDocument().removeDocumentListener(this);
	}

	/*
	 * Add a DocumentLister before the redo is done so we can position the
	 * Caret correctly as each edit is redone.
	 */
	public void redo() {
		textComponent.getDocument().addDocumentListener(this);
		super.redo();
		textComponent.getDocument().removeDocumentListener(this);
	}

	/*
	 * Whenever an UndoableEdit happens the edit will either be absorbed by
	 * the current compound edit or a new compound edit will be started
	 */
	public void undoableEditHappened(UndoableEditEvent e) {
		// Start a new compound edit

		if (compoundEdit == null) {
			compoundEdit = startCompoundEdit(e.getEdit());
			return;
		}

		int offsetChange = textComponent.getCaretPosition() - lastOffset;
		int lengthChange = textComponent.getDocument().getLength() - lastLength;

		// Check for an attribute change

		AbstractDocument.DefaultDocumentEvent event = (AbstractDocument.DefaultDocumentEvent) e
				.getEdit();

		if (event.getType().equals(DocumentEvent.EventType.CHANGE)) {
			if (offsetChange == 0) {
				compoundEdit.addEdit(e.getEdit());
				return;
			}
		}

		boolean typeChanged = false;

		if (offsetChange == 1) {
			String text = textComponent.getText();
			int offset = event.getOffset();
			char c = text.charAt(offset);
			char c0 = text
					.charAt(offset < text.length() && offset > 0 ? offset - 1
							: offset);

			typeChanged = Character.isWhitespace(c) != Character
					.isWhitespace(c0);
		}

		// Check for an incremental edit or backspace.
		// The Change in Caret position and Document length should both be
		// either 1 or -1.

		if (offsetChange == lengthChange && Math.abs(offsetChange) == 1
				&& !typeChanged) {
			compoundEdit.addEdit(e.getEdit());
			lastOffset = textComponent.getCaretPosition();
			lastLength = textComponent.getDocument().getLength();
			return;
		}

		// Not incremental edit, end previous edit and start a new one

		compoundEdit.end();
		compoundEdit = startCompoundEdit(e.getEdit());
	}

	/*
	 * Each CompoundEdit will store a group of related incremental edits (ie.
	 * each character typed or backspaced is an incremental edit)
	 */
	private CompoundEdit startCompoundEdit(UndoableEdit anEdit) {
		// Track Caret and Document information of this compound edit

		lastOffset = textComponent.getCaretPosition();
		lastLength = textComponent.getDocument().getLength();

		// The compound edit is used to store incremental edits

		compoundEdit = new RestartingCompoundEdit();
		compoundEdit.addEdit(anEdit);

		// The compound edit is added to the UndoManager. All incremental
		// edits stored in the compound edit will be undone/redone at once

		addEdit(compoundEdit);

		undoAction.updateState();
		redoAction.updateState();

		return compoundEdit;
	}

	/*
	 * The Action to Undo changes to the Document. The state of the Action is
	 * managed by the CompoundUndoManager
	 */
	public Action getUndoAction() {
		return undoAction;
	}

	/*
	 * The Action to Redo changes to the Document. The state of the Action is
	 * managed by the CompoundUndoManager
	 */
	public Action getRedoAction() {
		return redoAction;
	}

	//
	// Implement DocumentListener
	//
	/*
	 * Updates to the Document as a result of Undo/Redo will cause the Caret to
	 * be repositioned
	 */
	public void insertUpdate(final DocumentEvent e) {
		UIUtil.invokeLater(new Runnable() {
			public void run() {
				int offset = e.getOffset() + e.getLength();
				offset = Math.min(offset, textComponent.getDocument()
						.getLength());
				textComponent.setCaretPosition(offset);
			}
		});
	}

	public void removeUpdate(DocumentEvent e) {
		textComponent.setCaretPosition(e.getOffset());
	}

	public void changedUpdate(DocumentEvent e) {
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("document".equals(evt.getPropertyName())) { //$NON-NLS-1$
			discardAllEdits();
			((Document) evt.getOldValue()).removeUndoableEditListener(this);
			((Document) evt.getNewValue()).addUndoableEditListener(this);

			undoAction.updateState();
			redoAction.updateState();
		}
	}

	class RestartingCompoundEdit extends CompoundEdit {

		private static final long serialVersionUID = -3056208356551366150L;

		public boolean isInProgress() {
			// In order for the canUndo() and canRedo() methods to work
			// assume that the compound edit is never in progress

			return false;
		}

		public void undo() throws CannotUndoException {
			// End the edit so future edits don't get absorbed by this edit

			if (compoundEdit != null)
				compoundEdit.end();

			super.undo();

			// Always start a new compound edit after an undo

			compoundEdit = null;
		}
	}

	/*
	 * Perform the Undo and update the state of the undo/redo Actions
	 */
	class UndoAction extends AbstractAction {

		private static final long serialVersionUID = 3185862094079651912L;

		final boolean undo;

		public UndoAction(boolean undo) {
			this.undo = undo;

			if (undo) {
				putValue(Action.SMALL_ICON, IconRegistry.getGlobalRegistry().getIcon("undo.gif")); //$NON-NLS-1$
				putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_U));
				putValue(Action.ACCELERATOR_KEY, KeyStroke
						.getKeyStroke("control Z")); //$NON-NLS-1$
			} else {
				putValue(Action.SMALL_ICON, IconRegistry.getGlobalRegistry().getIcon("redo.gif")); //$NON-NLS-1$
				putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_R));
				putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
						KeyEvent.VK_Y, InputEvent.CTRL_MASK));
			}

			ResourceManager.getInstance().getGlobalDomain().prepareAction(this, undo ? "undo" : "redo", null); //$NON-NLS-1$ //$NON-NLS-2$
			ResourceManager.getInstance().getGlobalDomain().addAction(this);

			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			try {
				if (undo)
					undoManager.undo();
				else
					undoManager.redo();

				textComponent.requestFocusInWindow();
			} catch (CannotUndoException ex) {
			}

			updateState();
			if (undo)
				redoAction.updateState();
			else
				undoAction.updateState();
		}

		private void updateState() {
			setEnabled(undo ? undoManager.canUndo() : undoManager.canRedo());
		}
	}
}
