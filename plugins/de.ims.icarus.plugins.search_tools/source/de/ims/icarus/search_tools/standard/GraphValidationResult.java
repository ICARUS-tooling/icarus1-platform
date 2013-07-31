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
package de.ims.icarus.search_tools.standard;

import java.util.ArrayList;
import java.util.List;

import de.ims.icarus.resources.ResourceManager;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class GraphValidationResult {
	
	private List<ValidationEntry> warnings;
	private List<ValidationEntry> errors;
	
	private boolean locked = false;

	public GraphValidationResult() {
		// no-op
	}
	
	public void lock() {
		locked = true;
	}
	
	public void addWarning(String message, Object...params) {
		if(locked)
			throw new IllegalStateException("Result is locked"); //$NON-NLS-1$
		if(message==null)
			throw new IllegalArgumentException("Invalid message"); //$NON-NLS-1$
		
		if(warnings==null) {
			warnings = new ArrayList<>();
		}
		
		warnings.add(new ValidationEntry(message, params));
	}
	
	public void addError(String message, Object...params) {
		if(locked)
			throw new IllegalStateException("Result is locked"); //$NON-NLS-1$
		if(message==null)
			throw new IllegalArgumentException("Invalid message"); //$NON-NLS-1$
		
		if(errors==null) {
			errors = new ArrayList<>();
		}
		
		errors.add(new ValidationEntry(message, params));
	}
	
	public int getWarningCount() {
		return warnings==null ? 0 : warnings.size();
	}
	
	public String getWarningMessage(int index) {
		return warnings.get(index).getLocalizedMessage();
	}
	
	public int getErrorCount() {
		return errors==null ? 0 : errors.size();
	}
	
	public String getErrorMessage(int index) {
		return errors.get(index).getLocalizedMessage();
	}
	
	public boolean isEmpty() {
		return getWarningCount()==0 && getErrorCount()==0;
	}

	private static class ValidationEntry {
		private String message;
		private Object[] params;
		
		private ValidationEntry(String message, Object[] params) {
			this.message = message;
			this.params = params;
		}
		
		String getLocalizedMessage() {
			return ResourceManager.getInstance().get(message, params);
		}
	}
}
