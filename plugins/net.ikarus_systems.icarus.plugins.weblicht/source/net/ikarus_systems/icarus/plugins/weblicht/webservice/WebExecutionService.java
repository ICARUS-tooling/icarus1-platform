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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Logger;

import net.ikarus_systems.icarus.language.MutableSentenceData;
import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.ui.dialog.DialogFactory;
import net.ikarus_systems.icarus.ui.events.EventSource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import de.tuebingen.uni.sfs.wlf1.io.TextCorpusStreamed;
import de.tuebingen.uni.sfs.wlf1.io.WLFormatException;
import de.tuebingen.uni.sfs.wlf1.tc.xb.TextCorpusLayerTag;

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
		
		System.out.println(query);

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
		
		
		createTCFfromString(result, getReadableLayerTags(query));

		
		System.out.println("Webresult: " + result);
		
	}
	
	private EnumSet<TextCorpusLayerTag> getReadableLayerTags(List<String> format){
		EnumSet<TextCorpusLayerTag> layers2Read = EnumSet.noneOf(TextCorpusLayerTag.class);
		
		for (int i = 0; i < format.size(); i++){
			EnumSet<TextCorpusLayerTag> tmp = EnumSet.noneOf(TextCorpusLayerTag.class);

			if (format.get(i).equals("text")){ //$NON-NLS-1$
				tmp = EnumSet.of(TextCorpusLayerTag.TEXT);
			}
			
			if (format.get(i).equals("tokens")){ //$NON-NLS-1$
				tmp = EnumSet.of(TextCorpusLayerTag.TOKENS);
			}
			
			if (format.get(i).equals("sentences")){ //$NON-NLS-1$
				tmp = EnumSet.of(TextCorpusLayerTag.SENTENCES);
			}
			
			
			//TODO add other stuff
			/*
			[TEXT, TOKENS, SENTENCES,
			 LEMMAS, POSTAGS, MORPHOLOGY,
			 PARSING_CONSTITUENT, PARSING_DEPENDENCY,
			 RELATIONS, NAMED_ENTITIES, REFERENCES,
			 SYNONYMY, ANTONYMY, HYPONYMY, HYPERONYMY,
			 WORD_SPLITTINGS, PHONETICS, GEO, ORTHOGRAPHY,
			 TEXT_STRUCTURE, DISCOURSE_CONNECTIVES, CORPUS_MATCHES]
			 */
			layers2Read.addAll(tmp);
			
		}
		System.out.println(layers2Read);
		return layers2Read;
		
	}
	
	
	private void createTCFfromString(String input, EnumSet<TextCorpusLayerTag> layersToRead){
		InputStream is = new ByteArrayInputStream(input.getBytes());
		
		
		try {
			TextCorpusStreamed tcs = new TextCorpusStreamed(is, layersToRead);
			
			//see WebLichtAdapter wlfxb
			System.out.println(tcs.getTokensLayer());
			System.out.println(tcs.getAntonymyLayer());
		} catch (WLFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	//TODO return format/corpus
}
