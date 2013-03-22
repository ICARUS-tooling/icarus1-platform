/* 
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package net.ikarus_systems.icarus.plugins.weblicht;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import javax.swing.AbstractListModel;

import net.ikarus_systems.icarus.plugins.weblicht.webservice.Webservice;
import net.ikarus_systems.icarus.plugins.weblicht.webservice.WebserviceRegistry;
import net.ikarus_systems.icarus.ui.events.EventListener;
import net.ikarus_systems.icarus.ui.events.EventObject;
import net.ikarus_systems.icarus.ui.events.Events;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class WebserviceViewListModel extends AbstractListModel<Object>{
		

	private static final long serialVersionUID = -5945892868643517260L;
	

		protected static WebserviceLoaderListener sharedListener;
		protected static WeakHashMap<WebserviceViewListModel, Object> instances = new WeakHashMap<>();
		protected static final Object present = new Object();
		
		protected static List<Webservice> webservices;
		
		
		public WebserviceViewListModel() {
			
			if(sharedListener==null) {
				sharedListener = new WebserviceLoaderListener();
				WebserviceRegistry.getInstance().addListener(null, sharedListener);				
			}			
			
			instances.put(this, present);

		}


		
		protected static List<Webservice> getAllWebservices(){
			webservices = new ArrayList<>();
			int count = WebserviceRegistry.getInstance().getWebserviceCount();
			for (int i = 0; i< count;i++){
				webservices.add(WebserviceRegistry.getInstance().getWebserviceAt(i));
			}
			return webservices;
		}
		

		protected static void reload() {			
			getAllWebservices();
		}
		
		/**
		 * @see javax.swing.ListModel#getElementAt(int)
		 */
		@Override
		public Object getElementAt(int index) {
			return webservices==null ? null : webservices.get(index);
		}

		/**
		 * @see javax.swing.ListModel#getSize()
		 */
		@Override
		public int getSize() {
			return webservices.size();
		}
		
		/*

		protected void removeWebservice(Webservice webservice) {
			int removedindex = webservices.indexOf(webservice);
			webservices.remove(webservice);
			fireIntervalRemoved(webservice, removedindex, removedindex);
		}
		
		protected void addWebservice(Webservice webservice) {
			
			webservices.add(webservice);
			int newindex = webservices.indexOf(webservice);
			fireIntervalAdded(webservice, newindex, newindex);
			
			System.out.println("NewVal set " + webservice);
		}
		*/

		
		
		protected static class WebserviceLoaderListener implements EventListener{
			

			@Override
			public void invoke(Object sender, EventObject event) {
				//System.out.println("Sender: " + sender);
				
				Webservice webservice = (Webservice) event.getProperty("webservice"); //$NON-NLS-1$
				int index = (int) event.getProperty("index"); //$NON-NLS-1$
				
				/*
				System.out.println("Webservice: " + webservice
									+ " Index: " + index);
				*/
				
				if(webservice==null) {
					return;
				}
				reload();
				
				List<WebserviceViewListModel> models = new ArrayList<>(instances.keySet());
				switch (event.getName()) {
				case Events.ADDED:;
					for(WebserviceViewListModel model : models) {						
						model.fireIntervalAdded(webservice, index, index);
					}
					break;	
					
				case Events.REMOVED:
					System.out.println(WebserviceRegistry.getInstance().getWebserviceCount());
					for(WebserviceViewListModel model : models) {
						model.fireIntervalRemoved(webservice, index, index);
					}

					break;
					
				case Events.CHANGED:
					for(WebserviceViewListModel model : models) {
						model.fireContentsChanged(webservice, index, index);
					}
					break;

			}
		}		
	}
}
