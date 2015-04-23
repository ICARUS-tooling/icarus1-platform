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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.prosody.sampa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.ims.icarus.util.ToolException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SampaValidator {

	private List<SampaIterator> iterators = new ArrayList<>();
	private int wordCount = 0;
	private int syllableCount = 0;
	private boolean validationStarted = false;

	private SampaMapper2 mapper;
	private boolean verbose = false;

	private List<SampaSet> errors = new ArrayList<>();

	public synchronized void addSampaIterator(SampaIterator iterator) {
		if (iterator == null)
			throw new NullPointerException("Invalid iterator"); //$NON-NLS-1$

		if(validationStarted)
			throw new IllegalStateException("Cannot add new data after validation has started"); //$NON-NLS-1$

		iterators.add(iterator);
	}

	public synchronized void validate() throws ToolException {
		if(validationStarted)
			throw new IllegalStateException("Validator intented for one-time use only!"); //$NON-NLS-1$

		validationStarted = true;

		if(iterators.isEmpty())
			throw new IllegalArgumentException("No data to be validated..."); //$NON-NLS-1$

		if(mapper==null) {
			mapper = new SampaMapper2();
		}

		for(int i=0; i<iterators.size(); i++) {
			try (SampaIterator iterator = iterators.get(i)) {
				iterator.reset();

				SampaSet data;

				while((data=iterator.next()) != null) {
					wordCount++;
					syllableCount += data.getSampaBlocks().length;

					if(!validate(data)) {
						errors.add(data);
					}
				}
			}
		}
	}

	public boolean validate(SampaSet data) {
		String[] parts = mapper.split(data.getWord(), data.getSampaBlocks());

		if(verbose) {
			System.out.printf("word='%s' sampa='%s' splits='%s'\n",
					data.getWord(), Arrays.toString(data.getSampaBlocks()),
					parts==null ? "-" : Arrays.toString(parts));
		}

		return parts!=null;
	}

	public synchronized SampaMapper2 getMapper() {
		return mapper;
	}

	public synchronized void setMapper(SampaMapper2 mapper) {
		if(validationStarted)
			throw new IllegalStateException("Cannot change mapper after validation has started"); //$NON-NLS-1$

		this.mapper = mapper;
	}

	public int getWordCount() {
		return wordCount;
	}

	public int getSyllableCount() {
		return syllableCount;
	}

	public int getErrorCount() {
		return errors.size();
	}

	public boolean hasErrors() {
		return !errors.isEmpty();
	}

	public SampaSet getErroneousSetAt(int index) {
		return errors.get(index);
	}

	public boolean isVerbose() {
		return verbose;
	}

	public synchronized void setVerbose(boolean verbose) {
		if(validationStarted)
			throw new IllegalStateException("Cannot change verbose state after validation has started"); //$NON-NLS-1$

		this.verbose = verbose;
	}
}
