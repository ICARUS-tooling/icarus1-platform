/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.ui.view;

import net.ikarus_systems.icarus.util.Options;

/**
 * A very abstract specification of an object that is capable of
 * visually {@code presenting} other objects. The term {@code present}
 * is used in a rather general manner. This interface only describes
 * the absolute basic methods required. It is up to extending interfaces
 * or implementing classes to provide functionality to actually access
 * the rendering components like {@code Component} or {@code Graphics}
 * instances. 
 * 
 * @author Markus Gärtner
 * @version $Id$
 * @see AWTPresenter
 *
 */
public interface Presenter {

	/**
	 * Checks whether an implementation is capable of presenting
	 * a certain {@code data} object.
	 */
	boolean supports(Object data);
	
	/**
	 * {@code Presents} the given {@code data} object using the
	 * {@code options} parameter. As a general rule {@code data}
	 * should never be {@code null}. If a program wants to erase
	 * the internal state of a {@code Presenter} it should call
	 * {@link #clear()} instead!
	 * <p>
	 * If this method returns without errors all subsequent calls
	 * to {@link #isPresenting()} must return {@code true} until
	 * {@link #clear()} is performed.
	 * @throws UnsupportedPresentationDataException
	 */
	void present(Object data, Options options) throws UnsupportedPresentationDataException;
	
	/**
	 * Erases all previously set presentation data so that later calls
	 * to {@link #isPresenting()} return {@code false} until new data
	 * is being set.
	 */
	void clear();
	
	/**
	 * Releases all underlying resources.
	 */
	void close();
	
	/**
	 * Returns {@code true} if valid data has been set for presentation
	 * and no call to {@link #clear()} has been performed since then.
	 */
	boolean isPresenting();
	
	/**
	 * Returns the currently presented data or {@code null} if no
	 * data is being presented right now. In the later case a
	 * previous call to {@link #isPresenting()} should have returned
	 * {@code false}.
	 */
	Object getPresentedData();
}
