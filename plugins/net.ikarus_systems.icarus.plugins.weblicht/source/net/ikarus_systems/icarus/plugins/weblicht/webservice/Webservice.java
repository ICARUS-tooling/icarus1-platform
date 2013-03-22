package net.ikarus_systems.icarus.plugins.weblicht.webservice;

import java.util.ArrayList;
import java.util.List;


import net.ikarus_systems.icarus.util.id.Identifiable;
import net.ikarus_systems.icarus.util.id.Identity;

/**
 * 
 * @author Gregor Thiele
 * @version $Id$
 * 
 */
public class Webservice implements Identifiable {
	

	protected String serviceID;
	protected Identity identity;
	protected String creator;
	protected String contact;
	protected String url;
	protected List<WebserviceIOAttributes> input;
	protected List<WebserviceIOAttributes> output;	

	public Webservice() {
		List<WebserviceIOAttributes> input = new ArrayList<>();
		this.input = input;
		List<WebserviceIOAttributes> output = new ArrayList<>();
		this.output = output;
	}

	/**
	 * @return the identity
	 */
	public Identity getIdentity() {
		return identity;
	}

	/**
	 * @param identity the identity to set
	 */
	void setIdentity(Identity identity) {
		this.identity = identity;
	}

	public String getUID() {
		return identity.getId();
	}

	public void setServiceID(String serviceID) {
		this.serviceID = serviceID;
	}

	public String getServiceID() {
		return this.serviceID;
	}

	public String getName() {
		return identity.getName();
	}

	public String getDescription() {
		return identity.getDescription();
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getCreator() {
		return this.creator;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getContact() {
		return this.contact;
	}
	
	public void setURL(String url) {
		this.url= url;
	}

	public String getURL() {
		return this.url;	}



	/**
	 * @return the input
	 */
	public List<WebserviceIOAttributes> getInput() {
		return input;
	}

	/**
	 * @param input the input to set
	 */
	public void setInput(List<WebserviceIOAttributes> input) {		
		this.input = input;
	}

	/**
	 * @return the output
	 */
	public List<WebserviceIOAttributes> getOutput() {
		return output;
	}

	/**
	 * @param output the output to set
	 */
	public void setOutput(List<WebserviceIOAttributes> output) {
		this.output = output;
	}
	
	
	public int getOutputAttributesSize(){
		return output.size();
	}
	
	
	public WebserviceIOAttributes getOutputAttributesAt(int i){
		return output.get(i);
	}	
	
	
	public int indexOfOutputAttribute(WebserviceIOAttributes out){
		return output.indexOf(out);
	}
	
	
	public int getInputAttributesSize(){
		return input.size();
	}
	
	
	public WebserviceIOAttributes getInputAttributesAt(int i){
		return input.get(i);
	}
	
	
	public int indexOfInputAttribute(WebserviceIOAttributes in){
		return input.indexOf(in);
	}
	
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}




}
