/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.ui.dialog;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;

import de.ims.icarus.Core;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.util.location.Location;
import de.ims.icarus.util.location.Locations;



/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LocationFormEntry extends LabeledFormEntry<LocationFormEntry> implements ActionListener {
	
	protected final JTextField locationInput;
	protected final JButton locationButton;
	
	protected static JFileChooser locationChooser;

	/**
	 * @param label
	 */
	public LocationFormEntry(String label) {
		super(label);
		
		locationInput = new JTextField(InputFormEntry.DEFAULT_COLUMNS);
		
		locationButton = new JButton(IconRegistry.getGlobalRegistry().getIcon("fldr_obj.gif")); //$NON-NLS-1$
		locationButton.setPreferredSize(new Dimension(24, 20));
		locationButton.setFocusPainted(false);
		locationButton.addActionListener(this);
		
		setResizeMode(FormBuilder.RESIZE_HORIZONTAL);
	}

	/**
	 * 
	 */
	public LocationFormEntry() {
		this("labels.location"); //$NON-NLS-1$
	}
	
	protected static JFileChooser getLocationChooser() {
		if(locationChooser==null) {
			locationChooser = new JFileChooser();
			locationChooser.setMultiSelectionEnabled(false);
			locationChooser.setCurrentDirectory(Core.getCore().getDataFolder());
			// TODO configure file chooser
		}
		
		return locationChooser;
	}

	/**
	 * @see de.ims.icarus.ui.dialog.LabeledFormEntry#addComponents(de.ims.icarus.ui.dialog.FormBuilder)
	 */
	@Override
	public void addComponents(FormBuilder builder) {
		builder.feedComponent(locationInput, null, FormBuilder.RESIZE_HORIZONTAL);
		builder.feedComponent(locationButton);
	}

	/**
	 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#setValue(java.lang.Object)
	 */
	@Override
	public LocationFormEntry setValue(Object value) {
		locationInput.setText(Locations.getPath((Location)value));
		return this;
	}

	/**
	 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#getValue()
	 */
	@Override
	public Object getValue() {
		String locationString = locationInput.getText();
		if(locationString==null || locationString.isEmpty()) {
			return null;
		}
		
		try {
			return Locations.getLocation(locationString);
		} catch (MalformedURLException e) {
			throw new InvalidFormDataException("Invalid location: "+locationString, e); //$NON-NLS-1$
		}
	}
	
	public String getLocationString() {
		return locationInput.getText();
	}

	/**
	 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#clear()
	 */
	@Override
	public LocationFormEntry clear() {
		locationInput.setText(null);
		return this;
	}
	
	protected void openLocationChooser() {
		JFileChooser fileChooser = getLocationChooser();
		File file = null;
		String locationString = locationInput.getText();
		if(locationString!=null && !locationString.isEmpty()) {
			file = new File(locationString);
			fileChooser.setSelectedFile(file);
		}
		int result = fileChooser.showDialog(null, 
				ResourceManager.getInstance().get("select")); //$NON-NLS-1$
		
		if(result==JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
			locationInput.setText(file.getAbsolutePath());
		}
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		
		// Location button
		if(e.getSource()==locationButton) {				
			try {
				openLocationChooser();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to edit location", ex); //$NON-NLS-1$
			}
			return;
		}
	}

}
