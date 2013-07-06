/*
 * $Revision: 7 $
 * $Date: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.core/source/net/ikarus_systems/icarus/plugins/core/log/LogListCellRenderer.java $
 *
 * $LastChangedDate: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $ 
 * $LastChangedRevision: 7 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.plugins.core.log;

import java.awt.Component;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.LogRecord;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 * @author Markus Gärtner
 * @version $Id: LogListCellRenderer.java 7 2013-02-27 13:18:56Z mcgaerty $
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
