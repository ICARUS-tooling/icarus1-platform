/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.util.strings.pattern;

import java.util.Locale;
import java.util.Map;

import de.ims.icarus.util.Options;
import de.ims.icarus.util.collections.CollectionUtils;
import de.ims.icarus.util.strings.StringUtil;

/**
 * Bottom most member of the pattern framework. Implementations of this type are
 * used to access data in the form of properties or methods on the target data.
 * Note that each accessor is assigned an enum indicating what level it is operating
 * at.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class Accessor<L extends Enum<L>> {

	private final String source;
	private final L level;
	private final String specifier;

	// Modifers for strings
	protected String defaultText;
	protected Locale locale = Locale.US;
	protected String separator;
	protected String prefix, suffix;
	protected int minLength, maxLength;
	protected boolean forceUpperCase, forceLowerCase;

	protected Accessor(String source, String specifier, L type) {
		if (source == null)
			throw new NullPointerException("Invalid source"); //$NON-NLS-1$
		if (specifier == null)
			throw new NullPointerException("Invalid specifier"); //$NON-NLS-1$
		if (type == null)
			throw new NullPointerException("Invalid level"); //$NON-NLS-1$

		this.source = source;
		this.specifier = specifier;
		this.level = type;
	}

	public String getSource() {
		return source;
	}

	public L getLevel() {
		return level;
	}

	public String getSpecifier() {
		return specifier;
	}

	public String getDefaultText() {
		return defaultText;
	}

	public Locale getLocale() {
		return locale;
	}

	public String getSeparator() {
		return separator;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public int getMinLength() {
		return minLength;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public String getText(Object data, Options env) {
		Object value = fetchValue(data, env);

		if(value==null) {
			return null;
		}

		value = applyValueOptions(value);

		if(value==null) {
			return null;
		}

		String text = value.toString();

		text = applyStringOptions(text);

		return text;
	}

	public void readOptions(Map<String, String> options) {

		minLength = CollectionUtils.firstSetInt(options, -1, "min", "min_length"); //$NON-NLS-1$ //$NON-NLS-2$
		maxLength = CollectionUtils.firstSetInt(options, -1, "max", "max_length"); //$NON-NLS-1$ //$NON-NLS-2$
		defaultText = CollectionUtils.firstSetString(options, PatternFactory.DEFAULT_EMPTY_TEXT, "def", "default"); //$NON-NLS-1$ //$NON-NLS-2$
		separator = CollectionUtils.firstSetString(options, PatternFactory.DEFAULT_TEXT_SEPARATOR, "sep", "separator"); //$NON-NLS-1$ //$NON-NLS-2$

		suffix = CollectionUtils.firstSet(options, "suf", "suffix"); //$NON-NLS-1$ //$NON-NLS-2$
		prefix = CollectionUtils.firstSet(options, "pref", "prefix"); //$NON-NLS-1$ //$NON-NLS-2$

		forceLowerCase = CollectionUtils.firstSetBoolean(options, false, "lower", "lower_case"); //$NON-NLS-1$ //$NON-NLS-2$
		forceUpperCase = CollectionUtils.firstSetBoolean(options, false, "upper", "upper_case"); //$NON-NLS-1$ //$NON-NLS-2$

		String loca = CollectionUtils.firstSet(options, "locale"); //$NON-NLS-1$
		if(loca!=null) {
			locale = new Locale(loca);
		}
	}

	protected abstract Object fetchValue(Object data, Options env);

	protected Object applyValueOptions(Object value) {
		return value;
	}

	protected String applyStringOptions(String s) {
		if(maxLength>0 && s.length()>maxLength) {
			s = s.substring(0, maxLength);
		} else if(minLength>0 && s.length()<minLength) {
			s = StringUtil.padRight(s, minLength-s.length());
		}

		if(forceLowerCase) {
			s = s.toLowerCase(locale);
		} else if(forceUpperCase) {
			s = s.toUpperCase(locale);
		}

		if(prefix!=null) {
			s = prefix+s;
		}

		if(suffix!=null) {
			s = s+suffix;
		}

		return s;
	}
}
