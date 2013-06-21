/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util.annotation;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.ui.NavigationControl;
import net.ikarus_systems.icarus.ui.NavigationControl.ArrowStyle;
import net.ikarus_systems.icarus.ui.NavigationControl.ElementType;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.actions.ActionManager;
import net.ikarus_systems.icarus.util.CorruptedStateException;
import net.ikarus_systems.icarus.util.PropertyChangeSource;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class AnnotationControl extends PropertyChangeSource implements PropertyChangeListener {

	protected AnnotationManager annotationManager;
	
	protected ActionManager actionManager;
	
	private static ActionManager sharedActionManager;
	
	protected ArrowStyle arrowStyle = NavigationControl.MINI_ARROW_STYLE;

	protected static final String selectModeActionId = "core.helpers.annotationControl.selectModeAction"; //$NON-NLS-1$
	protected static final String firstActionId = "core.helpers.annotationControl.firstElementAction"; //$NON-NLS-1$
	protected static final String previousActionId = "core.helpers.annotationControl.previousElementAction"; //$NON-NLS-1$
	protected static final String nextActionId = "core.helpers.annotationControl.nextElementAction"; //$NON-NLS-1$
	protected static final String lastActionId = "core.helpers.annotationControl.lastElementAction"; //$NON-NLS-1$
	
	protected JLabel navigationLabel;
	protected JButton[] navigationButtons;
	protected JButton menuButton;
	
	protected JPopupMenu displayModeMenu;
	
	protected final boolean allowSelection;

	public AnnotationControl(boolean allowSelection) {
		this.allowSelection = allowSelection;
		
		registerActionCallbacks();		
		
		buildComponents();
		setEnabled(false);
	}

	public AnnotationControl() {
		this(true);
	}
	
	protected JPopupMenu createPopupMenu() {
		String menuId = allowSelection ?
				"core.helpers.annotationControl.modePopupMenuList" //$NON-NLS-1$
				: "core.helpers.annotationControl.limitedModePopupMenuList"; //$NON-NLS-1$
		return getActionManager().createPopupMenu(menuId, null);
	}
	
	protected void buildComponents() {
		ActionManager actionManager = getActionManager();
		
		menuButton = new JButton(actionManager.getAction(
				"core.helpers.annotationControl.selectModeAction")); //$NON-NLS-1$
		menuButton.setFocusable(false);
		menuButton.setFocusPainted(false);
		
		if(allowSelection) {
			navigationButtons = new JButton[4];
			navigationButtons[0] = new JButton(actionManager.getAction(
					"core.helpers.annotationControl.firstElementAction")); //$NON-NLS-1$
			navigationButtons[1] = new JButton(actionManager.getAction(
					"core.helpers.annotationControl.previousElementAction")); //$NON-NLS-1$
			navigationButtons[2] = new JButton(actionManager.getAction(
					"core.helpers.annotationControl.nextElementAction")); //$NON-NLS-1$
			navigationButtons[3] = new JButton(actionManager.getAction(
					"core.helpers.annotationControl.lastElementAction")); //$NON-NLS-1$
		}
		
		navigationLabel = new JLabel();
		navigationLabel.setHorizontalAlignment(SwingConstants.CENTER);
		UIUtil.resizeComponent(navigationLabel, 30, 40, 50, 21, 21, 26);
	}

	public ArrowStyle getArrowStyle() {
		return arrowStyle;
	}

	public void setArrowStyle(ArrowStyle arrowStyle) {
		if(arrowStyle==null)
			throw new IllegalArgumentException("Invalid arrow style"); //$NON-NLS-1$
		
		if(this.arrowStyle==arrowStyle) {
			return;
		}
		
		this.arrowStyle = arrowStyle;
		
		ActionManager actionManager = getActionManager();
		
		// Set action icons according to arrow style value
		actionManager.getAction(firstActionId).putValue(
				Action.SMALL_ICON, arrowStyle.getIcon(ElementType.FIRST_ELEMENT));
		actionManager.getAction(previousActionId).putValue( 
				Action.SMALL_ICON, arrowStyle.getIcon(ElementType.PREVIOUS_ELEMENT));
		actionManager.getAction(nextActionId).putValue( 
				Action.SMALL_ICON, arrowStyle.getIcon(ElementType.NEXT_ELEMENT));
		actionManager.getAction(lastActionId).putValue( 
				Action.SMALL_ICON, arrowStyle.getIcon(ElementType.LAST_ELEMENT));
	}
	
	protected static synchronized final ActionManager getSharedActionManager() {
		if(sharedActionManager==null) {
			sharedActionManager = ActionManager.globalManager().derive();

			URL actionLocation = AnnotationControl.class.getResource("annotation-control-actions.xml"); //$NON-NLS-1$
			if(actionLocation==null)
				throw new CorruptedStateException("Missing resources: annotation-control-actions.xml"); //$NON-NLS-1$
			
			try {
				sharedActionManager.loadActions(actionLocation);
			} catch (IOException e) {
				LoggerFactory.log(AnnotationControl.class, Level.SEVERE, 
						"Failed to load actions from file", e); //$NON-NLS-1$
			}
		}
		
		return sharedActionManager;
	}
	
	protected ActionManager getActionManager() {
		if(actionManager==null) {
			actionManager = getSharedActionManager().derive();
			
			registerActionCallbacks();
		}
		
		return actionManager;
	}
	
	protected void registerActionCallbacks() {
		ActionManager actionManager = getActionManager();

		actionManager.addHandler("core.helpers.annotationControl.selectModeAction",  //$NON-NLS-1$
				this, "showModeMenu"); //$NON-NLS-1$
		
		actionManager.addHandler(firstActionId, this, "navigate"); //$NON-NLS-1$
		actionManager.addHandler(previousActionId, this, "navigate"); //$NON-NLS-1$
		actionManager.addHandler(nextActionId, this, "navigate"); //$NON-NLS-1$
		actionManager.addHandler(lastActionId, this, "navigate"); //$NON-NLS-1$

		actionManager.addHandler("core.helpers.annotationControl.modeAllAction",  //$NON-NLS-1$
				this, "selectMode"); //$NON-NLS-1$
		actionManager.addHandler("core.helpers.annotationControl.modeNoneAction",  //$NON-NLS-1$
				this, "selectMode"); //$NON-NLS-1$
		actionManager.addHandler("core.helpers.annotationControl.modeFirstAction",  //$NON-NLS-1$
				this, "selectMode"); //$NON-NLS-1$
		actionManager.addHandler("core.helpers.annotationControl.modeLastAction",  //$NON-NLS-1$
				this, "selectMode"); //$NON-NLS-1$
		actionManager.addHandler("core.helpers.annotationControl.modeSelectedAction",  //$NON-NLS-1$
				this, "selectMode"); //$NON-NLS-1$
	}
	
	protected boolean hasAnnotation() {
		return annotationManager!=null && annotationManager.getAnnotation()!=null;
	}
	
	public void selectMode(boolean b) {
		// ignore
	}
	
	public void selectMode(ActionEvent e) {
		if(!hasAnnotation()) {
			return;
		}
		
		String command = e.getActionCommand();
		AnnotationDisplayMode displayMode = null; 
		
		switch (command) {
		case "all": //$NON-NLS-1$
			displayMode = AnnotationDisplayMode.ALL;
			break;

		case "first": //$NON-NLS-1$
			displayMode = AnnotationDisplayMode.FIRST_ONLY;
			break;

		case "last": //$NON-NLS-1$
			displayMode = AnnotationDisplayMode.LAST_ONLY;
			break;

		case "none": //$NON-NLS-1$
			displayMode = AnnotationDisplayMode.NONE;
			break;

		case "select": //$NON-NLS-1$
			displayMode = AnnotationDisplayMode.SELECTED;
			break;
		}
		
		if(displayMode==null) {
			LoggerFactory.log(this, Level.WARNING, 
					"Invalid display-mode passed to handler: "+command, new IllegalStateException()); //$NON-NLS-1$
		} else {
			annotationManager.setDisplayMode(displayMode);
		}
	}
	
	public void showModeMenu(ActionEvent e) {
		if(displayModeMenu==null) {
			displayModeMenu = createPopupMenu();
		}
		
		Component source = (Component) e.getSource();
		Rectangle bounds = source.getBounds();
		
		displayModeMenu.show(source, bounds.height, 0);
	}

	public void navigate(ActionEvent e) {
		if(!hasAnnotation()) {
			return;
		}
		
		String command = e.getActionCommand();
		ElementType type = ElementType.parse(command);
		
		int maxIndex = annotationManager.getMaxTraversalIndex();
		int selectedIndex = annotationManager.getTraversalIndex();
		
		switch (type) {
		case FIRST_ELEMENT:
			selectedIndex = 0;
			break;
			
		case PREVIOUS_ELEMENT:
			selectedIndex--;
			break;
			
		case NEXT_ELEMENT:
			selectedIndex++;
			break;
			
		case LAST_ELEMENT:
			selectedIndex = maxIndex;
			break;
		}
		
		if(selectedIndex<0 || selectedIndex>=maxIndex) {
			LoggerFactory.log(this, Level.WARNING, 
					"Invalid action state - selection index is out of bounds: "+selectedIndex, new Throwable()); //$NON-NLS-1$
			return;
		}
		
		annotationManager.setTraversalIndex(selectedIndex);
	}
	
	protected void refreshActions() {
		if(!hasAnnotation()) {
			return;
		}
		int maxIndex = annotationManager.getMaxTraversalIndex();
		int selectedIndex = annotationManager.getTraversalIndex();
		
		boolean selected = selectedIndex!=Annotation.BEFORE_FIRST;
		
		boolean firstEnabled = selected && selectedIndex>0;
		boolean previousEnabled = selected && selectedIndex>0;
		boolean nextEnabled = selected && selectedIndex<maxIndex;
		boolean lastEnabled = selected && selectedIndex<maxIndex;
		
		ActionManager actionManager = getActionManager();
		actionManager.setEnabled(firstEnabled, firstActionId);
		actionManager.setEnabled(previousEnabled, previousActionId);
		actionManager.setEnabled(nextEnabled, nextActionId);
		actionManager.setEnabled(lastEnabled, lastActionId);
	}

	/**
	 * @return the annotationManager
	 */
	public AnnotationManager getAnnotationManager() {
		return annotationManager;
	}

	/**
	 * @param annotationManager the annotationManager to set
	 */
	public void setAnnotationManager(AnnotationManager annotationManager) {
		if(this.annotationManager==annotationManager) {
			return;
		}
		
		Object oldValue = this.annotationManager;
		
		if(this.annotationManager!=null) {
			this.annotationManager.removePropertyChangeListener(this);
		}
		
		this.annotationManager = annotationManager;
		
		setEnabled(annotationManager!=null);
		
		if(this.annotationManager!=null) {
			this.annotationManager.addPropertyChangeListener(this);
			updateDisplayMode();
		}
		
		firePropertyChange("annotationManager", oldValue, annotationManager); //$NON-NLS-1$
	}
	
	public void setEnabled(boolean enabled) {
		navigationLabel.setEnabled(enabled);
		menuButton.setEnabled(enabled);
		if(navigationButtons!=null) {
			for(JButton button : navigationButtons) {
				button.setEnabled(enabled);
			}
		}
	}
	
	protected void updateDisplayMode() {
		if(!hasAnnotation()) {
			return;
		}
		
		AnnotationDisplayMode displayMode = annotationManager.getDisplayMode();
		boolean navMode = allowSelection && displayMode==AnnotationDisplayMode.SELECTED;
		
		if(allowSelection) {
			navigationButtons[0].setVisible(navMode);
			navigationButtons[1].setVisible(navMode);
			navigationButtons[2].setVisible(navMode);
			navigationButtons[3].setVisible(navMode);
		}
		
		if(navMode) {
			navigationLabel.setIcon(null);
			updateNavigationLabel();
		} else {
			navigationLabel.setText(displayMode.getName());
			navigationLabel.setIcon(displayMode.getIcon());
		}
			
	}

	protected void updateNavigationLabel() {
		if(!hasAnnotation()) {
			return;
		}
		
		if(annotationManager.getDisplayMode()!=AnnotationDisplayMode.SELECTED) {
			return;
		}
		
		int index = annotationManager.getTraversalIndex();
		String text = "- / -"; //$NON-NLS-1$
		if (index != Annotation.BEFORE_FIRST) {
			text = String.format("%d / %d", index + 1,  //$NON-NLS-1$
					annotationManager.getMaxTraversalIndex()+1);
		}
		navigationLabel.setText(text);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if("displayMode".equals(evt.getPropertyName())) { //$NON-NLS-1$
			updateDisplayMode();
		} else if("traversalIndex".equals(evt.getPropertyName()) //$NON-NLS-1$
				|| "annotation".equals(evt.getPropertyName())) { //$NON-NLS-1$
			updateNavigationLabel();
		} else if("annotation".equals(evt.getPropertyName())) { //$NON-NLS-1$
			setEnabled(annotationManager.getAnnotation()!=null);
		}
	}
	
	public Component[] getComponents() {
		List<Component> comps = new ArrayList<>();
		comps.add(menuButton);
		if(navigationButtons!=null) {
			comps.add(navigationButtons[0]);
			comps.add(navigationButtons[1]);
		}

		comps.add(navigationLabel);
		
		if(navigationButtons!=null) {
			comps.add(navigationButtons[2]);
			comps.add(navigationButtons[3]);
		}
		
		return comps.toArray(new Component[0]);
	}
	
	public void attach(JToolBar toolBar) {
		toolBar.add(menuButton);
		if(navigationButtons!=null) {
			toolBar.add(navigationButtons[0]);
			toolBar.add(navigationButtons[1]);
		}

		toolBar.add(navigationLabel);
		
		if(navigationButtons!=null) {
			toolBar.add(navigationButtons[2]);
			toolBar.add(navigationButtons[3]);
		}
	}
}
