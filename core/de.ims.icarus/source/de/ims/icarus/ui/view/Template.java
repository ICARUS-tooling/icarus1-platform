/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.ui.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.util.HtmlUtils;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.id.UnknownIdentifierException;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class Template {

	public static final String SKIP_WHITESPACE_OPTION = "skipWhitespace"; //$NON-NLS-1$
	public static final String TRIM_OPTION = "trim"; //$NON-NLS-1$
	
	private static final String EMPTY = ""; //$NON-NLS-1$
	
	private Map<String, Object> wildcards;
	private Map<String, SubTemplateCache> subTemplates;
	private List<Object> elements;

	private Template() {
	}
	
	private static final String wildcardCaptionBegin = "${"; //$NON-NLS-1$
	private static final String captionEnd = "}"; //$NON-NLS-1$
	private static final String subTemplateCaptionBegin = "§{"; //$NON-NLS-1$
	private static final String subTemplateBegin = "[["; //$NON-NLS-1$
	private static final String subTemplateEnd = "]]"; //$NON-NLS-1$
	
	private static final String patternString =
			"\\$\\{|\\}|\\§\\{|\\[\\[|\\]\\]"; //$NON-NLS-1$
	
	private static Pattern pattern;
	static {
		try {
			pattern = Pattern.compile(patternString);
		} catch(PatternSyntaxException e) {
			LoggerFactory.log(Template.class, Level.SEVERE, 
					"Unable to generate template 'Pattern' intance", e); //$NON-NLS-1$
		}
	}
	
	private static String processString(String text, Options options) {
		if(options==null || options.isEmpty()) {
			return text;
		}
		
		if(options.get(SKIP_WHITESPACE_OPTION, false)) {
			text = text.replaceAll("\\s", EMPTY); //$NON-NLS-1$
		} else if(options.get(TRIM_OPTION, false)) {
			text = text.trim();
		}
		
		return text;
	}
	
	private static boolean isNullTag(String tag, String...allowedTags) {
		return tag==null || isTag(tag, allowedTags);
	}
	
	private static boolean isTag(String tag, String...allowedTags) {
		if(tag==null) {
			return false;
		}
		for(String allowedTag : allowedTags) {
			if(tag.equals(allowedTag))
				return true;
		}
		return false;
	}
	
	public static Template compile(String source, Options options) throws MalformedTemplateException {
		if(source==null)
			throw new IllegalArgumentException("Invalid source string"); //$NON-NLS-1$
		
		if(pattern==null)
			throw new IllegalStateException("No pattern defined for template compilation"); //$NON-NLS-1$
		
		if(options==null) {
			options = Options.emptyOptions;
		}
		
		// For an empty string return an empty template
		if(source.isEmpty()) {
			return new Template();
		}
		
		Stack<Object> objectStack = new Stack<>();
		Stack<String> tagStack = new Stack<>();
		
		int offset = 0;
		int start, end;
		Template template = new Template();
		String tag, lastTag;
		boolean isSubContext = false;

		Matcher matcher = pattern.matcher(source);
		while(matcher.find()) {
			start = matcher.start();
			end = matcher.end();
			tag = matcher.group();
			lastTag = tagStack.isEmpty() ? null : tagStack.peek();
			
			//System.out.printf("start=%d end=%d group=%s\n", start, end, matcher.group()); //$NON-NLS-1$
			
			switch (tag) {
			case wildcardCaptionBegin:
				// Form check
				if(!isNullTag(lastTag, captionEnd, subTemplateBegin, subTemplateEnd))
					throw new MalformedTemplateException(String.format(
							"Unexpected begin of wildcard caption at position %d", start)); //$NON-NLS-1$
				// Add intermediate text
				if(start-offset > 0) {
					template.add(processString(source.substring(offset, start), options));
				}
				// Add tag to stack
				tagStack.push(matcher.group());
				break;

			case subTemplateCaptionBegin:
				// Form check
				if(!isNullTag(lastTag, captionEnd, subTemplateBegin, subTemplateEnd))
					throw new MalformedTemplateException(String.format(
							"Unexpected begin of sub-template caption at position %d", start)); //$NON-NLS-1$
				// Add intermediate text
				if(start-offset > 0) {
					template.add(processString(source.substring(offset, start), options));
				}
				// Add tag to stack
				tagStack.push(tag);
				break;

			case captionEnd:
				// Missing begin declaration for caption
				if(!isTag(lastTag, wildcardCaptionBegin, subTemplateCaptionBegin))
					throw new MalformedTemplateException(String.format(
							"Unexpected end of caption at position %d", start)); //$NON-NLS-1$
				if(start-offset > 0) {
					String id = source.substring(offset, start);
					if(wildcardCaptionBegin.equals(lastTag)) {
						// Just add the wildcard id
						template.add(new Wildcard(id));
					} else {
						// Create new cache
						SubTemplateCache cache = new SubTemplateCache(id);
						if(!isSubContext) {
							objectStack.add(template);
						}
						objectStack.add(cache);
						template.add(id, cache);
					}
					tagStack.pop();
				} else {
					// Empty caption
					throw new MalformedTemplateException(String.format(
							"Empty caption at position %d", start)); //$NON-NLS-1$
				}
				break;

			case subTemplateBegin:
				if(start-offset > 0) {
					throw new MalformedTemplateException(String.format(
							"No character data allowed before sub-template declaration at position %d", start)); //$NON-NLS-1$
				}
				if(objectStack.isEmpty() || !(objectStack.peek() instanceof SubTemplateCache)) {
					throw new MalformedTemplateException(String.format(
							"Unexpected begin of sub-template declaration at position %d", start)); //$NON-NLS-1$
				} else {
					SubTemplateCache cache = (SubTemplateCache) objectStack.peek();
					cache.setTemplate(new Template());
					template = cache.getTemplate();
				}
				tagStack.push(tag);
				break;

			case subTemplateEnd:
				// Missing begin declaration for sub-template
				if(!isTag(lastTag, subTemplateBegin))
					throw new MalformedTemplateException(String.format(
							"Unexpected sub-template end at position %d", start)); //$NON-NLS-1$// Add intermediate text
				if(start-offset > 0) {
					template.add(processString(source.substring(offset, start), options));
				}
				if(objectStack.isEmpty() || !(objectStack.peek() instanceof SubTemplateCache))
					throw new MalformedTemplateException(String.format(
							"No begin defined for end of sub-template declaration at position %d", start)); //$NON-NLS-1$// Add intermediate text
				objectStack.pop();
				if(objectStack.isEmpty() || !(objectStack.peek() instanceof Template))
					throw new MalformedTemplateException(String.format(
							"Error validating sub-template declaration at position %d", start)); //$NON-NLS-1$// Add intermediate text
				template = (Template) objectStack.pop();
				tagStack.pop();
				break;

			default:
				throw new IllegalStateException("Unexpected match: "+matcher.group()); //$NON-NLS-1$
			}
			
			offset = end;
		}
		
		if(offset<source.length()) {
			template.add(processString(source.substring(offset), options));
		}
		
		if(!objectStack.isEmpty())
			throw new MalformedTemplateException("Unclosed sub-template declaration in input"); //$NON-NLS-1$
		
		return template;
	}
	
	private void add(Object element) {
		if(element==null)
			throw new IllegalArgumentException("Invalid element"); //$NON-NLS-1$
		
		if(elements==null) {
			elements = new ArrayList<>();
		}
		
		elements.add(element);
	}
	
	private void add(String id, SubTemplateCache subTemplate) {
		if(id==null)
			throw new IllegalArgumentException("Invalid id"); //$NON-NLS-1$
		if(subTemplate==null)
			throw new IllegalArgumentException("Invalid sub-template"); //$NON-NLS-1$
		
		if(elements==null) {
			elements = new ArrayList<>();
		}
		elements.add(subTemplate);
		
		if(subTemplates==null) {
			subTemplates = new HashMap<>();
		}
		subTemplates.put(id, subTemplate);
	}
	
	public void clear() {
		if(wildcards!=null) {
			wildcards.clear();
		}
		if(subTemplates!=null) {
			for(SubTemplateCache cache : subTemplates.values()) {
				cache.clear();
			}
		}
	}
	
	public void setRawValue(String id, Object value) {
		if(elements==null) {
			return;
		}
		if(wildcards==null) {
			wildcards = new HashMap<>();
		}
		
		wildcards.put(id, value);
	}
	
	public void setValue(String id, Object value) {
		if(elements==null) {
			return;
		}
		if(wildcards==null) {
			wildcards = new HashMap<>();
		}
		
		// Escape string values!
		if(value instanceof String) {
			value = HtmlUtils.escapeHTML((String) value);
		}
		
		wildcards.put(id, value);
	}
	
	public Object getValue(String id) {
		if(wildcards==null) {
			return null;
		}
		return wildcards.get(id);
	}
	
	public SubTemplateCache getSubTemplate(String id) {
		if(id==null)
			throw new IllegalArgumentException("Invalid id"); //$NON-NLS-1$
		
		if(elements==null) {
			return null;
		}
		
		SubTemplateCache cache = null;
		if(subTemplates!=null) {
			cache = subTemplates.get(id);
		}
		
		if(cache==null)
			throw new UnknownIdentifierException("No such sub-template: "+id); //$NON-NLS-1$
		
		return cache;
	}
	
	public void appendText(StringBuilder sb) {
		if(elements==null) {
			return;
		}

		for(Object element : elements) {
			if(element instanceof String) {
				// Simple string -> append
				sb.append((String) element);
			} else if(element instanceof Wildcard) {
				// Wildcard -> append value if set or else use wildcard's id
				Wildcard wc = (Wildcard) element;
				Object value = wildcards==null ? null : wildcards.get(wc.id);
				if(value instanceof Template) {
					((Template)value).appendText(sb);
				} else if(value!=null) {
					sb.append(value);
				} else {
					sb.append(wildcardCaptionBegin).append(wc.id).append(captionEnd);
				}
			} else if(element instanceof SubTemplateCache) {
				SubTemplateCache cache = (SubTemplateCache)element;
				// Let cache append its content
				Object value = wildcards==null ? null : wildcards.get(cache.id);
				cache.appendText(sb, value);
			}
		}
	}
	
	public String getText() {
		if(elements==null) {
			return null;
		}
		StringBuilder sb = new StringBuilder(elements.size()*20);
		appendText(sb);
		
		return sb.toString();
	}
	
	public void setValues(Map<String, Object> data) {
		// TODO
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public static class SubTemplateCache {
		private Template template;
		private StringBuilder buffer;
		private final String id;
		
		private SubTemplateCache(String id) {
			this.id = id;
		}
		
		public void clear() {
			if(buffer!=null) {
				buffer.setLength(0);
			}
			if(template!=null) {
				template.clear();
			}
		}
		
		public void reset() {
			if(template!=null) {
				template.clear();
			}
		}
		
		private void setTemplate(Template template) {
			this.template = template;
			buffer = null;
		}
		
		public Template getTemplate() {
			return template;
		}
		
		public void setValue(String id, Object value) {
			if(template==null)
				throw new IllegalStateException("Cannot assign wildcard value without template being set"); //$NON-NLS-1$
			
			template.setValue(id, value);
		}
		
		public void setRawValue(String id, Object value) {
			if(template==null)
				throw new IllegalStateException("Cannot assign wildcard value without template being set"); //$NON-NLS-1$
			
			template.setRawValue(id, value);
		}

		public void commit() {
			if(template==null)
				throw new IllegalStateException("Cannot commit without template being set"); //$NON-NLS-1$
			
			if(buffer==null) {
				buffer = new StringBuilder(500);
			}
			buffer.append(template.getText());
			template.clear();
		}
		
		@Override
		public String toString() {
			return "sub-template: "+id; //$NON-NLS-1$
		}
		
		public String getText() {
			return buffer==null ? null : buffer.toString();
		}
		
		private void appendText(StringBuilder sb, Object alternateValue) {
			if(buffer!=null) {
				sb.append(buffer);
			} else if(alternateValue!=null) {
				sb.append(alternateValue);
			}
		}
	}

	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public static class Wildcard {
		private final String id;
		
		private Wildcard(String id) {
			this.id = id;
		}
		
		public String getId() {
			return id;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof Wildcard) {
				return id.equals(((Wildcard)obj).id);
			}
			return false;
		}
		
		@Override
		public String toString() {
			return "wildcard: "+id; //$NON-NLS-1$
		}
	}
}
