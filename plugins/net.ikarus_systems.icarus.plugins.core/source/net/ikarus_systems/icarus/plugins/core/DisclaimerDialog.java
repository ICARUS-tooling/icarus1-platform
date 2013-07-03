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
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;

import net.ikarus_systems.icarus.config.ConfigRegistry;
import net.ikarus_systems.icarus.config.ConfigRegistry.Handle;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.GridBagUtil;
import net.ikarus_systems.icarus.ui.IconRegistry;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.dialog.DialogFactory;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class DisclaimerDialog extends JDialog {
	

	private static final long serialVersionUID = -5245394732102753579L;
	
	
    final String GPL = "Elevplan checker version \n" //$NON-NLS-1$
            + "Copyright (C) 2013  Philip Jakobsen \n" //$NON-NLS-1$
            + "This program is free software: you can redistribute it and/or modify \n" //$NON-NLS-1$
            + "it under the terms of the GNU General Public License as published by \n" //$NON-NLS-1$
            + "the Free Software Foundation, either version 3 of the License, or \n" //$NON-NLS-1$
            + "(at your option) any later version. \n" //$NON-NLS-1$
            + "This program is distributed in the hope that it will be useful, \n" //$NON-NLS-1$
            + "but WITHOUT ANY WARRANTY; without even the implied warranty of \n" //$NON-NLS-1$
            + "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the \n" //$NON-NLS-1$
            + "GNU General Public License for more details. \n" //$NON-NLS-1$
            + "You should have received a copy of the GNU General Public License \n" //$NON-NLS-1$
            + "along with this program.  If not, see \n" //$NON-NLS-1$
            + "http://www.gnu.org/licenses";  //$NON-NLS-1$
	
	
	protected JPanel disclaimerPanel;
	protected JCheckBox showOnStart;
	protected JScrollPane jsp;
	protected JButton accept;

	
	public DisclaimerDialog(){
		disclaimerPanel = new JPanel(new GridBagLayout());
		
		buildDisclaimer();			

		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.pack();
		this.setMinimumSize(new Dimension(700, 650));
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
		DisclaimerDialog dd = new DisclaimerDialog();
		dd.setTitle(ResourceManager.getInstance()
				.get("plugins.core.disclaimer.eula")); //$NON-NLS-1$
		dd.setVisible(true);

	}
	
	
	@SuppressWarnings("static-access")
	protected void addSeperator(JPanel panel, GridBagConstraints gbc){
		//seperator
		gbc.fill = gbc.HORIZONTAL;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridx = 0;
		panel.add(new JSeparator(JSeparator.HORIZONTAL), gbc);
	}
	
	
	
	/**
	 * 
	 */
	@SuppressWarnings("static-access")
	private void buildDisclaimer() {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc = GridBagUtil.makeGbc(0, 0, 0, 1, 1);
		
		disclaimerPanel.add(new JLabel(IconRegistry.getGlobalRegistry()
				.getIcon("icarus_logo_small.png")), gbc); //$NON-NLS-1$
	
		gbc.gridy = 1;
		
		gbc.insets = new Insets(3, 3, 3, 3);
		
//		addSeperator(disclaimerPanel, gbc);
//		gbc.gridy++;
		
		
		gbc = GridBagUtil.makeGbc(0, gbc.gridy, 1, 1, 1);
		disclaimerPanel.add(new JLabel(ResourceManager.getInstance()
									.get("plugins.core.disclaimerDialog.lilcense")), gbc); //$NON-NLS-1$
		gbc.gridy++;
		
		//License Text
		gbc = GridBagUtil.makeGbc(0, gbc.gridy, 1, 1, 1);
		gbc.gridwidth=5;
//		JTextArea jta = new JTextArea(ResourceManager.getInstance()
//								.get("plugins.core.disclaimerDialog.lilcenseText")); //$NON-NLS-1$
		JTextArea jta = new JTextArea(GPL + GPL + GPL, 25, 70);
		jta.setEditable(false);
		jta.setLineWrap(true);
		jta.setWrapStyleWord(true);
		
		jsp = new JScrollPane(jta);
		jsp.setMinimumSize(new Dimension (600,400));
		jsp.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
		jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jsp.getVerticalScrollBar().addAdjustmentListener(new VAdjustmentListener());
		UIUtil.defaultSetUnitIncrement(jsp);
		disclaimerPanel.add(jsp, gbc);
		gbc.gridy++;
		
//		gbc = GridBagUtil.makeGbc(0, gbc.gridy, 1, 1, 1);
//		gbc.gridwidth=5;
//		addSeperator(disclaimerPanel, gbc);
//		gbc.gridy++;
		
		//Buttons		
		showOnStart = new JCheckBox(ResourceManager.getInstance()
				.get("plugins.core.disclaimerDialog.showOnStart")); //$NON-NLS-1$
		
		//accept button
		accept = new JButton(ResourceManager.getInstance()
								.get("plugins.core.disclaimerDialog.accept")); //$NON-NLS-1$
		accept.setName("accept"); //$NON-NLS-1$
		accept.addActionListener(new LicenseActionListener());
		accept.setEnabled(false);
		
		
		//decline button
		JButton decline = new JButton(ResourceManager.getInstance()
								.get("plugins.core.disclaimerDialog.decline")); //$NON-NLS-1$
		decline.setName("decline"); //$NON-NLS-1$
		decline.addActionListener(new LicenseActionListener());
			
		
		//addbutton stuff to panel
		gbc = GridBagUtil.makeGbc(1, gbc.gridy, 1, 1, 1);
		disclaimerPanel.add(showOnStart, gbc);
		
		gbc = GridBagUtil.makeGbc(2, gbc.gridy, 1, 1, 1);
		disclaimerPanel.add(accept, gbc);
		
		gbc = GridBagUtil.makeGbc(3, gbc.gridy, 1, 1, 1);
		disclaimerPanel.add(decline, gbc);
		
		//add everything to jdialog
		this.add(disclaimerPanel);

		
	}



	// DialogExitAction
	protected void dialogExitAction() {
		this.setVisible(false);
		this.dispose();
	}
	
	
	class VAdjustmentListener implements AdjustmentListener{

		/**
		 * @see java.awt.event.AdjustmentListener#adjustmentValueChanged(java.awt.event.AdjustmentEvent)
		 */
		@Override
		public void adjustmentValueChanged(AdjustmentEvent ae) {
			int extent = jsp.getVerticalScrollBar().getModel().getExtent();
	        int current =  jsp.getVerticalScrollBar().getValue()+extent;
	        int max = jsp.getVerticalScrollBar().getMaximum();
	        
	        // System.out.println("Value: " + current + " Max: " + max);
	        
	        // enable accept only if reachd end of scrollpane
	        // (user read the complete EULA)
	        if(current==max){
	        	accept.setEnabled(true);
	        }			
		}
	}

	class LicenseActionListener implements ActionListener{

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {

			if (e.getSource() instanceof JButton){
				JButton button = (JButton) e.getSource();
				
				//System.out.println(button.getName());
				
				if(button.getName().equals("accept")){ //$NON-NLS-1$
					
					ConfigRegistry config = ConfigRegistry.getGlobalRegistry();
					Handle ch = config.getChildHandle("general", "eula"); //$NON-NLS-1$ //$NON-NLS-2$
					config.setValue(ch, showOnStart.isSelected());
	
					dialogExitAction();
				}
				
				if(button.getName().equals("decline")){ //$NON-NLS-1$
					
					boolean exit = DialogFactory.getGlobalFactory().showWarningConfirm(getParent(),
							"plugins.core.disclaimerDialog.decline.dialogTitle", //$NON-NLS-1$
							"plugins.core.disclaimerDialog.decline.dialogMessage", //$NON-NLS-1$
							null,null);
					if(exit){
						System.exit(0);		
					}
				}
			}
		}
		
	}


}
