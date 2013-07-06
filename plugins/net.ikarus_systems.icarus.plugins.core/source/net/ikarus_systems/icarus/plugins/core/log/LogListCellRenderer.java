/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.core.log;

import java.awt.Component;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.LogRecord;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LogListCellRenderer extends DefaultListCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4405990824190588008L;
	
	private final Date date = new Date();
	private final DateFormat format = new SimpleDateFormat("HH:mm:ss"); //$NON-NLS-1$

	/**
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList<?> list,
			Object value, int index, boolean isSelected, boolean cellHasFocus) {
		
		LogRecord record = (LogRecord) value;
		date.setTime(record.getMillis());
		
		value = format.format(date)+": "+record.getMessage(); //$NON-NLS-1$
		
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		
		setIcon(LogView.getLogIcon(record));
		
		return this;
	}

}
