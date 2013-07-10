/* 
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package de.ims.icarus.plugins.weblicht.webservice;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;


import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.tuebingen.uni.sfs.wlf1.io.TextCorpusStreamed;
import de.tuebingen.uni.sfs.wlf1.io.WLFormatException;
import de.tuebingen.uni.sfs.wlf1.tc.xb.TextCorpusLayerTag;

/**
 * @author Gregor Thiele
 * @version $Id$
 * 
 */
public class WebExecutionService {

	protected Client client;
	protected Map<String, TextCorpusLayerTag> tcfTags;

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

	private WebExecutionService() {
		ClientConfig config = new DefaultClientConfig();
		client = Client.create(config);
	}

	public TextCorpusStreamed runWebchain(Webchain webchain, String input) {

		// empty data cant be processed
		if (input == null) {
			throw new IllegalArgumentException("Empty Input"); //$NON-NLS-1$			
		}
		
		List<String> query = WebchainRegistry.getInstance()
				.getQueryFromWebchain(webchain);

		//			System.out.println("Chain InPut: " + input); //$NON-NLS-1$
		//			System.out.println("Querystring: " + query); //$NON-NLS-1$				
		

		//TODO input stream result?
		String result = input;

		for (int i = 0; i < webchain.getWebserviceCount(); i++) {
			Webservice webservice = webchain.getWebserviceAt(i).get();

			// creating new Webresource using webservice url

			WebResource webresource = client.resource(webservice.getURL());
			ClientResponse response = webresource.get(ClientResponse.class);

			// http://de.wikipedia.org/wiki/HTTP-Statuscode#4xx_.E2.80.93_Client-Fehler
			switch (response.getStatus()) {
			case 500:
				DialogFactory
						.getGlobalFactory()
						.showError(
								null,
								"plugins.weblicht.weblichtWebserviceView.dialogs.error500.title", //$NON-NLS-1$
								"plugins.weblicht.weblichtWebserviceView.dialogs.error500.message", //$NON-NLS-1$
								webservice.getURL());
				return null;

			case 503:
				DialogFactory
						.getGlobalFactory()
						.showError(
								null,
								"plugins.weblicht.weblichtWebserviceView.dialogs.error503.title", //$NON-NLS-1$
								"plugins.weblicht.weblichtWebserviceView.dialogs.error503.message", //$NON-NLS-1$
								webservice.getURL());
				return null;

			case 404:
				DialogFactory
						.getGlobalFactory()
						.showError(
								null,
								"plugins.weblicht.weblichtWebserviceView.dialogs.error404.title", //$NON-NLS-1$
								"plugins.weblicht.weblichtWebserviceView.dialogs.error404.message", //$NON-NLS-1$
								webservice.getURL());
				return null;

			case 401:
				DialogFactory
						.getGlobalFactory()
						.showError(
								null,
								"plugins.weblicht.weblichtWebserviceView.dialogs.error401.title", //$NON-NLS-1$
								"plugins.weblicht.weblichtWebserviceView.dialogs.error401.message", //$NON-NLS-1$
								webservice.getURL());
				return null;
				
			case 405:
			DialogFactory
					.getGlobalFactory()
					.showError(
							null,
							"plugins.weblicht.weblichtWebserviceView.dialogs.error405.title", //$NON-NLS-1$
							"plugins.weblicht.weblichtWebserviceView.dialogs.error405.message", //$NON-NLS-1$
							webservice.getURL());
			return null;
			
			case 415:
			DialogFactory
					.getGlobalFactory()
					.showError(
							null,
							"plugins.weblicht.weblichtWebserviceView.dialogs.error415.title", //$NON-NLS-1$
							"plugins.weblicht.weblichtWebserviceView.dialogs.error415.message", //$NON-NLS-1$
							webservice.getURL());
			return null;	
			
				
			}
			

		

			
			System.out.println("Status: " + webservice.getURL() //$NON-NLS-1$
					+ " " + response.getStatus()); //$NON-NLS-1$

			
			// we use only the latest input type for our query!
			result = webresource.accept(webservice.getWebresourceFormat())
					.post(String.class, result);

			response.close();

		}
		//System.out.println("Webresult: " + result);
		return createTCFfromString(result, getReadableLayerTags(query));
	}

	
	
	@SuppressWarnings("deprecation")
	private void initializeTCFLayerTags() {
		
		tcfTags = new HashMap<String,TextCorpusLayerTag>();
		
		tcfTags.put("text", TextCorpusLayerTag.TEXT); //$NON-NLS-1$
		tcfTags.put("tokens", TextCorpusLayerTag.TOKENS); //$NON-NLS-1$
		tcfTags.put("sentences", TextCorpusLayerTag.SENTENCES); //$NON-NLS-1$
		tcfTags.put("lemmas", TextCorpusLayerTag.LEMMAS); //$NON-NLS-1$
		tcfTags.put("postags", TextCorpusLayerTag.POSTAGS); //$NON-NLS-1$
		tcfTags.put("morphology", TextCorpusLayerTag.MORPHOLOGY); //$NON-NLS-1$
		
		//added becouse of "inconsistant" service descriptions
		tcfTags.put("depparsing", TextCorpusLayerTag.PARSING_DEPENDENCY); //$NON-NLS-1$
	
		
		// not used but for completeness
		tcfTags.put("parsing_constituent", TextCorpusLayerTag.PARSING_CONSTITUENT); //$NON-NLS-1$
		tcfTags.put("parsing_dependency", TextCorpusLayerTag.PARSING_DEPENDENCY); //$NON-NLS-1$
		tcfTags.put("relations", TextCorpusLayerTag.RELATIONS); //$NON-NLS-1$
		tcfTags.put("names_entities", TextCorpusLayerTag.NAMED_ENTITIES); //$NON-NLS-1$
		tcfTags.put("references", TextCorpusLayerTag.REFERENCES); //$NON-NLS-1$
		tcfTags.put("synonymy", TextCorpusLayerTag.SYNONYMY); //$NON-NLS-1$
		tcfTags.put("antonymy", TextCorpusLayerTag.ANTONYMY); //$NON-NLS-1$
		tcfTags.put("hyponymy", TextCorpusLayerTag.HYPONYMY); //$NON-NLS-1$
		tcfTags.put("hyperonymy", TextCorpusLayerTag.HYPERONYMY); //$NON-NLS-1$
		tcfTags.put("word_splittings", TextCorpusLayerTag.WORD_SPLITTINGS); //$NON-NLS-1$
		tcfTags.put("phonetics", TextCorpusLayerTag.PHONETICS); //$NON-NLS-1$
		tcfTags.put("geo", TextCorpusLayerTag.GEO); //$NON-NLS-1$
		tcfTags.put("orthography", TextCorpusLayerTag.ORTHOGRAPHY); //$NON-NLS-1$
		tcfTags.put("text_structure", TextCorpusLayerTag.TEXT_STRUCTURE); //$NON-NLS-1$
		tcfTags.put("discourse_connectives", TextCorpusLayerTag.DISCOURSE_CONNECTIVES); //$NON-NLS-1$
		tcfTags.put("corpus_mathes", TextCorpusLayerTag.CORPUS_MATCHES); //$NON-NLS-1$
		
		
	}
	
	/**
	 * All readable layers in the result are inserted in the return EnumSet.
	 * Later TCF reader may use only the TextCorpusLayerTags which are inside
	 * the returnvalue. Thus the reader is not forced to progress all the given
	 * layers if some of them are not needed.
	 * 
	 * All Possible TCF 0.4 Layers: [TEXT, TOKENS, SENTENCES, LEMMAS, POSTAGS,
	 * MORPHOLOGY, PARSING_CONSTITUENT, PARSING_DEPENDENCY, RELATIONS,
	 * NAMED_ENTITIES, REFERENCES, SYNONYMY, ANTONYMY, HYPONYMY, HYPERONYMY,
	 * WORD_SPLITTINGS, PHONETICS, GEO, ORTHOGRAPHY, TEXT_STRUCTURE,
	 * DISCOURSE_CONNECTIVES, CORPUS_MATCHES]
	 * 
	 * @param format
	 * @return
	 */
	private EnumSet<TextCorpusLayerTag> getReadableLayerTags(List<String> format) {
		
		EnumSet<TextCorpusLayerTag> layers2Read = EnumSet.noneOf(TextCorpusLayerTag.class);
		
		if (tcfTags == null){
			initializeTCFLayerTags();
		}
		
		

		// going trough formatstring and add all known layers
		for (int i = 0; i < format.size(); i++) {
			String statement = format.get(i);
			
			//used for dependency.parsing
			if (statement.contains(".")){ //$NON-NLS-1$
				statement = (String) statement.subSequence(0, statement.indexOf("."));				 //$NON-NLS-1$
			}
			
			if (tcfTags.containsKey(statement)){
				layers2Read.add(tcfTags.get(statement));
			}
			
		}

//		System.out.println("EnumSize: " + layers2Read.size()); //$NON-NLS-1$
//		System.out.println("Included EnumLayers: " + layers2Read); //$NON-NLS-1$
		return layers2Read;

	}

	private TextCorpusStreamed createTCFfromString(String input,
			EnumSet<TextCorpusLayerTag> layersToRead) {
		InputStream is = new ByteArrayInputStream(input.getBytes());
		TextCorpusStreamed tcs = null;

		//System.out.println(input);
		
		try {
			tcs = new TextCorpusStreamed(is, layersToRead);

			// see WebLichtAdapter wlfxb
			//	System.out.println(tcs.getTokensLayer());
			//	System.out.println(tcs.getAntonymyLayer());
		} catch (WLFormatException e) {
			LoggerFactory.log(this, Level.SEVERE,
					"TextCorpusFormat Exception", e); //$NON-NLS-1$
		}
		
		//return format for message
		return tcs;

	}
}
