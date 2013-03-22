/* 
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package net.ikarus_systems.icarus.plugins.weblicht;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;

import net.ikarus_systems.icarus.plugins.weblicht.webservice.WebchainRegistry;
import net.ikarus_systems.icarus.plugins.weblicht.webservice.Webservice;
import net.ikarus_systems.icarus.plugins.weblicht.webservice.WebserviceIOAttributes;
import net.ikarus_systems.icarus.plugins.weblicht.webservice.WebserviceRegistry;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.dialog.BasicDialogBuilder;
import net.ikarus_systems.icarus.ui.dialog.DialogFactory;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class WebserviceDialogs {
	


	private static WebserviceDialogs webserviceDialogFactory;
	
	public static WebserviceDialogs getWebserviceDialogFactory() {
		if(webserviceDialogFactory==null) {
			synchronized (DialogFactory.class) {
				if(webserviceDialogFactory==null)
					webserviceDialogFactory= new WebserviceDialogs();
			}
		}
		
		return webserviceDialogFactory;
	}
	
	private Component getContainer(JComponent comp) {
		Object container = comp.getClientProperty("container"); //$NON-NLS-1$
		return container instanceof Component ? (Component)container : comp;
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
	
	
	public WebserviceIOAttributes showWebserviceIOEditAttributes(Component parent, String title, 
		String message, String attributename, String attributevalues, Object...params) {
		BasicDialogBuilder builder = new BasicDialogBuilder(DialogFactory.getGlobalFactory().getResourceDomain());
		
		WebserviceIOAttributes wio = new WebserviceIOAttributes();

		JTextField attributenameField = new JTextField(30);
		attributenameField.setText(attributename);
		
		JTextField attributevaluesField = new JTextField(30);
		attributevaluesField.setText(attributevalues);
		
		builder.setTitle(title);
		builder.setMessage(message, params);
		builder.addMessage("Attribute Name"); //$NON-NLS-1$
		builder.addMessage(attributenameField);
		builder.addMessage("Attribute Values"); //$NON-NLS-1$
		builder.addMessage(attributevaluesField);
		builder.setPlainType();
		builder.setOptions("ok", "cancel"); //$NON-NLS-1$ //$NON-NLS-2$
		
		builder.showDialog(parent);
		
		if(!builder.isYesValue()) {
			return null;
		}
		
		wio.setAttributename(attributenameField.getText());
		wio.setAttributevalues(attributevaluesField.getText());
		
		return wio;
	}
	
	
	public String showWebserviceCooserDialog(Component parent, String title, 
			String message, List<Webservice> ws, List<String> wsQuery, String query, Object...params) {
		
		JList<Object> webserviceList;
		JList<Object> queryList;
		
		WebserviceViewListModel webserviceViewListModel;
		
		BasicDialogBuilder builder = new BasicDialogBuilder(DialogFactory.getGlobalFactory().getResourceDomain());
		
		builder.setTitle(title);
		
		webserviceViewListModel = new WebserviceViewListModel();
		webserviceList = new JList<Object>(webserviceViewListModel);
		webserviceList.setBorder(UIUtil.defaultContentBorder);
		DefaultListSelectionModel selectionModel = new DefaultListSelectionModel();
		selectionModel.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
		
		
		//TODO nicer gui
		queryList = new JList<Object>(wsQuery.toArray());
		queryList.setBorder(UIUtil.defaultContentBorder);

		
		builder.setMessage(message, params);
		builder.addMessage(webserviceList);
		builder.addMessage("WebchainOutformat"); //$NON-NLS-1$
		builder.addMessage(queryList);
		builder.setPlainType();
		builder.setOptions("ok", "cancel"); //$NON-NLS-1$ //$NON-NLS-2$
		
		builder.showDialog(parent);
		
		Webservice webservice = (Webservice) webserviceList.getSelectedValue();
		
		return builder.isYesValue() ? webservice.getUID() : null;
	}
	
	
	
	public Webservice showWebserviceInputDialog(Component parent, String title, 
			String message, String uID, Object...params) {
		
		JTextField uniqueID = new JTextField(30);
		uniqueID.setText(uID);
		uniqueID.setEditable(false);
		
		JTextField name = new JTextField(30);
		JTextArea description = createTextArea();
		JTextField creator = new JTextField(30);
		JTextField contact = new JTextField(30);
		JTextField url = new JTextField(60);
		JTextField serviceID = new JTextField(30);
		
		BasicDialogBuilder builder = new BasicDialogBuilder(DialogFactory.getGlobalFactory().getResourceDomain());
		

		builder.setTitle(title);
		builder.setMessage(message, params);
		builder.addMessage("UniqueID:"); //$NON-NLS-1$
		builder.addMessage(uniqueID);
		builder.addMessage("Name:"); //$NON-NLS-1$
		builder.addMessage(name);
		builder.addMessage("Description:"); //$NON-NLS-1$
		builder.addMessage(getContainer(description));
		builder.addMessage("Creator:"); //$NON-NLS-1$
		builder.addMessage(creator);
		builder.addMessage("Contact:"); //$NON-NLS-1$
		builder.addMessage(contact);
		builder.addMessage("URL:"); //$NON-NLS-1$
		builder.addMessage(url);
		builder.addMessage("ServiceID:"); //$NON-NLS-1$
		builder.addMessage(serviceID);
		
		builder.setPlainType();
		builder.setOptions("ok", "cancel"); //$NON-NLS-1$ //$NON-NLS-2$
		
		builder.showDialog(parent);
		
		System.out.println(" " + uniqueID.getText()+" " + 
				name.getText()+" " +  description.getText()+" " + 
				contact.getText()+" " +  creator.getText()+" " + 
				url.getText()+" " +  serviceID.getText());
		
		Webservice webservice = new Webservice();
		webservice = WebserviceRegistry.getInstance().createWebservice(
				uniqueID.getText(),
				name.getText(), description.getText(),
				contact.getText(), creator.getText(),
				url.getText(), serviceID.getText());
		
		return builder.isYesValue() ? webservice : null;
	}
}
