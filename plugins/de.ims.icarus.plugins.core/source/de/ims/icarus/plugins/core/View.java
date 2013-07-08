/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.core;

import java.awt.Frame;

import javax.swing.JComponent;

import org.java.plugin.registry.Extension;

import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.events.EventListener;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.ui.events.EventSource;
import de.ims.icarus.util.Capability;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.id.Identifiable;
import de.ims.icarus.util.id.Identity;
import de.ims.icarus.util.mpi.Message;
import de.ims.icarus.util.mpi.ResultMessage;

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public abstract class View implements Identifiable {
	
	private Perspective perspective;
	
	private Extension extension;
	
	/**
	 * For security reasons we do not extend {@code EventSource} but rather
	 * store an internal instance and forward methods we want to expose.
	 * Since {@code EventSource} does not offer access control to methods 
	 * like {@link EventSource#setEventsEnabled(boolean)} this is necessary
	 * to prevent external sources from disabling event handling for us.
	 * Implementations might reconsider about that and expose more 
	 * functionality of the {@code EventSource} via new forwarding methods.
	 */
	protected final EventSource eventSource = new EventSource(this);

	/**
	 * 
	 */
	protected View() {
		// no-op
	}
	
	public final Extension getExtension() {
		return extension;
	}
	
	final void setExtension(Extension extension) {
		this.extension = extension;
	}

	/**
	 * @see de.ims.icarus.util.id.Identifiable#getIdentity()
	 */
	@Override
	public Identity getIdentity() {
		return PluginUtil.getIdentity(getExtension());
	}

	/**
	 * Registers the given {@code listener} for events of the
	 * specified {@code eventName} or as a listener for all
	 * events in the case the {@code eventName} parameter is {@code null}
	 * @param eventName name of events to listen for or {@code null} if
	 * the listener is meant to receive all fired events
	 * @param listener the {@code EventListener} to be registered
	 */
	public void addListener(String eventName, EventListener listener) {
		eventSource.addListener(eventName, listener);
	}

	/**
	 * Removes the given {@code EventListener} from all events
	 * it was previously registered for.
	 * @param listener the {@code EventListener} to be removed
	 */
	public void removeListener(EventListener listener) {
		eventSource.removeListener(listener);
	}

	/**
	 * Removes from the list of registered listeners all pairs
	 * matching the given combination of {@code EventListener}
	 * and {@code eventName}. If {@code eventName} is {@code null}
	 * then all occurrences of the given {@code listener} will be
	 * removed.
	 * @param listener
	 * @param eventName
	 */
	public void removeListener(EventListener listener, String eventName) {
		eventSource.removeListener(listener, eventName);
	}

	/**
	 * Tells the {@code view} to layout its visible parts. From the moment
	 * of this call until {@code #close()} is called this {@code View} is allowed
	 * to take complete ownership if the provided {@code container}. Due to the
	 * nature of Swing's component hierarchy it is possible for a {@code View} object
	 * to access components outside its {@code root} container but this is not 
	 * recommended! The basic job to be done within this method is to add components
	 * to the {@code container} and to resize it if needed.<p>
	 * This method will always be called on the {@code EventDispatchThread}!
	 * @param container
	 */
	public abstract void init(JComponent container);
	
	/**
	 * Tells this {@code View} to release all its resources and
	 * return ownership of the {@code JComponent} it was provided with
	 * on the initial call to {@code #init(JComponent)}. After this
	 * method is called the {@code View} object is considered unusable
	 * and will no longer be accessible from its previous {@code Perspective}
	 * context! If an implementation encounters an error that renders it
	 * unable to release some of its resources (e.g. threads still running
	 * in the background) then it should throw a {@link CorruptedStateException}
	 * so the enclosing {@code Perspective} can handle this by presenting
	 * the user some kind of feedback that suggests him to exit the program
	 * or that at least informs him about the unusual state and the possible
	 * problems that may result from it. The default implementation only
	 * clears the container component of all its child components to fasten
	 * the release of ui-resources.
	 * @throws CorruptedStateException if the {@code View} was unable to release
	 * all of its managed resources and therefore suggests immediate exit of
	 * the program
	 */
	public void close() {
		JComponent container = getContainer();
		if(container!=null) {
			container.removeAll();
		}
	}
	
	/**
	 * Checks whether this {@code View} can be closed. This method exists 
	 * so that implementations that have ongoing background operations
	 * still running can ask the user to cancel those. Note that in some
	 * situations the return value from this method being {@code true} does
	 * {@code not} prevent the enclosing {@code Perspective} to close this
	 * {@code View} by calling {@code #close()}!
	 * <p>
	 * The default implementation returns {@code true}.
	 * @return {@code true} if this {@code View} is ready to be closed
	 */
	public boolean isClosable() {
		return true;
	}
	
	public void reset() {
		// no-op
	}
	
	@Override
	public String toString() {
		return getExtension().getId();
	}
	
	/**
	 * Returns the enclosing {@code Perspective} that manages this {@code View}
	 * instance. Note that it is not recommended to call this method before
	 * a {@code View} got initialized by the {@link #init(JComponent)} method!
	 * @return the enclosing {@code Perspective}
	 * @throws IllegalStateException if this {@code View} is currently not
	 * a visible member of any {@code Perspective}
	 */
	public final Perspective getPerspective() {
		if(perspective==null)
			throw new IllegalStateException("No enclosing view available"); //$NON-NLS-1$
		
		return perspective;
	}
	
	/**
	 * Returns the enclosing {@link Frame}.
	 */
	protected final Frame getFrame() {
		return getPerspective().getFrame();
	}
	
	/**
	 * Returns the {@code InfoPanel} available for this {@code View}.
	 * This request is forwarded to the enclosing {@code Perspective}
	 * which will check whether or not this {@code View} is the currently
	 * active view, i.e. has the input focus. If this {@code View} is not
	 * the active one {@code null} is returned.
	 */
	protected InfoPanel getInfoPanel() {
		return getPerspective().getInfoPanel(this);
	}
	
	/**
	 * Called when the {@code View} gets the input focus.
	 * Hook for subclasses to initialize displayed fields on the info panel.
	 * The default implementation just calls {@link InfoPanel#clear()} on
	 * the supplied argument.
	 */
	protected void refreshInfoPanel(InfoPanel infoPanel) {
		infoPanel.clear();
	}
	
	/**
	 * Entry point for subclasses to add tool-bar elements to the
	 * frame-wide tool-bar. The default implementation does nothing.
	 */
	protected void buildToolBar(ToolBarDelegate delegate) {
		// no-op
	}

	/**
	 * Called from within a {@code Perspective} when a {@code View}
	 * gets attached to it or is being removed. In the attempt to set
	 * a new {@code Perspective} on a {@code View} that is already owned
	 * by another one an {@link IllegalArgumentException} will be thrown.
	 * @param perspective the perspective to set (may be {@code null})
	 * @throws IllegalArgumentException if this {@code View} is already
	 * attached to a {@code Perspective} and the given {@code perspecitve}
	 * argument is not {@code null}
	 */
	final void addNotify(Perspective perspective) {
		if(this.perspective!=null && perspective!=null)
			throw new IllegalArgumentException("View already owned by "+this.perspective); //$NON-NLS-1$
		
		this.perspective = perspective;
	}

	protected ResultMessage handleRequest(Message message) throws Exception {
		return message.unknownRequestResult(this);
	}
	
	/**
	 * Forwards the given {@code data} to this {@code View}'s {@code Perspective}
	 * to be dispatched. The {@code receiver} argument serves as a filter for the
	 * {@code Perspective} to find suitable targets. It may either be a {@code String}
	 * defining the exact {@code unique-id} of the desired {@code View}, a {@code Class} 
	 * object describing a super-type for targets, a {@link Capability} describing
	 * required capabilities or {@code null} if the {@code data} should be dispatched as a broadcast.
	 * <p>
	 * <b>Note:</b> As long as data is being dispatched by {@link Perspective#dispatchData(View, String, Object, Object, Options)}
	 * this method will always be called on the {@code EventDispatchThread}
	 * @param receiver target filter, may be a {@code ViewFilter} a {@code String}, a {@code Class} or {@code null}
	 * @param message the request to be dispatched
	 */
	protected final ResultMessage sendRequest(Object receiver, Message message) {
		return getPerspective().sendRequest(this, receiver, message);
	}
	
	protected final ResultMessage sendRequest(Object perspective, Object receiver, Message message) {
		return getPerspective().sendRequest(this, perspective, receiver, message);
	}
	
	/**
	 * Tells the enclosing {@code Perspective} to set the {@code tab}
	 * that holds this {@code View}'s {@code container} object as the
	 * selected component in the corresponding {@code JTabbedPane}.
	 */
	protected final void selectViewTab() {
		getPerspective().selectViewTab(this);
	}
	
	/**
	 * Forces the enclosing {@code Perspective} to update the following
	 * properties of the {@code JTabbedPane}'s {@code tab} that holds this
	 * view's {@code container} by using the view's {@code Identity}:
	 * <p>
	 * <ul>
	 * <li>icon - replaced by result of {@link Identity#getIcon()} if not {@code null}</li>
	 * <li>tooltipText - replaced by {@link Identity#getDescription()}</li>
	 * <li>title - replaced by {@link Identity#getName()}</li>
	 * </ul>
	 * This enables the {@code View} to change its {@code tab}'s appearance 
	 * at any time.
	 */
	protected final void reloadViewTab() {
		getPerspective().reloadViewTab(this);
	}
	
	/**
	 * Toggles the <i>state</i> of this {@code View}'s container which can
	 * be either {@code maximized} or the default state. Usually this method
	 * is only called by the enclosing {@code Perspective} when the user clicks
	 * the corresponding control buttons or components.
	 */
	protected final void toggleContainer() {
		getPerspective().toggleView(this);
	}
	
	/**
	 * Returns the {@code JComponent} that was provided by the
	 * enclosing {@code Perspective} for this {@code View} to lay out
	 * its user interface.
	 */
	protected final JComponent getContainer() {
		return getPerspective().getViewContainer(this);
	}
	
	protected final void addBroadcastListener(String eventName, EventListener listener) {
		getPerspective().addBroadcastListener(eventName, listener);
	}
	
	protected final void removeBroadcastListener(EventListener listener) {
		getPerspective().removeBroadcastListener(listener);
	}
	
	protected final void removeBroadcastListener(EventListener listener, String eventName) {
		getPerspective().removeBroadcastListener(listener, eventName);
	}
	
	protected final void fireBroadcastEvent(EventObject event) {
		getPerspective().fireBroadcastEvent(this, event);
	}
	
	/**
	 * Returns the default {@code ActionManager} that is available to this
	 * {@code View}. Per convention all views within the same {@code Perspective}
	 * share the manager of that enclosing {@code Perspective}.
	 */
	protected final ActionManager getDefaultActionManager() {
		return getPerspective().getActionManager();
	}
	
	/**
	 * Requests this {@code View} to get the focus. The default
	 * implementation delegates this call to {@link JComponent#requestFocusInWindow()}
	 * on the {@code component} obtained through {@link #getContainer()}.
	 * Subclasses might override this method to forward focus to some
	 * components within their personal root container.
	 */
	public void focusView() {
		getContainer().requestFocusInWindow();
	}
	
	public interface ViewEvents {

		public static final String CLOSING = "closing"; //$NON-NLS-1$
		public static final String CLOSED = "closed"; //$NON-NLS-1$

	}
}
