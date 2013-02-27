/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util;

import java.awt.Color;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class HtmlUtils {

	private HtmlUtils() {
	}

	public static String hexString(Color color) {
		return String.format("#%02X%02X%02X",  //$NON-NLS-1$
				color.getRed(), 
				color.getGreen(),
				color.getBlue());
	}

	public static String escapeHTML(String s) {
		StringBuffer sb = new StringBuffer(s.length());
		int n = s.length();
		for (int i = 0; i < n; i++) {
			char c = s.charAt(i);
			switch (c) {
			case '<':
				sb.append("&lt;"); //$NON-NLS-1$
				break;
			case '>':
				sb.append("&gt;"); //$NON-NLS-1$
				break;
			case '&':
				sb.append("&amp;"); //$NON-NLS-1$
				break;
			case '"':
				sb.append("&quot;"); //$NON-NLS-1$
				break;
			case 'à':
				sb.append("&agrave;"); //$NON-NLS-1$
				break;
			case 'À':
				sb.append("&Agrave;"); //$NON-NLS-1$
				break;
			case 'â':
				sb.append("&acirc;"); //$NON-NLS-1$
				break;
			case 'Â':
				sb.append("&Acirc;"); //$NON-NLS-1$
				break;
			case 'ä':
				sb.append("&auml;"); //$NON-NLS-1$
				break;
			case 'Ä':
				sb.append("&Auml;"); //$NON-NLS-1$
				break;
			case 'å':
				sb.append("&aring;"); //$NON-NLS-1$
				break;
			case 'Å':
				sb.append("&Aring;"); //$NON-NLS-1$
				break;
			case 'æ':
				sb.append("&aelig;"); //$NON-NLS-1$
				break;
			case 'Æ':
				sb.append("&AElig;"); //$NON-NLS-1$
				break;
			case 'ç':
				sb.append("&ccedil;"); //$NON-NLS-1$
				break;
			case 'Ç':
				sb.append("&Ccedil;"); //$NON-NLS-1$
				break;
			case 'é':
				sb.append("&eacute;"); //$NON-NLS-1$
				break;
			case 'É':
				sb.append("&Eacute;"); //$NON-NLS-1$
				break;
			case 'è':
				sb.append("&egrave;"); //$NON-NLS-1$
				break;
			case 'È':
				sb.append("&Egrave;"); //$NON-NLS-1$
				break;
			case 'ê':
				sb.append("&ecirc;"); //$NON-NLS-1$
				break;
			case 'Ê':
				sb.append("&Ecirc;"); //$NON-NLS-1$
				break;
			case 'ë':
				sb.append("&euml;"); //$NON-NLS-1$
				break;
			case 'Ë':
				sb.append("&Euml;"); //$NON-NLS-1$
				break;
			case 'ï':
				sb.append("&iuml;"); //$NON-NLS-1$
				break;
			case 'Ï':
				sb.append("&Iuml;"); //$NON-NLS-1$
				break;
			case 'ô':
				sb.append("&ocirc;"); //$NON-NLS-1$
				break;
			case 'Ô':
				sb.append("&Ocirc;"); //$NON-NLS-1$
				break;
			case 'ö':
				sb.append("&ouml;"); //$NON-NLS-1$
				break;
			case 'Ö':
				sb.append("&Ouml;"); //$NON-NLS-1$
				break;
			case 'ø':
				sb.append("&oslash;"); //$NON-NLS-1$
				break;
			case 'Ø':
				sb.append("&Oslash;"); //$NON-NLS-1$
				break;
			case 'ß':
				sb.append("&szlig;"); //$NON-NLS-1$
				break;
			case 'ù':
				sb.append("&ugrave;"); //$NON-NLS-1$
				break;
			case 'Ù':
				sb.append("&Ugrave;"); //$NON-NLS-1$
				break;
			case 'û':
				sb.append("&ucirc;"); //$NON-NLS-1$
				break;
			case 'Û':
				sb.append("&Ucirc;"); //$NON-NLS-1$
				break;
			case 'ü':
				sb.append("&uuml;"); //$NON-NLS-1$
				break;
			case 'Ü':
				sb.append("&Uuml;"); //$NON-NLS-1$
				break;
			case '®':
				sb.append("&reg;"); //$NON-NLS-1$
				break;
			case '©':
				sb.append("&copy;"); //$NON-NLS-1$
				break;
			case '€':
				sb.append("&euro;"); //$NON-NLS-1$
				break;
			// be careful with this one (non-breaking white space)
			// case ' ': sb.append("&nbsp;");break;

			default:
				sb.append(c);
				break;
			}
		}
		return sb.toString();
	}
}
