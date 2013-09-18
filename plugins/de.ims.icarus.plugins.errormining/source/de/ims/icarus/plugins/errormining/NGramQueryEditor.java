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
package de.ims.icarus.plugins.errormining;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.ims.icarus.Core;
import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.search_tools.view.editor.QueryEditor;
import de.ims.icarus.resources.ResourceDomain;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchDescriptor;
import de.ims.icarus.search_tools.SearchEdge;
import de.ims.icarus.search_tools.SearchNode;
import de.ims.icarus.search_tools.SearchQuery;
import de.ims.icarus.search_tools.standard.DefaultConstraint;
import de.ims.icarus.search_tools.standard.DefaultGraphNode;
import de.ims.icarus.search_tools.standard.DefaultSearchGraph;
import de.ims.icarus.search_tools.standard.DefaultSearchOperator;
import de.ims.icarus.ui.GridBagUtil;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.ui.UIDummies;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.dialog.BasicDialogBuilder;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class NGramQueryEditor extends QueryEditor{
	
	protected JPanel contentPanel;

	protected Collection<JComponent> localizedComponents;

	private JScrollPane scrollPane;
	
	//Tablestuff
	protected NGramQTableModel qtm;
	protected JTable qt;
	protected JButton qtAddButton;
	protected JButton qtEditButton;
	protected JButton qtRemoveButton;
	//protected JButton qtIncludeButton;

	private JLabel header;
	//private JLabel infoLabel;

	private Handler handler;
	private CallbackHandler callbackHandler;
	
	private ActionManager actionManager;
	
	protected SearchDescriptor searchDescriptor;
	
	/**
	 * Constructor
	 */
	public NGramQueryEditor(){
		//noop
	}
	
	protected ActionManager getActionManager() {
		if(actionManager==null) {
			actionManager = ActionManager.globalManager().derive();
		}
		
		return actionManager;
	}

	
	public void initContentPanel() {

		// Load actions
		URL actionLocation = NGramQueryEditor.class
				.getResource("query-editor-actions.xml"); //$NON-NLS-1$
		if (actionLocation == null)
			throw new CorruptedStateException(
					"Missing resources: query-editor-actions.xml"); //$NON-NLS-1$
		
		try {
			getActionManager().loadActions(actionLocation);
		} catch (IOException e) {
			LoggerFactory.log(this, Level.SEVERE, "Failed to load actions from file: "+actionLocation, e); //$NON-NLS-1$
			UIDummies.createDefaultErrorOutput(contentPanel, e);
			return;
		}
		
		localizedComponents = new ArrayList<>();
		
		contentPanel = new JPanel();
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		

		handler = createHandler();
		
		// Header label
		header = new JLabel(""); //$NON-NLS-1$
		header.setBorder(new EmptyBorder(3, 5, 10, 20));
		header.setFont(header.getFont().deriveFont(header.getFont().getSize2D() + 2));

		// Info label
//		infoLabel = new JLabel();
//		infoLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
//		infoLabel.setVerticalAlignment(SwingConstants.TOP);
//		ResourceManager.getInstance().getGlobalDomain()
//				.prepareComponent(infoLabel,"plugins.errormining.nGramQueryEditor.notAvailable", null); //$NON-NLS-1$
//		ResourceManager.getInstance().getGlobalDomain().addComponent(infoLabel);
		
		
		ResourceDomain resourceDomain = ResourceManager.getInstance().getGlobalDomain();
		
		//Buttons (Add/Edit/Remove)
		qtAddButton = new JButton();
		qtAddButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("add_obj.gif")); //$NON-NLS-1$
		resourceDomain.prepareComponent(qtAddButton, "add", null); //$NON-NLS-1$
		resourceDomain.addComponent(qtAddButton);
		localizedComponents.add(qtAddButton);
		qtAddButton.addActionListener(handler);
		
		qtEditButton = new JButton();
		qtEditButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("write_obj.gif")); //$NON-NLS-1$
		resourceDomain.prepareComponent(qtEditButton, "edit", null); //$NON-NLS-1$
		resourceDomain.addComponent(qtEditButton);
		localizedComponents.add(qtEditButton);
		qtEditButton.addActionListener(handler);
		
		qtRemoveButton = new JButton();
		qtRemoveButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("delete_obj.gif")); //$NON-NLS-1$
		resourceDomain.prepareComponent(qtRemoveButton, "delete", null); //$NON-NLS-1$
		resourceDomain.addComponent(qtRemoveButton);
		localizedComponents.add(qtRemoveButton);
		qtRemoveButton.addActionListener(handler);
		
//		qtIncludeButton = new JButton();
//		qtIncludeButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("include_on-off.png")); //$NON-NLS-1$
//		resourceDomain.prepareComponent(qtIncludeButton, "include", null); //$NON-NLS-1$
//		resourceDomain.addComponent(qtIncludeButton);
//		localizedComponents.add(qtIncludeButton);
//		qtIncludeButton.addActionListener(handler);	

		
		
		// Description Scrollpane
		scrollPane = new JScrollPane();
		scrollPane.setBorder(null);	
		UIUtil.defaultSetUnitIncrement(scrollPane);
		scrollPane.setPreferredSize(new Dimension(400, 400));

		// Footer area
		JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
		footer.setBorder(new EmptyBorder(5, 20, 5, 20));
		footer.add(new JButton(actionManager.getAction(
				"plugins.errorMining.nGramQueryEditor.loadQueryAction"))); //$NON-NLS-1$
		footer.add(new JButton(actionManager.getAction(
				"plugins.errorMining.nGramQueryEditor.saveQueryAction"))); //$NON-NLS-1$	
		footer.add(new JButton(actionManager.getAction(
				"plugins.errorMining.nGramQueryEditor.resetQueryAction"))); //$NON-NLS-1$	

		

		//put all on viewcontainer
		contentPanel.setLayout(new BorderLayout());
		contentPanel.add(header, BorderLayout.NORTH);
		contentPanel.add(scrollPane, BorderLayout.CENTER);
		contentPanel.add(footer, BorderLayout.SOUTH);
		
		registerActionCallbacks();
				
		//showDefaultInfo();						
		
		buildDialog();		
		
		refreshActions();		
	}
	
	
	
//	private void showDefaultInfo() {
//		scrollPane.setViewportView(infoLabel);
//		header.setText(""); //$NON-NLS-1$
//	
//	}
	

	protected void refreshActions() {
		//nullcheck
		if(qt == null){
			qtAddButton.setEnabled(true);
			qtEditButton.setEnabled(false);
			qtRemoveButton.setEnabled(false);
		}
		
		//empty no items inside -> only allow adding new 
		System.out.println(qt.getRowCount());
		if (qt.getRowCount() == 0){
			qtAddButton.setEnabled(true);
			qtEditButton.setEnabled(false);
			qtRemoveButton.setEnabled(false);
			//qtIncludeButton.setEnabled(false);
		} else {
			qtAddButton.setEnabled(true);
			qtEditButton.setEnabled(true);
			qtRemoveButton.setEnabled(true);
			//qtIncludeButton.setEnabled(true);
		}
	}
	
	
public void buildDialog() {
		
		
		//Buttons
		JPanel buttonPanel = new JPanel(new GridLayout(4, 1));
		buttonPanel.setBorder(new EmptyBorder(0, 5, 5, 5));
		buttonPanel.add(qtAddButton);
		buttonPanel.add(qtEditButton);
		buttonPanel.add(qtRemoveButton);
		//buttonPanel.add(qtIncludeButton);
		
		
		//Table
		JScrollPane scrollPaneTable = new JScrollPane();
		scrollPaneTable.setBorder(null);	
		UIUtil.defaultSetUnitIncrement(scrollPaneTable);
		scrollPaneTable.setPreferredSize(new Dimension(500, 300));
		qtm = new NGramQTableModel();
		
		qt = new JTable(qtm);
		qt.setRowHeight(25);

		//qt.setDefaultRenderer(Object.class, new NGramQTableCellRenderer());
		qt.getColumnModel().getColumn(0).setCellRenderer(qt.getDefaultRenderer(Boolean.class));
		qt.getColumnModel().getColumn(0).setCellEditor(qt.getDefaultEditor(Boolean.class));
		qt.setBorder(UIUtil.defaultContentBorder);
		qt.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		qt.getTableHeader().setReorderingAllowed(false);
		qt.getSelectionModel().addListSelectionListener(handler);
		qt.addMouseListener(handler);
		qt.setIntercellSpacing(new Dimension(4, 4));
		scrollPaneTable.setViewportView(qt);
	

		JPanel jp = new JPanel(new GridBagLayout());
		//GridBagConstraints gbc = GridBagUtil.makeGbcN(0, 1, 1, 1);
		
		jp.add(header, GridBagUtil.makeGbcN(0, 0, 1, 1));
		jp.add(buttonPanel, GridBagUtil.makeGbcN(0, 1, 1, 1));
		jp.add(scrollPaneTable, GridBagUtil.makeGbcN(1, 1, 1, 1));		
		
		
		
//		contentPanel.add(buttonPanel);
//		contentPanel.add(scrollPaneTable);
//		contentPanel.add(jp);
		scrollPane.setViewportView(jp);
		
	}
	
	

	private void addQueryInput() {

		NGramQAttributes att = showEditQAttributes(null,
				"plugins.errormining.dialogs.addInput.title", //$NON-NLS-1$
				"plugins.errormining.dialogs.addInput.message", //$NON-NLS-1$
				null, null);

		if (att == null) {
			return;
		}

		// return cuz of empty attributename
		if (att.getKey().equals("")) { //$NON-NLS-1$
			return;
		}		


		// System.out.println(wio.getAttributename() + " " +
		// wio.getAttributevalues());
		// -1 create item
		qtm.setQueryAttributes(att, -1);
		
		refreshActions();
	}

	private void editQueryInput(String attribute, String attributevalue,
			int index) {

		NGramQAttributes att = showEditQAttributes(null,
				"plugins.errormining.dialogs.editInput.title", //$NON-NLS-1$
				"plugins.errormining.dialogs.editInput.message", //$NON-NLS-1$
				attribute, attributevalue);

		// empty attributename will fail later / not allowed
		if (att.getKey().equals("")) { //$NON-NLS-1$
			DialogFactory.getGlobalFactory().showWarning(null,
					"plugins.errormining.dialogs.emptyAttributename.title", //$NON-NLS-1$
					"plugins.errormining.dialogs.emptyAttributename.message", //$NON-NLS-1$
					null, null);
			return;
		}

		// System.out.println(wio.getAttributename() + " " +
		// wio.getAttributevalues());
		qtm.setQueryAttributes(att, index);
		refreshActions();
	}

	private void deleteQueryInput(String attribute, int itemIndex) {
		if (DialogFactory.getGlobalFactory().showConfirm(null,
				"plugins.errormining.dialogs.deleteInput.title", //$NON-NLS-1$
				"plugins.errormining.dialogs.deleteInput.message", //$NON-NLS-1$
				attribute)) {
			qtm.deleteQueryAttribute(itemIndex);
			refreshActions();
		}
	}

	/**
	 * @param row 
	 * @param attributevalue 
	 * @param attribute 
	 * 
	 */
	@SuppressWarnings("unused")
	private void includeQueryTag(int row) {
		qtm.setInclueQueryAttribute(row);
		refreshActions();
		
	}

	protected void registerActionCallbacks() {
		if (callbackHandler == null) {
			callbackHandler = createCallbackHandler();
		}

		ActionManager actionManager = getActionManager();

		actionManager.addHandler(
				"plugins.errorMining.nGramQueryEditor.loadQueryAction", //$NON-NLS-1$
				callbackHandler, "loadQuery"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.errorMining.nGramQueryEditor.saveQueryAction", //$NON-NLS-1$
				callbackHandler, "saveQuery"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.errorMining.nGramQueryEditor.resetQueryAction", //$NON-NLS-1$
				callbackHandler, "resetQuery"); //$NON-NLS-1$

	}

	protected Handler createHandler() {
		return new Handler();
	}

	
	protected CallbackHandler createCallbackHandler() {
		return new CallbackHandler();
	}
	
	
	private void loadQueryXML(File fXmlFile) throws Exception {		
		//File fXmlFile = new File(Core.getCore().getDataFolder(), "ngramquery.xml"); //$NON-NLS-1$
		
		if(!fXmlFile.exists() || fXmlFile.length()==0) {
			return;
		}
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);

		doc.getDocumentElement().normalize();
		
		ArrayList<NGramQAttributes>  qList = new ArrayList<NGramQAttributes>();
		
		NodeList itemList = doc.getElementsByTagName("Queryitem"); //$NON-NLS-1$

		for (int i = 0; i < itemList.getLength(); i++) {
			Node sNode = itemList.item(i);
			
			if (sNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) sNode;
				
				NGramQAttributes qatt = new NGramQAttributes();
				qatt.setKey(eElement.getAttribute("Tagclass")); //$NON-NLS-1$
				qatt.setValue(eElement.getAttribute("Value")); //$NON-NLS-1$
				qatt.setInclude(Boolean.parseBoolean(
									eElement.getAttribute("Include"))); //$NON-NLS-1$
//				System.out.print(eElement.getAttribute("Tagclass"));
//				System.out.println(+ " " + eElement.getAttribute("Value"));
				qList.add(qatt);				
			}
		}
		qtm.reload(qList);
		synchronizeQuery();
	}
	
	
	
	private void saveQueryXML(File file) throws Exception{
		String root = "NGram-Query"; //$NON-NLS-1$
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.newDocument();

		Element rootElement = document.createElement(root);

		document.appendChild(rootElement);
		
		for (int i = 0; i < qt.getRowCount(); i++){
			Element query = document.createElement("Queryitem"); //$NON-NLS-1$
			Boolean include = (Boolean) qt.getModel().getValueAt(i, 0);
			String key = (String) qt.getModel().getValueAt(i, 1);
			String val = (String) qt.getModel().getValueAt(i, 2);
			// System.out.println(key + " " + val);
			query.setAttribute("Include", include.toString()); //$NON-NLS-1$
			query.setAttribute("Tagclass", key); //$NON-NLS-1$
			query.setAttribute("Value", val); //$NON-NLS-1$
			rootElement.appendChild(query);
		}
		
	    TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        
        //format output
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", //$NON-NLS-1$
        								"2"); //$NON-NLS-1$
        DOMSource source = new DOMSource(document);
        StreamResult result =  new StreamResult(new StringWriter());
        transformer.transform(source, result);
        

        saveXMLToFile(result, file);
	}
	
	private void saveXMLToFile (StreamResult result, File file) throws Exception{

//		if (file.getName().equalsIgnoreCase("xml")) {
//		    // filename is OK as-is
//		} else {
//		    file = new File(file.toString() + ".xml");  // append .xml if "foo.jpg.xml" is OK
//		    file = new File(file.getParentFile(), FilenameUtils.getBaseName(file.getName())+".xml"); // ALTERNATIVELY: remove the extension (if any) and replace it with ".xml"
//		}
		
		
		//writing to file
        FileOutputStream fop = null;
        
        //debug        
        //File file = new File("E:/ngramquery.xml"); //$NON-NLS-1$
        //File  file = new File(Core.getCore().getDataFolder(), "ngramquery.xml"); //$NON-NLS-1$
        fop = new FileOutputStream(file);

        // if file doesnt exists, then create it
        if (!file.exists()) {
            file.createNewFile();
        }

        // get the content in bytes
        String xmlString = result.getWriter().toString();
        // System.out.println(xmlString);
        byte[] contentInBytes = xmlString.getBytes();

        fop.write(contentInBytes);
        fop.flush();
        fop.close();
	}
	
	
	public void setSearchDescriptor(SearchDescriptor searchDescriptor) {
		if(searchDescriptor!=null && searchDescriptor.equals(this.searchDescriptor)) {
			return;
		}
		
		this.searchDescriptor = searchDescriptor;
		
		SearchQuery searchQuery = searchDescriptor==null ? null : searchDescriptor.getQuery();
		
		if(searchQuery==null) {
			//TODO clear all
			qtm.removeAllQueryAttributes();
			//infoLabel.setVisible(true);
			return;
		}

//		graphPresenter.setConstraintContext(searchDescriptor.getSearchFactory().getConstraintContext());
//		SearchGraph graph = searchQuery.getSearchGraph();
//		if(graph!=null) {
//			try {
//				graphPresenter.present(searchQuery.getSearchGraph(), null);
//			} catch (UnsupportedPresentationDataException e) {
//				LoggerFactory.log(this, Level.SEVERE, 
//						"Failed to forward presentation of search graph for query:\n"+searchQuery.getQueryString(), e); //$NON-NLS-1$
//				graph = null;
//			}
//		}


//		infoLabel.setVisible(false);
	}
	
	
	public SearchDescriptor getSearchDescriptor() {
		return searchDescriptor;
	}
	
	
	/*
	public void commitQuery() throws Exception {
		
		Message message = new Message(this, Commands.SELECT, 
				getSearchDescriptor(), null);
		
		sendRequest(ErrorMiningConstants.ERRORMINING_PERSPECTIVE_ID, message);
	}
	
	
	/*
	public void reset() {
		setSearchDescriptor(null);
	}
	
	
	
	protected ResultMessage handleRequest(Message message) throws Exception {
		if(Commands.PRESENT.equals(message.getCommand())
				|| Commands.DISPLAY.equals(message.getCommand())) {
			if(message.getData() instanceof SearchDescriptor) {
				selectViewTab();
				
				setSearchDescriptor((SearchDescriptor)message.getData());
				return message.successResult(this, null);
			} else {
				return message.unsupportedDataResult(this);
			}
		} else if(Commands.CLEAR.equals(message.getCommand())) {
			reset();
			return message.successResult(this, null);
		} else if(Commands.COMMIT.equals(message.getCommand())) {
			commitQuery();
			return message.successResult(this, searchDescriptor);
		} else {
			return message.unknownRequestResult(this);
		}
	}
	
	*/
	

	protected class Handler extends MouseAdapter implements ActionListener,
			ListSelectionListener {

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			// Add Input / output
			if (e.getSource() == qtAddButton) {

				try {
					if (e.getSource() == qtAddButton) {
						addQueryInput();
					}

				} catch (Exception ex) {
					LoggerFactory.log(this, Level.SEVERE,
							"Failed to add key ", ex); //$NON-NLS-1$
				}
				return;
			}

			// edit Input
			if (e.getSource() == qtEditButton) {

				int row = qt.getSelectedRow();
				if (row == -1) {
					return;
				}

				String attribute = (String) qt.getValueAt(row, 1);
				String attributevalue = (String) qt.getValueAt(row, 2);

				try {
					editQueryInput(attribute, attributevalue, row);
				} catch (Exception ex) {
					LoggerFactory.log(this, Level.SEVERE,
							"Failed to edit input attribute", ex); //$NON-NLS-1$
				}
				return;
			}

			// delete Input
			if (e.getSource() == qtRemoveButton) {

				int row = qt.getSelectedRow();
				if (row == -1) {
					return;
				}

				String message = (String) qt.getValueAt(row, 1)
						+ " = " //$NON-NLS-1$
						+ (String) qt.getValueAt(row, 2);

				// System.out.println("DelRow " + inputTableModel.getRowCount()
				// + " " + message);

				try {
					deleteQueryInput(message, row);
				} catch (Exception ex) {
					LoggerFactory.log(this, Level.SEVERE,
							"Failed to delete attribute", ex); //$NON-NLS-1$
				}
				return;
			}
			
			
//			// Include Y/N
//			if (e.getSource() == qtIncludeButton) {
//				int row = qt.getSelectedRow();
//				
//				//no selection
//				if (row == -1) {
//					return;
//				}
//
//				try {
//					includeQueryTag(row);
//				} catch (Exception ex) {
//					LoggerFactory.log(this, Level.SEVERE,
//							"Failed to edit input attribute", ex); //$NON-NLS-1$
//				}
//				return;
//			}

		}

		/**
		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
		 */
		@Override
		public void valueChanged(ListSelectionEvent e) {
			boolean enabled = e.getFirstIndex() > -1;

			if (e.getSource().equals(qt.getSelectionModel())) {
				qtAddButton.setEnabled(enabled);
				qtEditButton.setEnabled(enabled);
				qtRemoveButton.setEnabled(enabled);
				//qtIncludeButton.setEnabled(enabled);
			}

		}
	}

	public NGramQAttributes showEditQAttributes(Component parent, String title,
			String message, String attributename, String attributevalues,
			Object... params) {
		BasicDialogBuilder builder = new BasicDialogBuilder(DialogFactory
				.getGlobalFactory().getResourceDomain());

		NGramQAttributes att = new NGramQAttributes();

		//set curser in textfield
		final JTextField attributenameField = new JTextField(30);
		attributenameField.setText(attributename);
		attributenameField.setCaretPosition(0);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				attributenameField.requestFocus();
			}
		});
		
		JTextField attributevaluesField = new JTextField(30);
		attributevaluesField.setText(attributevalues);

		builder.setTitle(title);
		builder.setMessage(message, params);
		builder.addMessage("plugins.errormining.labels.Key"); //$NON-NLS-1$
		builder.addMessage(attributenameField);
		builder.addMessage("plugins.errormining.labels.Value"); //$NON-NLS-1$
		builder.addMessage(attributevaluesField);
		builder.setPlainType();
		builder.setOptions("ok", "cancel"); //$NON-NLS-1$ //$NON-NLS-2$

		builder.showDialog(parent);

		if (!builder.isYesValue()) {
			return null;
		}

		att.setKey((attributenameField.getText()));
		att.setValue((attributevaluesField.getText()));

		return att;
	}

	public final class CallbackHandler {

		private CallbackHandler() {
			// no-op
		}

		public void loadQuery(ActionEvent e) {
			try {
	            FileNameExtensionFilter xmlfilter = new FileNameExtensionFilter(
	                    "xml files (*.xml)", //$NON-NLS-1$
	                    "xml"); //$NON-NLS-1$
				File file = DialogFactory.getGlobalFactory().showSourceFileDialog(
						null,
						"plugins.errormining.dialogs.selectQueryLoadFile.title", //$NON-NLS-1$
						Core.getCore().getDataFolder()
						,xmlfilter);
				//user cancelled
				if (file == null){
					return;
				}
				
				loadQueryXML(file);
			} catch (Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to load query from file", ex); //$NON-NLS-1$
			}
		}

		public void saveQuery(ActionEvent e) {
			try {
	            FileNameExtensionFilter xmlfilter = new FileNameExtensionFilter(
	                    "xml files (*.xml)", //$NON-NLS-1$
	                    "xml"); //$NON-NLS-1$
				File file = DialogFactory.getGlobalFactory().showDestinationFileDialog(
						null,
						"plugins.errormining.dialogs.selectQuerySaveFile.title", //$NON-NLS-1$
						Core.getCore().getDataFolder(),
						xmlfilter);
				
				//user cancelled
				if(file == null){
					return;
				}
				
				
				//workaround to ensure .xml / .XML file ending
				String fname = file.getAbsolutePath();
	            //if(!fname.toLowerCase().endsWith(".xml")) { //$NON-NLS-1$
	            if(!fname.endsWith(".xml")) { //$NON-NLS-1$
	                file = new File(fname + ".xml"); //$NON-NLS-1$
	            }
				//System.out.println(file.getName());
				
				saveQueryXML(file);
				
			} catch (Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to save query to file", ex); //$NON-NLS-1$
			}
		}

		public void resetQuery(ActionEvent e) {
			boolean reset = DialogFactory.getGlobalFactory().showConfirm(
					null,
					"plugins.errormining.dialogs.resetQuery.title", //$NON-NLS-1$
					"plugins.errormining.dialogs.resetQuery.message"); //$NON-NLS-1$
			
			if (reset){
				qtm.removeAllQueryAttributes();				
			}
		}

	}
	
	
	
	
	/**
	 * @see de.ims.icarus.ui.helper.Editor#getEditorComponent()
	 */
	@Override
	public Component getEditorComponent() {
		if(contentPanel==null) {
			initContentPanel();
		}
		return contentPanel;
	}

	/**
	 * @see de.ims.icarus.ui.helper.Editor#setEditingItem(java.lang.Object)
	 */
	@Override
	public void setEditingItem(SearchDescriptor searchDescriptor) {
		if(contentPanel==null) {
			initContentPanel();
		}
		
		if(searchDescriptor!=null && searchDescriptor.equals(this.searchDescriptor)) {
			return;
		}
		
		this.searchDescriptor = searchDescriptor;
		
		SearchQuery searchQuery = searchDescriptor==null ? null : searchDescriptor.getQuery();
		
		if(searchQuery==null) {
			qtm.removeAllQueryAttributes();
			return;
		}
		
		//TODO reload ngramlist
		//qtm.reload(searchQuery.getQueryString());
		
	}

	/**
	 * @see de.ims.icarus.ui.helper.Editor#applyEdit()
	 */
	@Override
	public void applyEdit() {
		try {
			synchronizeQuery();
		} catch (Exception e) {
			throw new IllegalStateException("Synchronization of query failed", e); //$NON-NLS-1$
		}		
	}
	
	
	
	public void synchronizeQuery() throws Exception {
		System.out.println("Sync Query"); //$NON-NLS-1$
		if(searchDescriptor==null) {
			return;
		}
					
		SearchQuery searchQuery = searchDescriptor.getQuery();
		if(searchQuery==null) {
			return;
		}
		
		//searchQuery.getSearchGraph();
		
		//create one DefaultGraphNode for all constraints from ngrammaquery table		
		DefaultGraphNode dgn = new DefaultGraphNode();
		
		if (qtm.qList == null){
			return;
		}		
		
		SearchConstraint[] constraints = new SearchConstraint[1];
		constraints[0] = new DefaultConstraint("TAG", //$NON-NLS-1$
												qtm.qList,
												DefaultSearchOperator.EQUALS);
		dgn.setConstraints(constraints);
		
		//default create search node and push the only searchconstraint into it
		SearchNode[] sn = new SearchNode[1];
		sn[0] = dgn;
		
		//create new graph
		DefaultSearchGraph graph = new DefaultSearchGraph();
		graph.setRootNodes(sn);
		graph.setNodes(sn);
		//creating empty edges (otherwise unsupportet format thile graph translation
		graph.setEdges(new SearchEdge[0]);		
		
		searchQuery.setSearchGraph(graph);
		searchDescriptor.getSearchFactory().getQueryLabel(searchQuery);
		
		//System.out.println("Graph Added " + searchQuery.getQueryString()); //$NON-NLS-1$
	}


	/**
	 * @see de.ims.icarus.plugins.search_tools.view.editor.QueryEditor#supports(de.ims.icarus.util.data.ContentType)
	 */
	@Override
	public boolean supports(ContentType contentType) {
		return ContentTypeRegistry.isCompatible(
				LanguageUtils.getSentenceDataContentType(), contentType);
	}

}
