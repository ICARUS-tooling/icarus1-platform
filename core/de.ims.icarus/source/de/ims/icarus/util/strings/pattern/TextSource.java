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
package de.ims.icarus.util.strings.pattern;

import java.util.ArrayList;
import java.util.List;

import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class TextSource {

	private String defaultText = "-"; //$NON-NLS-1$
	private String externalForm;

	public abstract String getText(Object data, Options env);

	public String getDefaultText() {
		return defaultText;
	}

	public void setDefaultText(String defaultText) {
		this.defaultText = defaultText;
	}

	public String getExternalForm() {
		return externalForm;
	}

	public void setExternalForm(String externalForm) {
		this.externalForm = externalForm;
	}

	@Override
	public String toString() {
		return getExternalForm();
	}

	public static abstract class AggregatedTextSource extends TextSource {
		protected final StringBuilder buffer = new StringBuilder();

		@Override
		public String getText(Object data, Options env) {
			buffer.setLength(0);

			if(!aggregateText(data, env)) {
				return getDefaultText();
			}

			return buffer.toString();
		}

		/**
		 * Creates an aggregated text in the internal buffer. Returns {@code true}
		 * iff the buffer contains valid data. A return value of {@code false} indicates
		 * that this {@code TextSource}'s default text should be returned;
		 */
		protected abstract boolean aggregateText(Object data, Options env);
	}

	public static class StaticTextSource extends TextSource {
		private final String text;

		public StaticTextSource(String text) {
			if (text == null)
				throw new NullPointerException("Invalid text"); //$NON-NLS-1$

			this.text = text;
			setExternalForm(text);
		}

		/**
		 * @see de.ims.icarus.util.strings.pattern.TextSource#getText(java.lang.Object, de.ims.icarus.util.Options)
		 */
		@Override
		public String getText(Object data, Options env) {
			return text;
		}
	}

	public static class DirectTextSource extends TextSource {
		private final Accessor<?> accessor;

		public DirectTextSource(Accessor<?> accessor) {
			if (accessor == null)
				throw new NullPointerException("Invalid accessor"); //$NON-NLS-1$

			this.accessor = accessor;
		}

		@Override
		public String getText(Object data, Options env) {
			String text = accessor.getText(data, env);
			return text==null ? getDefaultText() : text;
		}
	}

	public static class CompoundTextSource extends AggregatedTextSource {
		private final List<TextSource> elements = new ArrayList<>();


		public void addElement(TextSource element) {
			if (element == null)
				throw new NullPointerException("Invalid element"); //$NON-NLS-1$

			elements.add(element);
		}


		/**
		 * @see de.ims.icarus.util.strings.pattern.TextSource.AggregatedTextSource#aggregateText(java.lang.Object, de.ims.icarus.util.Options)
		 */
		@Override
		protected boolean aggregateText(Object data, Options env) {
			if(!elements.isEmpty()) {
				for(int i=0; i<elements.size(); i++) {
					TextSource element = elements.get(i);
					String text = element.getText(data, env);
					if(text==null) {
						text = element.getDefaultText();
					}
					buffer.append(text);
				}

				return true;
			}

			return false;
		}
	}
}
