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
package de.ims.icarus.plugins.search_tools.view.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import de.ims.icarus.search_tools.ConstraintFactory;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.ConstraintAdapter;
import de.ims.icarus.search_tools.standard.DefaultConstraint;
import de.ims.icarus.search_tools.util.SearchUtils;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class ConstraintCellData<E extends ConstraintCellData<E>> implements Serializable, Cloneable {

	private static final long serialVersionUID = -3036452029676803967L;

	@XmlAttribute(required=false)
	boolean negated = false;

	@XmlElement(required=false)
	@XmlJavaTypeAdapter(value=ConstraintAdapter.class)
	List<SearchConstraint> constraints;

	@XmlTransient
	String id = "<undefined>"; //$NON-NLS-1$

	protected ConstraintCellData() {
		// no-op
	}

	protected ConstraintCellData(List<ConstraintFactory> factories) {
		if(factories==null)
			throw new NullPointerException("Invalid factories"); //$NON-NLS-1$

		constraints = new ArrayList<>();

		for(ConstraintFactory factory : factories) {
			int min = SearchUtils.getMinInstanceCount(factory);

			SearchOperator operator = factory.getSupportedOperators()[0];
			Object value = factory.getDefaultValue(null);
			String token = factory.getToken();
			Object specifier = SearchUtils.getDefaultSpecifier(factory);

			for(int i=0; i<min; i++) {
				constraints.add(new DefaultConstraint(
						token, value, operator, specifier));
			}
		}
	}

	public abstract void copyFrom(E source);

	public boolean isNegated() {
		return negated;
	}

	public void setNegated(boolean negated) {
		this.negated = negated;
	}

	public void setConstraint(int index, SearchConstraint constraint) {
		if(constraints==null) {
			constraints = new ArrayList<>();
		}
		constraints.set(index, constraint);
	}

	public SearchConstraint[] getConstraints() {
		if(constraints==null) {
			return null;
		}
		SearchConstraint[] result = new SearchConstraint[constraints.size()];
		return constraints.toArray(result);
	}

	public int getConstraintCount() {
		return constraints==null ? 0 : constraints.size();
	}

	public SearchConstraint getConstraintAt(int index) {
		return constraints==null ? null : constraints.get(index);
	}

	public void setConstraints(SearchConstraint[] constraints) {
		if(this.constraints==null) {
			this.constraints = new ArrayList<>();
		} else {
			this.constraints.clear();
		}

		if(constraints==null) {
			return;
		}

		for(SearchConstraint constraint : constraints) {
			this.constraints.add(constraint);
		}
	}

	public void setConstraints(Collection<SearchConstraint> constraints) {
		if(this.constraints==null) {
			this.constraints = new ArrayList<>();
		} else {
			this.constraints.clear();
		}

		if(constraints==null) {
			return;
		}

		this.constraints.addAll(constraints);
	}

	/*public void setConstraints(SearchConstraint[] constraints, Map<String, Integer> constraintMap) {
		if(this.constraints==null)
			throw new IllegalStateException("Cannot assign constraints - not initialized"); //$NON-NLS-1$

		if(constraints==null || constraints.length==0) {
			return;
		}

		for(SearchConstraint constraint : constraints) {
			int index = constraintMap.get(constraint.getToken());
			this.constraints[index] = constraint;
		}
	}*/

	public void addConstraint(SearchConstraint constraint) {
		if(constraints==null) {
			constraints = new ArrayList<>();
		}

		constraints.add(constraint);
	}

	public void insertConstraint(int index, SearchConstraint constraint) {
		if(constraints==null) {
			constraints = new ArrayList<>();
		}

		constraints.add(index, constraint);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
