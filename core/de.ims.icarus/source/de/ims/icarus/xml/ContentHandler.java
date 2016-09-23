/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class ContentHandler extends DefaultHandler {

	private StringBuilder textBuffer = new StringBuilder(30);
	private boolean ignoreCharacters = false;

	protected boolean isIgnoreCharacters() {
		return ignoreCharacters;
	}

	protected void setIgnoreCharacters(boolean ignoreCharacters) {
		this.ignoreCharacters = ignoreCharacters;
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if(!ignoreCharacters) {
			textBuffer.append(ch, start, length);
		}
	}

	protected void clearText() {
		textBuffer.setLength(0);
	}

	protected String getText() {
		String text = textBuffer.toString().trim();
		clearText();
		return text;
	}

	protected long longText() {
		String s = getText();
		return (s==null || s.isEmpty()) ? 0 : Long.parseLong(s);
	}

	protected int intText() {
		String s = getText();
		return (s==null || s.isEmpty()) ? 0 : Integer.parseInt(s);
	}

	protected float floatText() {
		String s = getText();
		return (s==null || s.isEmpty()) ? 0 : Float.parseFloat(s);
	}

	protected double doubleText() {
		String s = getText();
		return (s==null || s.isEmpty()) ? 0 : Double.parseDouble(s);
	}

	protected boolean booleanText() {
		String s = getText();
		return (s==null || s.isEmpty()) ? false : Boolean.parseBoolean(s);
	}

	protected boolean bitText() {
		return "1".equals(getText()); //$NON-NLS-1$
	}

	public static String stringValue(Attributes attr, String key) {
		return attr.getValue(key);
	}

	public static long longValue(String s) {
		return (s==null || s.isEmpty()) ? 0 : Long.parseLong(s);
	}

	public static long longValue(Attributes attr, String key) {
		return longValue(attr.getValue(key));
	}

	public static double doubleValue(String s) {
		return (s==null || s.isEmpty()) ? 0 : Double.parseDouble(s);
	}

	public static double doubleValue(Attributes attr, String key) {
		return doubleValue(attr.getValue(key));
	}

	public static float floatValue(String s) {
		return (s==null || s.isEmpty()) ? 0 : Float.parseFloat(s);
	}

	public static float floatValue(Attributes attr, String key) {
		return floatValue(attr.getValue(key));
	}

	public static int intValue(String s) {
		return (s==null || s.isEmpty()) ? 0 : Integer.parseInt(s);
	}

	public static int intValue(Attributes attr, String key) {
		return intValue(attr.getValue(key));
	}

	public static boolean booleanValue(String s) {
		return s!=null && Boolean.parseBoolean(s);
	}

	public static boolean booleanValue(Attributes attr, String key) {
		return bitValue(attr.getValue(key));
	}

	public static boolean bitValue(String s) {
		return "1".equals(s); //$NON-NLS-1$
	}

	public static boolean bitValue(Attributes attr, String key) {
		return bitValue(attr.getValue(key));
	}
}
