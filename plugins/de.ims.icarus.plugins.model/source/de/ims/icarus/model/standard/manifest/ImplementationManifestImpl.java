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

import de.ims.icarus.model.api.manifest.ImplementationManifest;
import de.ims.icarus.model.xml.XmlSerializer;
import de.ims.icarus.util.ClassUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ImplementationManifestImpl<M extends ImplementationManifest> extends AbstractModifiableManifest<M> implements ImplementationManifest {

	private SourceType sourceType = SourceType.DEFAULT;
	private String source;
	private String classname;
	private boolean useFactory = false;

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractModifiableManifest#readTemplate(de.ims.icarus.model.api.manifest.ModifiableManifest)
	 */
	@Override
	protected void readTemplate(M template) {
		super.readTemplate(template);

		sourceType = template.getSourceType();
		source = template.getSource();
		classname = template.getClassname();
		useFactory = template.isUseFactory();
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ImplementationManifest#getSourceType()
	 */
	@Override
	public SourceType getSourceType() {
		return sourceType;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ImplementationManifest#getSource()
	 */
	@Override
	public String getSource() {
		return source;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ImplementationManifest#getClassname()
	 */
	@Override
	public String getClassname() {
		return classname;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ImplementationManifest#isUseFactory()
	 */
	@Override
	public boolean isUseFactory() {
		return useFactory;
	}

	/**
	 * @param sourceType the sourceType to set
	 */
	public void setSourceType(SourceType sourceType) {
		if (sourceType == null)
			throw new NullPointerException("Invalid sourceType"); //$NON-NLS-1$

		this.sourceType = sourceType;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * @param classname the classname to set
	 */
	public void setClassname(String classname) {
		this.classname = classname;
	}

	/**
	 * @param useFactory the useFactory to set
	 */
	public void setUseFactory(boolean useFactory) {
		this.useFactory = useFactory;
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractModifiableManifest#writeTemplateXmlElements(de.ims.icarus.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeTemplateXmlElements(XmlSerializer serializer)
			throws Exception {
		super.writeTemplateXmlElements(serializer);

		ImplementationManifest template = getTemplate();

		if(!ClassUtils.equals(sourceType, template.getSourceType())) {
			serializer.writeAttribute("source-type", sourceType.name().toLowerCase()); //$NON-NLS-1$
		}

		if(!ClassUtils.equals(source, template.getSource())) {
			serializer.writeAttribute("source", source); //$NON-NLS-1$
		}

		if(!ClassUtils.equals(classname, template.getClassname())) {
			serializer.writeAttribute("classname", classname); //$NON-NLS-1$
		}

		if(useFactory && useFactory!=template.isUseFactory()) {
			serializer.writeAttribute("use-factory", useFactory); //$NON-NLS-1$
		}
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractModifiableManifest#writeFullXmlElements(de.ims.icarus.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeFullXmlElements(XmlSerializer serializer)
			throws Exception {
		super.writeFullXmlElements(serializer);

		serializer.writeAttribute("source-type", sourceType.name().toLowerCase()); //$NON-NLS-1$
		serializer.writeAttribute("source", source); //$NON-NLS-1$
		serializer.writeAttribute("classname", classname); //$NON-NLS-1$
		if(useFactory) {
			serializer.writeAttribute("use-factory", true); //$NON-NLS-1$
		}
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractDerivable#getXmlTag()
	 */
	@Override
	protected String getXmlTag() {
		return "implementation"; //$NON-NLS-1$
	}

}
