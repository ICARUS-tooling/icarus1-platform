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

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class WebserviceIOAttributes {

		protected String attributename;
		protected String attributevalues;
		
		public WebserviceIOAttributes(){
			
		}

		/**
		 * @return the attributename
		 */
		public String getAttributename() {
			return attributename;
		}

		/**
		 * @param attributename the attributename to set
		 */
		public void setAttributename(String attributename) {
			this.attributename = attributename;
		}

		/**
		 * @return the attrubutevalues
		 */
		public String getAttributevalues() {
			return attributevalues;
		}

		/**
		 * @param attrubutevalues the attrubutevalues to set
		 */
		public void setAttributevalues(String attributevalues) {
			this.attributevalues = attributevalues;
		}
		
}
