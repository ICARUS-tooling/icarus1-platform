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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import net.ikarus_systems.icarus.language.dependency.search.DependencyDirectionContraintFactory;
import net.ikarus_systems.icarus.language.dependency.search.DependencyDistanceContraintFactory;
import net.ikarus_systems.icarus.language.dependency.search.DependencyFeaturesContraintFactory;
import net.ikarus_systems.icarus.language.dependency.search.DependencyFormContraintFactory;
import net.ikarus_systems.icarus.language.dependency.search.DependencyLemmaContraintFactory;
import net.ikarus_systems.icarus.language.dependency.search.DependencyPosContraintFactory;
import net.ikarus_systems.icarus.language.dependency.search.DependencyRelationContraintFactory;
import net.ikarus_systems.icarus.search_tools.ConstraintContext;
import net.ikarus_systems.icarus.search_tools.ConstraintFactory;
import net.ikarus_systems.icarus.search_tools.EdgeType;
import net.ikarus_systems.icarus.search_tools.NodeType;
import net.ikarus_systems.icarus.search_tools.SearchConstraint;
import net.ikarus_systems.icarus.search_tools.SearchEdge;
import net.ikarus_systems.icarus.search_tools.SearchGraph;
import net.ikarus_systems.icarus.search_tools.SearchNode;
import net.ikarus_systems.icarus.search_tools.SearchOperator;
import net.ikarus_systems.icarus.search_tools.util.SearchUtils;
import net.ikarus_systems.icarus.util.CollectionUtils;
import net.ikarus_systems.icarus.util.CompactProperties;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.Order;
import net.ikarus_systems.icarus.util.UnsupportedFormatException;
import net.ikarus_systems.icarus.util.data.ContentTypeRegistry;

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
 * node				=	"[", [ identifier ] { ",",  identifier } { node }"]" ;
 * 
 * query			=	[ "!" ], [ space ]
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultQueryParser {
	
	public static void main(String[] args) throws Exception {
		ConstraintContext context = new ConstraintContext(
				ContentTypeRegistry.getInstance().getTypeForClass(String.class));
		
		context.registerFactory("form", DependencyFormContraintFactory.class); //$NON-NLS-1$
		context.registerFactory("pos", DependencyPosContraintFactory.class); //$NON-NLS-1$
		context.registerFactory("lemma", DependencyLemmaContraintFactory.class); //$NON-NLS-1$
		context.registerFactory("features", DependencyFeaturesContraintFactory.class); //$NON-NLS-1$
		context.registerFactory("relation", DependencyRelationContraintFactory.class); //$NON-NLS-1$
		context.registerFactory("distance", DependencyDistanceContraintFactory.class); //$NON-NLS-1$
		context.registerFactory("direction", DependencyDirectionContraintFactory.class); //$NON-NLS-1$
		
		DefaultQueryParser parser = new DefaultQueryParser(context, null);
		
		String query = "[form=bla [lemma~'%sfg&'] {[! form#foo][]}]"; //$NON-NLS-1$
		
		// [form=bla [(id=node_3) , lemma~"%sfg&" ]{[! (node_3=before, node_6=before) , form#foo ][(id=node_6)  ]}]
		
		SearchGraph graph = parser.parseQuery(query, null);
		
		System.out.println("done"); //$NON-NLS-1$
	}
	
	public static final String EXPAND_TOKENS_OPTION = "expandTokens"; //$NON-NLS-1$

	public static final String NODE_NAME_PATTERN_OPTION = "nodeNamePattern"; //$NON-NLS-1$
	public static final String EDGE_NAME_PATTERN_OPTION = "edgeNamePattern"; //$NON-NLS-1$
	public static final String ID_OPTION = "id"; //$NON-NLS-1$
	public static final String EDGETYPE_OPTION = "edgeType"; //$NON-NLS-1$
	public static final String NODETYPE_OPTION = "nodeType"; //$NON-NLS-1$
	
	public static final String DEFAULT_NODE_NAME_PATTERN = "node_%d"; //$NON-NLS-1$
	public static final String DEFAULT_EDGE_NAME_PATTERN = "edge_%d"; //$NON-NLS-1$
	
	// Quotation marks symbol
	protected static final char QUOTATIONMARK = '"';
	protected static final char SINGLE_QUOTATIONMARK = '\'';
	
	// Escaping character
	protected static final char ESCAPE = '\\';
	
	protected static final char SPACE = ' ';
	
	protected static final char UNDERSCORE = '_';
	
	protected static final char EQUALITY_SIGN = '=';
	
	// Square brackets (used as wrappers for a node definition)
	protected static final char SQUAREBRAKET_OPENING = '[';
	protected static final char SQUAREBRAKET_CLOSING = ']';
	
	// Brackets (used as wrappers for a collection of meta-constraints or properties)
	protected static final char BRAKET_OPENING = '(';
	protected static final char BRAKET_CLOSING = ')';
	
	// Curly brackets (used as wrappers for defining disjunctions)
	protected static final char CURLYBRAKET_OPENING = '{';
	protected static final char CURLYBRAKET_CLOSING = '}';
	
	protected static final char NEGATION_SIGN = '!';
	
	// General enumeration delimiter (comma)
	protected static final char COMMA = ',';
	
	protected static final char COLON = ':';
	
	protected String query;
	
	protected int index;
	
	protected StringBuilder buffer = new StringBuilder(100);
	
	// Optional context, used to complete token fragments
	protected final ConstraintContext context;
	protected final Options options;

	protected final NodeStack nodeStack = new NodeStack();

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
	
	public ConstraintContext getConstraintContext() {
		return context;
	}
	
	protected Object getProperty(String key, DefaultGraphNode node, Object defaultValue) {
		Map<String, Object> properties = nodeStack.getProperties(node);
		Object value = properties==null ? null : properties.get(key);
		value = value==null ? options.get(key) : value;
		return value==null ? defaultValue : value;
	}
	
	/**
	 * Moves the parse pointer until a non-whitespace
	 * character is encountered.
	 */
	protected void skipWS() {
		while(Character.isWhitespace(current())) {
			if(!hasNext()) {
				break;
			}
			next();
		}
	}
	
	/**
	 * Checks whether the end of the query string is reached
	 */
	protected boolean isEOS() {
		return index>=query.length();
	}
	
	/**
	 * Checks whether there are unread characters after the
	 * current pointer position
	 */
	protected boolean hasNext() {
		return index < query.length()-1;
	}
	
	/**
	 * Moves the pointer one step and returns the character
	 * at the new location
	 */
	protected char next() {
		index++;
		return current();
	}
	protected char tryNext() {
		return hasNext() ? next() : '\0';
	}
	
	/**
	 * Returns the character at the current pointer position
	 */
	protected char current() {
		return query.charAt(index);
	}
	
	protected char getAndStep() {
		char c = current();
		next();
		return c;
	}
	
	/**
	 * Moves the pointer back to the first position in the
	 * input string and resets the node stack
	 */
	protected void reset() {
		index = 0;
		nodeStack.reset();
		buffer.setLength(0);
		query = null;
	}

	public SearchGraph parseQuery(String query, Options options) throws ParseException {
		if(query==null)
			throw new IllegalArgumentException("Invalid query"); //$NON-NLS-1$
		
		if(query.trim().isEmpty()) {
			return null;
		}
		
		if(options==null) {
			options = Options.emptyOptions;
		}
		
		reset();
		this.query = query;
		
		skipWS();
		
		// Check for global properties
		if(current()==BRAKET_OPENING) {
			parseProperties();
			skipWS();
		}
		
		int rootOperator = SearchGraph.OPERATOR_CONJUNCTION;
		int disjunctionStart = index;
		
		// Check for disjunctive root operator
		if(current()==CURLYBRAKET_OPENING) {
			rootOperator = SearchGraph.OPERATOR_DISJUNCTION;
			nodeStack.setRootOperator(rootOperator);
			next();
		}

		boolean closed = rootOperator!=SearchGraph.OPERATOR_DISJUNCTION;
		
		// Now parse in all the root nodes
		while(!isEOS()) {
			skipWS();
			
			char c = current();
			
			if(c==SQUAREBRAKET_OPENING) {
				parseNode();
			} else if(c==CURLYBRAKET_CLOSING && rootOperator==SearchGraph.OPERATOR_DISJUNCTION) {
				closed = true;
			} else
				throw new ParseException(errorMessage(
						"Illegal character at index "+index), index); //$NON-NLS-1$
			
			if(closed) {
				break;
			}
		}
		
		if(!closed)
			throw new ParseException(errorMessage(
					"Unclosed disjunction at index "+disjunctionStart), index); //$NON-NLS-1$
		
		nodeStack.close();
		
		Map<DefaultGraphNode, Map<String, Object>> pendingProperties = new HashMap<>();
		Map<String, Object> idMap = new HashMap<>();
		List<DefaultGraphEdge> edges = nodeStack.getEdges();
		List<DefaultGraphNode> nodes = nodeStack.getNodes();
		
		// Collect explicitly defined node ids
		for(DefaultGraphNode node : nodes) {
			Map<String, Object> properties = nodeStack.getProperties(node);
			String id = properties==null ? null : (String)properties.get(ID_OPTION);
			if(id!=null && !id.isEmpty()) {
				if(idMap.containsKey(id))
					throw new ParseException("Duplicate static node id: "+id, index); //$NON-NLS-1$
				
				node.setId(id);
				idMap.put(id, node);
				properties.remove(ID_OPTION);
			}
		}

		// Set edge ids
		String edgeNamePattern = options.get(EDGE_NAME_PATTERN_OPTION, DEFAULT_EDGE_NAME_PATTERN);
		int edgeIndex = 0;
		for(DefaultGraphEdge edge : edges) {
			String id = String.format(edgeNamePattern, edgeIndex);
			if(idMap.containsKey(id))
				throw new ParseException("Duplicate edge id: "+id, index); //$NON-NLS-1$
			
			edge.setId(id);
			idMap.put(id, edge);
			edgeIndex++;
		}
		
		// Set remaining node ids and store pending meta-properties
		int nodeIndex = 0;
		for(DefaultGraphNode node : nodes) {
			String namePattern = (String) getProperty(NODE_NAME_PATTERN_OPTION, node, DEFAULT_NODE_NAME_PATTERN);
			
			if(idMap.containsKey(namePattern))
				throw new ParseException("Duplicate static id: "+namePattern, index); //$NON-NLS-1$
			
			// Generate and set id
			String id;
			while(idMap.containsKey((id=String.format(namePattern, nodeIndex)))) {
				nodeIndex++;
			}			
			idMap.put(id, node);			
			nodeIndex++;
			
			Map<String, Object> properties = nodeStack.getProperties(node);
			if(properties==null || properties.isEmpty()) {
				continue;
			}
			
			// Parse and apply types
			if(properties.containsKey(NODETYPE_OPTION)) {
				node.setNodeType(NodeType.parseNodeType(
						(String)properties.get(NODETYPE_OPTION)));
				properties.remove(NODETYPE_OPTION);
			}
			if(properties.containsKey(EDGETYPE_OPTION)) {
				DefaultGraphEdge edge = nodeStack.getFrame(node).getEdge();
				edge.setEdgeType(EdgeType.parseEdgeType(
						(String)properties.get(EDGETYPE_OPTION)));
				properties.remove(EDGETYPE_OPTION);
			}
			
			pendingProperties.put(node, properties);
		}
		
		// Process remaining properties
		Set<String> links = new HashSet<>();
		for(DefaultGraphNode node : pendingProperties.keySet()) {
			Map<String, Object> properties = pendingProperties.get(node);
			for(Map.Entry<String, Object> entry : properties.entrySet()) {
				Order order = null;
				try {
					order = Order.parseOrder((String)entry.getValue());
				} catch(Exception e) {
					// ignore
				}
				
				if(order!=null && order!=Order.UNDEFINED) {
					Object idRef = idMap.get(entry.getKey());
					if(idRef==null || !(idRef instanceof DefaultGraphNode))
						throw new ParseException("Unknown order target reference: "+entry.getKey(), index); //$NON-NLS-1$
					
					DefaultGraphNode target = (DefaultGraphNode)idRef;
					DefaultGraphEdge edge = null;
					
					if(order==Order.AFTER) {
						edge = new DefaultGraphEdge(node, target);
					} else if(order==Order.BEFORE) {
						edge = new DefaultGraphEdge(target, node);
					}
					
					// TODO ensure that there is no precedence edge between nodes in disjoint disjunctive sub-trees
					
					if(edge!=null) {
						edge.setEdgeType(EdgeType.PRECEDENCE);
						String link = edge.getSource().getId()+"_"+edge.getTarget().getId(); //$NON-NLS-1$
						
						if(links.contains(link))
							throw new ParseException("Duplicate link: "+link, index); //$NON-NLS-1$
						
						node.addEdge(edge, node==edge.getTarget());
						target.addEdge(edge, target==edge.getTarget());
						edges.add(edge);
						continue;
					}
				}
				
				throw new ParseException("Unknown property assignment: '"+entry.getKey()+"="+entry.getValue()+"'", index); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
		
		// TODO ensure all generated ids are well formed (only alpha-numeric characters and underscores)!!
		
		
		// Collect root nodes and return graph
		List<SearchNode> roots = SearchUtils.getChildNodes(nodeStack.getVirtualRoot());
		
		DefaultSearchGraph graph = new DefaultSearchGraph();
		graph.setRootOperator(nodeStack.getRootOperator());
		graph.setEdges(edges.toArray(new SearchEdge[0]));
		graph.setNodes(nodes.toArray(new SearchNode[0]));
		graph.setRootNodes(roots.toArray(new SearchNode[0]));
		
		reset();
		
		return graph;
	}
	
	protected String parseUnquotedText() throws ParseException {
		if(isEOS())
			throw new ParseException(errorMessage(
					"Unexpected end of query string - expected 'unquoted-text'-content"), index); //$NON-NLS-1$

		buffer.setLength(0);
		
		while(!isEOS()) {
			char c = current();
			
			if(isLegalId(c)) {
				buffer.append(c);
			} else {
				break;
			}
			
			if(!hasNext()) {
				break;
			}
			
			next();
		}
		
		if(buffer.length()==0)
			throw new ParseException(errorMessage(
					"Unexpected character at index "+index+" - expected alpha-numeric character [a-zA-Z0-9]"), index); //$NON-NLS-1$ //$NON-NLS-2$
		
		return buffer.toString();
	}
	
	protected String parseQuotedText() throws ParseException {
		if(isEOS())
			throw new ParseException(errorMessage(
					"Unexpected end of query string - expected 'quoted-text'-content"), index); //$NON-NLS-1$
		
		char delimiter = getAndStep();
		
		if(delimiter!=QUOTATIONMARK && delimiter!=SINGLE_QUOTATIONMARK)
			throw new ParseException(errorMessage(
					"Illegal delimiter character for quoted text"), index); //$NON-NLS-1$
		
		int delimiterIndex = index;
		boolean escape = false;
		boolean closed = true;
		
		buffer.setLength(0);
		
		while(!isEOS()) {
			char c = current();
			
			if(c==delimiter) {
				closed = true;
				break;
			} else if(escape) {
				// Only escape delimiters, this prevents the need
				// of "escape-flooding" in regex-pattern
				if(c!=delimiter) {
					buffer.append(ESCAPE);
				}
				buffer.append(c);
				escape = false;
			} else if(c==ESCAPE) {
				escape = true;
			} else {
				buffer.append(c);
			}
			
			if(!hasNext()) {
				break;
			}
			
			next();
		}
		
		if(!closed)
			throw new ParseException(errorMessage(
					"Unclosed delimiter '"+delimiter+"' at index "+delimiterIndex), index); //$NON-NLS-1$ //$NON-NLS-2$

		tryNext();
		
		return buffer.toString();
	}
	
	protected String parseIdentifier() throws ParseException {
		if(isEOS())
			throw new ParseException(errorMessage(
					"Unexpected end of query string - expected 'identifier'-content"), index); //$NON-NLS-1$
		
		buffer.setLength(0);
		
		while(!isEOS()) {
			char c = current();
			
			if(isLegalId(c)) {
				buffer.append(c);
			} else {
				break;
			}
			
			if(!hasNext()) {
				break;
			}
			
			next();
		}
		
		if(buffer.length()==0)
			throw new ParseException(errorMessage(
					"Unexpected non-letter character at index "+index+" - expected letter character [a-zA-Z_]"), index); //$NON-NLS-1$ //$NON-NLS-2$
		
		return buffer.toString();
	}
	
	protected void parseProperty() throws ParseException {
		if(isEOS())
			throw new ParseException(errorMessage(
					"Unexpected end of query string - expected 'property'-content"), index); //$NON-NLS-1$
		
		// Parse key
		String key = parseIdentifier();
		skipWS();
		
		// ENsure existence of equality sign
		if(getAndStep()!=EQUALITY_SIGN)
			throw new ParseException(errorMessage(
					"Illegal character at index "+index+" - expected equality sign '='"), index); //$NON-NLS-1$ //$NON-NLS-2$
		skipWS();

		// Parse value
		Object value;
		if(current()==QUOTATIONMARK || current()==SINGLE_QUOTATIONMARK) {
			value = parseQuotedText();
		} else {
			value = parseUnquotedText();
		}
		
		nodeStack.pushProperty(key, value);
	}
	
	protected SearchOperator parseSearchOperator() throws ParseException {
		if(isEOS())
			throw new ParseException(errorMessage(
					"Unexpected end of query string - expected 'search-operator'-content"), index); //$NON-NLS-1$
		
		String s = ""; //$NON-NLS-1$
		while(!isEOS()) {
			char c = current();
			
			if(isLegalId(c)	|| c==QUOTATIONMARK || c==SINGLE_QUOTATIONMARK) {
				break;
			}
			
			s += current();
			
			// Max length of any operator is 3 (GROUPING <*>)
			if(s.length()>=3) {
				break;
			}
			
			if(!hasNext()) {
				break;
			}
			
			next();
		}

		SearchOperator operator = null;
		
		for(SearchOperator op : SearchOperator.values()) {
			if(op.getSymbol().equals(s)) {
				operator = op;
				break;
			}
		}
		
		if(operator==null)
			throw new ParseException(errorMessage(
					"Illegal search-operator prefix"), index); //$NON-NLS-1$
		
		return operator;
	}
	
	protected void parseConstraint() throws ParseException {
		if(isEOS())
			throw new ParseException(errorMessage(
					"Unexpected end of query string - expected 'constraint'-content"), index); //$NON-NLS-1$
		
		// Parse token
		String fragment = parseIdentifier().toLowerCase();
		String token = fragment;
		if(options.get(EXPAND_TOKENS_OPTION, true) && context!=null) {
			token = context.completeToken(fragment);
		}
		if(token==null || (context!=null && !context.isRegistered(token)))
			throw new ParseException(errorMessage(
					"Unrecognized constraint token fragment '"+fragment+"'"), index); //$NON-NLS-1$ //$NON-NLS-2$
		skipWS();
		
		// Parse operator
		SearchOperator operator = parseSearchOperator();
		if(context!=null) {
			ConstraintFactory factory = context.getFactory(token);
			if(!CollectionUtils.contains(factory.getSupportedOperators(), operator))
				throw new ParseException(errorMessage(
						"Unsupported operator '"+operator.getName()+"' for token '"+token+"'"), index); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		skipWS();
		
		// Parse value
		Object value;
		if(current()==QUOTATIONMARK || current()==SINGLE_QUOTATIONMARK) {
			value = parseQuotedText();
		} else {
			value = parseUnquotedText();
		}
		
		if(context!=null) {
			ConstraintFactory factory = context.getFactory(token);
			value = factory.labelToValue(value);
		}
		
		nodeStack.pushConstraint(new DefaultConstraint(token, value, operator));
	}
	
	protected void parseProperties() throws ParseException {
		if(isEOS())
			throw new ParseException(errorMessage(
					"Unexpected end of query string - expected 'property-collection'-content"), index); //$NON-NLS-1$
		
		if(current()!=BRAKET_OPENING)
			throw new ParseException(errorMessage(
					"Illegal character at index "+index+" - expected opening bracket '('"), index); //$NON-NLS-1$ //$NON-NLS-2$
		int collectionStart = index;
		next();
		boolean closed = false;
		
		while(!isEOS()) {
			skipWS();
			
			char c = current();
			
			if(c==COMMA) {
				tryNext();
			} else if(c==BRAKET_CLOSING) {
				closed = true;
			} else if(isLegalId(c)) {
				parseProperty();
			} else if(c!=COLON)
				throw new ParseException(errorMessage(
						"Illegal character at index "+index), index); //$NON-NLS-1$
			
			if(closed) {
				break;
			}
		}
		
		if(!closed)
			throw new ParseException(errorMessage(
					"Unclosed properties collection at index "+collectionStart), index); //$NON-NLS-1$
		
		tryNext();
	}
	
	protected void parseDisjunction() throws ParseException {
		if(isEOS())
			throw new ParseException(errorMessage(
					"Unexpected end of query string - expected 'node-collection'-content"), index); //$NON-NLS-1$
		
		if(current()!=CURLYBRAKET_OPENING)
			throw new ParseException(errorMessage(
					"Illegal character at index "+index+" - expected opening curly bracket '{'"), index); //$NON-NLS-1$ //$NON-NLS-2$
		int disjunctionStart = index;
		boolean closed = false;
		
		nodeStack.openNode();
		nodeStack.getCurrentNode().setNodeType(NodeType.DISJUNCTION);
		next();		
		
		// Apply negation if specified
		if(current()==NEGATION_SIGN) {
			nodeStack.getCurrentNode().setNegated(true);
			next();
		}
		
		int optionCount = 0;

		while(!isEOS()) {
			skipWS();
			
			char c = current();
			
			if(c==COMMA) {
				tryNext();
			} else if(c==CURLYBRAKET_CLOSING) {
				closed = true;
			} else if(c==SQUAREBRAKET_OPENING) {
				parseNode();
				optionCount++;
			} else
				throw new ParseException(errorMessage(
						"Illegal character at index "+index), index); //$NON-NLS-1$
			
			if(closed) {
				break;
			}
		}
		
		if(!closed)
			throw new ParseException(errorMessage(
					"Unclosed disjunction definition at index "+disjunctionStart), index); //$NON-NLS-1$
		if(optionCount<2)
			throw new ParseException(errorMessage(
					"Missing disjunction member nodes - expected 2 or more, got "+optionCount), index); //$NON-NLS-1$
		
		nodeStack.closeNode();
		
		tryNext();
	}
	
	protected void parseNode() throws ParseException {
		if(isEOS())
			throw new ParseException(errorMessage(
					"Unexpected end of query string - expected 'node'-content"), index); //$NON-NLS-1$
		
		if(current()!=SQUAREBRAKET_OPENING)
			throw new ParseException(errorMessage(
					"Illegal character at index "+index+" - expected opening square bracket '['"), index); //$NON-NLS-1$ //$NON-NLS-2$
		
		// Remember start of node definition
		int nodeStart = index;
		boolean closed = false;
		
		nodeStack.openNode();
		next();		
		
		// Apply negation if specified
		if(current()==NEGATION_SIGN) {
			nodeStack.getCurrentNode().setNegated(true);
			next();
		}
		
		while(!isEOS()) {
			skipWS();
			
			char c = current();
			
			if(c==COMMA) {
				tryNext();
			} else if(c==SQUAREBRAKET_OPENING) {
				parseNode();
			} else if(c==SQUAREBRAKET_CLOSING) {
				closed = true;
			} else if(c==CURLYBRAKET_OPENING) {
				parseDisjunction();
			} else if(c==BRAKET_OPENING) {
				parseProperties();
			} else if(isLegalId(c)) {
				parseConstraint();
			} else
				throw new ParseException(errorMessage(
						"Illegal character at index "+index), index); //$NON-NLS-1$
			
			if(closed) {
				break;
			}
		}
		
		if(!closed)
			throw new ParseException(errorMessage(
					"Unclosed node definition at index "+nodeStart), index); //$NON-NLS-1$
		
		nodeStack.closeNode();
		
		tryNext();
	}
	
	protected String errorMessage(String msg) {
		StringBuilder sb = new StringBuilder(query.length()*2);
		sb.append(msg).append(":\n\n"); //$NON-NLS-1$
		// Make output query fit one line and preserve total length
		sb.append(query.replaceAll("\r\n|\r|\n", " ")).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		sb.append(String.format("%-"+index+"s", "")).append("^"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		
		return sb.toString();
	}

	public String toQuery(SearchGraph graph, Options options) throws UnsupportedFormatException {
		if(graph==null)
			throw new IllegalArgumentException("Invalid graph"); //$NON-NLS-1$
		if(SearchUtils.isEmpty(graph))
			throw new IllegalArgumentException("Empty graph"); //$NON-NLS-1$
		
		if(options==null) {
			options = Options.emptyOptions;
		}
		
		reset();
		
		buffer.setLength(0);
		
		Set<SearchNode> idSet = new HashSet<>();
		for(SearchEdge edge : graph.getEdges()) {
			if(edge.getEdgeType()==EdgeType.PRECEDENCE) {
				idSet.add(edge.getTarget());
			}
		}
		
		boolean isDisjuntive = graph.getRootOperator()==SearchGraph.OPERATOR_DISJUNCTION;
		
		if(isDisjuntive) {
			buffer.append(CURLYBRAKET_OPENING);
		}
		
		SearchNode[] roots = graph.getRootNodes();
		for(int i=0; i<roots.length; i++) {
			if(i>0) {
				buffer.append(SPACE);
			}
			
			appendNode(roots[i], null, idSet);
		}

		if(isDisjuntive) {
			buffer.append(CURLYBRAKET_CLOSING);
		}
		
		String result = buffer.toString();
		
		reset();
		
		return result;
	}
	
	protected void appendNode(SearchNode node, SearchEdge head, Set<SearchNode> idSet) {
		buffer.append(SQUAREBRAKET_OPENING);
		
		if(node.isNegated()) {
			buffer.append(NEGATION_SIGN).append(SPACE);
		}
		
		// Collect properties and meta-constraints
		CompactProperties properties = new CompactProperties();
		if(idSet.contains(node)) {
			properties.setProperty(ID_OPTION, node.getId());
		}
		for(int i=0; i<node.getOutgoingEdgeCount(); i++) {
			SearchEdge edge = node.getOutgoingEdgeAt(i);
			if(edge.getEdgeType()==EdgeType.PRECEDENCE) {
				properties.setProperty(edge.getTarget().getId(), Order.AFTER.getToken());
			}
		}
		if(node.getNodeType()!=NodeType.GENERAL) {
			properties.setProperty(NODETYPE_OPTION, node.getNodeType().getToken());
		}
		if(head!=null && head.getEdgeType()!=EdgeType.DOMINANCE) {
			properties.setProperty(EDGETYPE_OPTION, head.getEdgeType().getToken());
		}
		
		// Append properties
		Map<String, Object> propertiesMap = properties.asMap();
		if(propertiesMap!=null && !propertiesMap.isEmpty()) {
			boolean empty = true;
			buffer.append(BRAKET_OPENING);
			
			for(Map.Entry<String, Object> entry : propertiesMap.entrySet()) {
				if(!empty) {
					buffer.append(COMMA).append(SPACE);
				}
				
				empty = false;
				
				buffer.append(entry.getKey());
				buffer.append(EQUALITY_SIGN);
				appendText(entry.getValue().toString());
			}

			buffer.append(BRAKET_CLOSING);			
			buffer.append(SPACE);
		}
		
		// Append constraints
		boolean leadingComma = false;
		if(head!=null) {
			leadingComma = appendConstraints(head.getConstraints(), leadingComma);
		}
		leadingComma = appendConstraints(node.getConstraints(), leadingComma);
		if(leadingComma) {
			buffer.append(SPACE);
		}
		
		// Append sub-nodes
		for(int i=0; i<node.getOutgoingEdgeCount(); i++) {
			SearchEdge edge = node.getOutgoingEdgeAt(i);
			
			if(edge.getEdgeType()==EdgeType.PRECEDENCE 
					|| edge.getEdgeType()==EdgeType.LINK) {
				continue;
			}
			
			SearchNode target = edge.getTarget();
			
			if(target.getNodeType()==NodeType.DISJUNCTION) {
				appendDisjunction(target, idSet);
			} else {
				appendNode(target, edge, idSet);
			}
		}
		
		buffer.append(SQUAREBRAKET_CLOSING);
	}
	
	protected void appendDisjunction(SearchNode node, Set<SearchNode> idSet) {
		buffer.append(CURLYBRAKET_OPENING);
		if(node.isNegated()) {
			buffer.append(NEGATION_SIGN).append(SPACE);
		}
		
		int nodeCount = 0;
		for(int i=0; i<node.getOutgoingEdgeCount(); i++) {
			SearchEdge edge = node.getOutgoingEdgeAt(i);
			if(edge.getEdgeType()==EdgeType.PRECEDENCE 
					|| edge.getEdgeType()==EdgeType.LINK) {
				continue;
			}
			
			appendNode(edge.getTarget(), edge, idSet);
			nodeCount++;
		}
		
		if(nodeCount<2) 
			throw new IllegalArgumentException("Missing disjunction node members - expected 2 or more, got "+nodeCount); //$NON-NLS-1$
		
		buffer.append(CURLYBRAKET_CLOSING);
	}
	
	protected boolean appendConstraints(SearchConstraint[] constraints, boolean leadingComma) {
		if(constraints==null || constraints.length==0) {
			return false;
		}
		
		int definedCount = 0;
		
		for(int i=0; i<constraints.length; i++) {
			SearchConstraint constraint = constraints[i];
			if(constraint.isUndefined()) {
				continue;
			}
			
			if(leadingComma || definedCount>0) {
				buffer.append(COMMA).append(SPACE);
			}
			
			buffer.append(constraint.getToken());
			buffer.append(constraint.getOperator().getSymbol());
			
			Object value = constraint.getValue();
			String label;
			if(context!=null) {
				ConstraintFactory factory = context.getFactory(constraint.getToken());
				label = String.valueOf(factory.valueToLabel(value));
			} else {
				label = String.valueOf(value);
			}
			
			appendText(label);
			definedCount++;
		}
		
		return true;
	}
	
	protected boolean requiresQuote(String s) {
		if(s==null || s.isEmpty()) {
			return false;
		}
		
		for(int i=0; i<s.length(); i++) {
			char c = s.charAt(i);
			if(!isLegalId(c)) {
				return true;
			}
		}
		
		return false;
	}
	
	protected void appendText(String s) {		
		if(requiresQuote(s)) {
			char delimiter = s.indexOf(QUOTATIONMARK)!=-1 ? SINGLE_QUOTATIONMARK : QUOTATIONMARK;
			s = s.replace(String.valueOf(delimiter), "\\"+delimiter); //$NON-NLS-1$
			
			buffer.append(delimiter).append(s).append(delimiter);
		} else {
			buffer.append(s);
		}
	}
	
	protected boolean isLegalId(char c) {
		return c==UNDERSCORE || Character.isLetter(c) || Character.isDigit(c);
	}
	
	protected class NodeStack {
		private DefaultGraphNode virtualRoot = new DefaultGraphNode();
		
		private Stack<NodeStackFrame> stack = new Stack<>();
		
		private List<DefaultGraphNode> nodes = new ArrayList<>();
		private List<DefaultGraphEdge> edges = new ArrayList<>();
		
		private Map<DefaultGraphNode, NodeStackFrame> frameMap = new HashMap<>();
		
		// Lookup-table for all meta-data encountered so far
		private Map<DefaultGraphNode, Map<String, Object>> nodeProperties = new HashMap<>();
		
		public void pushConstraint(DefaultConstraint constraint) throws ParseException {
			if(constraint==null)
				throw new IllegalArgumentException("Invalid constraint"); //$NON-NLS-1$
			
			getFrame().addConstraint(constraint);
		}
		
		public void pushProperty(String key, Object value) throws ParseException {
			if(key==null)
				throw new IllegalArgumentException("Invalid key"); //$NON-NLS-1$
			if(value==null)
				throw new IllegalArgumentException("Invalid value"); //$NON-NLS-1$
			
			if(stack.isEmpty()) {
				// Root meta-data
				Map<String, Object> properties = nodeProperties.get(virtualRoot);
				if(properties==null) {
					properties = new HashMap<>();
					nodeProperties.put(virtualRoot, properties);
				}
				
				if(properties.containsKey(key))
					throw new ParseException(errorMessage(
							"Duplicate property: "+key), index); //$NON-NLS-1$
				
				properties.put(key, value);
			} else {
				// Node-level meta-data
				getFrame().setProperty(key, value);
			}
		}
		
		private NodeStackFrame getFrame() throws ParseException {
			if(stack.isEmpty())
				throw new ParseException(errorMessage(
						"No node-frame available on the parse stack"), index); //$NON-NLS-1$
			
			return stack.peek();
		}
		
		public DefaultGraphNode getCurrentNode() throws ParseException {
			return getFrame().getNode();
		}
		
		public DefaultGraphEdge getCurrentEdge() throws ParseException {
			return getFrame().getEdge();
		}
		
		public NodeStackFrame getFrame(DefaultGraphNode node) {
			return frameMap.get(node);
		}
		
		public List<DefaultGraphNode> getNodes() {
			return nodes;
		}
		
		public List<DefaultGraphEdge> getEdges() {
			return edges;
		}
		
		public DefaultGraphNode getVirtualRoot() {
			return virtualRoot;
		}
		
		public Map<String, Object> getProperties(DefaultGraphNode node) {
			return nodeProperties.get(node);
		}
		
		/**
		 * Defines the operator used on the root level
		 * <p>
		 * Possible values are
		 * <ul>
		 * <li>{@value SearchGraph#OPERATOR_DISJUNCTION}</li>
		 * <li>{@value SearchGraph#OPERATOR_CONJUNCTION}</li>
		 * </ul>
		 */
		public void setRootOperator(int operator) {
			virtualRoot.setNodeType(operator==SearchGraph.OPERATOR_DISJUNCTION ?
					NodeType.DISJUNCTION : NodeType.GENERAL);
		}
		
		public int getRootOperator() {
			return virtualRoot.getNodeType()==NodeType.DISJUNCTION ?
					SearchGraph.OPERATOR_DISJUNCTION : SearchGraph.OPERATOR_CONJUNCTION;
		}
		
		public SearchNode closeNode() throws ParseException {
			if(stack.isEmpty()) {
				return null;
			}
			
			NodeStackFrame frame = stack.pop();
			frame.close();
			
			Map<String, Object> properties = frame.getProperties();
			if(properties!=null) {
				nodeProperties.put(frame.getNode(), properties);
			}
			
			return frame.getNode();
		}
		
		public void openNode() throws ParseException {		
			NodeStackFrame frame;
			
			if(stack.isEmpty()) {
				// Only link the new node to the root, do NOT
				// apply a link backwards!
				frame = new NodeStackFrame();
				DefaultGraphEdge dummyEdge = new DefaultGraphEdge(
						virtualRoot, frame.getNode());
				virtualRoot.addEdge(dummyEdge, false);
			} else {
				// The new frame handles the linking to an existing
				// parent, so no need to worry about linking here
				DefaultGraphNode parent = getFrame().getNode();
				frame = new NodeStackFrame(parent);
				edges.add(frame.getEdge());
			}
			
			stack.push(frame);
			nodes.add(frame.getNode());
			frameMap.put(frame.getNode(), frame);
		}
		
		public void close() throws ParseException {
			if(!stack.isEmpty())
				throw new ParseException(errorMessage(
						stack.size()+" unclosed node definitions"), index); //$NON-NLS-1$
		}
		
		public void reset() {
			virtualRoot = new DefaultGraphNode();
			stack.clear();
			nodeProperties.clear();
			nodes.clear();
			edges.clear();
		}
	}
	
	private class NodeStackFrame {
		List<DefaultConstraint> nodeConstraints;
		List<DefaultConstraint> edgeConstraints;
		DefaultGraphNode node;
		DefaultGraphEdge edge;
		
		CompactProperties properties;
		
		NodeStackFrame() {
			this(null);
		}
		
		NodeStackFrame(DefaultGraphNode parent) {
			node = new DefaultGraphNode();
			nodeConstraints = new LinkedList<>();
			
			if (parent!=null) {
				edge = new DefaultGraphEdge();
				edgeConstraints = new LinkedList<>();
				
				edge.setSource(parent);
				edge.setTarget(node);
				
				parent.addEdge(edge, false);
				node.addEdge(edge, true);
			}
		}

		void setProperty(String key, Object value) throws ParseException {
			if(properties==null) {
				properties = new CompactProperties();
			}
			
			if(properties.get(key)!=null)
				throw new ParseException(errorMessage(
						"Duplicate property: "+key), index); //$NON-NLS-1$
			
			properties.setProperty(key, value);
		}
		
		Map<String, Object> getProperties() {
			return properties==null ? null : properties.asMap();
		}
		
		DefaultGraphNode getNode() {
			return node;
		}
		
		DefaultGraphEdge getEdge() {
			return edge;
		}
		
		private boolean containsConstraint(List<? extends SearchConstraint> constraints, SearchConstraint constraint) {
			for(SearchConstraint target : constraints) {
				if(target.getToken().equals(constraint.getToken())) {
					return true;
				}
			}
			
			return false;
		}
		
		void addConstraint(DefaultConstraint constraint) throws ParseException {
			ConstraintFactory factory = getConstraintContext().getFactory(constraint.getToken());
			if(factory.getConstraintType()==ConstraintFactory.NODE_CONSTRAINT_TYPE) {
				if(nodeConstraints==null)
					throw new ParseException(errorMessage(
							"Unexpected node-constraint '"+constraint.getToken()+"'"), index); //$NON-NLS-1$ //$NON-NLS-2$
				
				if(containsConstraint(nodeConstraints, constraint))
					throw new ParseException(errorMessage(
							"Duplicate node-constraint '"+constraint.getToken()+"'"), index); //$NON-NLS-1$ //$NON-NLS-2$
				
				nodeConstraints.add(constraint);
			} else {
				if(edgeConstraints==null)
					throw new ParseException(errorMessage(
							"Unexpected edge-constraint '"+constraint.getToken()+"'"), index); //$NON-NLS-1$ //$NON-NLS-2$

				if(containsConstraint(nodeConstraints, constraint))
					throw new ParseException(errorMessage(
							"Duplicate edge-constraint '"+constraint.getToken()+"'"), index); //$NON-NLS-1$ //$NON-NLS-2$
					
				edgeConstraints.add(constraint);
			}
		}
		
		void close() throws ParseException {
			if(nodeConstraints!=null && !nodeConstraints.isEmpty()) {
				node.setConstraints(nodeConstraints.toArray(new SearchConstraint[0]));
			}
			
			if(edgeConstraints!=null && !edgeConstraints.isEmpty()) {
				edge.setConstraints(edgeConstraints.toArray(new SearchConstraint[0]));
			}
		}
	}
}
