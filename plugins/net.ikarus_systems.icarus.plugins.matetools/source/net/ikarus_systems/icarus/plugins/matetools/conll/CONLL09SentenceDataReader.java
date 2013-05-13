/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.matetools.conll;

import is2.data.SentenceData09;
import is2.io.CONLLReader09;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.ikarus_systems.icarus.language.SentenceData;
import net.ikarus_systems.icarus.language.SentenceDataReader;
import net.ikarus_systems.icarus.language.dependency.DependencyUtils;
import net.ikarus_systems.icarus.language.dependency.SimpleDependencyData;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.UnsupportedFormatException;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.location.DefaultFileLocation;
import net.ikarus_systems.icarus.util.location.Location;
import net.ikarus_systems.icarus.util.location.UnsupportedLocationException;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class CONLL09SentenceDataReader implements SentenceDataReader {
	
	protected CONLLReader09 reader;
	protected boolean normalize;
	protected boolean gold;
	protected boolean system;
	protected int inputFormat; // 0 (default) or 1
	protected int maxLength = 0;
	

	/**
	 * 
	 */
	public CONLL09SentenceDataReader() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceDataReader#init(net.ikarus_systems.icarus.util.location.Location, net.ikarus_systems.icarus.util.Options)
	 */
	@Override
	public void init(Location location, Options options) throws IOException,
			UnsupportedLocationException {
		
				
		File file = location.getFile();
		
		if(file == null)
			throw new IllegalArgumentException("Filelocation Undef"); //$NON-NLS-1$	
		
		if(!file.exists())
			throw new FileNotFoundException("Missing File: " //$NON-NLS-1$
											+file.getAbsolutePath());
		
		if (options == null){
			options = options.emptyOptions;
		}
		
		normalize = true;
		inputFormat = 0;
		//TODO extend me
		gold = options.get(INCLUDE_GOLD_OPTION, false);
		system = options.get(INCLUDE_SYSTEM_OPTION, false);
		
		if (!gold && !system){
			system = true;
		}	
		inputFormat = 0;
		
		reader = new CONLLReader09(file.toString(), normalize);	
		
		
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceDataReader#next()
	 */
	@Override
	public SentenceData next() throws IOException, UnsupportedFormatException {
		
		SentenceData09 input;
		String[] forms, lemmas, features, poss, relations;
		int[] heads;
		int iSource;
		long[] flags;
		
		SimpleDependencyData sdd = null;
		
		
		//more sentences left?
		if ((input = reader.getNext()) != null) {
					
			int size = input.forms.length - 1;

			heads = new int[size];
			poss = new String[size];
			forms = new String[size];
			lemmas = new String[size];
			features = new String[size];
			relations = new String[size];
			flags = new long[size];
			
			for (int i = 0; i < size; i++) {
				
				iSource = i+1;
				
				forms[i] = ensureDummy(input.forms[iSource], "<empty>"); //$NON-NLS-1$
				
				
				//TODO extend compound setData()
				 if(system) {
					heads[i] = input.pheads[iSource] - 1;
					lemmas[i] = ensureValid(input.plemmas[iSource]);
					features[i] = ensureValid(input.pfeats[iSource]);
					poss[i] = ensureValid(input.ppos[iSource]);
					relations[i] = ensureValid(input.plabels[iSource]);
				 }	
				 else {
					heads[i] = input.heads[iSource] - 1;
					lemmas[i] = ensureValid(input.lemmas[iSource]);
					features[i] = ensureValid(input.ofeats[iSource]);
					poss[i] = ensureValid(input.gpos[iSource]);
					relations[i] = ensureValid(input.labels[iSource]);					
				 }
				

				/*
				System.out.print("Form: "+ forms[i]);
				System.out.print(" Head: "+ heads[i]);
				System.out.print(" PoS: "+ poss[i]);
				System.out.print(" Feat: "+ features[i]);
				System.out.print(" Lemma: "+ lemmas[i]);
				System.out.println(" Relation: "+ relations[i]);
				*/
			}
			
			maxLength = Math.max(maxLength, input.forms.length);
			
			sdd = new SimpleDependencyData(forms, lemmas, features, poss, relations, heads, flags);

		}
		
		return (SentenceData) sdd;
	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceDataReader#close()
	 */
	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see net.ikarus_systems.icarus.language.SentenceDataReader#getDataType()
	 */
	@Override
	public ContentType getDataType() {
		return DependencyUtils.getDependencyContentType();
	}
	
	
	
	protected String ensureValid(String input) {
		return input==null ? "" : input; //$NON-NLS-1$
	}
	
	
	protected String ensureDummy(String input, String dummy) {
		return input==null ? dummy : input;
	}
	
	
	public static void main(String[] args) throws UnsupportedFormatException {
		
		File file = new File ("E:\\test_small.txt"); //$NON-NLS-1$
		
		DefaultFileLocation dloc = new DefaultFileLocation(file);
		
		
		Options o = null;

		CONLL09SentenceDataReader t4 = new CONLL09SentenceDataReader();
		try {
			t4.init(dloc, o);
			
			while (t4.next() != null){
				t4.next();
			}
			System.out.println("Finished reading"); //$NON-NLS-1$
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
