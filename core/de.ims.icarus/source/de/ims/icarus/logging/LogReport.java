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
package de.ims.icarus.logging;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LogReport {

	private List<LogRecord> records = new ArrayList<>();

	private final Object owner;

	private boolean published = false;

	public LogReport(Object owner) {
		if (owner == null)
			throw new NullPointerException("Invalid owner"); //$NON-NLS-1$

		this.owner = owner;
	}

	public Object getOwner() {
		return owner;
	}

	private void publish(int minLevel, int maxLevel) {
		if(published)
			throw new IllegalStateException("Records already published"); //$NON-NLS-1$

		published = true;

		for(LogRecord record : records) {
			int level = record.getLevel().intValue();
			if(minLevel!=-1 && level<minLevel) {
				continue;
			}
			if(maxLevel!=-1 && level>maxLevel) {
				continue;
			}

			LoggerFactory.getLogger(owner).log(record);
		}
	}

	public void publish() {
		publish(-1, -1);
	}

	/**
	 * @return the published
	 */
	public boolean isPublished() {
		return published;
	}

	public LogRecord log(Level level, String message) {
		LogRecord record = LoggerFactory.record(level, message);
		records.add(record);
		return record;
	}

	public LogRecord log(Level level, String message, Throwable t) {
		LogRecord record = LoggerFactory.record(level, message, t);
		records.add(record);
		return record;
	}

	public LogRecord debug(String message) {
		return log(Level.FINE, message);
	}

	public LogRecord debug(String message, Throwable t) {
		return log(Level.FINE, message, t);
	}

	public LogRecord info(String message) {
		return log(Level.INFO, message);
	}

	public LogRecord info(String message, Throwable t) {
		return log(Level.INFO, message, t);
	}

	public LogRecord warning(String message) {
		return log(Level.WARNING, message);
	}

	public LogRecord warning(String message, Throwable t) {
		return log(Level.WARNING, message, t);
	}

	public LogRecord error(String message) {
		return log(Level.SEVERE, message);
	}

	public LogRecord error(String message, Throwable t) {
		return log(Level.SEVERE, message, t);
	}

	public void clear() {
		records.clear();
	}

	public boolean isEmpty() {
		return records.isEmpty();
	}

	public List<LogRecord> getRecords() {
		return CollectionUtils.getListProxy(records);
	}

	private List<LogRecord> getRecords(int minLevel, int maxLevel) {
		List<LogRecord> result = new ArrayList<>();

		for(LogRecord record : records) {
			int level = record.getLevel().intValue();
			if(minLevel!=-1 && level<minLevel) {
				continue;
			}
			if(maxLevel!=-1 && level>maxLevel) {
				continue;
			}

			result.add(record);
		}

		return result;
	}

	public List<LogRecord> getRecords(Level level) {
		return getRecords(level.intValue(), level.intValue());
	}

	public List<LogRecord> getRecordsBelow(Level level) {
		return getRecords(-1, level.intValue());
	}

	public List<LogRecord> getRecordsAbove(Level level) {
		return getRecords(level.intValue(), -1);
	}

	public List<LogRecord> getRecordsBetween(Level minLevel, Level maxLevel) {
		return getRecords(minLevel.intValue(), maxLevel.intValue());
	}

	public List<LogRecord> getDebugRecords() {
		return getRecords(Level.FINE);
	}

	public List<LogRecord> getInfoRecords() {
		return getRecords(Level.INFO);
	}

	public List<LogRecord> getWarningRecords() {
		return getRecords(Level.WARNING);
	}

	public List<LogRecord> getErrorRecords() {
		return getRecords(Level.SEVERE);
	}

	private boolean hasRecords(int minLevel, int maxLevel) {
		for(LogRecord record : records) {
			int level = record.getLevel().intValue();
			if(minLevel!=-1 && level<minLevel) {
				continue;
			}
			if(maxLevel!=-1 && level>maxLevel) {
				continue;
			}

			return true;
		}

		return false;
	}

	public boolean hasRecords(Level level) {
		return hasRecords(level.intValue(), level.intValue());
	}

	public boolean hasRecordsBelow(Level level) {
		return hasRecords(-1, level.intValue());
	}

	public boolean hasRecordsAbove(Level level) {
		return hasRecords(level.intValue(), -1);
	}

	public boolean hasRecordsBetween(Level minLevel, Level maxLevel) {
		return hasRecords(minLevel.intValue(), maxLevel.intValue());
	}

	public boolean hasDebugRecords() {
		return hasRecords(Level.FINE);
	}

	public boolean hasInfoRecords() {
		return hasRecords(Level.INFO);
	}

	public boolean hasWarningRecords() {
		return hasRecords(Level.WARNING);
	}

	public boolean hasErrorRecords() {
		return hasRecords(Level.SEVERE);
	}

	private int countRecords(int minLevel, int maxLevel) {
		int result = 0;

		for(LogRecord record : records) {
			int level = record.getLevel().intValue();
			if(minLevel!=-1 && level<minLevel) {
				continue;
			}
			if(maxLevel!=-1 && level>maxLevel) {
				continue;
			}

			result++;
		}

		return result;
	}

	public int countRecords(Level level) {
		return countRecords(level.intValue(), level.intValue());
	}

	public int countRecordsBelow(Level level) {
		return countRecords(-1, level.intValue());
	}

	public int countRecordsAbove(Level level) {
		return countRecords(level.intValue(), -1);
	}

	public int countRecordsBetween(Level minLevel, Level maxLevel) {
		return countRecords(minLevel.intValue(), maxLevel.intValue());
	}

	public int countDebugRecords() {
		return countRecords(Level.FINE);
	}

	public int countInfoRecords() {
		return countRecords(Level.INFO);
	}

	public int countWarningRecords() {
		return countRecords(Level.WARNING);
	}

	public int countErrorRecords() {
		return countRecords(Level.SEVERE);
	}
}
