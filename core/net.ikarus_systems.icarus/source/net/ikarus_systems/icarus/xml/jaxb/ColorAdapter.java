/*
 * $Revision: 23 $
 * $Date: 2013-04-17 14:39:04 +0200 (Mi, 17 Apr 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/xml/jaxb/ColorAdapter.java $
 *
 * $LastChangedDate: 2013-04-17 14:39:04 +0200 (Mi, 17 Apr 2013) $ 
 * $LastChangedRevision: 23 $ 
 * $LastChangedBy: mcgaerty $
 */
package net.ikarus_systems.icarus.xml.jaxb;

import java.awt.Color;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Markus Gärtner
 * @version $Id: ColorAdapter.java 23 2013-04-17 12:39:04Z mcgaerty $
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
