/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.treebank.search;

import net.ikarus_systems.icarus.util.UnsupportedFormatException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface SearchQuery {

	/**
	 * Reads the given {@code query} string and converts it into a
	 * collection of constraint objects. If the provided {@code query}
	 * does not satisfy syntactic requirements enforced by the implementing
	 * {@code SearchQuery} class then an {@link UnsupportedFormatException}
	 * should be thrown.
	 * <p>
	 * Note the special requirements mentioned at {@link #getQueryString()}
	 * 
	 * @see #getQueryString()
	 */
	void parseQueryString(String query) throws UnsupportedFormatException;
	
	/**
	 * Returns a textual representation of this {@code SearchQuery} instance.
	 * If it was created using a {@code query} string then this method should
	 * return this string in raw form. Otherwise the constraint objects within this
	 * instance should be used to construct such a {@code query} string.
	 * <p>
	 * <b>Note:</b> It is required that all implementations honor the following
	 * special convention. A {@code query} string obtained from a call to
	 * {@link #getQueryString()} on some {@code SearchQuery} <i>A</i> must 
	 * result in exactly the same collection of constraint objects when passed
	 * to the {@link #parseQueryString(String)} method of some other {@code SearchQuery}
	 * instance <i>B</i>. This holds as long as both instances operate on the
	 * same <i>search language</i>! In addition an implementation may ignore
	 * a call to {@link #parseQueryString(String)} when the supplied {@code query}
	 * string matches the current collection of constraints. 
	 * 
	 * @see #parseQueryString(String)
	 */
	String getQueryString();
	
	/**
	 * Tests this {@code SearchQuery} instance for equality with the given object.
	 * Equality in terms of {@code SearchQuery} objects is defined by equality of the
	 * strings returned by calls to their respective {@link #getQueryString()} methods!
	 */
	boolean equals(Object obj);
	
	void setProperty(String key, Object value);
	
	Object getProperty(String key);
}
