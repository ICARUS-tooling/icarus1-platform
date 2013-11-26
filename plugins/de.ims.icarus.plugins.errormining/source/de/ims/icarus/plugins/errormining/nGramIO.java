/* 
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package de.ims.icarus.plugins.errormining;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.ui.dialog.DialogFactory;

/**
 * @author Gregor Thiele
 * @version $Id$
 * 
 */
public class nGramIO {

	protected Map<String, ArrayList<ItemInNuclei>> nGramCache;
	
	protected DocumentBuilderFactory documentBuilderFactory;
	protected DocumentBuilder documentBuilder;
	protected Document document;
	protected Element rootElement;

	public nGramIO() {

	}
	
	/**
	 * 
	 * @param document
	 * @param eName
	 * @param eData
	 * @return
	 */
	private Element generateElement(Document document, String eName, String eData){
		Element em = document.createElement(eName);
		em.appendChild(document.createTextNode(eData));
		return em;		
	}
	
	
	private void saveXMLToFile (StreamResult result) throws Exception{
        //writing to file
        FileOutputStream fop = null;
        
        File file = null;

        //File file = new File("E:\\nuclei_out.xml"); //$NON-NLS-1$
        
		if(ConfigRegistry.getGlobalRegistry()
				.getBoolean("plugins.errorMining.appearance.fileOutput.useDefaultFile")){ //$NON-NLS-1$
			file = new File(ConfigRegistry.getGlobalRegistry().getString("plugins.errorMining.appearance.filepath")); //$NON-NLS-1$
		} else {
			FileNameExtensionFilter xmlfilter = new FileNameExtensionFilter("xml files (*.xml)", //$NON-NLS-1$
																			"xml"); //$NON-NLS-1$
			file = DialogFactory.getGlobalFactory().showDestinationFileDialog(
					null,
					"chooseXMLfile", //$NON-NLS-1$
					file,
					xmlfilter);
		}
        
		
		if(file == null){
			return;
		}
       
        fop = new FileOutputStream(file);

        // if file doesnt exists, then create it
        if (!file.exists()) {
            file.createNewFile();
        }

        // get the content in bytes
        String xmlString = result.getWriter().toString();
        //System.out.println(xmlString);
        byte[] contentInBytes = xmlString.getBytes();

        fop.write(contentInBytes);
        fop.flush();
        fop.close();
	}
	
	/**
	 * @param nGramCache2
	 * @throws Exception 
	 */
	public void nGramsToXMLDependency(Map<String, ArrayList<DependencyItemInNuclei>> nGramResult) throws Exception {
		String root = "nGrams"; //$NON-NLS-1$
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.newDocument();

		Element rootElement = document.createElement(root);

		document.appendChild(rootElement);
		
		for(Iterator<String> i = nGramResult.keySet().iterator(); i.hasNext();){
			String wordform = i.next();
			
			// "\\s+" equals " "
			String[] keyArray = wordform.split("\\s+"); //$NON-NLS-1$

			ArrayList<DependencyItemInNuclei> arrItem = nGramResult.get(wordform);
			
			//System.out.println("\n### Wordform: " + key + " ###");
			
			//rootElement.appendChild(generateElement(document, "WordForm", wordform));
			//Element emWordform = generateElement(document, "WordForm", wordform); //$NON-NLS-1$
			//rootElement.appendChild(emWordform);
			
			Element emWordform = generateElement(document, "WordForm", ""); //$NON-NLS-1$ //$NON-NLS-2$
			emWordform.setAttribute("nGram", String.valueOf(keyArray.length)); //$NON-NLS-1$
			emWordform.setAttribute("form", wordform); //$NON-NLS-1$
			
			rootElement.appendChild(emWordform);
			for (int j = 0; j < arrItem.size();j++){	
				
				DependencyItemInNuclei iin = arrItem.get(j);				
				
				String posTag = iin.getPosTag();
				String posCount = String.valueOf(iin.getCount());	
				
				
				//System.out.println("PoSTag: "+ iin.getPosTag() + "  PoSCount: " + iin.getCount());
				String elementPoSTag = "PoSTag"; //$NON-NLS-1$
				Element emPoS = document.createElement(elementPoSTag);				
				
				emPoS.setAttribute("tag", posTag); //$NON-NLS-1$
				emPoS.setAttribute("count", posCount); //$NON-NLS-1$
				
				
				for (int k = 0; k < iin.getSentenceInfoSize(); k++){
					
					DependencySentenceInfo si = iin.getSentenceInfoAt(k);
					

					Element emSentence = document.createElement("Sentence"); //$NON-NLS-1$
					
					//emSentence.appendChild(document.createTextNode(String.valueOf(si.getSentenceNr())));
					
					String sentenceNR  = String.valueOf(si.getSentenceNr());
					String nucleiCount = String.valueOf(si.getNucleiIndexListSize());
					String nucleiIndex = String.valueOf(si.getNucleiIndex());
					String headIndex = String.valueOf(si.getSentenceHeadIndex());
					String sStart = String.valueOf(si.getSentenceBegin());
					String sEnd = String.valueOf(si.getSentenceEnd());
					
					emSentence.setAttribute("sentenceNr", sentenceNR); //$NON-NLS-1$
					emSentence.setAttribute("nucleiCount", nucleiCount); //$NON-NLS-1$
					emSentence.setAttribute("nucleiStartIndex", nucleiIndex); //$NON-NLS-1$
					emSentence.setAttribute("headIndex", headIndex); //$NON-NLS-1$
					emSentence.setAttribute("begin", sStart); //$NON-NLS-1$
					emSentence.setAttribute("end", sEnd); //$NON-NLS-1$
					
					//when more than one nuclei in string
					String elementNucleiNr = "NucleiIndex"; //$NON-NLS-1$					
					
					
					//TODO only add if nucleicount > 1
					for(int n = 0; n < si.getNucleiIndexListSize(); n++){
						Element emNuclei = document.createElement(elementNucleiNr);
						String nucleiNr = String.valueOf(si.getNucleiIndexListAt(n));
						emNuclei.appendChild(document.createTextNode(nucleiNr));
						emSentence.appendChild(emNuclei);
					}					
					
					emPoS.appendChild(emSentence);					
					emWordform.appendChild(emPoS);
					
				}
				
			}
			
			//rootElement.appendChild(emWordform);
		}
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		
        //format output
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", //$NON-NLS-1$
        								"2"); //$NON-NLS-1$
		DOMSource source = new DOMSource(document);
		//StreamResult result = new StreamResult(System.out);
		StreamResult result =  new StreamResult(new StringWriter());
		transformer.transform(source, result);

        

        saveXMLToFile(result);
	}
	
	

	/**
	 * @param nGramCache2
	 * @throws Exception 
	 */
	public void nGramsToXML(Map<String, ArrayList<ItemInNuclei>> nGramResult) throws Exception {
		String root = "nGrams"; //$NON-NLS-1$
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.newDocument();

		Element rootElement = document.createElement(root);

		document.appendChild(rootElement);
		
		for(Iterator<String> i = nGramResult.keySet().iterator(); i.hasNext();){
			String wordform = i.next();
			
			// "\\s+" equals " "
			String[] keyArray = wordform.split("\\s+"); //$NON-NLS-1$

			ArrayList<ItemInNuclei> arrItem = nGramResult.get(wordform);
			
			//System.out.println("\n### Wordform: " + key + " ###");
			
			//rootElement.appendChild(generateElement(document, "WordForm", wordform));
			//Element emWordform = generateElement(document, "WordForm", wordform); //$NON-NLS-1$
			//rootElement.appendChild(emWordform);
			
			Element emWordform = generateElement(document, "WordForm", ""); //$NON-NLS-1$ //$NON-NLS-2$
			emWordform.setAttribute("nGram", String.valueOf(keyArray.length)); //$NON-NLS-1$
			emWordform.setAttribute("form", wordform); //$NON-NLS-1$
			
			rootElement.appendChild(emWordform);
			for (int j = 0; j < arrItem.size();j++){	
				
				ItemInNuclei iin = arrItem.get(j);				
				
				String posTag = iin.getPosTag();
				String posCount = String.valueOf(iin.getCount());	
				
				
				//System.out.println("PoSTag: "+ iin.getPosTag() + "  PoSCount: " + iin.getCount());
				String elementPoSTag = "PoSTag"; //$NON-NLS-1$
				Element emPoS = document.createElement(elementPoSTag);				
				
				emPoS.setAttribute("tag", posTag); //$NON-NLS-1$
				emPoS.setAttribute("count", posCount); //$NON-NLS-1$
				
				for (int k = 0; k < iin.getSentenceInfoSize(); k++){
					
					SentenceInfo si = iin.getSentenceInfoAt(k);					

					Element emSentence = document.createElement("Sentence"); //$NON-NLS-1$
					
					//emSentence.appendChild(document.createTextNode(String.valueOf(si.getSentenceNr())));
					
					String sentenceNR  = String.valueOf(si.getSentenceNr());
					String nucleiCount = String.valueOf(si.getNucleiIndexListSize());
					String nucleiIndex = String.valueOf(si.getNucleiIndex());
					String sStart = String.valueOf(si.getSentenceBegin());
					String sEnd = String.valueOf(si.getSentenceEnd());
					
					emSentence.setAttribute("sentenceNr", sentenceNR); //$NON-NLS-1$
					emSentence.setAttribute("nucleiCount", nucleiCount); //$NON-NLS-1$
					emSentence.setAttribute("nucleiStartIndex", nucleiIndex); //$NON-NLS-1$
					emSentence.setAttribute("begin", sStart); //$NON-NLS-1$
					emSentence.setAttribute("end", sEnd); //$NON-NLS-1$
					
					//when more than one nuclei in string
					String elementNucleiNr = "NucleiIndex"; //$NON-NLS-1$					
					
					//TODO only add if nucleicount > 1
					for(int n = 0; n < si.getNucleiIndexListSize(); n++){
						Element emNuclei = document.createElement(elementNucleiNr);
						String nucleiNr = String.valueOf(si.getNucleiIndexListAt(n));
						emNuclei.appendChild(document.createTextNode(nucleiNr));
						emSentence.appendChild(emNuclei);
					}					
					
					emPoS.appendChild(emSentence);					
					emWordform.appendChild(emPoS);
					
				}
				
			}
			
			//rootElement.appendChild(emWordform);
		}
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		
        //format output
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", //$NON-NLS-1$
        								"2"); //$NON-NLS-1$
		DOMSource source = new DOMSource(document);
		//StreamResult result = new StreamResult(System.out);
		StreamResult result =  new StreamResult(new StringWriter());
		transformer.transform(source, result);

        

        saveXMLToFile(result);
	}

}
