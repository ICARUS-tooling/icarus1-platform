/* 
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package net.ikarus_systems.icarus.plugins.weblicht.webservice;

import java.util.List;
import java.util.logging.Logger;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.ui.dialog.DialogFactory;
import net.ikarus_systems.icarus.ui.events.EventSource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

/**
 * @author Gregor Thiele
 * @version $Id$
 * 
 */
public class WebExecutionService{

	protected Client client;
	// protected TextCorpusStreamed textCorpusStreamed;

	private EventSource eventSource;
	private static WebExecutionService instance;

	public static WebExecutionService getInstance() {
		if (instance == null) {
			synchronized (WebExecutionService.class) {
				if (instance == null) {
					instance = new WebExecutionService();
				}
			}
		}
		return instance;
	}

	private static Logger logger;

	private static Logger getLogger() {
		if (logger == null) {
			logger = LoggerFactory.getLogger(WebExecutionService.class);
		}
		return logger;
	}
	

	private WebExecutionService() {
		ClientConfig config = new DefaultClientConfig();
		client = Client.create(config);
	}
	


	public void runWebchain(Webchain webchain, String input) {

		// empty data cant be processed
		if (input==null) {
			throw new IllegalArgumentException("Empty Input"); //$NON-NLS-1$
		}
		
		System.out.println(input);

		List<String> query = WebchainRegistry.getInstance()
				.getQueryFromWebchain(webchain);

		String result = input;

		for (int i = 0; i < webchain.getWebserviceCount(); i++) {
			Webservice webservice = webchain.getWebserviceAt(i).get();

			// creating new Webresource using webservice url

			WebResource webresource = client.resource(webservice.getURL());
			ClientResponse response = webresource.get(ClientResponse.class);
				


			//http://de.wikipedia.org/wiki/HTTP-Statuscode#4xx_.E2.80.93_Client-Fehler
			switch (response.getStatus()) {
			case 500:
				DialogFactory.getGlobalFactory().showError(null,
						"plugins.weblicht.weblichtWebserviceView.dialogs.error500.title", //$NON-NLS-1$
						"plugins.weblicht.weblichtWebserviceView.dialogs.error500.message", //$NON-NLS-1$
						webservice.getURL());
				return;

			case 503:
				DialogFactory.getGlobalFactory().showError(null,
						"plugins.weblicht.weblichtWebserviceView.dialogs.error503.title", //$NON-NLS-1$
						"plugins.weblicht.weblichtWebserviceView.dialogs.error503.message", //$NON-NLS-1$
						webservice.getURL());
				return;
				
			case 404:
				DialogFactory.getGlobalFactory().showError(null,
						"plugins.weblicht.weblichtWebserviceView.dialogs.error404.title", //$NON-NLS-1$
						"plugins.weblicht.weblichtWebserviceView.dialogs.error404.message", //$NON-NLS-1$
						webservice.getURL());
				return;	
				
			case 401:
				DialogFactory.getGlobalFactory().showError(null,
						"plugins.weblicht.weblichtWebserviceView.dialogs.error401.title", //$NON-NLS-1$
						"plugins.weblicht.weblichtWebserviceView.dialogs.error401.message", //$NON-NLS-1$
						webservice.getURL());
				return;	
			}
			
			
			System.out.println("Status: " + webservice.getURL() //$NON-NLS-1$
					+ " " + response.getStatus()); //$NON-NLS-1$

			//we use only the latest input type for our query!
			result = webresource.accept(webservice.getWebresourceFormat()).post(
					String.class, result);
			
			response.close();
			
		}	
		
		System.out.println("Webresult: " + result);

	}
	
	//TODO return format/corpus
}
