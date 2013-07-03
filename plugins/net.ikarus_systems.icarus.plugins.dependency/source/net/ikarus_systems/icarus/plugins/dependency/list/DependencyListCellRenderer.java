/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.dependency.list;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

import net.ikarus_systems.icarus.config.ConfigRegistry;
import net.ikarus_systems.icarus.language.SentenceData;
import net.ikarus_systems.icarus.language.dependency.DependencyData;
import net.ikarus_systems.icarus.language.dependency.annotation.AnnotatedDependencyData;
import net.ikarus_systems.icarus.language.dependency.annotation.DependencyAnnotationManager;
import net.ikarus_systems.icarus.language.dependency.annotation.DependencyHighlighting;
import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.search_tools.annotation.ResultAnnotation;
import net.ikarus_systems.icarus.ui.DummyTextPane;
import net.ikarus_systems.icarus.util.Installable;
import net.ikarus_systems.icarus.util.annotation.Annotation;
import net.ikarus_systems.icarus.util.annotation.AnnotationController;
import net.ikarus_systems.icarus.util.annotation.AnnotationDisplayMode;
import net.ikarus_systems.icarus.util.annotation.AnnotationManager;
import net.ikarus_systems.icarus.util.annotation.HighlightType;
import net.ikarus_systems.icarus.util.annotation.HighlightUtils;
import net.ikarus_systems.icarus.util.annotation.HighlightUtils.OutlineHighlightPainter;
import net.ikarus_systems.icarus.util.annotation.HighlightUtils.UnderlineHighlightPainter;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyListCellRenderer extends DummyTextPane 
		implements ListCellRenderer<DependencyData>, Installable {

	private static final long serialVersionUID = 7392683584037946715L;
	
	protected AnnotationController annotationSource;
	
	protected DependencyDocument offlineDocument;
	protected StringBuilder buffer;
	
	protected static Border noFocusBorder; 
	
	public DependencyListCellRenderer() {
		setHighlighter(createHighlighter());
		setDocument(new DependencyDocument());
	}

	/**
	 * @see net.ikarus_systems.icarus.util.Installable#install(java.lang.Object)
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
	 * @see net.ikarus_systems.icarus.util.Installable#uninstall(java.lang.Object)
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
			JList<? extends DependencyData> list, DependencyData value,
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
    		offlineDocument = new DependencyDocument();
    	} else {
    		offlineDocument.clear();
    	}
    	
    	if(buffer==null) {
    		buffer = new StringBuilder();
    	} else {
    		buffer.setLength(0);
    	}
        
        boolean annotated = false;
		
		if(value instanceof AnnotatedDependencyData && getAnnotationManager()!=null
				&& getAnnotationManager().getDisplayMode()!=AnnotationDisplayMode.NONE) {
			annotated = annotate(list, (AnnotatedDependencyData) value, index, isSelected, cellHasFocus);
		} 
		
		if(!annotated){
			plain(value, index, isSelected, cellHasFocus);
		}
		
		try {
			offlineDocument.applyBatchUpdates(0);
		} catch (BadLocationException e) {
			LoggerFactory.log(this, Level.SEVERE,  
					"Unexpected exception on list rendering", e); //$NON-NLS-1$
		}
		
		DependencyDocument doc = offlineDocument;
		offlineDocument = (DependencyDocument) getDocument();
		
		setDocument(doc);

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
		
		Style defaultStyle = offlineDocument.addStyle(null, null);
		StyleConstants.setForeground(defaultStyle, getForeground());
		
		// Show index
		if(config.getBoolean("plugins.dependency.highlighting.showIndex")) { //$NON-NLS-1$
			buffer.append((index+1)+": "); //$NON-NLS-1$
		}
		
		// Show corpus index if available
		if(config.getBoolean("plugins.dependency.highlighting.showCorpusIndex") //$NON-NLS-1$
				&& annotation instanceof ResultAnnotation) {
			buffer.append(String.format("(%d) ",  //$NON-NLS-1$
					((ResultAnnotation)annotation).getResultEntry().getIndex()+1));
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
			if(DependencyHighlighting.isHighlighted(highlight)) {
				// Process any pending non-highlighted text stuff
				if(buffer.length()>0) {
					offset += offlineDocument.appendBatchString(buffer.toString(), defaultStyle);
					buffer.setLength(0);
				}
				
				style = offlineDocument.addStyle(null, null);

				Color col = DependencyHighlighting.getGroupColor(highlight);
				if(col!=null) {
					painter = applyHighlightType(groupHighlightType, style, col);
					StyleConstants.setBold(style, true);
				} else {
					col = DependencyHighlighting.getHighlightColor(highlight);
					painter = applyHighlightType(highlightType, style, col);
				}
				
				// add the form string
				int off0 = offset;
				offset += offlineDocument.appendBatchString(data.getForm(i), style);
				int off1 = offset;
				
				// mark multiple annotations if desired
				if(markMultiple && DependencyHighlighting.isConcurrentHighlight(highlight)) {
					style = DependencyHighlighting.getStyleContext().getStyle("multiple"); //$NON-NLS-1$
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
    
    protected void plain(SentenceData data, int index, boolean isSelected,
			boolean cellHasFocus) {
		Style defaultStyle = offlineDocument.addStyle(null, null);
		StyleConstants.setForeground(defaultStyle, getForeground());
		
		// Show index
		if(ConfigRegistry.getGlobalRegistry().getValue(
				"plugins.dependency.highlighting.showIndex", true)) { //$NON-NLS-1$
			buffer.append((index+1)+": "); //$NON-NLS-1$
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
    
    protected static class DependencyDocument extends DefaultStyledDocument {

		private static final long serialVersionUID = -4128539534922746201L;

	    private static final char[] EOL_ARRAY = { '\n' };

		private List<ElementSpec> batch = new ArrayList<>();
		
		public int appendBatchString(String str, AttributeSet attr) {
	        batch.add(new ElementSpec(attr, ElementSpec.ContentType, str.toCharArray(), 0, str.length()));
	        return str.length();
		}
		
		public int appendBatchLineFeed(AttributeSet attr) {
	        batch.add(new ElementSpec(attr, ElementSpec.ContentType, EOL_ARRAY, 0, 1));

	        // Then add attributes for element start/end tags. Ideally 
	        // we'd get the attributes for the current position, but we 
	        // don't know what those are yet if we have unprocessed 
	        // batch inserts. Alternatives would be to get the last 
	        // paragraph element (instead of the first), or to process 
	        // any batch changes when a linefeed is inserted.
	        Element paragraph = getParagraphElement(0);
	        AttributeSet pattr = paragraph.getAttributes();
	        batch.add(new ElementSpec(null, ElementSpec.EndTagType));
	        batch.add(new ElementSpec(pattr, ElementSpec.StartTagType));
	        
	        return EOL_ARRAY.length;
		}

	    public void applyBatchUpdates(int offset) throws BadLocationException {
	        ElementSpec[] inserts = new ElementSpec[batch.size()];
	        batch.toArray(inserts);
	        batch.clear();

	        // Process all of the inserts in bulk
	        insert(offset, inserts);
	    }
	    
	    public void clear() {
	    	try {
				remove(0, getLength());
			} catch (BadLocationException e) {
				// ignore
			}
	    }
    }
}
