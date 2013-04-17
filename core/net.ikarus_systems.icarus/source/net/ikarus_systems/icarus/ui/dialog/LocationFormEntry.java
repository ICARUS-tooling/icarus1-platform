/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.ui.dialog;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.IconRegistry;
import net.ikarus_systems.icarus.util.location.Location;
import net.ikarus_systems.icarus.util.location.Locations;


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
			// TODO configure file chooser
		}
		
		return locationChooser;
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.dialog.LabeledFormEntry#addComponents(net.ikarus_systems.icarus.ui.dialog.FormBuilder)
	 */
	@Override
	public void addComponents(FormBuilder builder) {
		builder.feedComponent(locationInput, null, getResizeMode());
		builder.feedComponent(locationButton);
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.dialog.FormBuilder.FormEntry#setValue(java.lang.Object)
	 */
	@Override
	public LocationFormEntry setValue(Object value) {
		locationInput.setText(Locations.getPath((Location)value));
		return this;
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.dialog.FormBuilder.FormEntry#getValue()
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
	 * @see net.ikarus_systems.icarus.ui.dialog.FormBuilder.FormEntry#clear()
	 */
	@Override
	public LocationFormEntry clear() {
		locationInput.setText(null);
		return this;
	}
	
	protected void openLocationChooser() {
		File file = new File(locationInput.getText());
		JFileChooser fileChooser = getLocationChooser();
		fileChooser.setSelectedFile(file);
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
				LoggerFactory.getLogger(LocationFormEntry.class).log(LoggerFactory.record(Level.SEVERE, 
						"Failed to edit location", ex)); //$NON-NLS-1$
			}
			return;
		}
	}

}
