/* 
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package de.ims.icarus.plugins.errormining.ngram_tools;

import java.util.ArrayList;
import java.util.List;

import de.ims.icarus.language.Grammar;
import de.ims.icarus.language.annotation.AnnotatedSentenceData;
import de.ims.icarus.language.dependency.DependencyUtils;
import de.ims.icarus.language.dependency.MutableDependencyData.DependencyDataEntry;
import de.ims.icarus.util.annotation.Annotation;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class NGramSentenceData implements AnnotatedSentenceData {

	private static final long serialVersionUID = 3303973536847711267L;

	private List<DependencyDataEntry> items = new ArrayList<>();
	
	protected Annotation annotation = null; // TODO change to default value?
	
	public NGramSentenceData(int index){
		//noop
		
	}



	//TODO
	@Override
	public NGramSentenceData clone() {
		return this;
	}
	
	

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceData#getForm(int)
	 */
	@Override
	public String getForm(int index) {
		return items.get(index).getForm();
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceData#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return items.isEmpty();
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceData#length()
	 */
	@Override
	public int length() {
		return items.size();
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceData#getSourceGrammar()
	 */
	@Override
	public Grammar getSourceGrammar() {
		return DependencyUtils.getDependencyGrammar();
	}

	/**
	 * @see net.ikarus_systems.icarus.language.annotation.AnnotatedSentenceData#getAnnotation()
	 */
	@Override
	public Annotation getAnnotation() {
		return annotation;
	}


	/**
	 * @see net.ikarus_systems.icarus.ui.helper.TextItem#getText()
	 */
	@Override
	public String getText() {
		// TODO Auto-generated method stub
		return null;
	}

}

