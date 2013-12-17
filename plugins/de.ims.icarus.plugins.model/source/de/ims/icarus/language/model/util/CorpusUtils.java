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
package de.ims.icarus.language.model.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.ims.icarus.language.model.AnnotationLayer;
import de.ims.icarus.language.model.Context;
import de.ims.icarus.language.model.Corpus;
import de.ims.icarus.language.model.CorpusMember;
import de.ims.icarus.language.model.Layer;
import de.ims.icarus.language.model.LayerType;
import de.ims.icarus.language.model.Markable;
import de.ims.icarus.language.model.MarkableLayer;
import de.ims.icarus.language.model.manifest.LayerManifest;
import de.ims.icarus.language.model.manifest.Manifest;
import de.ims.icarus.language.model.registry.CorpusRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class CorpusUtils {

	private CorpusUtils() {
		throw new AssertionError();
	}

//	public static String getText(Container c) {
//		StringBuilder sb = new StringBuilder(c.getMarkableCount()*10);
//
//		sb.append("["); //$NON-NLS-1$
//		for(int i=0; i<c.getMarkableCount(); i++) {
//			if(i>0) {
//				sb.append(", "); //$NON-NLS-1$
//			}
//			sb.append(c.getMarkableAt(i).getText());
//		}
//		sb.append("]"); //$NON-NLS-1$
//
//		return sb.toString();
//	}

	public static boolean matches(LayerManifest.Prerequisite prerequisite, Corpus corpus) {
		if(prerequisite==null)
			throw new NullPointerException("Invalid prerequisite"); //$NON-NLS-1$
		if(corpus==null)
			throw new NullPointerException("Invalid corpus"); //$NON-NLS-1$

		String id = prerequisite.getLayerId();
		if(id!=null) {
			try {
				Object member = corpus.getLayer(id);

				return member instanceof Layer;
			} catch(IllegalArgumentException e) {
				return false;
			}
		}

		String typeId = prerequisite.getTypeId();
		if(typeId!=null && !typeId.isEmpty()) {
			LayerType type = CorpusRegistry.getInstance().getLayerType(typeId);
			return !corpus.getLayers(type).isEmpty();
		}

		return true;
	}

	public static String getName(LayerManifest.Prerequisite prerequisite) {
		String id = prerequisite.getLayerId();
		if(id!=null)
			return "Required layer-id: "+id; //$NON-NLS-1$

		String typeName = prerequisite.getTypeId();
		if(typeName!=null && !typeName.isEmpty())
			return "Required type-id: "+typeName; //$NON-NLS-1$

		return prerequisite.toString();
	}

	public static String getName(Layer layer) {
		return layer.getName()+" ("+layer.getType().getName()+")"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static Set<MarkableLayer> getMarkableLayers(Corpus corpus) {
		return getLayers(MarkableLayer.class, corpus.getLayers());
	}

	public static Set<AnnotationLayer> getAnnotationLayers(Corpus corpus) {
		return getLayers(AnnotationLayer.class, corpus.getLayers());
	}

	public static <L extends Layer> Set<L> getLayers(Class<L> clazz, Collection<Layer> layers) {
		if(clazz==null)
			throw new NullPointerException("Invalid layer class"); //$NON-NLS-1$
		if(layers==null)
			throw new NullPointerException("Invalid layers collection"); //$NON-NLS-1$

		Set<L> result = new HashSet<>();

		for(Layer layer : layers) {
			if(clazz.isAssignableFrom(layer.getClass())) {
				result.add(clazz.cast(layer));
			}
		}

		return result;
	}

	public static List<Layer> getBaseLayers(Layer layer) {
		if(layer==null)
			throw new NullPointerException("Invalid layer"); //$NON-NLS-1$

		List<Layer> result = new ArrayList<>();

		while((layer=layer.getBaseLayer())!=null) {
			result.add(layer);
		}

		Collections.reverse(result);

		return result;
	}

	public static List<Layer> getDependingLayers(Layer target) {
		if(target==null)
			throw new NullPointerException("Invalid target layer"); //$NON-NLS-1$

		List<Layer> result = new ArrayList<>();

		for(Layer layer : target.getCorpus()) {
			// Identity check, since layers should not be duplicated etc...
			if(layer.getBaseLayer()==target) {
				result.add(layer);
			}
		}

		return result;
	}

	public static Map<String, Object> getProperties(Manifest manifest) {
		if(manifest==null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$

		Map<String, Object> result = new HashMap<>();

		for(String name : manifest.getPropertyNames()) {
			result.put(name, manifest.getProperty(name));
		}

		return result;
	}

	public static Context getContext(CorpusMember member) {
		if(member==null)
			throw new NullPointerException("Invalid member"); //$NON-NLS-1$

		Layer layer = null;

		if(member instanceof Markable) {
			layer = ((Markable)member).getLayer();
		} else if(member instanceof Layer) {
			layer = (Layer)member;
		}

		return layer==null ? null : layer.getContext();
	}

	public static int compare(Markable m1, Markable m2) {
		int result = m1.getBeginOffset()-m2.getBeginOffset();

		if(result==0) {
			result = m1.getEndOffset()-m2.getEndOffset();
		}

		return result;
	}
}
