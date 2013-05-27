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
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.ui.dialog.DialogFactory;
import net.ikarus_systems.icarus.ui.events.EventSource;
import net.ikarus_systems.icarus.util.mpi.Message;

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
public class WebExecutionService {

	protected Client client;

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

		//		System.out.println("Chain InPut: " + input); //$NON-NLS-1$
		//		System.out.println("Querystring: " + query); //$NON-NLS-1$

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
			}

			/*
			System.out.println("Status: " + webservice.getURL() //$NON-NLS-1$
					+ " " + response.getStatus()); //$NON-NLS-1$
			 */
			
			// we use only the latest input type for our query!
			result = webresource.accept(webservice.getWebresourceFormat())
					.post(String.class, result);

			response.close();

		}
		//System.out.println("Webresult: " + result);		
		return createTCFfromString(result, getReadableLayerTags(query));


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
	@SuppressWarnings("deprecation")
	private EnumSet<TextCorpusLayerTag> getReadableLayerTags(List<String> format) {
		
		EnumSet<TextCorpusLayerTag> layers2Read = EnumSet.noneOf(TextCorpusLayerTag.class);

		// going trough formatstring and add all known layers
		for (int i = 0; i < format.size(); i++) {

			switch (format.get(i)) {
			case "text": //$NON-NLS-1$
				layers2Read.add(TextCorpusLayerTag.TEXT);
				break;
			case "tokens": //$NON-NLS-1$
				layers2Read.add(TextCorpusLayerTag.TOKENS);
				break;
			case "sentences": //$NON-NLS-1$
				layers2Read.add(TextCorpusLayerTag.SENTENCES);
				break;
			case "lemmas": //$NON-NLS-1$
				layers2Read.add(TextCorpusLayerTag.LEMMAS);
				break;
			case "postags": //$NON-NLS-1$
				layers2Read.add(TextCorpusLayerTag.POSTAGS);
				break;
			case "morphology": //$NON-NLS-1$
				layers2Read.add(TextCorpusLayerTag.MORPHOLOGY);
				break;

			// not used but for completeness
			case "antonymy": //$NON-NLS-1$
				layers2Read.add(TextCorpusLayerTag.ANTONYMY);
				break;
			case "corpus_matches": //$NON-NLS-1$
				layers2Read.add(TextCorpusLayerTag.CORPUS_MATCHES);
				break;
			case "discourse_connectives": //$NON-NLS-1$
				layers2Read.add(TextCorpusLayerTag.DISCOURSE_CONNECTIVES);
				break;
			case "geo": //$NON-NLS-1$
				layers2Read.add(TextCorpusLayerTag.GEO);
				break;
			case "hyperonymy": //$NON-NLS-1$
				layers2Read.add(TextCorpusLayerTag.HYPERONYMY);
				break;
			case "hyponomy": //$NON-NLS-1$
				layers2Read.add(TextCorpusLayerTag.HYPONYMY);
				break;
			case "named_entities": //$NON-NLS-1$
				layers2Read.add(TextCorpusLayerTag.NAMED_ENTITIES);
				break;
			case "ortography": //$NON-NLS-1$
				layers2Read.add(TextCorpusLayerTag.ORTHOGRAPHY);
				break;
			case "phonetics": //$NON-NLS-1$
				layers2Read.add(TextCorpusLayerTag.PHONETICS);
				break;
			case "parsing_constituent": //$NON-NLS-1$
				layers2Read.add(TextCorpusLayerTag.PARSING_CONSTITUENT);
				break;
			case "parsing_dependency": //$NON-NLS-1$
				layers2Read.add(TextCorpusLayerTag.PARSING_DEPENDENCY);
				break;
			case "references": //$NON-NLS-1$
				layers2Read.add(TextCorpusLayerTag.REFERENCES);
				break;
			case "relations": //$NON-NLS-1$
				layers2Read.add(TextCorpusLayerTag.RELATIONS);
				break;
			case "synonymy": //$NON-NLS-1$
				layers2Read.add(TextCorpusLayerTag.SYNONYMY);
				break;
			case "text_structure": //$NON-NLS-1$
				layers2Read.add(TextCorpusLayerTag.TEXT_STRUCTURE);
				break;
			case "word_splittings": //$NON-NLS-1$
				layers2Read.add(TextCorpusLayerTag.WORD_SPLITTINGS);
				break;
			// no a string associated with an tcf layer (Example: lang=de field)
			default:
				break;
			}

			/*
			 * if (format.get(i).equals("text")){ //$NON-NLS-1$
			 * layers2Read.add(TextCorpusLayerTag.TEXT); }
			 */
		}

		System.out.println("EnumSize: " + layers2Read.size()); //$NON-NLS-1$
		System.out.println("Included EnumLayers: " + layers2Read); //$NON-NLS-1$
		return layers2Read;

	}

	private TextCorpusStreamed createTCFfromString(String input,
			EnumSet<TextCorpusLayerTag> layersToRead) {
		InputStream is = new ByteArrayInputStream(input.getBytes());
		TextCorpusStreamed tcs = null;

		try {
			tcs = new TextCorpusStreamed(is, layersToRead);

			// see WebLichtAdapter wlfxb
			//	System.out.println(tcs.getTokensLayer());
			//	System.out.println(tcs.getAntonymyLayer());
		} catch (WLFormatException e) {
			LoggerFactory.log(this, Level.SEVERE,
					"TextCorpusFormat Exception", e); //$NON-NLS-1$
		}
		
		return tcs;

	}

	// TODO return format/treebank
}
