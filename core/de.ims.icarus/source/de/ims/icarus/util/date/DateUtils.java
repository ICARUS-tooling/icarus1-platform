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
package de.ims.icarus.util.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import de.ims.icarus.util.strings.StringUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DateUtils {


	private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss"; //$NON-NLS-1$

	private static SimpleDateFormat createFormat() {
		SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN);
		format.setTimeZone(TimeZone.getTimeZone("GMT")); //$NON-NLS-1$
		return format;
	}

	private static SimpleDateFormat dateInFormat = createFormat();
	private static SimpleDateFormat dateOutFormat = createFormat();
	private static SimpleDateFormat localDateFormat = new SimpleDateFormat(DATE_PATTERN);

	public static String formatDate(Date date) {
		if(date==null) {
			return null;
		}
		synchronized (dateOutFormat) {
			return dateOutFormat.format(date);
		}
	}

	public static String formatLocalDate(Date date) {
		if(date==null) {
			return null;
		}
		synchronized (localDateFormat) {
			return localDateFormat.format(date);
		}
	}

	public static String formatDuration(long time) {
		if(time<=0)
			return null;

		long s = time/1000;
		long m = s/60;
		long h = m/60;
		long d = h/24;

		s = s%60;
		m = m%60;
		h = h%24;

		StringBuilder sb = new StringBuilder();
		if(d>0) {
			sb.append(' ').append(d).append('D');
		}
		if(h>0) {
			sb.append(' ').append(h).append('H');
		}
		if(m>0) {
			sb.append(' ').append(m).append('M');
		}
		if(s>0) {
			sb.append(' ').append(s).append('S');
		}

		StringUtil.trim(sb);

		return sb.toString();
	}

	public static String formatMilliDuration(long time) {
		if(time<=0)
			return null;

		long ms = time%1000;
		long s = time/1000;
		long m = s/60;
		long h = m/60;
		long d = h/24;

		s = s%60;
		m = m%60;
		h = h%24;

		StringBuilder sb = new StringBuilder();
		if(d>0) {
			sb.append(' ').append(d).append('D');
		}
		if(h>0) {
			sb.append(' ').append(h).append('H');
		}
		if(m>0) {
			sb.append(' ').append(m).append('M');
		}
		if(s>0) {
			sb.append(' ').append(s).append('S');
		}
		if(ms>0) {
			sb.append(' ').append(ms).append("MS"); //$NON-NLS-1$
		}

		StringUtil.trim(sb);

		return sb.toString();
	}

	public static long getTime(Date date) {
		return date==null ? 0L : date.getTime();
	}

	public static Date parseDate(String s) throws ParseException {
		synchronized (dateInFormat) {
			return dateInFormat.parse(s);
		}
	}
}
