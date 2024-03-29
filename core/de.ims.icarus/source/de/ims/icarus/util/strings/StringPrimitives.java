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
package de.ims.icarus.util.strings;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class StringPrimitives {

    static NumberFormatException forInputString(CharSequence s) {
        return new NumberFormatException("For input string: \"" + s + "\""); //$NON-NLS-1$ //$NON-NLS-2$
    }

	/**
	 * @see Integer#parseInt(String, int)
	 */
    public static int parseInt(CharSequence s, int radix, int from, int to)
                throws NumberFormatException
    {
        /*
         * WARNING: This method may be invoked early during VM initialization
         * before IntegerCache is initialized. Care must be taken to not use
         * the valueOf method.
         */

        if (s == null) {
            throw new NumberFormatException("null"); //$NON-NLS-1$
        }

        if (radix < Character.MIN_RADIX) {
            throw new NumberFormatException("radix " + radix + //$NON-NLS-1$
                                            " less than Character.MIN_RADIX"); //$NON-NLS-1$
        }

        if (radix > Character.MAX_RADIX) {
            throw new NumberFormatException("radix " + radix + //$NON-NLS-1$
                                            " greater than Character.MAX_RADIX"); //$NON-NLS-1$
        }

        if(to==-1) {
        	to = s.length()-1;
        }

        int result = 0;
        boolean negative = false;
        int i = from;
        int len = to-from+1;
        int limit = -Integer.MAX_VALUE;
        int multmin;
        int digit;

        if (len > 0) {
            char firstChar = s.charAt(i);
            if (firstChar < '0') { // Possible leading "+" or "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Integer.MIN_VALUE;
                } else if (firstChar != '+')
                    throw forInputString(s);

                if (len == 1) // Cannot have lone "+" or "-"
                    throw forInputString(s);
                i++;
            }
            multmin = limit / radix;
            while (i <= to) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit(s.charAt(i++),radix);
                if (digit < 0) {
                    throw forInputString(s);
                }
                if (result < multmin) {
                    throw forInputString(s);
                }
                result *= radix;
                if (result < limit + digit) {
                    throw forInputString(s);
                }
                result -= digit;
            }
        } else {
            throw forInputString(s);
        }
        return negative ? result : -result;
    }

    public static int parseInt(CharSequence s, int radix) throws NumberFormatException {
    	return parseInt(s, radix, 0, -1);
    }

    public static int parseInt(CharSequence s, int from, int to) throws NumberFormatException {
    	return parseInt(s, 10, from, to);
    }

    public static int parseInt(CharSequence s) throws NumberFormatException {
        return parseInt(s,10,0,-1);
    }

    /**
     * @see Long#parseLong(String, int)
     */
    public static long parseLong(CharSequence s, int radix, int from, int to)
              throws NumberFormatException
    {
        if (s == null) {
            throw new NumberFormatException("null"); //$NON-NLS-1$
        }

        if (radix < Character.MIN_RADIX) {
            throw new NumberFormatException("radix " + radix + //$NON-NLS-1$
                                            " less than Character.MIN_RADIX"); //$NON-NLS-1$
        }
        if (radix > Character.MAX_RADIX) {
            throw new NumberFormatException("radix " + radix + //$NON-NLS-1$
                                            " greater than Character.MAX_RADIX"); //$NON-NLS-1$
        }

        if(to==-1) {
        	to = s.length()-1;
        }

        long result = 0;
        boolean negative = false;
        int i = from;
        int len = to-from+1;
        long limit = -Long.MAX_VALUE;
        long multmin;
        int digit;

        if (len > 0) {
            char firstChar = s.charAt(i);
            if (firstChar < '0') { // Possible leading "+" or "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Long.MIN_VALUE;
                } else if (firstChar != '+')
                    throw forInputString(s);

                if (len == 1) // Cannot have lone "+" or "-"
                    throw forInputString(s);
                i++;
            }
            multmin = limit / radix;
            while (i <= to) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit(s.charAt(i++),radix);
                if (digit < 0) {
                    throw forInputString(s);
                }
                if (result < multmin) {
                    throw forInputString(s);
                }
                result *= radix;
                if (result < limit + digit) {
                    throw forInputString(s);
                }
                result -= digit;
            }
        } else {
            throw forInputString(s);
        }
        return negative ? result : -result;
    }

    public static long parseLong(CharSequence s, int radix) throws NumberFormatException {
    	return parseLong(s, radix, 0, -1);
    }

    public static long parseLong(CharSequence s, int from, int to) throws NumberFormatException {
    	return parseLong(s, 10, from, to);
    }

    public static long parseLong(CharSequence s) throws NumberFormatException {
        return parseLong(s, 10, 0, -1);
    }

    /**
     * @see Short#parseShort(String, int)
     */
    public static short parseShort(CharSequence s, int radix, int from, int to)
        throws NumberFormatException {
        int i = parseInt(s, radix, from, to);
        if (i < Short.MIN_VALUE || i > Short.MAX_VALUE)
            throw new NumberFormatException(
                "Value out of range. Value:\"" + s + "\" Radix:" + radix); //$NON-NLS-1$ //$NON-NLS-2$
        return (short)i;
    }

    public static short parseShort(CharSequence s, int radix) throws NumberFormatException {
    	return parseShort(s, radix, 0, -1);
    }

    public static short parseShort(CharSequence s, int from, int to) throws NumberFormatException {
    	return parseShort(s, 10, from, to);
    }

    public static short parseShort(CharSequence s) throws NumberFormatException {
        return parseShort(s, 10);
    }

    /**
     * @see Float#parseFloat(String)
     */
    public static float parseFloat(CharSequence s) throws NumberFormatException {
        return Float.parseFloat(s.toString());
    }

    /**
     * @see Double#parseDouble(String)
     */
    public static double parseDouble(CharSequence s) throws NumberFormatException {
        return Double.parseDouble(s.toString());
    }

    /**
     * @see Boolean#parseBoolean(String)
     */
    public static boolean parseBoolean(CharSequence s) {
        return toBoolean(s, 0);
    }

    public static boolean parseBoolean(CharSequence s, int offset) {
    	return toBoolean(s, offset);
    }

    private static boolean toBoolean(CharSequence s, int offset) {
        return ((s != null)
        		&& s.length()==4
        		&& (s.charAt(0)=='T' || s.charAt(0)=='t')
        		&& (s.charAt(1)=='R' || s.charAt(1)=='r')
        		&& (s.charAt(2)=='U' || s.charAt(2)=='u')
        		&& (s.charAt(3)=='E' || s.charAt(3)=='e'));
    }
}
