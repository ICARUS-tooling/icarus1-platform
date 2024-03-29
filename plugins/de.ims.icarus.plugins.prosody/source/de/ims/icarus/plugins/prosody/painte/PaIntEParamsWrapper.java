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
package de.ims.icarus.plugins.prosody.painte;

import java.io.Serializable;

import javax.swing.Icon;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import de.ims.icarus.util.Wrapper;
import de.ims.icarus.util.id.Identity;

@XmlRootElement(name="painte-wrapper")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaIntEParamsWrapper implements Wrapper<PaIntEParams>, Identity, Serializable, Cloneable {

	private static final long serialVersionUID = -2036008698457220647L;

	@XmlElement(name="params")
	private PaIntEParams params;
	@XmlElement(name="label", required=true)
	private String label;
	@XmlElement(name="description", required=false)
	private String description;
	@XmlAttribute(name="compact")
	private boolean compact;

	protected PaIntEParamsWrapper() {
		params = null;
	}

	public PaIntEParamsWrapper(String label) {
		params = new PaIntEParams();
		setLabel(label);
	}

	public PaIntEParamsWrapper(PaIntEParams params, String label) {
		if (params == null)
			throw new NullPointerException("Invalid params"); //$NON-NLS-1$

		this.params = params;
		setLabel(label);
	}

	public PaIntEParamsWrapper(PaIntEParams params, String label, String description) {
		this(params, label);

		setDescription(description);
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
		if (label == null)
			throw new NullPointerException("Invalid label");  //$NON-NLS-1$

		this.label = label;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isCompact() {
		return compact;
	}

	public void setCompact(boolean compact) {
		this.compact = compact;
	}

	@Override
	public int hashCode() {
		return params.hashCode() * (label==null ? 1 : label.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof PaIntEParamsWrapper) {
			PaIntEParamsWrapper other = (PaIntEParamsWrapper)obj;
			return label.equals(other.label)
					&& params.equals(other.params);
		}
		return false;
	}

	@Override
	public String toString() {
		return label+":"+params.toString(); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getId()
	 */
	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getName()
	 */
	@Override
	public String getName() {
		return label;
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getIcon()
	 */
	@Override
	public Icon getIcon() {
		return null;
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getOwner()
	 */
	@Override
	public Object getOwner() {
		return this;
	}

	@Override
	public PaIntEParamsWrapper clone() {
		PaIntEParamsWrapper newWrapper = null;
		try {
			newWrapper = (PaIntEParamsWrapper) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException();
		}
		newWrapper.params = params.clone();

		return newWrapper;
	}
}