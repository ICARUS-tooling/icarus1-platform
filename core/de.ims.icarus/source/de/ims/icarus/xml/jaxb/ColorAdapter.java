/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.xml.jaxb;

import java.awt.Color;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Markus Gärtner
 * @version $Id$
 * 
 */
public class ColorAdapter extends XmlAdapter<String, Color> {
	public Color unmarshal(String s) {
		return Color.decode(s);
	}

	public String marshal(Color c) {
		return "#" + Integer.toHexString(c.getRGB()); //$NON-NLS-1$
	}
}
