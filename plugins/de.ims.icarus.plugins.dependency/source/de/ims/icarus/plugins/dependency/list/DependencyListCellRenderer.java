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
package de.ims.icarus.plugins.dependency.list;

import java.awt.Color;
import java.awt.Component;
import java.util.logging.Level;

import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.dependency.DependencySentenceData;
import de.ims.icarus.language.dependency.annotation.AnnotatedDependencyData;
import de.ims.icarus.language.dependency.annotation.DependencyAnnotationManager;
import de.ims.icarus.language.dependency.annotation.DependencyHighlighting;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.ui.DummyTextPane;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.ui.text.BatchDocument;
import de.ims.icarus.util.Installable;
import de.ims.icarus.util.annotation.Annotation;
import de.ims.icarus.util.annotation.AnnotationController;
import de.ims.icarus.util.annotation.AnnotationDisplayMode;
import de.ims.icarus.util.annotation.AnnotationManager;
import de.ims.icarus.util.annotation.HighlightType;
import de.ims.icarus.util.annotation.HighlightUtils;
import de.ims.icarus.util.annotation.HighlightUtils.OutlineHighlightPainter;
import de.ims.icarus.util.annotation.HighlightUtils.UnderlineHighlightPainter;
import de.ims.icarus.util.strings.StringUtil;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyListCellRenderer extends DummyTextPane
		implements ListCellRenderer<DependencySentenceData>, Installable {

	private static final long serialVersionUID = 7392683584037946715L;

	protected AnnotationController annotationSource;

	protected BatchDocument offlineDocument;
	protected StringBuilder buffer;

	protected static Border noFocusBorder;

	protected static Style multipleAnnotationStyle;

	public DependencyListCellRenderer() {
		setHighlighter(createHighlighter());
		setDocument(new BatchDocument());
		setOpaque(true);
	}

	protected static Style getMultipleAnnotationStyle() {
		if(multipleAnnotationStyle==null) {
			multipleAnnotationStyle = StyleContext.getDefaultStyleContext().addStyle("multiple", null); //$NON-NLS-1$
			StyleConstants.setIcon(multipleAnnotationStyle,
					IconRegistry.getGlobalRegistry().getIcon("multiple_annotation.gif")); //$NON-NLS-1$
		}
		return multipleAnnotationStyle;
	}

	/**
	 * @see de.ims.icarus.util.Installable#install(java.lang.Object)
	 */
	@Override
	public void install(Object target) {
		if(target instanceof AnnotationController) {
			if(this.annotationSource!=null && this.annotationSource!=target)
				throw new IllegalStateException("Cannot be assigned to multiple annotation controllers"); //$NON-NLS-1$

			this.annotationSource = (AnnotationController)target;
		} else {
			this.annotationSource = null;
		}
	}

	/**
	 * @see de.ims.icarus.util.Installable#uninstall(java.lang.Object)
	 */
	@Override
	public void uninstall(Object target) {
		this.annotationSource = null;
	}

	protected AnnotationManager getAnnotationManager() {
		return annotationSource==null ? null : annotationSource.getAnnotationManager();
	}

	/**
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(
			JList<? extends DependencySentenceData> list, DependencySentenceData value,
			int index, boolean isSelected, boolean cellHasFocus) {

        setComponentOrientation(list.getComponentOrientation());

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        setEnabled(list.isEnabled());
        setFont(list.getFont());

        Border border = null;
        if (cellHasFocus) {
            if (isSelected) {
                border = UIManager.getBorder("List.focusSelectedCellHighlightBorder"); //$NON-NLS-1$
            }
            if (border == null) {
                border = UIManager.getBorder("List.focusCellHighlightBorder"); //$NON-NLS-1$
            }
        } else {
            border = getNoFocusBorder();
        }
        setBorder(border);

    	if(offlineDocument==null) {
    		offlineDocument = new BatchDocument();
    	} else {
    		offlineDocument.clear();
    	}

    	if(buffer==null) {
    		buffer = new StringBuilder();
    	} else {
    		buffer.setLength(0);
    	}

    	if(value==null) {
    		setText("-"); //$NON-NLS-1$
    	} else {

	        boolean annotated = false;

			if(value instanceof AnnotatedDependencyData && getAnnotationManager()!=null
					&& getAnnotationManager().getDisplayMode()!=AnnotationDisplayMode.NONE) {
				annotated = annotate(list, (AnnotatedDependencyData) value, index, isSelected, cellHasFocus);
			}

			if(!annotated){
				plain(list, value, index, isSelected, cellHasFocus);
			}

			try {
				offlineDocument.applyBatchUpdates(0);
			} catch (BadLocationException e) {
				LoggerFactory.log(this, Level.SEVERE,
						"Unexpected exception on list rendering", e); //$NON-NLS-1$
			}

			BatchDocument doc = offlineDocument;
			offlineDocument = (BatchDocument) getDocument();

			setDocument(doc);
    	}

        return this;
	}

	protected Highlighter createHighlighter() {
		DefaultHighlighter highlighter = new DefaultHighlighter();

		highlighter.setDrawsLayeredHighlights(true);

		return highlighter;
	}

    protected final Border getNoFocusBorder() {
        Border border = UIManager.getBorder("List.cellNoFocusBorder"); //$NON-NLS-1$
        if(border!=null) {
        	return border;
        }
        if(noFocusBorder==null) {
        	noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        }
        return noFocusBorder;
    }

    protected HighlightPainter applyHighlightType(HighlightType type, Style style, Color col) {
    	switch (type) {
		case BACKGROUND:
			StyleConstants.setBackground(style, col);
			break;

		case FOREGROUND:
			StyleConstants.setForeground(style, col);
			break;

		case ITALIC:
			StyleConstants.setItalic(style, true);
			break;

		case BOLD:
			StyleConstants.setBold(style, true);
			break;

		case UNDERLINED:
			return HighlightUtils.getPainter(col, UnderlineHighlightPainter.class);

		case OUTLINED:
			return HighlightUtils.getPainter(col, OutlineHighlightPainter.class);

		default:
			throw new IllegalArgumentException("Highlight type not supported: "+type); //$NON-NLS-1$
		}

    	return null;
    }

    protected boolean annotate(JList<?> list, AnnotatedDependencyData data, int index,
    		boolean isSelected,	boolean cellHasFocus) {

    	Annotation annotation = data.getAnnotation();
    	DependencyAnnotationManager manager = (DependencyAnnotationManager) getAnnotationManager();
    	manager.setAnnotation(annotation);

    	if(!manager.hasAnnotation()) {
    		return false;
    	}

		getHighlighter().removeAllHighlights();

		ConfigRegistry config = ConfigRegistry.getGlobalRegistry();

		HighlightType highlightType = config.getValue(
				"plugins.dependency.highlighting.highlightType", HighlightType.class); //$NON-NLS-1$
		HighlightType groupHighlightType = config.getValue(
				"plugins.dependency.highlighting.groupHighlightType", HighlightType.class); //$NON-NLS-1$

		int offset = 0;
		boolean markMultiple = config.getBoolean(
				"plugins.dependency.highlighting.markMultipleAnnotations"); //$NON-NLS-1$

		Color fg = isSelected ? list.getSelectionForeground() : list.getForeground();
		Color bg = isSelected ? list.getSelectionBackground() : list.getBackground();

		setBackground(bg);

		Style defaultStyle = offlineDocument.addStyle(null, null);
		StyleConstants.setForeground(defaultStyle, fg);
		StyleConstants.setBackground(defaultStyle, bg);

		// Show index
		if(LanguageUtils.isShowIndex()) {
			buffer.append(StringUtil.formatDecimal(index+1)+": "); //$NON-NLS-1$
		}

		// Show corpus index if available
		if(LanguageUtils.isShowCorpusIndex()) {
			buffer.append("(").append(StringUtil.formatDecimal(data.getIndex()+1)).append(") "); //$NON-NLS-1$ //$NON-NLS-2$
		}

		for(int i=0; i<data.length(); i++) {
			Style style = null;
			HighlightPainter painter = null;

			// Fetch highlight for this form token
			long highlight = manager.getHighlight(i);

			if(i>0) {
				buffer.append(" "); //$NON-NLS-1$
			}

			// If it should be highlighted fetch painter and modify style
			if(DependencyHighlighting.getInstance().isHighlighted(highlight)) {
				// Process any pending non-highlighted text stuff
				if(buffer.length()>0) {
					offset += offlineDocument.appendBatchString(buffer.toString(), defaultStyle);
					buffer.setLength(0);
				}

				style = offlineDocument.addStyle(null, defaultStyle);

				Color col = DependencyHighlighting.getInstance().getGroupColor(highlight);
				if(col!=null) {
					painter = applyHighlightType(groupHighlightType, style, col);
					StyleConstants.setBold(style, true);
				} else {
					col = DependencyHighlighting.getInstance().getHighlightColor(highlight);
					painter = applyHighlightType(highlightType, style, col);
				}

				// Add the form string
				int off0 = offset;
				offset += offlineDocument.appendBatchString(data.getForm(i), style);
				int off1 = offset;

				// Mark multiple annotations if desired
				// TODO mark all concurrent annotations or only "real" ones (hosted in different annotation layers) ?
				if(markMultiple && DependencyHighlighting.getInstance().isConcurrentHighlight(highlight)) {
					style = getMultipleAnnotationStyle();
					offset += offlineDocument.appendBatchString(" ", style); //$NON-NLS-1$
				}

				// apply highlighting
				if(painter!=null) {
					try {
						getHighlighter().addHighlight(off0, off1, painter);
					} catch (BadLocationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else {
				buffer.append(data.getForm(i));
			}
		}

		// Process any pending non-highlighted text stuff
		if(buffer.length()>0) {
			offset += offlineDocument.appendBatchString(buffer.toString(), defaultStyle);
			buffer.setLength(0);
		}

		return true;
    }

    protected void plain(JList<?> list, SentenceData data, int index, boolean isSelected,
			boolean cellHasFocus) {

		Color fg = isSelected ? list.getSelectionForeground() : list.getForeground();
		Color bg = isSelected ? list.getSelectionBackground() : list.getBackground();

		setBackground(bg);

		Style defaultStyle = offlineDocument.addStyle(null, null);
		StyleConstants.setForeground(defaultStyle, fg);
		StyleConstants.setBackground(defaultStyle, bg);

		// Show index
		if(LanguageUtils.isShowIndex()) {
			buffer.append(StringUtil.formatDecimal(index+1)+": "); //$NON-NLS-1$
		}

		// Show corpus index if available
		if(LanguageUtils.isShowCorpusIndex()) {
			buffer.append("(").append(StringUtil.formatDecimal(data.getIndex()+1)).append(") "); //$NON-NLS-1$ //$NON-NLS-2$
		}

		for(int i=0; i<data.length(); i++) {
			if(i>0) {
				buffer.append(" "); //$NON-NLS-1$
			}
			buffer.append(data.getForm(i));
		}

		offlineDocument.appendBatchString(buffer.toString(), defaultStyle);

		buffer.setLength(0);
    }
}
