/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.language.coref;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.font.TextAttribute;
import java.text.AttributedString;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.coref.text.CoreferenceDocument;
import de.ims.icarus.language.coref.text.CoreferenceEditorKit;
import de.ims.icarus.plugins.coref.io.CONLL12DocumentReader;
import de.ims.icarus.plugins.coref.io.CONLL12Reader;
import de.ims.icarus.plugins.coref.view.CoreferenceCellRenderer;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.util.data.DataListModel;
import de.ims.icarus.util.location.Location;
import de.ims.icarus.util.location.Locations;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CorefTest extends JFrame {

	public static void main(String[] args) throws Exception {
		new CorefTest();
	}
	
	public CorefTest() throws Exception {
		test3();
		
		
		pack();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	void test1() {
		final AttributedString as = new AttributedString("Test 1[2bla2]1 test");
		as.addAttribute(TextAttribute.BACKGROUND, Color.green, 5, 14);
		as.addAttribute(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUPER, 5, 6);
		
		JComponent comp = new JComponent() {

			@Override
			protected void paintComponent(Graphics g) {
				g.drawString(as.getIterator(), 10, 30);
			}
			
		};
		comp.setPreferredSize(new Dimension(300, 200));
		getContentPane().add(comp);
	}
	
	void test2() throws Exception {
		JTextPane pane = new JTextPane();
		pane.setEditable(false);
		//pane.setDocument(new CoreferenceDocument());
		pane.setEditorKit(new CoreferenceEditorKit());
		
		String[] forms = new String[] {
				"This", //$NON-NLS-1$
				"is", //$NON-NLS-1$
				"a", //$NON-NLS-1$
				"test", //$NON-NLS-1$
				"and", //$NON-NLS-1$
				"nothing", //$NON-NLS-1$
				"else", //$NON-NLS-1$
				",", //$NON-NLS-1$
				"or", //$NON-NLS-1$
				"is", //$NON-NLS-1$
				"it", //$NON-NLS-1$
				"?", //$NON-NLS-1$
		};
		Span[] mentions = new Span[] {
				new Span(1, 4, 23),
				new Span(2, 2, 7),
				new Span(5, 6, 12),
		};
		
		CoreferenceData data = new DefaultCoreferenceData(forms, mentions);
		
		CoreferenceDocument doc = (CoreferenceDocument)pane.getDocument();
		doc.insertCoreferenceData(doc.getLength(), data);
		
		pane.setMinimumSize(new Dimension(300, 150));
		getContentPane().add(pane);
	}
	
	void test3() throws Exception {
		JTextPane pane = new JTextPane();
		pane.setEditable(false);
		//pane.setEditorKit(new CoreferenceEditorKit());
		
		StyledDocument doc = (StyledDocument) pane.getDocument();
		
		Color bg = Color.green;
		
		MutableAttributeSet empty = new SimpleAttributeSet();
		StyleConstants.setBackground(empty, bg);
		
		MutableAttributeSet superscript = new SimpleAttributeSet();
		StyleConstants.setBackground(superscript, bg);
		StyleConstants.setSuperscript(superscript, true);

		MutableAttributeSet subscript = new SimpleAttributeSet();
		StyleConstants.setBackground(subscript, bg);
		StyleConstants.setSubscript(subscript, true);
		
		doc.remove(0, doc.getLength());
		doc.insertString(doc.getLength(), "test ", empty);
		doc.insertString(doc.getLength(), "23", superscript);
		doc.insertString(doc.getLength(), "test ", empty);
		doc.insertString(doc.getLength(), "5", subscript);
		
		pane.setMinimumSize(new Dimension(300, 150));
		getContentPane().add(pane);
	}
	
	void test4() throws Exception {
		Location location = Locations.getFileLocation("data/coref/eng_dev_v4_auto_conll.gz");
		CONLL12DocumentReader reader = new CONLL12DocumentReader();
		reader.init(location, null);
		CoreferenceDocumentData documentData;
		if((documentData=reader.next())==null) {
			return;
		}
		

		final JTextPane pane = new JTextPane() {

			@Override
			public boolean getScrollableTracksViewportWidth() {
				//return super.getScrollableTracksViewportWidth();
				return true;
			}
			
		};
		
		pane.setEditable(false);
		pane.setEditorKit(new CoreferenceEditorKit());
		//UIUtil.enableToolTip(pane);
		
		CoreferenceDocument doc = (CoreferenceDocument)pane.getDocument(); 
		for(int i=0; i<documentData.size(); i++) {
			doc.insertCoreferenceData(documentData.get(i));
			doc.newLine();
		}
		
		JScrollPane scrollPane = new JScrollPane(pane);
		scrollPane.setMinimumSize(new Dimension(300, 150));
		scrollPane.getViewport().addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				pane.repaint(pane.getVisibleRect());
			}
		});
		
		getContentPane().add(scrollPane);
	}
	
	void test5() throws Exception {
		Location location = Locations.getFileLocation("data/coref/eng_dev_v4_auto_conll.gz");
		CONLL12DocumentReader reader = new CONLL12DocumentReader();
		reader.init(location, null);
		CoreferenceDocumentData documentData;
		if((documentData=reader.next())==null) {
			return;
		}
		
		DataListModel<SentenceData> model = new DataListModel<>(documentData);
		final JList<SentenceData> list = new JList<SentenceData>(model){

			@Override
			public boolean getScrollableTracksViewportWidth() {
				return super.getScrollableTracksViewportWidth();
				//return true;
			}
			
		};
		//list.setCellRenderer(new CoreferenceCellRenderer());

		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setMinimumSize(new Dimension(300, 150));
		scrollPane.getViewport().addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				list.repaint(list.getVisibleRect());
			}
		});
		
		getContentPane().add(scrollPane);
	}
}