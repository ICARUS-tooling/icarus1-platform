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

import java.util.ArrayList;
import java.util.List;

import de.ims.icarus.language.dependency.DependencySentenceData;
import de.ims.icarus.language.dependency.annotation.AnnotatedDependencyData;
import de.ims.icarus.language.dependency.annotation.DependencyAnnotation;
import de.ims.icarus.language.dependency.annotation.DependencyHighlighting;
import de.ims.icarus.plugins.errormining.NGramQAttributes;
import de.ims.icarus.search_tools.annotation.AbstractLazyResultAnnotator;
import de.ims.icarus.search_tools.annotation.BitmaskHighlighting;
import de.ims.icarus.search_tools.result.Hit;
import de.ims.icarus.search_tools.result.ResultEntry;
import de.ims.icarus.util.annotation.AnnotatedData;
import de.ims.icarus.util.annotation.Annotation;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class NGramResultAnnotator extends AbstractLazyResultAnnotator {

	private static long nucleiHighlight = DependencyHighlighting.getInstance()
												.getHighlight("form"); //$NON-NLS-1$
	private static long ngramHighlight = BitmaskHighlighting.NODE_HIGHLIGHT;
	List<NGramQAttributes> nqList;

	public NGramResultAnnotator(BitmaskHighlighting highlighting, List<NGramQAttributes> nqList) {
		super(highlighting);
		this.nqList = nqList;
	}


//	/**
//	 * @param matcher
//	 */
//	public NGramResultAnnotator(NGramResultMatcher matcher) {
//		this(NGramHighlighting.getInstance(), matcher);
//	}


	/**
	 * @see de.ims.icarus.search_tools.annotation.ResultAnnotator#getAnnotationType()
	 */
	@Override
	public ContentType getAnnotationType() {
		//return DependencyUtils.getDependencyAnnotationType();
		return ContentTypeRegistry.getInstance().getTypeForClass(NGramAnnotation.class);
	}

	/**
	 * @see de.ims.icarus.search_tools.annotation.AbstractLazyResultAnnotator#getHighlightCount()
	 */
	@Override
	public int getHighlightCount() {
		// nuclei or ngram
		return 2;
	}

	/**
	 * @see de.ims.icarus.search_tools.annotation.AbstractLazyResultAnnotator#createBaseHighlight(int)
	 */
	@Override
	protected long createBaseHighlight(int index) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see de.ims.icarus.search_tools.annotation.AbstractLazyResultAnnotator#supports(java.lang.Object)
	 */
	@Override
	protected boolean supports(Object data) {
		//System.out.println(data);
		return ((DependencySentenceData) data instanceof DependencySentenceData);
	}

	/**
	 * @see de.ims.icarus.search_tools.annotation.AbstractLazyResultAnnotator#createAnnotatedData(java.lang.Object, de.ims.icarus.search_tools.result.ResultEntry)
	 */
	@Override
	protected AnnotatedData createAnnotatedData(Object data, ResultEntry entry) {
		return new LazyAnnotatedNGramData((DependencySentenceData) data, entry);
	}

	@Override
	protected Annotation createAnnotation(Object data, ResultEntry entry) {
		return new LazyNGramAnnotation(data, entry);
	}


	protected String getTagQuery(String qtag){
		String tag = qtag;
		for(int i = 0; i < nqList.size(); i++){
			NGramQAttributes att = nqList.get(i);
			//System.out.println(tag + " vs " + att.getKey());

			//is there a key in querylist?
			if (att.getKey().equals(tag)){
				//System.out.print("Hit " + tag + " vs " + att.getKey());

				//shall we use key?
				if(att.isInclude()){
					if(att.getValue().equals("")){ //$NON-NLS-1$
						//reuse old tag
						//System.out.println(" Included Oldtag " + tag);
						return tag;
					} else {
						//use newspecified tag
						//System.out.println(" Included Newtag " + att.getValue());
						return att.getValue();
					}
				} else {
					//ignore tag for search
					//System.out.println(" excluded ");
					return null;
				}
			}
		}
		return tag;
	}



	/**
	 * @see de.ims.icarus.search_tools.annotation.AbstractLazyResultAnnotator#createHighlight(java.lang.Object, de.ims.icarus.search_tools.result.Hit)
	 */
	@Override
	protected Highlight createHighlight(Object data, Hit hit) {

		// Flexible buffer structures to allow for addition of
		// needed highlight data during construction process
		List<Integer> indexMap = new ArrayList<>(hit.getIndexCount());
		List<Long> highlights = new ArrayList<>(hit.getIndexCount());

		int[] hitArray = hit.getIndices();


		//Debug print hitarray, inside the array all informations need for highlight
//		System.out.println("HitArray " + hitArray[0] + " " //$NON-NLS-1$ //$NON-NLS-2$
//							+ hitArray[1] + " " //$NON-NLS-1$
//							+ hitArray[2]);


		int start = hitArray[0];
		int end = hitArray[1];
		int nuclei = hitArray[2];


		// used when highlight dependency result
		int sourceNode = 0;
		//dependency hit consists of target(nucleus) and sourceNode information
		if (hit.getIndexCount() == 4){
			sourceNode = hitArray[3];
		}

		//only nuclei
		if(end == start){
			indexMap.add(nuclei-1);
			highlights.add(nucleiHighlight);
		} else {
			for(int i = start-1; i < end; i++){
				long highlight = 0L;
				if (i == nuclei-1 || i == sourceNode-1){
					//nuclei
					highlight = nucleiHighlight;
				} else {
					highlight = ngramHighlight;
				}

				indexMap.add(i);
				highlights.add(highlight);
			}
		}

		// Create final buffer structures
		int size = indexMap.size();
		int[]_indexMap = new int[size];
		long[] _highlights = new long[size];
		for(int i=0; i<size; i++) {
			_indexMap[i] = indexMap.get(i);
			_highlights[i] = highlights.get(i);
		}

		return new DefaultHighlight(_indexMap, _highlights);
	}


//
//	/**
//	 *
//	 * @param data
//	 * @param hit
//	 * @return
//	 */
//	protected Highlight createHighlights(Object data, Hit[] hit) {
//
//		int[] hitArray = hit[0].getIndices();
//
//		System.out.println(hitArray[0] + " " //$NON-NLS-1$
//				+ hitArray[1] + " " //$NON-NLS-1$
//				+ hitArray[2]);
//
//		int start = hitArray[0];
//		int end = hitArray[1];
//		int nuclei = hitArray[2];
//
//		int[] iMap = new int[end-start+1];
//		long[] longHighligth = new long[end-start+1];
//
//		for(int j = 0; j < hit.length; j++){
//			hitArray = hit[j].getIndices();
//
//			System.out.println(hitArray[0] + " " //$NON-NLS-1$
//					+ hitArray[1] + " " //$NON-NLS-1$
//					+ hitArray[2]);
//
//			start = hitArray[0];
//			end = hitArray[1];
//			nuclei = hitArray[2];
//
//			for(int i = 0; i < end-start+1; i++){
//				if (iMap[i] == 0){
//					if (i == nuclei-1){
//						//nuclei
//						iMap[i] = i;
//						longHighligth[i] = 2L;
//					}
//				}
//			}
//		}
//
//		Highlight highlight = new Highlight(iMap, longHighligth);
//		for(int index : iMap){
//			System.out.print(iMap[index] + " "); //$NON-NLS-1$
//		}
//		System.out.println();
//		return highlight;
//	}


	//	/**
	//	 * @param matcher
	//	 */
	//	public NGramResultAnnotator(NGramResultMatcher matcher) {
	//		this(NGramHighlighting.getInstance(), matcher);
	//	}


	protected class LazyAnnotatedNGramData extends AnnotatedDependencyData {

		private static final long serialVersionUID = 2141988696554570730L;
		private final ResultEntry entry;

		public LazyAnnotatedNGramData(DependencySentenceData source, ResultEntry entry) {
			super(source);
			this.entry = entry;
		}

		@Override
		public Annotation getAnnotation() {
			Annotation annotation = super.getAnnotation();

			if (annotation == null) {
				annotation = createAnnotation(this, entry);
				setAnnotation(annotation);
			}

			return annotation;
		}
	}


	protected class LazyNGramAnnotation extends LazyAnnotation implements DependencyAnnotation {

		public LazyNGramAnnotation(Object data, ResultEntry entry) {
			super(data, entry);

			//System.out.println("AnnoCount: " +LazyNGramAnnotation.this.getAnnotationCount());

		}


	}


}
