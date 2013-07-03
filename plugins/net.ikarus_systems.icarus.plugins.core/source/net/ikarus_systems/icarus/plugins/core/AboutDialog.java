/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.core;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.WindowConstants;

import net.ikarus_systems.icarus.config.ConfigRegistry;
import net.ikarus_systems.icarus.config.ConfigRegistry.Handle;
import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.GridBagUtil;
import net.ikarus_systems.icarus.ui.IconRegistry;

import org.java.plugin.registry.PluginDescriptor;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class AboutDialog extends JDialog {
	

	private static final long serialVersionUID = 7752834817128092360L;

	protected final JPanel aboutPanel;
	
	private static final String A_HREF = "<a href=\""; //$NON-NLS-1$
	private static final String HREF_CLOSED = "\">"; //$NON-NLS-1$
	private static final String HREF_END = "</a>"; //$NON-NLS-1$
	private static final String HTML = "<html>"; //$NON-NLS-1$
	private static final String HTML_END = "</html>"; //$NON-NLS-1$


	
	public AboutDialog() {
		aboutPanel = new JPanel(new GridBagLayout());
		
		buildAbout();			

		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.pack();
		this.setMinimumSize(new Dimension(600, 450));
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		//center jdialog
		this.setLocationRelativeTo(null);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dialogExitAction();
			}
		});
	}

	public static void showDialog(Component parent) {	
		AboutDialog ad = new AboutDialog();
		ad.setTitle(ResourceManager.getInstance()
				.get("plugins.core.aboutDialog.aboutIcarus")); //$NON-NLS-1$
		ad.setVisible(true);
//		
//		DisclaimerDialog dd = new DisclaimerDialog();
//		dd.setVisible(true);

	}
	
	@SuppressWarnings("static-access")
	protected void addSeperator(JPanel panel, GridBagConstraints gbc){
		//seperator
		gbc.fill = gbc.HORIZONTAL;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridx = 0;
		panel.add(new JSeparator(JSeparator.HORIZONTAL), gbc);
	}
	
	protected void buildAbout() {	

		GridBagConstraints gbc = new GridBagConstraints();
		gbc = GridBagUtil.makeGbc(0, 0, 0, 1, 1);
		//gbc.weightx = 0;
		
			
		aboutPanel.add(new JLabel(IconRegistry.getGlobalRegistry()
				.getIcon("icarus_logo_small.png")), gbc); //$NON-NLS-1$
	
		gbc.gridy = 1;
		
		
		//Version / NR
		//gbc = GridBagUtil.makeGbc(0, gbc.gridy, 0, 1, 1);	
		gbc.insets = new Insets(3, 3, 3, 3);
		
		addSeperator(aboutPanel, gbc);
		gbc.gridy++;

		gbc = GridBagUtil.makeGbc(0, gbc.gridy, 1, 1, 1);
		aboutPanel.add(new JLabel(ResourceManager.getInstance()
									.get("plugins.core.aboutDialog.version")), gbc); //$NON-NLS-1$
		gbc = GridBagUtil.makeGbc(1, gbc.gridy, 0, 1, 1);
		aboutPanel.add(new JLabel(ResourceManager.getInstance()
									.get("plugins.core.aboutDialog.versionNR")), gbc); //$NON-NLS-1$
		gbc.gridy++;
		
		
		//Authors + Name
		gbc = GridBagUtil.makeGbc(0, gbc.gridy, 1, 1, 0);
		aboutPanel.add(new JLabel(ResourceManager.getInstance()
									.get("plugins.core.aboutDialog.authors")), gbc);//$NON-NLS-1$
		gbc = GridBagUtil.makeGbc(1, gbc.gridy, 0, 1, 0);
		aboutPanel.add(new JLabel(ResourceManager.getInstance()
									.get("plugins.core.aboutDialog.authors.name")), gbc);//$NON-NLS-1$
		gbc.gridy++;
		
		
		//Contact + 2x Email
		gbc = GridBagUtil.makeGbc(0, gbc.gridy, 1, 1, 0);
		aboutPanel.add(new JLabel(ResourceManager.getInstance()
									.get("plugins.core.aboutDialog.eMail")), gbc); //$NON-NLS-1$
		gbc = GridBagUtil.makeGbc(1, gbc.gridy, 1, 1, 0);
		JLabel eMailMarkus = new JLabel(ResourceManager.getInstance()
								.get("plugins.core.aboutDialog.eMail.markus")); //$NON-NLS-1$
		aboutPanel.add(eMailMarkus, gbc);
		gbc.gridy++;
		gbc = GridBagUtil.makeGbc(1, gbc.gridy, 1, 1, 0);
		JLabel eMailGregor = new JLabel(ResourceManager.getInstance()
								.get("plugins.core.aboutDialog.eMail.gregor")); //$NON-NLS-1$
		aboutPanel.add(eMailGregor, gbc);
		gbc.gridy++;
		
		//URL + Visit
		gbc = GridBagUtil.makeGbc(0, gbc.gridy, 1, 1, 0);
		aboutPanel.add(new JLabel(ResourceManager.getInstance()
									.get("plugins.core.aboutDialog.uri.visit")), gbc); //$NON-NLS-1$
		gbc = GridBagUtil.makeGbc(1, gbc.gridy, 0, 1, 0);
		JLabel url = new JLabel(ResourceManager.getInstance()
									.get("plugins.core.aboutDialog.uri.url")); //$NON-NLS-1$
		aboutPanel.add(url, gbc);
		gbc.gridy++;
		

		addSeperator(aboutPanel, gbc);
		gbc.gridy++;
		
		//PluginsListed
		gbc = GridBagUtil.makeGbc(0, gbc.gridy, 1, 1, 0);
		aboutPanel.add(new JLabel(ResourceManager.getInstance()
									.get("plugins.core.aboutDialog.plugins")), gbc); //$NON-NLS-1$

		//adds all available plugin names
		for(Iterator<PluginDescriptor> i = PluginUtil.getPluginManager()
			.getRegistry().getPluginDescriptors().iterator(); i.hasNext();){
			PluginDescriptor pd = i.next();
//			System.out.println("ID: " + pd.getId() +" "
//					+" Vendor " + pd.getVendor()
//					+" UniqueID " + pd.getUniqueId()
//					+" Version " + pd.getVersion()
//					+" Class " + pd.getClass()
//					+" Reg " + pd.getRegistry());
			JLabel pluginName = new JLabel(pd.getId()
					 + " (" //$NON-NLS-1$
					 + pd.getVersion()
					 + " )"); //$NON-NLS-1$
			
			gbc = GridBagUtil.makeGbc(1, gbc.gridy, 0, 1, 0);
			aboutPanel.add(pluginName, gbc);
			gbc.gridy++;	
			
		}
	
		
		if (isBrowsingSupported()) {
			makeLinkable(url, new LinkMouseListener());
			makeLinkable(eMailMarkus, new LinkMouseListener());
			makeLinkable(eMailGregor, new LinkMouseListener());
		}
		
		
		JButton bclose = new JButton(ResourceManager.getInstance().get("ok")); //$NON-NLS-1$
		
		bclose.addActionListener(new ActionListener() {  
            public void actionPerformed(ActionEvent e) {  
                dialogExitAction(); 
            }  
        });
        
		
		addSeperator(aboutPanel, gbc);
		gbc.gridy++;
		
		//close Button
		gbc = GridBagUtil.makeGbc(1, gbc.gridy, 1, 1, 0);
		aboutPanel.add(bclose,gbc);

		this.add(aboutPanel);

	}
	
	// DialogExitAction
	protected void dialogExitAction() {
		this.setVisible(false);
		this.dispose();
	}
	
	
	
	private static void makeLinkable(JLabel c, MouseListener ml) {
		assert ml != null;
//		System.out.println("Text: " + c.getText());
//		System.out.println("LinkIfy: " + linkIfy(c.getText()));
//		System.out.println("HTMLIfy: " + htmlIfy(linkIfy(c.getText())));
		c.setText(htmlIfy(linkIfy(c.getText())));
		c.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
		c.addMouseListener(ml);
	}
	
	
	private static boolean isBrowsingSupported() {
		if (!Desktop.isDesktopSupported()) {
			return false;
		}
		boolean result = false;
		Desktop desktop = Desktop.getDesktop();
		if (desktop.isSupported(Desktop.Action.BROWSE)) {
			result = true;
		}
		return result;

	}
	
	private static class LinkMouseListener extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent evt) {
			JLabel l = (JLabel) evt.getSource();
			try {
				Desktop desktop = java.awt.Desktop.getDesktop();
				URI uri = new URI(getPlainLink(l.getText()));
				desktop.browse(uri);
			} catch (URISyntaxException use) {
				throw new AssertionError(use);
			} catch (IOException ioe) {
				ioe.printStackTrace();
				JOptionPane.showMessageDialog(
					null,
					ResourceManager.getInstance()
					.get("plugins.core.aboutDialog.urlError") //$NON-NLS-1$
					,ResourceManager.getInstance()
					.get("plugins.core.aboutDialog.urlErrorTitle") //$NON-NLS-1$
					, JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	

	
	private static String getPlainLink(String s) {
		return s.substring(s.indexOf(A_HREF) + A_HREF.length(),
				s.indexOf(HREF_CLOSED));
	}

	// WARNING
	// This method requires that s is a plain string that requires
	// no further escaping
	private static String linkIfy(String s) {
		return A_HREF.concat(s).concat(HREF_CLOSED).concat(s).concat(HREF_END);
	}

	// WARNING
	// This method requires that s is a plain string that requires
	// no further escaping
	private static String htmlIfy(String s) {
		return HTML.concat(s).concat(HTML_END);
	}
	
	
}
