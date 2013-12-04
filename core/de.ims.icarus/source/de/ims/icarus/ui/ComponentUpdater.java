package de.ims.icarus.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.Timer;

import de.ims.icarus.util.collections.WeakHashSet;

public final class ComponentUpdater {
	
	private static ComponentUpdater instance;
	
	public static ComponentUpdater getInstance() {
		if(instance==null) {
			synchronized (ComponentUpdater.class) {
				if(instance==null) {
					instance = new ComponentUpdater();
				}
			}
		}
		
		return instance;
	}
	
	private Set<Component> components = new WeakHashSet<>();
	private Timer timer;
	
	private ActionListener timerHandler = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			doUpdate();
		}
	};

	private ComponentUpdater() {
		// no-op
	}
	
	private Timer getTimer() {
		if(timer==null) {
			timer = new Timer(1000, timerHandler);
			timer.setInitialDelay(0);
		}
		return timer;
	}
	
	public void addComponent(Component comp) {
		if(comp==null)
			throw new NullPointerException("Invalid component"); //$NON-NLS-1$
		
		components.add(comp);
		Timer timer = getTimer();
		if(!timer.isRunning()) {
			timer.start();
		}
	}
	
	public void removeComponent(Component comp) {
		if(comp==null)
			throw new NullPointerException("Invalid component"); //$NON-NLS-1$
		
		components.remove(comp);
		if(components.isEmpty()) {
			getTimer().stop();
		}
	}
	
	public void doUpdate() {
		if(components.isEmpty()) {
			getTimer().stop();
		} else {
			for(Component comp :components) {
				comp.repaint();
			}
		}
	}
}
