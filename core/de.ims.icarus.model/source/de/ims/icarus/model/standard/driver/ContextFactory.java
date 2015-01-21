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
package de.ims.icarus.model.standard.driver;

import java.util.ArrayList;
import java.util.List;

import de.ims.icarus.model.ModelError;
import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.api.Context;
import de.ims.icarus.model.api.Corpus;
import de.ims.icarus.model.api.driver.Driver;
import de.ims.icarus.model.api.layer.AnnotationLayer;
import de.ims.icarus.model.api.layer.Dependency;
import de.ims.icarus.model.api.layer.DependencyType;
import de.ims.icarus.model.api.layer.FragmentLayer;
import de.ims.icarus.model.api.layer.Layer;
import de.ims.icarus.model.api.layer.LayerGroup;
import de.ims.icarus.model.api.layer.MarkableLayer;
import de.ims.icarus.model.api.manifest.AnnotationLayerManifest;
import de.ims.icarus.model.api.manifest.ContextManifest;
import de.ims.icarus.model.api.manifest.FragmentLayerManifest;
import de.ims.icarus.model.api.manifest.LayerGroupManifest;
import de.ims.icarus.model.api.manifest.LayerManifest;
import de.ims.icarus.model.api.manifest.LayerManifest.TargetLayerManifest;
import de.ims.icarus.model.api.manifest.ItemLayerManifest;
import de.ims.icarus.model.api.manifest.RasterizerManifest;
import de.ims.icarus.model.api.manifest.StructureLayerManifest;
import de.ims.icarus.model.api.raster.Rasterizer;
import de.ims.icarus.model.standard.corpus.DefaultContext;
import de.ims.icarus.model.standard.elements.MemberSets;
import de.ims.icarus.model.standard.layer.AbstractLayer;
import de.ims.icarus.model.standard.layer.ComplexAnnotationLayer;
import de.ims.icarus.model.standard.layer.DefaultFragmentLayer;
import de.ims.icarus.model.standard.layer.DefaultLayerGroup;
import de.ims.icarus.model.standard.layer.DefaultMarkableLayer;
import de.ims.icarus.model.standard.layer.DefaultStructureLayer;
import de.ims.icarus.model.standard.layer.SimpleAnnotationLayer;
import de.ims.icarus.model.util.CorpusUtils;
import de.ims.icarus.model.util.ImplementationLoader;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ContextFactory {

	public Context createContext(Corpus corpus, ContextManifest manifest, Driver driver) throws ModelException {
		if (corpus == null)
			throw new NullPointerException("Invalid corpus");  //$NON-NLS-1$
		if (manifest == null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$
		if (driver == null)
			throw new NullPointerException("Invalid driver");  //$NON-NLS-1$

		DefaultContext context = new DefaultContext(corpus, manifest, driver);
		List<LayerLinker> linkers = new ArrayList<>(manifest.getLayerManifests().size());

		// First pass, create layers
		for(LayerGroupManifest groupManifest : manifest.getGroupManifests()) {

			// Create group
			DefaultLayerGroup group = (DefaultLayerGroup) createLayerGroup(context, groupManifest);

			// Create layers
			for(LayerManifest layerManifest : groupManifest.getLayerManifests()) {
				LayerLinker linker = createLayer(layerManifest, group);

				group.addLayer(linker.getLayer());
				context.addLayer(linker.getLayer());

				linkers.add(linker);
			}

			// Finally set primary layer
			ItemLayerManifest primaryManifest = groupManifest.getPrimaryLayerManifest();
			if(primaryManifest!=null) {
				group.setPrimaryLayer((MarkableLayer) context.getLayer(primaryManifest.getId()));
			}
		}

		// Intermediate linking

		// Set context wide primary layer
		ItemLayerManifest primaryManifest = manifest.getPrimaryLayerManifest();
		if(primaryManifest!=null) {
			context.setPrimaryLayer((MarkableLayer) context.getLayer(primaryManifest.getId()));
		}

		// Second pass, link dependencies and attach groups
		// This task is delegated to the linker implementations
		for(LayerLinker linker : linkers) {
			linker.link();
		}

		return context;
	}

	protected LayerGroup createLayerGroup(Context context, LayerGroupManifest groupManifest) {
		return new DefaultLayerGroup(context, groupManifest);
	}

	//*********************************************
	//			LAYER CREATION
	//*********************************************

	/**
	 * Instantiates a layer according to the given {@link LayerManifest} and adds it to
	 * the supplied layer group.
	 *
	 * @param manifest
	 * @param group
	 * @return
	 */
	protected LayerLinker createLayer(LayerManifest manifest, LayerGroup group) {

		switch (manifest.getManifestType()) {
		case ANNOTATION_LAYER_MANIFEST:
			return createAnnotationLayer((AnnotationLayerManifest) manifest, group);

		case MARKABLE_LAYER_MANIFEST:
			return createMarkableLayer((ItemLayerManifest) manifest, group);

		case STRUCTURE_LAYER_MANIFEST:
			return createStructureLayer((StructureLayerManifest) manifest, group);

		case FRAGMENT_LAYER_MANIFEST:
			return createFragmentLayer((FragmentLayerManifest) manifest, group);

		default:
			throw new IllegalArgumentException("Unsupported manifest type for layer: "+manifest.getManifestType()); //$NON-NLS-1$
		}
	}

	protected LayerLinker createAnnotationLayer(AnnotationLayerManifest manifest, LayerGroup group) {
		boolean isComplex = manifest.isAllowUnknownKeys() || !manifest.getAvailableKeys().isEmpty();

		if(isComplex) {
			return new LayerLinker(new ComplexAnnotationLayer(manifest, group));
		} else {
			return new LayerLinker(new SimpleAnnotationLayer(manifest, group));
		}
	}

	protected LayerLinker createMarkableLayer(ItemLayerManifest manifest, LayerGroup group) {
		return new MarkableLayerLinker(new DefaultMarkableLayer(manifest, group));
	}

	protected LayerLinker createStructureLayer(StructureLayerManifest manifest, LayerGroup group) {
		return new MarkableLayerLinker(new DefaultStructureLayer(manifest, group));
	}

	protected LayerLinker createFragmentLayer(FragmentLayerManifest manifest, LayerGroup group) {
		return new FragmentLayerLinker(new DefaultFragmentLayer(manifest, group));
	}

	public static class LayerLinker {

		private final Layer layer;

		public LayerLinker(Layer layer) {
			if (layer == null)
				throw new NullPointerException("Invalid layer"); //$NON-NLS-1$
			this.layer = layer;
		}

		public final Layer getLayer() {
			return layer;
		}

		/**
		 * Callback to perform linking operations as soon as all layers are instantiated.
		 * The default implementation casts the internal layer to {@link AbstractLayer} and
		 * adds all necessary base layers. In addition it assumes that layer groups used for
		 * supplied layers (not those of foreign contexts!) will be of type {@link DefaultLayerGroup}.
		 */
		public void link() throws ModelException {
			LayerManifest layerManifest = layer.getManifest();

			// Link base layers
			List<TargetLayerManifest> targets = layerManifest.getBaseLayerManifests();
			if(!targets.isEmpty()) {

				List<MarkableLayer> buffer = new ArrayList<>(targets.size());

				for(TargetLayerManifest target : targets) {

					MarkableLayer targetLayer = resolveTargetLayer(target);

					// Add layer to base set
					buffer.add(targetLayer);

					// If foreign layer, add inter-group dependency
					DefaultLayerGroup group = (DefaultLayerGroup) layer.getLayerGroup();
					if(targetLayer.getLayerGroup()!=group) {
						group.addDependency(new Dependency<>(targetLayer.getLayerGroup(), DependencyType.STRONG));
					}
				}


				((AbstractLayer<?>)layer).setBaseLayers(MemberSets.createMemberSet(buffer));
			}
		}

		@SuppressWarnings("unchecked")
		public <L extends Layer> L resolveTargetLayer(TargetLayerManifest target) {
			LayerManifest targetManifest = target.getResolvedLayerManifest();
			ContextManifest targetContextManifest = targetManifest.getContextManifest();

			// IMPORTANT:
			// We cannot use the layer lookup provided by the corpus interface, since
			// the context we are currently creating has not yet been added to the corpus.
			// Therefore we need to check for each target layer, whether it is hosted in the
			// same context or accessible via a foreign one.
			Context targetContext = layer.getContext();
			if(targetContextManifest!=layer.getManifest().getContextManifest()) {
				// Foreign context, previously registered, so use corpus for lookup
				targetContext = layer.getCorpus().getContext(targetContextManifest.getId());
			}

			// Resolve layer instance
			return (L) targetContext.getLayer(targetManifest.getId());
		}
	}

	public static class MarkableLayerLinker extends LayerLinker {

		public MarkableLayerLinker(MarkableLayer layer) {
			super(layer);
		}

		/**
		 * First calls the {@code #link()} method of the super implementation. Then casts
		 * the internal layer to {@link DefaultMarkableLayer} and attaches a boundary layer if required.
		 *
		 * @see de.ims.icarus.model.standard.driver.ContextFactory.LayerLinker#link()
		 */
		@Override
		public void link() throws ModelException {
			// Allow regular base layer linking to perform as usual
			super.link();

			// Now resolve and add boundary layer if required

			// For markable layers (and derived versions) we need to link the optional
			// boundary layer in addition!
			DefaultMarkableLayer layer = (DefaultMarkableLayer) getLayer();
			DefaultLayerGroup group = (DefaultLayerGroup) layer.getLayerGroup();
			TargetLayerManifest target = layer.getManifest().getBoundaryLayerManifest();
			if(target!=null) {
				// Resolve layer instance
				MarkableLayer targetLayer = resolveTargetLayer(target);
				layer.setBoundaryLayer(targetLayer);

				// If foreign layer, add inter-group dependency
				if(targetLayer.getLayerGroup()!=group) {
					group.addDependency(new Dependency<>(targetLayer.getLayerGroup(), DependencyType.BOUNDARY));
				}
			}
		}

	}

	public static class FragmentLayerLinker extends LayerLinker {

		public FragmentLayerLinker(FragmentLayer layer) {
			super(layer);
		}

		/**
		 * First calls the {@code #link()} method of the super implementation. Then casts
		 * the internal layer to {@link DefaultFragmentLayer} and links the correct annotation
		 * layer used for value rasterization. In addition the rasterizer used for the layer
		 * is instantiated.
		 *
		 * @see de.ims.icarus.model.standard.driver.ContextFactory.LayerLinker#link()
		 */
		@Override
		public void link() throws ModelException {
			// Allow regular base layer linking to perform as usual
			super.link();

			// Now resolve and add value annotation layer

			DefaultFragmentLayer layer = (DefaultFragmentLayer) getLayer();
			DefaultLayerGroup group = (DefaultLayerGroup) layer.getLayerGroup();
			TargetLayerManifest target = layer.getManifest().getValueLayerManifest();
			if(target!=null) {
				// Resolve layer instance
				AnnotationLayer targetLayer = resolveTargetLayer(target);
				layer.setValueLayer(targetLayer);

				// If foreign layer, add inter-group dependency
				if(targetLayer.getLayerGroup()!=group) {
					group.addDependency(new Dependency<>(targetLayer.getLayerGroup(), DependencyType.VALUE));
				}
			}

			// Finally instantiate the rasterizer for the fragment layer
			RasterizerManifest rasterizerManifest = layer.getManifest().getRasterizerManifest();

			// No default implementation available, therefore notify with exception
			if(rasterizerManifest==null) {
				throw new ModelException(layer.getCorpus(), ModelError.IMPLEMENTATION_MISSING,
						"Missing rasterizer manifest for fragment layer: "+CorpusUtils.getName(layer)); //$NON-NLS-1$
			}

			try {
				// Instantiate rasterizer and assign to layer
				Rasterizer rasterizer = new ImplementationLoader().instantiate(
						rasterizerManifest.getImplementationManifest(), Rasterizer.class);
				layer.setRasterizer(rasterizer);
			} catch (ClassNotFoundException e) {
				throw new ModelException(layer.getCorpus(), ModelError.IMPLEMENTATION_NOT_FOUND,
						"Implementing rasterizer class for fragment layer not found: "+CorpusUtils.getName(layer), e); //$NON-NLS-1$
			} catch (IllegalAccessException | InstantiationException e) {
				throw new ModelException(layer.getCorpus(), ModelError.IMPLEMENTATION_NOT_ACCESSIBLE,
						"Implementing rasterizer class for fragment layer not accessible: "+CorpusUtils.getName(layer), e); //$NON-NLS-1$
			} catch (ClassCastException e) {
				throw new ModelException(layer.getCorpus(), ModelError.IMPLEMENTATION_INCOMPATIBLE,
						"Implementing rasterizer class for fragment layer incompatible: "+CorpusUtils.getName(layer), e); //$NON-NLS-1$
			}
		}

	}
}
