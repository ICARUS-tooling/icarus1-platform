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
package de.ims.icarus.model.standard.manifest;

import javax.sql.rowset.spi.XmlWriter;

import de.ims.icarus.model.api.manifest.ImplementationManifest;
import de.ims.icarus.model.api.manifest.ManifestUtils;
import de.ims.icarus.model.api.manifest.MemberManifest;
import de.ims.icarus.model.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractForeignImplementationManifest<M extends MemberManifest> extends AbstractMemberManifest<M> {

	private ImplementationManifest implementationManifest;

	protected AbstractForeignImplementationManifest() {

	}

	/**
	 * @return the implementationManifest
	 */
	public ImplementationManifest getImplementationManifest() {
		return implementationManifest;
	}

	/**
	 * @param implementationManifest the implementationManifest to set
	 */
	public void setImplementationManifest(
			ImplementationManifest implementationManifest) {
		if (implementationManifest == null)
			throw new NullPointerException("Invalid implementationManifest"); //$NON-NLS-1$

		this.implementationManifest = implementationManifest;
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractModifiableManifest#writeTemplateXmlElements(de.ims.icarus.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeTemplateXmlElements(XmlSerializer serializer)
			throws Exception {
		super.writeTemplateXmlElements(serializer);

		AbstractForeignImplementationManifest<?> template = (AbstractForeignImplementationManifest<?>) getTemplate();

		if(!ManifestUtils.equals(implementationManifest, template.getImplementationManifest())) {
			XmlWriter.writeImplementationElement(serializer, implementationManifest);
		}
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractModifiableManifest#writeFullXmlElements(de.ims.icarus.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeFullXmlElements(XmlSerializer serializer)
			throws Exception {
		super.writeFullXmlElements(serializer);

		XmlWriter.writeImplementationElement(serializer, implementationManifest);
	}
}
