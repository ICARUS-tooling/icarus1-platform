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
package de.ims.icarus.plugins.weblicht.webservice;

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
