/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.xml;

import javax.xml.bind.ValidationException;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class StAXUtil {

	private StAXUtil() {
		// no-op
	}
	
	public static String errorText(XMLStreamReader reader) {
		Location location = reader.getLocation();
		return "ParseError at [row,col]:["+location.getLineNumber()+","+ //$NON-NLS-1$ //$NON-NLS-2$
        	location.getColumnNumber()+"]"; //$NON-NLS-1$
	}
	
	public static String errorText(XMLStreamReader reader, String msg) {
		Location location = reader.getLocation();
		return "ParseError at [row,col]:["+location.getLineNumber()+","+ //$NON-NLS-1$ //$NON-NLS-2$
        	location.getColumnNumber()+"]\n"+msg; //$NON-NLS-1$
	}

	public static String readText(XMLStreamReader reader, String tag)
			throws XMLStreamException, ValidationException {
		String value = null;
		while(true) {
			int eventType = reader.nextTag();
			if(eventType==XMLStreamConstants.START_ELEMENT) {
				String parsedTag = reader.getLocalName();
				if(!tag.equals(parsedTag)) {
					throw new ValidationException(errorText(reader, "Expected '"+parsedTag+"' - found '"+tag+"'")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
				value = reader.getElementText();
			} else if(eventType==XMLStreamConstants.END_ELEMENT) {
				break;
			} else {
				throw new ValidationException(errorText(reader, "Unexpected xml")); //$NON-NLS-1$
			}
		}
		return value;
	}
}
