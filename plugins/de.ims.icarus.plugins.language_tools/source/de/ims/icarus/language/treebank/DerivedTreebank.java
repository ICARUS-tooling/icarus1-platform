/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.language.treebank;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface DerivedTreebank extends Treebank {
	
	Treebank getBase();
	
	void setBase(Treebank treebank);
}