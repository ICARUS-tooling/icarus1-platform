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
package de.ims.icarus.plugins.prosody.params;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.ims.icarus.util.Wrapper;

@XmlRootElement(name="painte-wrapper")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaIntEParamsWrapper implements Wrapper<PaIntEParams> {

	@XmlElement(name="params")
	private final PaIntEParams params;
	@XmlElement(name="label", required=false)
	private String label;

	protected PaIntEParamsWrapper() {
		params = null;
	}

	public PaIntEParamsWrapper(PaIntEParams params) {
		if (params == null)
			throw new NullPointerException("Invalid params"); //$NON-NLS-1$

		this.params = params;
	}

	/**
	 * @see de.ims.icarus.util.Wrapper#get()
	 */
	@Override
	public PaIntEParams get() {
		return getParams();
	}

	/**
	 * @return the params
	 */
	public PaIntEParams getParams() {
		return params;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

}