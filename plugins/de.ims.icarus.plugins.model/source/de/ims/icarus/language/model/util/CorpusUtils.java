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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.ims.icarus.language.model.api.AnnotationLayer;
import de.ims.icarus.language.model.api.Container;
import de.ims.icarus.language.model.api.Context;
import de.ims.icarus.language.model.api.Corpus;
import de.ims.icarus.language.model.api.CorpusMember;
import de.ims.icarus.language.model.api.Fragment;
import de.ims.icarus.language.model.api.Layer;
import de.ims.icarus.language.model.api.LayerType;
import de.ims.icarus.language.model.api.Markable;
import de.ims.icarus.language.model.api.MarkableLayer;
import de.ims.icarus.language.model.api.MemberType;
import de.ims.icarus.language.model.api.manifest.AnnotationLayerManifest;
import de.ims.icarus.language.model.api.manifest.ContainerManifest;
import de.ims.icarus.language.model.api.manifest.ContextManifest;
import de.ims.icarus.language.model.api.manifest.HighlightLayerManifest;
import de.ims.icarus.language.model.api.manifest.MarkableLayerManifest;
import de.ims.icarus.language.model.api.manifest.MemberManifest;
import de.ims.icarus.language.model.api.manifest.Prerequisite;
import de.ims.icarus.language.model.api.manifest.StructureLayerManifest;
import de.ims.icarus.language.model.api.manifest.StructureManifest;
import de.ims.icarus.language.model.io.LocationType;
import de.ims.icarus.language.model.io.Path;
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

	public static ContextManifest getContextManifest(MemberManifest manifest) {
		if (manifest == null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$

		switch (manifest.getManifestType()) {
		case ANNOTATION_LAYER_MANIFEST:
			return ((AnnotationLayerManifest)manifest).getContextManifest();
		case MARKABLE_LAYER_MANIFEST:
			return ((MarkableLayerManifest)manifest).getContextManifest();
		case STRUCTURE_LAYER_MANIFEST:
			return ((StructureLayerManifest)manifest).getContextManifest();
		case HIGHLIGHT_LAYER_MANIFEST:
			return ((HighlightLayerManifest)manifest).getContextManifest();

		case CONTEXT_MANIFEST:
			return (ContextManifest) manifest;

		case CONTAINER_MANIFEST:
			return ((ContainerManifest) manifest).getLayerManifest().getContextManifest();
		case STRUCTURE_MANIFEST:
			return ((StructureManifest) manifest).getLayerManifest().getContextManifest();

		default:
			throw new IllegalArgumentException("MemberManifest does not procide scope to a context: "+manifest); //$NON-NLS-1$
		}
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

	public static boolean isVirtual(Markable markable) {
		return markable.getBeginOffset()==-1 || markable.getEndOffset()==-1;
	}

	public static boolean isOverlayContainer(Container container) {
		return container.getCorpus().getOverlayLayer().getContainer()==container;
	}

	public static boolean isOverlayLayer(MarkableLayer layer) {
		return layer.getCorpus().getOverlayLayer()==layer;
	}

	public static boolean isOverlayMember(Markable markable) {
		return isOverlayLayer(markable.getLayer());
	}

	public static boolean isLayerMember(CorpusMember member) {
		return member.getMemberType()==MemberType.LAYER;
	}

	public static boolean isMarkableMember(CorpusMember member) {
		return member.getMemberType()!=MemberType.LAYER;
	}

	public static boolean isContainerMember(CorpusMember member) {
		return member.getMemberType()==MemberType.CONTAINER
				|| member.getMemberType()==MemberType.STRUCTURE;
	}

	public static boolean isElementMember(CorpusMember member) {
		return member.getMemberType()==MemberType.MARKABLE
				|| member.getMemberType()==MemberType.EDGE
				|| member.getMemberType()==MemberType.FRAGMENT;
	}

	public static ContainerManifest getContainerManifest(Container container) {
		if (container == null)
			throw new NullPointerException("Invalid container"); //$NON-NLS-1$

		// Fetch the container level and ask the
		// hosting markable layer manifest for the container
		// manifest at the specific level

		// We assume that this container is nested at least one level
		// below a root container
		int level = 2;

		Container parent = container.getContainer();

		if(parent==null) {
			return container.getLayer().getManifest().getRootContainerManifest();
		}

		while(parent.getContainer()!=null) {
			level++;
			parent = parent.getContainer();
		}

		MarkableLayerManifest manifest = parent.getLayer().getManifest();

		return manifest.getContainerManifest(level);
	}

	public static boolean matches(Prerequisite prerequisite, Corpus corpus) {
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

	public static String getName(Prerequisite prerequisite) {
		String id = prerequisite.getLayerId();
		if(id!=null)
			return "Required layer-id: "+id; //$NON-NLS-1$

		String typeName = prerequisite.getTypeId();
		if(typeName!=null && !typeName.isEmpty())
			return "Required type-id: "+typeName; //$NON-NLS-1$

		return prerequisite.toString();
	}

	public static String getName(Layer layer) {
		return layer.getName()+" ("+layer.getLayerType().getName()+")"; //$NON-NLS-1$ //$NON-NLS-2$
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

	public static Map<String, Object> getProperties(MemberManifest manifest) {
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

	public static int compare(Fragment f1, Fragment f2) {
		int result = f1.getBeginOffset()-f2.getBeginOffset();

		if(result==0) {
			result = f1.getEndOffset()-f2.getEndOffset();
		}
		if(result==0) {
			result = f1.getFragmentBeginIndex()-f2.getFragmentBeginIndex();
		}
		if(result==0) {
			result = f1.getFragmentEndIndex()-f2.getFragmentEndIndex();
		}

		return result;
	}

	/**
	 * Returns {@code true} if {@code m2} is located within the span
	 * defined by {@code m1}.
	 */
	public static boolean contains(Markable m1, Markable m2) {
		return m2.getBeginOffset()>=m1.getBeginOffset()
				&& m2.getEndOffset()<=m1.getEndOffset();
	}

	public static File pathToFile(Path path) {
		if (path == null)
			throw new NullPointerException("Invalid path"); //$NON-NLS-1$
		if(path.getType()!=LocationType.FILE)
			throw new IllegalArgumentException("Path needs to be a file: "+path.getPath()); //$NON-NLS-1$

		return new File(path.getPath());
	}

	public static URL pathToURL(Path path) throws MalformedURLException {
		if (path == null)
			throw new NullPointerException("Invalid path"); //$NON-NLS-1$
		if(path.getType()!=LocationType.NETWORK)
			throw new IllegalArgumentException("Path needs to be a url: "+path.getPath()); //$NON-NLS-1$

		return new URL(path.getPath());
	}



	public static InputStream openPath(Path path) throws IOException {
		if (path == null)
			throw new NullPointerException("Invalid path"); //$NON-NLS-1$

		switch (path.getType()) {
		case FILE:
			return new FileInputStream(path.getPath());

		case NETWORK:
			return new URL(path.getPath()).openStream();

		default:
			throw new IllegalArgumentException("Cannot handle source type: "+path.getType()); //$NON-NLS-1$
		}
	}
}
