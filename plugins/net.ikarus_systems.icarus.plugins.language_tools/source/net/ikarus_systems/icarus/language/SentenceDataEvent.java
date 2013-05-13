/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language;

import net.ikarus_systems.icarus.util.Exceptions;

/**
 * Abstract base class of all objects that represent
 * changes occurring within {@code MutableSentenceData} objects.
 * Only the source of the change and the bare nature of the change
 * itself are encoded in this class. It is up to every implementation
 * to adjust the amount of stored information to the {@code Grammar}
 * being used.
 * 
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public abstract class SentenceDataEvent {

	/** Signals that the entire sentence data has been changed */
	public static final int CHANGE_EVENT = 0;

	/** Signals that one or more objects and/or fields have been added */
	public static final int INSERT_EVENT = 1;

	/** Signals that one or more objects and/or fields have been removed */
	public static final int REMOVE_EVENT = 2;

	/** Signals that one or more objects and/or fields have been updated */
	public static final int UPDATE_EVENT = 3;

	/**
	 * The source of the changes represented by this event
	 */
	protected final MutableSentenceData source;

	/**
	 * The type of the change being made
	 */
	protected int type;

	/**
	 * Constructs a new event for the given {@code source}
	 * @param source the {@code MutableSentenceData} object the
	 * changes originate from
	 */
	protected SentenceDataEvent(MutableSentenceData source) {
		Exceptions.testNullArgument(source, "source"); //$NON-NLS-1$
		
		this.source = source;
	}
	
	protected void setType(int type) {
		if(type==CHANGE_EVENT || type==INSERT_EVENT
				|| type==REMOVE_EVENT || type==UPDATE_EVENT) {
			this.type = type;
		} else
			throw new IllegalArgumentException("Invalid type: "+type); //$NON-NLS-1$
	}

	/**
	 * Returns the source of this event
	 * @return
	 */
	public MutableSentenceData getSource() {
		return source;
	}

	/**
	 * Returns the type of this event
	 * Possible return values are:
	 * <ul>
	 * <li>{@code CHANGE_EVENT} {@value #CHANGE_EVENT}</li>
	 * <li>{@code INSERT_EVENT} {@value #INSERT_EVENT}</li>
	 * <li>{@code REMOVE_EVENT} {@value #REMOVE_EVENT}</li>
	 * <li>{@code UPDATE_EVENT} {@value #UPDATE_EVENT}</li>
	 * </ul>
	 * @return the type of this event
	 */
	public int getType() {
		return type;
	}
}
