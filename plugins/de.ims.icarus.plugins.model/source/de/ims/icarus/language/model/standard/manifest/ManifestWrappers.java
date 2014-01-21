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
package de.ims.icarus.language.model.standard.manifest;

import java.util.List;
import java.util.Set;

import javax.swing.Icon;

import de.ims.icarus.language.model.manifest.ContextManifest;
import de.ims.icarus.language.model.manifest.LayerManifest;
import de.ims.icarus.language.model.manifest.Manifest;
import de.ims.icarus.language.model.manifest.ManifestType;
import de.ims.icarus.language.model.manifest.OptionsManifest;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ManifestWrappers {

	private static class ManifestDelegate<P extends Object, M extends Manifest> implements Manifest {

		protected final P parent;
		protected final M template;

		protected ManifestDelegate(P parent, M template) {
			if (parent == null)
				throw new NullPointerException("Invalid parent");  //$NON-NLS-1$
			if (template == null)
				throw new NullPointerException("Invalid template");  //$NON-NLS-1$

			this.parent = parent;
			this.template = template;
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getId()
		 */
		@Override
		public String getId() {
			return template.getId();
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getIcon()
		 */
		@Override
		public Icon getIcon() {
			return template.getIcon();
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getOwner()
		 */
		@Override
		public Object getOwner() {
			return this;
		}

		/**
		 * @see de.ims.icarus.language.model.manifest.Manifest#getManifestType()
		 */
		@Override
		public ManifestType getManifestType() {
			return template.getManifestType();
		}

		/**
		 * @see de.ims.icarus.language.model.manifest.Manifest#getName()
		 */
		@Override
		public String getName() {
			return template.getName();
		}

		/**
		 * @see de.ims.icarus.language.model.manifest.Manifest#getDescription()
		 */
		@Override
		public String getDescription() {
			return template.getDescription();
		}

		/**
		 * @see de.ims.icarus.language.model.manifest.Manifest#getOptionsManifest()
		 */
		@Override
		public OptionsManifest getOptionsManifest() {
			return template.getOptionsManifest();
		}

		/**
		 * @see de.ims.icarus.language.model.manifest.Manifest#getProperty(java.lang.String)
		 */
		@Override
		public Object getProperty(String name) {
			return template.getProperty(name);
		}

		/**
		 * @see de.ims.icarus.language.model.manifest.Manifest#setProperty(java.lang.String, java.lang.Object)
		 */
		@Override
		public void setProperty(String name, Object value) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.ims.icarus.language.model.manifest.Manifest#getPropertyNames()
		 */
		@Override
		public Set<String> getPropertyNames() {
			return template.getPropertyNames();
		}

	}

	private static class LayerManifestDelegate<L extends LayerManifest> extends ManifestDelegate<ContextManifest, L> implements LayerManifest {

		/**
		 * @param parent
		 * @param template
		 */
		protected LayerManifestDelegate(ContextManifest parent, L template) {
			super(parent, template);
		}

		/**
		 * @see de.ims.icarus.language.model.manifest.LayerManifest#getContextManifest()
		 */
		@Override
		public ContextManifest getContextManifest() {
			return parent;
		}

		/**
		 * @see de.ims.icarus.language.model.manifest.LayerManifest#getPrerequisites()
		 */
		@Override
		public List<Prerequisite> getPrerequisites() {
			return template.getPrerequisites();
		}

		/**
		 * @see de.ims.icarus.language.model.manifest.LayerManifest#isIndexable()
		 */
		@Override
		public boolean isIndexable() {
			return template.isIndexable();
		}

		/**
		 * @see de.ims.icarus.language.model.manifest.LayerManifest#isSearchable()
		 */
		@Override
		public boolean isSearchable() {
			return template.isSearchable();
		}

	}

	private static class
}
