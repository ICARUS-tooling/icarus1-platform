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
package de.ims.icarus.plugins.coref.view.grid;

import javax.swing.Icon;
import javax.xml.bind.annotation.XmlEnum;

import de.ims.icarus.language.coref.Cluster;
import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.CoreferenceUtils;
import de.ims.icarus.language.coref.Span;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlEnum
public enum ClusterLabelType implements Identity {
	FIRST("first") { //$NON-NLS-1$
		@Override
		protected Span getSignificantSpan(Cluster cluster,
				CoreferenceDocumentData document) {
			return cluster.size()==0 ? null : cluster.get(0);
		}
	}, 
	LAST("last") { //$NON-NLS-1$
		@Override
		protected Span getSignificantSpan(Cluster cluster,
				CoreferenceDocumentData document) {
			int size = cluster.size();
			return size==0 ? null : cluster.get(size-1);
		}
	}, 
	SHORTEST("shortest") { //$NON-NLS-1$
		@Override
		protected Span getSignificantSpan(Cluster cluster,
				CoreferenceDocumentData document) {

			int minLength = Integer.MAX_VALUE;
			Span minSpan = null;
			
			for(int i=0; i<cluster.size(); i++) {
				Span span = cluster.get(i);
				int length = CoreferenceUtils.getSpanLength(span, document);
				if(length<minLength) {
					minLength = length;
					minSpan = span;
				}
			}
			
			return minSpan;
		}
	}; 
	
	private final String key;
	
	private ClusterLabelType(String key) {
		this.key = key;
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getId()
	 */
	@Override
	public String getId() {
		return getClass().getSimpleName();
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getName()
	 */
	@Override
	public String getName() {
		return ResourceManager.getInstance().get(
				"plugins.coref.clusterLabelType."+key+".name"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getDescription()
	 */
	@Override
	public String getDescription() {
		return ResourceManager.getInstance().get(
				"plugins.coref.clusterLabelType."+key+".description"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getIcon()
	 */
	@Override
	public Icon getIcon() {
		return IconRegistry.getGlobalRegistry().getIcon("clusterlabel_"+key+".png"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getOwner()
	 */
	@Override
	public Object getOwner() {
		return this;
	}

	public String getLabel(Cluster cluster, CoreferenceDocumentData document) {
		Span span = getSignificantSpan(cluster, document);
		
		if(span==null) {
			return "-"; //$NON-NLS-1$
		}
		
		return CoreferenceUtils.getSpanText(span, document);
	}
	
	protected abstract Span getSignificantSpan(Cluster cluster, CoreferenceDocumentData document);
}
