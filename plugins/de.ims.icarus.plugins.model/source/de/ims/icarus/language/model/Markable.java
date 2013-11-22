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
package de.ims.icarus.language.model;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Markable extends CorpusMember, Comparable<Markable> {

	/**
	 * Returns the text fragment of the underlying corpus this 
	 * {@code Markable} covers. Note that this does not necessarily 
	 * have to be continuous text. For example an {@link Edge} would
	 * concatenate the text portions covered by its source and target
	 * {@code Markable}s respectively (typically separated by some sort
	 * of delimiter).
	 * <p>
	 * Note that when dealing with {@code Container}s it is advised 
	 * to cache results of the {@link #getText()} calls since they might 
	 * be expensive to create depending on the 'size' of the container.  
	 * 
	 * @return The portion of text this markable covers in the underlying corpus.
	 */
	String getText();
	
	/**
	 * If this markable is hosted within a container, returns that enclosing 
	 * container. Otherwise it represents a top-level markable and returns 
	 * {@code null}.
	 * <p>
	 * Note that this method returns the container that <b>owns</b> this markable
	 * and not necessarily the one through which it was obtained! It is perfectly
	 * legal for a container to reuse the elements of another container and to
	 * augment the collection with its own intermediate markables. For this
	 * reason it is advised to keep track of the container the markable was 
	 * fetched from when this method is called.
	 * 
	 * @return The enclosing container of this markable or {@code null} if this
	 * markable is not hosted within a container.
	 */
	Container getContainer();
	
	/**
	 * Returns the {@code MarkableLayer} this markable is hosted in. For nested
	 * markables this call should simply forward to the {@code Container} obtained
	 * via {@link #getContainer()} since storing a reference to the layer in each
	 * markable in addition to the respective container is expensive. Top-level
	 * markables should always store a direct reference to the enclosing layer.
	 * 
	 * @return The enclosing {@code MarkableLayer} that hosts this markable object.
	 */
	MarkableLayer getLayer();
	
	/**
	 * Returns the zero-based offset of this markable's begin within the corpus.
	 * The first {@code Markable} in the {@link MarkableLayer} obtained via
	 * {@link Corpus#getBaseLayer()} is defined to have offset {@code 0}. All other
	 * offsets are calculated relative to this. If this {@code Markable} is a 
	 * {@link Container} or {@link Structure} then the returned offset is the
	 * result of calling {@link Markable#getBeginOffset()} on the left-most markable
	 * hosted within this object.
	 * 
	 * @return The zero-based offset of this markable's begin within the corpus
	 */
	int getBeginOffset();

	/**
	 * Returns the zero-based offset of this markable's end within the corpus.
	 * The first {@code Markable} in the {@link MarkableLayer} obtained via
	 * {@link Corpus#getBaseLayer()} is defined to have offset {@code 0}. All other
	 * offsets are calculated relative to this. If this {@code Markable} is a 
	 * {@link Container} or {@link Structure} then the returned offset is the
	 * result of calling {@link Markable#getEndOffset()} on the right-most markable
	 * hosted within this object.
	 * 
	 * @return The zero-based offset of this markable's end within the corpus
	 */
	int getEndOffset();
}
