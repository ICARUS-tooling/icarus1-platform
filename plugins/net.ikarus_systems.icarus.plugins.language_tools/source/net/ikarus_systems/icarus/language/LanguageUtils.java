/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class LanguageUtils {
	// flags
	public static final int FLAG_PROJECTIVE = (1 << 0);

	/**
	 * Head value to mark the root node.
	 */
	public static final int DATA_HEAD_ROOT = -1;

	public static final String DATA_ROOT_LABEL = "<root>"; //$NON-NLS-1$

	public static final String DATA_UNDEFINED_LABEL = "?"; //$NON-NLS-1$

	public static final String DATA_GROUP_LABEL = "<*>"; //$NON-NLS-1$

	public static final String DATA_LEFT_LABEL = "<<"; //$NON-NLS-1$

	public static final String DATA_RIGHT_LABEL = ">>"; //$NON-NLS-1$

	public static final int DATA_LEFT_VALUE = -1;

	public static final int DATA_RIGHT_VALUE = 1;

	public static final int DATA_GROUP_VALUE = -3;

	public static final int DATA_UNDEFINED_VALUE = -2;

	public static final int DATA_YES_VALUE = 0;

	public static final int DATA_NO_VALUE = -1;
	
	private LanguageUtils() {
		// no-op
	}
	
	public static boolean isRoot(int value) {
		return value==DATA_HEAD_ROOT;
	}
	
	public static boolean isRoot(String value) {
		return DATA_ROOT_LABEL.equals(value);
	}
	
	public static boolean isUndefined(int value) {
		return value==DATA_UNDEFINED_VALUE;
	}
	
	public static boolean isUndefined(String value) {
		return value==null || value.isEmpty() || value.equals(DATA_UNDEFINED_LABEL);
	}
	
	public static String getBooleanLabel(int value) {
		switch (value) {
		case DATA_GROUP_VALUE:
			return DATA_GROUP_LABEL;
		case DATA_UNDEFINED_VALUE:
			return DATA_UNDEFINED_LABEL;
		case DATA_YES_VALUE:
			return String.valueOf(true);
		case DATA_NO_VALUE:
			return String.valueOf(false);
		}
		
		throw new IllegalArgumentException("Unknown value: "+value); //$NON-NLS-1$
	}
	
	public static int parseBooleanLabel(String label) {
		if(DATA_GROUP_LABEL.equals(label))
			return DATA_GROUP_VALUE;
		else if(DATA_UNDEFINED_LABEL.equals(label))
			return DATA_UNDEFINED_VALUE;
		else if(Boolean.parseBoolean(label))
			return DATA_YES_VALUE;
		else
			return DATA_NO_VALUE;
	}

	public static String getHeadLabel(int head) {
		switch (head) {
		case DATA_HEAD_ROOT:
			return DATA_ROOT_LABEL;
		case DATA_UNDEFINED_VALUE:
			return DATA_UNDEFINED_LABEL;
		case DATA_GROUP_VALUE:
			return DATA_GROUP_LABEL;
		default:
			return String.valueOf(head + 1);
		}
	}

	public static String getLabel(int value) {
		switch (value) {
		case DATA_UNDEFINED_VALUE:
			return DATA_UNDEFINED_LABEL;
		case DATA_GROUP_VALUE:
			return DATA_GROUP_LABEL;
		default:
			return String.valueOf(value);
		}
	}

	public static String getDirectionLabel(int value) {
		switch (value) {
		case DATA_UNDEFINED_VALUE:
			return DATA_UNDEFINED_LABEL;
		case DATA_GROUP_VALUE:
			return DATA_GROUP_LABEL;
		case DATA_LEFT_VALUE:
			return DATA_LEFT_LABEL;
		case DATA_RIGHT_VALUE:
			return DATA_RIGHT_LABEL;
		}

		return null;
	}

	public static int parseHeadLabel(String head) {
		head = head.trim();
		if (DATA_ROOT_LABEL.equals(head))
			return DATA_HEAD_ROOT;
		else if (DATA_UNDEFINED_LABEL.equals(head))
			return DATA_UNDEFINED_VALUE;
		else if (DATA_GROUP_LABEL.equals(head))
			return DATA_GROUP_VALUE;
		else
			return Integer.parseInt(head) - 1;
	}

	public static int parseLabel(String value) {
		value = value.trim();
		if (value.isEmpty() || DATA_UNDEFINED_LABEL.equals(value))
			return DATA_UNDEFINED_VALUE;
		else if (DATA_GROUP_LABEL.equals(value))
			return DATA_GROUP_VALUE;
		else
			return Integer.parseInt(value);
	}

	public static int parseDirectionLabel(String direction) {
		direction = direction.trim();
		if (DATA_GROUP_LABEL.equals(direction))
			return DATA_GROUP_VALUE;
		else if (DATA_LEFT_LABEL.equals(direction))
			return DATA_LEFT_VALUE;
		else if (DATA_RIGHT_LABEL.equals(direction))
			return DATA_RIGHT_VALUE;
		else
			return DATA_UNDEFINED_VALUE;
	}

	public static String normalizeLabel(String value) {
		if(value==null)
			return DATA_UNDEFINED_LABEL;
		
		value = value.trim();
		if (value.isEmpty())
			return DATA_UNDEFINED_LABEL;
		else
			return value;
	}
}
