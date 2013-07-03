/* 
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package ngram_tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ListModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataListener;

import net.ikarus_systems.icarus.language.AvailabilityObserver;
import net.ikarus_systems.icarus.language.DataType;
import net.ikarus_systems.icarus.language.Grammar;
import net.ikarus_systems.icarus.language.SentenceData;
import net.ikarus_systems.icarus.language.SentenceDataList;
import net.ikarus_systems.icarus.language.annotation.AnnotatedSentenceData;
import net.ikarus_systems.icarus.language.dependency.DependencyUtils;
import net.ikarus_systems.icarus.language.dependency.MutableDependencyData.DependencyDataEntry;
import net.ikarus_systems.icarus.plugins.errormining.ItemInNuclei;
import net.ikarus_systems.icarus.util.annotation.Annotation;
import net.ikarus_systems.icarus.util.data.ContentType;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class NGramDataList implements SentenceDataList, ListModel<Object> {
	
	
	protected Map<String,ArrayList<ItemInNuclei>> nGramMap;
	protected int index;
	
	Map<Integer, NGramSentenceData> nGramMapCache;
	
	
	public NGramDataList(Map<String,ArrayList<ItemInNuclei>> nGramMap){
		if (nGramMap == null)
			throw new IllegalArgumentException("No Data"); //$NON-NLS-1$
		setNGramMap(nGramMap);
		
	}
	

	/**
	 * @param nGramMap
	 */
	void setNGramMap(Map<String, ArrayList<ItemInNuclei>> nGramMap) {
		if (this.nGramMap != null) {
			return;
		}
		nGramMapCache = new HashMap<Integer, NGramSentenceData>();
		this.nGramMap = nGramMap;
		
	}
	
	private Map<String, ArrayList<ItemInNuclei>> getNGramMap(){
		return nGramMap;
	}
	
	
	private SentenceData getNGramDataFromIndex(int index) {
		if (!nGramMap.containsKey(index)) {
			NGramSentenceData ngramData = new NGramSentenceData(index);
			nGramMapCache.put(index, ngramData);
		}
		return nGramMapCache.get(index);
	}


	/**
	 * @see net.ikarus_systems.icarus.util.data.DataList#size()
	 */
	@Override
	public int size() {
		return getNGramMap().size();
	}

	/**
	 * @see net.ikarus_systems.icarus.util.data.DataList#get(int)
	 */
	@Override
	public SentenceData get(int index) {
		return get(index, DataType.SYSTEM);
	}

	/**
	 * @see net.ikarus_systems.icarus.util.data.DataList#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return DependencyUtils.getDependencyContentType();
	}

	/**
	 * @see net.ikarus_systems.icarus.util.data.DataList#addChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public void addChangeListener(ChangeListener listener) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see net.ikarus_systems.icarus.util.data.DataList#removeChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public void removeChangeListener(ChangeListener listener) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceDataList#supportsType(net.ikarus_systems.icarus.language.DataType)
	 */
	@Override
	public boolean supportsType(DataType type) {
		return type == DataType.SYSTEM;
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceDataList#get(int, net.ikarus_systems.icarus.language.DataType)
	 */
	@Override
	public SentenceData get(int index, DataType type) {
		if (type != DataType.SYSTEM) {
			return null;
		}
		return nGramMap == null ? null : getNGramDataFromIndex(index);
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceDataList#get(int, net.ikarus_systems.icarus.language.DataType, net.ikarus_systems.icarus.language.AvailabilityObserver)
	 */
	@Override
	public SentenceData get(int index, DataType type,
			AvailabilityObserver observer) {
		return get(index, type);
	}
	
	
	
	
	//data class
	
	private class NGramSentenceData implements AnnotatedSentenceData {

		private static final long serialVersionUID = 3303973536847711267L;

		private List<DependencyDataEntry> items = new ArrayList<>();
		
		protected Annotation annotation = null; // TODO change to default value?
		
		public NGramSentenceData(int index){
			
			
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

	}




	/**
	 * @see javax.swing.ListModel#addListDataListener(javax.swing.event.ListDataListener)
	 */
	@Override
	public void addListDataListener(ListDataListener l) {
		// TODO Auto-generated method stub
		
	}


	/**
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	@Override
	public Object getElementAt(int index) {
		// TODO Auto-generated method stub
		return get(index);
	}


	/**
	 * @see javax.swing.ListModel#getSize()
	 */
	@Override
	public int getSize() {
		// TODO Auto-generated method stub
		return size();
	}


	/**
	 * @see javax.swing.ListModel#removeListDataListener(javax.swing.event.ListDataListener)
	 */
	@Override
	public void removeListDataListener(ListDataListener l) {
		// TODO Auto-generated method stub
		
	}



}
