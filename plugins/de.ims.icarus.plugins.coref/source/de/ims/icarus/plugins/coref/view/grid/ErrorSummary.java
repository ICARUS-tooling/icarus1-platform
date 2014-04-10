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
package de.ims.icarus.plugins.coref.view.grid;

import de.ims.icarus.language.coref.CorefErrorType;
import de.ims.icarus.resources.ResourceManager;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ErrorSummary {

	private static final CorefErrorType[] ERROR_TYPES = CorefErrorType.values();

	private int[] counters = new int[ERROR_TYPES.length];

	private int totalMentionCount = 0;

	public void clear() {
		totalMentionCount = 0;

		for(int i=0; i<counters.length; i++) {
			counters[i] = 0;
		}
	}

	public void add(CorefErrorType errorType) {
		totalMentionCount++;

		if(errorType==null) {
			errorType = CorefErrorType.TRUE_POSITIVE_MENTION;
		}

		counters[errorType.ordinal()]++;
	}

	public int getCount(CorefErrorType errorType) {
		if(errorType==null) {
			errorType = CorefErrorType.TRUE_POSITIVE_MENTION;
		}

		return counters[errorType.ordinal()];
	}

	public int getTotalMentionCount() {
		return totalMentionCount;
	}

	public boolean hasErrors() {
		return getCount(null)!=getTotalMentionCount();
	}

	public CorefErrorType getClusterType() {
		for(CorefErrorType errorType : ERROR_TYPES) {
			if(getCount(errorType)==totalMentionCount) {
				return errorType;
			}
		}

		return null;
	}

	public void append(StringBuilder sb) {
		sb.append('\n');
		sb.append('\n');

		if(hasErrors()) {
			sb.append(ResourceManager.getInstance().get(
					"plugins.errorTypes.summary.title")); //$NON-NLS-1$
			sb.append(':');
			sb.append('\n');

			for(CorefErrorType errorType : ERROR_TYPES) {
				int count = getCount(errorType);
				if(count<=0) {
					continue;
				}

				sb.append(count);
				sb.append(' ');
				sb.append(errorType.getName());
				sb.append('\n');
			}

			sb.append('\n');
			sb.append('\n');
			sb.append(ResourceManager.getInstance().get(
					"plugins.errorTypes.summary.mentionCount", //$NON-NLS-1$
					getTotalMentionCount()));
		} else {
			sb.append(ResourceManager.getInstance().get(
					"plugins.errorTypes.summary.noErrors")); //$NON-NLS-1$
			sb.append(" ("); //$NON-NLS-1$
			sb.append(ResourceManager.getInstance().get(
					"plugins.errorTypes.summary.mentionCount", //$NON-NLS-1$
					getTotalMentionCount()));
			sb.append(')');
		}
	}
}
