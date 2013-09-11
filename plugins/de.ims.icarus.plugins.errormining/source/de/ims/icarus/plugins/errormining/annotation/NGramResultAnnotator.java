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
package de.ims.icarus.plugins.errormining.annotation;

import de.ims.icarus.language.dependency.DependencyData;
import de.ims.icarus.language.dependency.DependencyUtils;
import de.ims.icarus.language.dependency.annotation.AnnotatedDependencyData;
import de.ims.icarus.language.dependency.annotation.DependencyAnnotation;
import de.ims.icarus.search_tools.annotation.BitmaskHighlighting;
import de.ims.icarus.search_tools.result.ResultEntry;
import de.ims.icarus.search_tools.tree.AbstractTreeResultAnnotator;
import de.ims.icarus.search_tools.tree.Matcher;
import de.ims.icarus.util.annotation.AnnotatedData;
import de.ims.icarus.util.annotation.Annotation;
import de.ims.icarus.util.data.ContentType;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class NGramResultAnnotator extends AbstractTreeResultAnnotator {
	
	public NGramResultAnnotator(BitmaskHighlighting highlighting,
			Matcher rootMatcher) {
		super(highlighting, rootMatcher);
	}

	public NGramResultAnnotator(Matcher rootMatcher) {
		this(NGramHighlighting.getInstance(), rootMatcher);
	}
	

	/**
	 * @see de.ims.icarus.search_tools.annotation.ResultAnnotator#getAnnotationType()
	 */
	@Override
	public ContentType getAnnotationType() {
		// TODO passt?
		return DependencyUtils.getDependencyAnnotationType();
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.AbstractTreeResultAnnotator#getHead(java.lang.Object, int)
	 */
	@Override
	protected int getHead(Object data, int index) {
		// TODO passt?
		return ((DependencyData)data).getHead(index);
	}

	/**
	 * @see de.ims.icarus.search_tools.annotation.AbstractLazyResultAnnotator#supports(java.lang.Object)
	 */
	@Override
	protected boolean supports(Object data) {
		// TODO Auto-generated method stub
		return data instanceof DependencyData;
	}

	/**
	 * @see de.ims.icarus.search_tools.annotation.AbstractLazyResultAnnotator#createAnnotatedData(java.lang.Object, de.ims.icarus.search_tools.result.ResultEntry)
	 */
	@Override
	protected AnnotatedData createAnnotatedData(Object data, ResultEntry entry) {
		return new LazyAnnotatedNGramData((DependencyData) data, entry);
	}
	
	@Override
	protected Annotation createAnnotation(Object data, ResultEntry entry) {
		return new LazyNGramAnnotation(data, entry);
	}
	
	
	protected class LazyAnnotatedNGramData extends AnnotatedDependencyData {

		private static final long serialVersionUID = 2141988696554570730L;
		private final ResultEntry entry;

		public LazyAnnotatedNGramData(DependencyData source, ResultEntry entry) {
			super(source);
			
			this.entry = entry;
		}

		@Override
		public Annotation getAnnotation() {
			Annotation annotation = super.getAnnotation();
			
			if(annotation==null) {
				annotation = createAnnotation(this, entry);
				setAnnotation(annotation);
			}
			
			return annotation;
		}
	}
	
	
	protected class LazyNGramAnnotation extends LazyAnnotation implements DependencyAnnotation {

		public LazyNGramAnnotation(Object data, ResultEntry entry) {
			super(data, entry);
		}
	
	}

}
