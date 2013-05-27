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

import java.text.ParseException;

import net.ikarus_systems.icarus.search_tools.ConstraintContext;
import net.ikarus_systems.icarus.search_tools.ConstraintFactory;
import net.ikarus_systems.icarus.search_tools.SearchGraph;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.UnsupportedFormatException;

/**
 * 
 * 
 * 
 * Query-semi-EBNF:
 * 
 * digit			=	"0" to "9" ;<br>
 * number 			=	[ "-" ], digit, { digit } ;<br>
 * letter			=	"A" to "Z" ;<br>
 * space			=	all whitespace characters<br>
 * symbol			=	all special symbols ;<br>
 * character		=	letter | digit | symbol<br>
 * identifier		=	letter , { letter | "_" } ;<br>
 * char_sequence 	=	character , { character } ;<br>
 * text				=	"'" , char_sequence , "'"
 * 						| '"' , char_sequence , '"' ;<br>
 * operator			=	"=" | "!=" | "=~" | "!~" | "=#" | "!#" | "&gt;" | "&ge;" | "&lt;" | "&le;"
 * grouping			=	"&lt;*&gt;"<br>
 *  
 * assignment		=	identifier, [ space ], operator, [ space], (text | grouping);
 * node				=	"[", [ identifier ] { "," identifier } { node }"]" ;
 * 
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
	
	// Quotation marks symbol
	protected static final char QM = '"';
	protected static final char SQM = '\'';
	
	// Escaping character
	protected static final char ESC = '\\';
	
	protected static final char SPACE = ' ';
	
	protected static final char US = '_';
	
	protected static final char ESIG = '=';
	
	protected String query;
	
	protected int index;
	
	protected StringBuilder buffer;
	
	// Optional context, used to complete token fragments
	protected final ConstraintContext context;
	protected final Options options;


	public DefaultQueryParser(Options options) {
		this(null, options);
	}

	public DefaultQueryParser(ConstraintContext context, Options options) {
		if(options==null) {
			options = Options.emptyOptions;
		}
		
		this.options = options;
		this.context = context;
	}
	
	/**
	 * Moves the parse pointer until a non-whitespace
	 * character is encountered.
	 */
	protected void skipWS() {
		while(index < query.length()) {
			index++;
			
			if(!Character.isWhitespace(query.charAt(index))) {
				break;
			}
		}
	}
	
	/**
	 * Checks whether the end of the query string is reached
	 */
	protected boolean isEOS() {
		return index==query.length();
	}
	
	protected boolean hasNext() {
		return index < query.length()-1;
	}
	
	protected char next() {
		index++;
		return current();
	}
	
	protected char current() {
		return query.charAt(index);
	}
	
	protected void reset() {
		index = 0;
	}

	public SearchGraph parseQuery(String query, Options options) throws ParseException {
		if(query==null)
			throw new IllegalArgumentException("Invalid query"); //$NON-NLS-1$
		if(query.isEmpty())
			return null;
		
		this.query = query;
		reset();
		
		
		// TODO
		return null;
	}
	
	protected String parseText() throws ParseException {
		if(isEOS())
			throw new ParseException(errorMessage(
					"Unexpected end of query string - expected 'text'-content"), index); //$NON-NLS-1$
		
		char delimiter = SPACE;
		int delimiterIndex = index;
		boolean escape = false;
		boolean delimiterSet = false;
		boolean closed = true;
		
		buffer.setLength(0);
		
		while(!isEOS()) {
			char c = current();
			
			if(c==delimiter) {
				closed = true;
				break;
			} else if(escape) {
				buffer.append(c);
				escape = false;
			} else if(c==ESC) {
				escape = true;
				delimiterSet = true;
			} else if(!delimiterSet) {
				if(c==QM || c== SQM) {
					delimiter = c;
					closed = false;
					delimiterIndex = index;
				}
				delimiterSet = true;
			} else {
				buffer.append(c);
			}
			
			next();
		}
		
		if(!closed)
			throw new ParseException(errorMessage(
					"Unclosed delimiter '"+delimiter+"' at index "+delimiterIndex), index); //$NON-NLS-1$ //$NON-NLS-2$
		
		return buffer.toString();
	}
	
	protected String parseIdentifier() throws ParseException {
		if(isEOS())
			throw new ParseException(errorMessage(
					"Unexpected end of query string - expected 'identifier'-content"), index); //$NON-NLS-1$
		
		buffer.setLength(0);
		
		while(!isEOS()) {
			char c = current();
			
			if(Character.isLetter(c) || c==US) {
				buffer.append(c);
			} else {
				break;
			}
			
			next();
		}
		
		if(buffer.length()==0)
			throw new ParseException(errorMessage(
					"Unexpected non-letter character at index "+index+" - expected letter character [a-zA-Z_]"), index); //$NON-NLS-1$ //$NON-NLS-2$
		
		return buffer.toString().toLowerCase();
	}
	
	protected String errorMessage(String msg) {
		StringBuilder sb = new StringBuilder(query.length()*2);
		sb.append(msg).append(":\n\n"); //$NON-NLS-1$
		// Make output query fit one line and preserve total length
		sb.append(query.replaceAll("\r\n|\r|\n", " ")).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		sb.append(String.format("%-"+index+"%", "")).append("^"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		
		return sb.toString();
	}

	public String toQuery(SearchGraph graph, Options options) throws UnsupportedFormatException {
		// TODO
		return null;
	}
}
