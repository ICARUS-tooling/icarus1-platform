/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools.standard;

import net.ikarus_systems.icarus.search_tools.ConstraintFactory;
import net.ikarus_systems.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultQueryParser {
	
	/**
	 * Prefix used to lookup {@link ConstraintFactory} implementations
	 * when parsing a query. When not set the token as present in the
	 * query will be used.
	 * <p>
	 * The type of this property is {@code String}
	 */
	public static final String TOKEN_PREFIX_OPTION = "tokenPrefix"; //$NON-NLS-1$
	
	/**
	 * When set together with the {@value #TOKEN_PREFIX_OPTION} the
	 * base token will be used in blank form when a lookup with the
	 * given prefix did not yield a valid {@code ConstraintFactory}
	 * implementation.
	 */
	public static final String TOKEN_FALLBACK_OPTION = "tokenFallback"; //$NON-NLS-1$

	public DefaultQueryParser(Options options) {
	}

}
