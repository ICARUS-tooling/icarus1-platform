/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.language;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class LanguageUtils implements LanguageConstants {
	
	private LanguageUtils() {
		// no-op
	}
	
	public static String combine(SentenceData data) {
		StringBuilder sb = new StringBuilder(data.length()*4);
		for(int i=0; i<data.length(); i++) {
			if(i>0) {
				sb.append(" "); //$NON-NLS-1$
			}
			sb.append(data.getForm(i));
		}
		
		return sb.toString();
	}
	
	public static String[] getForms(SentenceData data) {
		String[] result = new String[data.length()];
		
		for(int i=0; i<data.length(); i++) {
			result[i] = data.getForm(i);
		}
		
		return result;
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
	
	public static int getBooleanValue(boolean value) {
		return value ? DATA_YES_VALUE : DATA_NO_VALUE;
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
		else if (DATA_LEFT_LABEL.equals(direction) || "left".equals(direction)) //$NON-NLS-1$
			return DATA_LEFT_VALUE;
		else if (DATA_RIGHT_LABEL.equals(direction) || "right".equals(direction)) //$NON-NLS-1$
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

	public static boolean isProjectiveSentence(short[] heads){
		for(int i=0;i<heads.length;++i){
			if(!isProjective(i, heads[i], heads))
				return false;
		}
		return true;
	}
	
	public static boolean isProjective(int index,short heads[]){
		return isProjective(index,heads[index],heads);
	}
	
	public static boolean isProjective(int dep,int head,short[] heads){
		if(head==DATA_HEAD_ROOT)
			return true;
		int min=dep;
		int max;
		if(head<dep){
			min=head;
			max=dep;
		} else {
			max=head;
		}
		for(int i=min+1;i<max;++i){
			int cur=i;
			while(cur!=dep && cur!=head){
				if(cur==DATA_HEAD_ROOT)
					return false;
				cur=heads[cur];
			}
		}
		return true;
	}

	public static final SentenceData dummySentenceData = new SentenceData() {
		
		private static final long serialVersionUID = 1565778089185335895L;
		
		private final String[] tokens = {
			"This", //$NON-NLS-1$
			"is", //$NON-NLS-1$
			"a", //$NON-NLS-1$
			"test", //$NON-NLS-1$
		};
		
		@Override
		public String getText() {
			return combine(this);
		}
		
		@Override
		public int length() {
			return tokens.length;
		}
		
		@Override
		public boolean isEmpty() {
			return false;
		}
		
		@Override
		public Grammar getSourceGrammar() {
			return null;
		}
		
		@Override
		public String getForm(int index) {
			return tokens[index];
		}
		
		@Override
		public SentenceData clone() {
			return this;
		}
	};
}
