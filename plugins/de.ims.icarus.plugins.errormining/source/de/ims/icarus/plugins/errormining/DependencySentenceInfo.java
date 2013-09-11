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
 *
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package de.ims.icarus.plugins.errormining;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class DependencySentenceInfo extends SentenceInfo {
	
	protected int sentenceHeadBegin;
	protected int sentenceHeadEnd;
	protected int sentenceHeadIndex;
	
	public DependencySentenceInfo(){
		//noop
	}

	/**
	 * @return the sentenceHeadBegin
	 */
	public int getSentenceHeadBegin() {
		return sentenceHeadBegin;
	}

	/**
	 * @return the sentenceHeadEnd
	 */
	public int getSentenceHeadEnd() {
		return sentenceHeadEnd;
	}

	/**
	 * @param sentenceHeadEnd the sentenceHeadEnd to set
	 */
	public void setSentenceHeadEnd(int sentenceHeadEnd) {
		this.sentenceHeadEnd = sentenceHeadEnd;
	}

	/**
	 * @param sentenceHeadBegin the sentenceHeadBegin to set
	 */
	public void setSentenceHeadBegin(int sentenceHeadBegin) {
		this.sentenceHeadBegin = sentenceHeadBegin;
	}

	/**
	 * @return the sentenceHeadIndex
	 */
	public int getSentenceHeadIndex() {
		return sentenceHeadIndex;
	}

	/**
	 * @param sentenceHeadIndex the sentenceHeadIndex to set
	 */
	public void setSentenceHeadIndex(int sentenceHeadIndex) {
		this.sentenceHeadIndex = sentenceHeadIndex;
	}

}
