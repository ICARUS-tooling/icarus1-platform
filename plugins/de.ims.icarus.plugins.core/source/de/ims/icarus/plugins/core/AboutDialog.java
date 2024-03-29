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
package de.ims.icarus.plugins.core;

import java.awt.Color;
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
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import org.java.plugin.registry.PluginDescriptor;

import de.ims.icarus.Core;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.GridBagUtil;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.dialog.DialogFactory;


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

		setIconImages(Core.getIconImages());

		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.pack();
		this.setMinimumSize(new Dimension(600, 450));
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setIconImage(Core.getSmallIcon().getImage());

		//center jdialog
		this.setLocationRelativeTo(null);
		this.addWindowListener(new WindowAdapter() {
			@Override
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
	}

	protected void addSeperator(JPanel panel, GridBagConstraints gbc){
		//seperator
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridx = 0;
		panel.add(new JSeparator(SwingConstants.HORIZONTAL), gbc);
	}

	private String versionString() {
		StringBuilder sb = new StringBuilder();

		sb.append(Core.getCore().getAppVersion())
		.append("  (") //$NON-NLS-1$
		.append(Core.getCore().getAppBuildDate())
		.append(')');

		return sb.toString();
	}

	protected void buildAbout() {

		GridBagConstraints gbc = new GridBagConstraints();
		gbc = GridBagUtil.makeGbc(0, 0, 0, 1, 1);
		gbc.insets = new Insets(5, 5, 5, 5);

		JLabel logo = new JLabel(IconRegistry.getGlobalRegistry()
							.getIcon("icarus_logo_small.png")); //$NON-NLS-1$
		logo.setBorder(BorderFactory.createLineBorder(new Color(41, 79, 157)));
		logo.setBackground(Color.white);
		logo.setOpaque(true);
		aboutPanel.add(logo, gbc);

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
		aboutPanel.add(new JLabel(versionString()), gbc);
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
//		JLabel eMailMarkus = new JLabel(ResourceManager.getInstance()
//								.get("plugins.core.aboutDialog.eMail.markus")); //$NON-NLS-1$
//		aboutPanel.add(eMailMarkus, gbc);
//		gbc.gridy++;
//		gbc = GridBagUtil.makeGbc(1, gbc.gridy, 1, 1, 0);
//		JLabel eMailGregor = new JLabel(ResourceManager.getInstance()
//								.get("plugins.core.aboutDialog.eMail.gregor")); //$NON-NLS-1$
//		aboutPanel.add(eMailGregor, gbc);
		JLabel eMailIcarus = new JLabel(ResourceManager.getInstance()
		.get("plugins.core.aboutDialog.eMail.icarus")); //$NON-NLS-1$
		aboutPanel.add(eMailIcarus, gbc);
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

		gbc = GridBagUtil.makeGbc(1, gbc.gridy, 1, 1, 1);
		gbc.gridwidth = 2;
		JList<PluginDescriptor> plugins = new JList<>(
				new Vector<>(PluginUtil.getPluginRegistry().getPluginDescriptors()));
		plugins.setCellRenderer(new PluginsListCellRenderer());
		plugins.setBorder(UIUtil.defaultContentBorder);
		JScrollPane pluginsJSP = new JScrollPane(plugins);
		UIUtil.defaultSetUnitIncrement(pluginsJSP);
		aboutPanel.add(pluginsJSP, gbc);
		gbc.gridy++;


		//license Button
		JButton license = new JButton(ResourceManager.getInstance().get(
				"plugins.core.aboutDialog.ShowlicenseText")); //$NON-NLS-1$
		license.setFocusable(false);

		license.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
            	DisclaimerDialog.showDialogLicenseOnly();
            }
        });

		gbc = GridBagUtil.makeGbc(0, gbc.gridy, 1, 1, 0);
		aboutPanel.add(new JLabel(ResourceManager.getInstance().get(
				"plugins.core.aboutDialog.licenseText")),gbc); //$NON-NLS-1$
		gbc = GridBagUtil.makeGbc(1, gbc.gridy, 1, 1, 0);
		aboutPanel.add(license,gbc);
		gbc.gridy++;


		if (isBrowsingSupported()) {
			makeLinkable(url, new LinkMouseListener());
			//makeLinkable(eMailMarkus, new LinkMouseListener());
			//makeLinkable(eMailGregor, new LinkMouseListener());
			makeLinkable(eMailIcarus, new LinkMouseListener());
		}


		JButton bclose = new JButton(ResourceManager.getInstance().get("close")); //$NON-NLS-1$

		bclose.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                dialogExitAction();
            }
        });

		gbc = GridBagUtil.makeGbc(0, gbc.gridy, 1, 1, 1);
		gbc.gridwidth = 3;
		addSeperator(aboutPanel, gbc);
		gbc.gridy++;

		//close Button
		gbc = GridBagUtil.makeGbc(0, gbc.gridy, 1, 1, 0);
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.CENTER;
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
				Desktop desktop = Desktop.getDesktop();
				URI uri = new URI(getPlainLink(l.getText()));
				desktop.browse(uri);
			} catch (URISyntaxException use) {
				throw new AssertionError(use);
			} catch (IOException ioe) {
				DialogFactory.getGlobalFactory().showDetailedError(
						null,
						"plugins.core.aboutDialog.urlErrorTitle",  //$NON-NLS-1$
						"plugins.core.aboutDialog.urlError",  //$NON-NLS-1$
						ioe);
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


	public class PluginsListCellRenderer extends JLabel implements ListCellRenderer<Object>{

		private static final long serialVersionUID = -6517753002122477794L;


		public PluginsListCellRenderer(){
	         setOpaque(true);
	     }


		/**
		 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
		 */
		@Override
		public Component getListCellRendererComponent(
				JList<? extends Object> list, Object value, int index,
				boolean isSelected, boolean cellHasFocus) {

			setBackground(list.getBackground());

			PluginDescriptor pd = (PluginDescriptor) value;
			String text = pd.getId()
					 + "   (" //$NON-NLS-1$
					 + pd.getVersion()
					 + " )"; //$NON-NLS-1$

			setText(text);

			return this;
		}
	}
}
