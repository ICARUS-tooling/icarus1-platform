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

import net.ikarus_systems.icarus.Core;
import net.ikarus_systems.icarus.config.ConfigRegistry;
import net.ikarus_systems.icarus.config.ConfigRegistry.Handle;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.GridBagUtil;
import net.ikarus_systems.icarus.ui.IconRegistry;
import net.ikarus_systems.icarus.ui.UIUtil;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class DisclaimerDialog extends JDialog {
	

	private static final long serialVersionUID = -5245394732102753579L;
	
	protected JPanel disclaimerPanel;
	protected JCheckBox showOnStart;
	protected JScrollPane jsp;
	protected JButton accept;

	protected boolean accepted = false;
	
	public DisclaimerDialog(){
		disclaimerPanel = new JPanel(new GridBagLayout());
		
		buildDisclaimer();			

		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.pack();
		this.setMinimumSize(new Dimension(700, 650));
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setIconImage(Core.getSmallIcon().getImage());
		
		//center jdialog
		this.setLocationRelativeTo(null);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dialogExitAction();
			}
		});
		
		
	}
	
	
	public static boolean showDialog() {
		
		DisclaimerDialog dd = new DisclaimerDialog();
		dd.setTitle(ResourceManager.getInstance()
				.get("plugins.core.disclaimer.eula")); //$NON-NLS-1$
		dd.setVisible(true);

		return dd.isAccepted();
	}
	
	protected boolean isAccepted() {
		return accepted;
	}
	
	protected void addSeperator(JPanel panel, GridBagConstraints gbc){
		//seperator
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridx = 0;
		panel.add(new JSeparator(JSeparator.HORIZONTAL), gbc);
	}
	
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

		String license = Core.getLicenseText();
		if(license==null) {
			license = "Failed to load license text.\n" + //$NON-NLS-1$
					"\n" + //$NON-NLS-1$
					"Please check the 'license.txt' file in your ICARUS folder}n" + //$NON-NLS-1$
					"or contact the authors if the file is missing."; //$NON-NLS-1$
		}
		
		JTextArea jta = new JTextArea(license, 25, 70);
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
		accept.setActionCommand("accept"); //$NON-NLS-1$
		accept.addActionListener(new LicenseActionListener());
		accept.setEnabled(false);
		
		
		//decline button
		JButton decline = new JButton(ResourceManager.getInstance()
								.get("plugins.core.disclaimerDialog.decline")); //$NON-NLS-1$
		decline.setActionCommand("decline"); //$NON-NLS-1$
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
	        
	        // enable accept only if reached end of scroll-pane
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
			if("accept".equals(e.getActionCommand())){ //$NON-NLS-1$
				
				ConfigRegistry config = ConfigRegistry.getGlobalRegistry();
				Handle ch = config.getHandle("general.eula"); //$NON-NLS-1$
				config.setValue(ch, showOnStart.isSelected());

				accepted = true;
			}

			dialogExitAction();
		}
		
	}


}
