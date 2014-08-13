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
package de.ims.icarus.model.standard.layer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;

import de.ims.icarus.model.api.Container;
import de.ims.icarus.model.api.CorpusMember;
import de.ims.icarus.model.api.Edge;
import de.ims.icarus.model.api.Markable;
import de.ims.icarus.model.api.MemberType;
import de.ims.icarus.model.api.Structure;
import de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange;
import de.ims.icarus.model.api.layer.AnnotationLayer;
import de.ims.icarus.model.api.layer.LayerGroup;
import de.ims.icarus.model.api.layer.MarkableLayer;
import de.ims.icarus.model.api.manifest.AnnotationLayerManifest;
import de.ims.icarus.model.util.CorpusMemberUtils;
import de.ims.icarus.model.util.CorpusUtils;
import de.ims.icarus.util.Collector;
import de.ims.icarus.util.CorruptedStateException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ComplexAnnotationLayer extends AbstractLayer<AnnotationLayerManifest> implements AnnotationLayer {

	// Lookup for annotations involving markables
	private final Map<Markable, AnnotationBundle> keyAnnotations = new WeakHashMap<>();

	// Factory for creating annotation bundles (constructor assigns default implementation)
	private BundleFactory bundleFactory;

	public static BundleFactory defaultCreateBundleFactory(AnnotationLayerManifest manifest) {
		if (manifest == null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$

		final int keyCount = manifest.getAvailableKeys().size();

		//TODO threshold regarding keyCount is subject to further evaluation!
		if(manifest.isAllowUnknownKeys() || keyCount>=10) {
			return new BundleFactory() {

				@Override
				public AnnotationBundle createBundle(Markable markable,
						AnnotationLayer layer) {
					return new LargeAnnotationBundle();
				}
			};
		} else {
			return new BundleFactory() {

				@Override
				public AnnotationBundle createBundle(Markable markable,
						AnnotationLayer layer) {
					return new CompactAnnotationBundle(keyCount);
				}
			};
		}
	}

	public ComplexAnnotationLayer(AnnotationLayerManifest manifest, LayerGroup group) {
		super(manifest, group);

		bundleFactory = defaultCreateBundleFactory(manifest);
	}

	/**
	 * @return the bundleFactory
	 */
	public BundleFactory getBundleFactory() {
		return bundleFactory;
	}

	/**
	 * @param bundleFactory the bundleFactory to set
	 */
	public void setBundleFactory(BundleFactory bundleFactory) {
		if (bundleFactory == null)
			throw new NullPointerException("Invalid bundleFactory"); //$NON-NLS-1$

		this.bundleFactory = bundleFactory;
	}

	private void checkKey(String key) {
		if (key == null)
			throw new NullPointerException("Invalid key"); //$NON-NLS-1$
		AnnotationLayerManifest manifest = getManifest();
		if(!manifest.isAllowUnknownKeys()
				&& !manifest.getAvailableKeys().contains(key))
			throw new IllegalArgumentException("Key not allowed in annotation layer: "+key); //$NON-NLS-1$
	}

	private void checkMarkable(Markable markable) {
		if (markable == null)
			throw new NullPointerException("Invalid markable"); //$NON-NLS-1$

		MarkableLayer layer = markable.getLayer();
		if(!getBaseLayers().contains(layer))
			throw new IllegalArgumentException("Host layer of markable "+markable+" is not a valid base layer of this annotation layer"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private AnnotationBundle getAnnotations(Markable markable, boolean createIfMissing) {
		AnnotationBundle annotations = keyAnnotations.get(markable);
		if(annotations==null && createIfMissing) {
			synchronized (keyAnnotations) {
				if((annotations = keyAnnotations.get(markable))==null) {
					annotations = bundleFactory.createBundle(markable, this);
					keyAnnotations.put(markable, annotations);
				}
			}
		}

		return annotations;
	}

	/**
	 * @see de.ims.icarus.model.api.SimpleAnnotationLayer.layer.DefaultAnnotationLayer#hasAnnotations()
	 */
	@Override
	public boolean hasAnnotations() {

		return !keyAnnotations.isEmpty();
	}

	/**
	 * @see de.ims.icarus.model.api.SimpleAnnotationLayer.layer.DefaultAnnotationLayer#getValue(de.ims.icarus.model.api.Markable, java.lang.String)
	 */
	@Override
	public Object getValue(Markable markable, String key) {
		checkMarkable(markable);
		checkKey(key);

		AnnotationBundle annotations = getAnnotations(markable, false);
		return annotations==null ? null : annotations.getValue(key);
	}

	/**
	 * @see de.ims.icarus.model.api.SimpleAnnotationLayer.layer.DefaultAnnotationLayer#removeAllValues()
	 */
	@Override
	public void removeAllValues() {
		if(!hasAnnotations()) {
			return;
		}

		execute(new ClearChange());
	}

	/**
	 * @see de.ims.icarus.model.api.SimpleAnnotationLayer.layer.DefaultAnnotationLayer#removeAllValues(java.lang.String)
	 */
	@Override
	public void removeAllValues(String key) {
		checkKey(key);

		if(keyAnnotations.isEmpty()) {
			return;
		}

		execute(new KeyAnnotationChange(key));
	}

	/**
	 * @see de.ims.icarus.model.api.SimpleAnnotationLayer.layer.DefaultAnnotationLayer#removeAllValues(de.ims.icarus.model.api.Markable, boolean)
	 */
	@Override
	public void removeAllValues(Markable markable, boolean recursive) {
		checkMarkable(markable);

		List<AnnotationBuffer> buffers = new ArrayList<>();

		AnnotationBuffer buffer = collectAnnotations(markable);
		if(buffer!=null) {
			// Ensure the original changes get preserved as well
			buffers.add(buffer);
		}

		if(recursive && getManifest().isDeepAnnotation()) {

			Set<Markable> lut = new HashSet<>();

			collectAnnotatedMembers(markable, lut, buffers);
		}

		execute(new BatchAnnotationChange(buffers));
	}

	private AnnotationBuffer collectAnnotations(Markable markable) {
		AnnotationBundle bundle = getAnnotations(markable, false);

		return bundle==null ? null : new AnnotationBuffer(markable);
	}

	private void collectAnnotatedMembers(Markable markable, Set<Markable> lut,
			List<AnnotationBuffer> buffers) {

		// Collect edge annotations
		//
		// Note: No need to bother with the terminals of edges, since they
		// will be processed later in this method and each structure must always
		// hold all terminals of all its edges!
		//
		// Note further, that the virtual root node of a structure is not allowed
		// to have annotations assigned to it!
		if(markable.getMemberType()==MemberType.STRUCTURE) {
			Structure structure = (Structure) markable;

			for(int i=0; i<structure.getEdgeCount(); i++) {
				Edge edge = structure.getEdgeAt(i);

				if(!lut.add(edge)) {
					continue;
				}

				AnnotationBuffer buffer = collectAnnotations(edge);
				if(buffer!=null) {
					buffers.add(buffer);
				}
			}
		}

		// Collect regular members of the container or structure
		//
		// Note: Containers or structures might hold edges as regular markables or
		// nodes, but the terminals of those edges are only of interest in case they
		// are available as regular members of the current markable tree!
		if(markable.getMemberType()==MemberType.CONTAINER
				|| markable.getMemberType()==MemberType.STRUCTURE) {

			Container container = (Container) markable;

			for(int i=0; i<container.getMarkableCount(); i++) {
				Markable member = container.getMarkableAt(i);

				if(!lut.add(member)) {
					continue;
				}


				AnnotationBuffer buffer = collectAnnotations(member);
				if(buffer!=null) {
					buffers.add(buffer);
				}

				// Recursively process containers
				collectAnnotatedMembers(member, lut, buffers);
			}
		}
	}

	/**
	 * @see de.ims.icarus.model.api.SimpleAnnotationLayer.layer.DefaultAnnotationLayer#setValue(de.ims.icarus.model.api.Markable, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setValue(Markable markable, String key, Object value) {
		checkMarkable(markable);
		checkKey(key);

		execute(new KeyAnnotationChange(key, markable, value));
	}

//	/**
//	 * @see de.ims.icarus.model.api.layer.AnnotationLayer#getValue(de.ims.icarus.model.api.Markable)
//	 */
//	@Override
//	public Object getValue(Markable markable) {
//		if(!hasDefaultKey)
//			throw new IllegalStateException("No default key defined");
//
//		return getValue(markable, defaultKey);
//	}

	/**
	 * @see de.ims.icarus.model.api.layer.AnnotationLayer#collectKeys(de.ims.icarus.model.api.Markable, de.ims.icarus.util.Collector)
	 */
	@Override
	public boolean collectKeys(Markable markable, Collector<String> buffer) {
		AnnotationBundle bundle = getAnnotations(markable, false);
		if(bundle!=null) {
			bundle.collectKeys(buffer);
		}

		return bundle!=null;
	}

//	/**
//	 * @see de.ims.icarus.model.api.layer.AnnotationLayer#setValue(de.ims.icarus.model.api.Markable, java.lang.Object)
//	 */
//	@Override
//	public void setValue(Markable markable, Object value) {
//		if(!hasDefaultKey)
//			throw new IllegalStateException("No default key defined"); //$NON-NLS-1$
//
//		setValue(markable, defaultKey, value);
//	}

	protected void setValue0(Markable markable, String key, Object value) {
		getAnnotations(markable, true).setValue(key, value);
	}

	/**
	 * Helper method to check whether or not the enclosing corpus is editable
	 * and to forward an atomic change to the edit model.
	 *
	 * @param change
	 * @throws UnsupportedOperationException if the corpus is not editable
	 */
	protected void execute(AtomicChange change) {
		CorpusUtils.dispatchChange(this, change);
	}

	private class ClearChange implements AtomicChange {

		private int expectedBundleCount = keyAnnotations.size();

		private Map<Markable, AnnotationBundle> bundles = new HashMap<>();

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			int bundleCount = keyAnnotations.size();
			if(expectedBundleCount!=bundleCount)
				throw new CorruptedStateException(CorpusMemberUtils.sizeMismatchMessage(
						"Clear failed (bundle count)", expectedBundleCount, bundleCount)); //$NON-NLS-1$

			// Process keyed annotations
			if(bundles.isEmpty()) {
				bundles.putAll(keyAnnotations);
				keyAnnotations.clear();
			} else {
				keyAnnotations.putAll(bundles);
				bundles.clear();
			}

			expectedBundleCount = keyAnnotations.size();
		}

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return ComplexAnnotationLayer.this;
		}

	}

	private class KeyAnnotationChange implements AtomicChange {

		private final String annotationKey;
		private final Markable[] markables;
		private final Object[] values;

		/**
		 * Models a set of changes to annotation entries
		 */
		private KeyAnnotationChange(String annotationKey, Markable[] markables, Object[] values) {
			if(markables.length!=values.length)
				throw new IllegalArgumentException("Size mismatch between markables ad values array"); //$NON-NLS-1$

			this.annotationKey = annotationKey;
			this.markables = markables;
			this.values = values;
		}

		/**
		 * Models the change of a single annotation entry
		 */
		private KeyAnnotationChange(String annotationKey, Markable key, Object value) {
			this.annotationKey = annotationKey;
			markables = new Markable[]{key};
			values = new Object[]{value};
		}

		/**
		 * Models removal of a subset of annotations for a single
		 * annotation key
		 */
		private KeyAnnotationChange(String annotationKey, Markable[] markables) {
			this.annotationKey = annotationKey;
			this.markables = markables;
			this.values = new Object[markables.length];
		}

		/**
		 * Models the removal of an entire annotation key
		 */
		private KeyAnnotationChange(String annotationKey) {
			this.annotationKey = annotationKey;

			List<Markable> m = new ArrayList<>();

			for(Entry<Markable, AnnotationBundle> entry : keyAnnotations.entrySet()) {
				if(entry.getValue().getValue(annotationKey)!=null) {
					m.add(entry.getKey());
				}
			}

			int size = m.size();
			markables = new Markable[size];
			values = new Object[size];

			m.toArray(markables);
		}

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {

			for(int i=markables.length-1; i>-1; i--) {
				Markable markable = markables[i];

				AnnotationBundle bundle = getAnnotations(markable, true);
				Object value = bundle.getValue(annotationKey);
				bundle.setValue(annotationKey, values[i]);

				values[i] = value;
			}
		}

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return ComplexAnnotationLayer.this;
		}

	}

	private class BatchAnnotationChange implements AtomicChange {

		private final List<AnnotationBuffer> buffers;

		/**
		 * Models a set of changes to annotation entries
		 */
		private BatchAnnotationChange(List<AnnotationBuffer> buffers) {
			if (buffers == null)
				throw new NullPointerException("Invalid buffers"); //$NON-NLS-1$

			this.buffers = buffers;
		}

		/**
		 * Models removal of a subset of annotations for a single
		 * annotation key
		 */
		private BatchAnnotationChange(AnnotationBuffer buffer) {
			if (buffer == null)
				throw new NullPointerException("Invalid buffer"); //$NON-NLS-1$

			this.buffers = Collections.singletonList(buffer);
		}

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {

			for(AnnotationBuffer buffer : buffers) {
				Markable markable = buffer.markable;

				// Now handle mapped annotations
				AnnotationBundle bundle = keyAnnotations.get(markable);
				Object[] map = buffer.map;
				if(map!=null) {
					for(int i=0; i<map.length-1; i+=2) {
						String key = (String) map[i];
						Object value = map[i+1];

						map[i+1] = bundle.getValue(key);
						bundle.setValue(key, value);
					}
				}
			}
		}

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return ComplexAnnotationLayer.this;
		}

	}

	private static class AnnotationBuffer {

		private final Markable markable;

		private Object[] map;

		private AnnotationBuffer(Markable markable) {
			this.markable = markable;
		}
	}

	public interface BundleFactory {
		AnnotationBundle createBundle(Markable markable, AnnotationLayer layer);
	}

	/**
	 * Models a set of key-value pairs describing annotations for a
	 * single markable.
	 *
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public interface AnnotationBundle {
		/**
		 * Fetches the mapped annotation value for the specified {@code key} or
		 * {@code null} if no value is stored for theat {@code key}.
		 *
		 * @param key
		 * @return
		 */
		Object getValue(String key);

		/**
		 * Maps the given {@code value} (allowed to be {@code null}) to
		 * the specified {@code key}.
		 *
		 * @param key
		 * @param value
		 * @return {@code true} iff the content of this bundle was changed by
		 * this method (i.e. there either was no mapping for {@code key} prior
		 * to calling this method or the previously mapped value did not equal the
		 * new {@code value} parameter).
		 */
		boolean setValue(String key, Object value);

		/**
		 * Collects and sends all the currently used keys in this
		 * bundle to the external {@code buffer} collection.
		 *
		 * @param buffer
		 */
		void collectKeys(Collector<String> buffer);

		/**
		 * Collects the keys and values in this bundle and sends them
		 * to the external {@code buffer} map.
		 *
		 * @param buffer
		 */
		void collect(Map<String, Object> buffer);
	}

	public static class LargeAnnotationBundle extends HashMap<String, Object> implements AnnotationBundle {

		private static final long serialVersionUID = -3058615796981616593L;

		/**
		 * @see de.ims.icarus.model.standard.layer.ComplexAnnotationLayer.AnnotationBundle#getValue(java.lang.String)
		 */
		@Override
		public Object getValue(String key) {
			return get(key);
		}

		/**
		 * @see de.ims.icarus.model.standard.layer.ComplexAnnotationLayer.AnnotationBundle#setValue(java.lang.String, java.lang.Object)
		 */
		@Override
		public boolean setValue(String key, Object value) {
			put(key, value);
			return true;
		}

		/**
		 * @see de.ims.icarus.model.standard.layer.ComplexAnnotationLayer.AnnotationBundle#collectKeys(java.util.Collection)
		 */
		@Override
		public void collectKeys(Collector<String> buffer) {
			for(String key : keySet()) {
				buffer.collect(key);
			}
		}

		/**
		 * @see de.ims.icarus.model.standard.layer.ComplexAnnotationLayer.AnnotationBundle#collect(java.util.Map)
		 */
		@Override
		public void collect(Map<String, Object> buffer) {
			buffer.putAll(this);
		}

	}

	public static class CompactAnnotationBundle implements AnnotationBundle {

		public static final int DEFAULT_CAPACITY = 6;

		private final Object[] data;

		public CompactAnnotationBundle() {
			this(DEFAULT_CAPACITY);
		}

		/**
		 * Creates a new compact bundle with initial storage for a number
		 * of entries equal to the {@code capacity} parameter.
		 */
		public CompactAnnotationBundle(int capacity) {
			data = new Object[capacity*2];
		}

		/**
		 * @see de.ims.icarus.model.standard.layer.ComplexAnnotationLayer.AnnotationBundle#getValue(java.lang.String)
		 */
		@Override
		public Object getValue(String key) {
			for(int i=0; i<data.length-1; i+=2) {
				if(data[i]!=null && data[i].equals(key)) {
					return data[i+1];
				}
			}

			return null;
		}

		/**
		 * @see de.ims.icarus.model.standard.layer.ComplexAnnotationLayer.AnnotationBundle#setValue(java.lang.String, java.lang.Object)
		 */
		@Override
		public boolean setValue(String key, Object value) {
			for(int i=0; i<data.length-1; i+=2) {
				if(data[i]==null || data[i].equals(key)) {
					data[i] = key;
					data[i+1] = value;
					return true;
				}
			}

			return false;
		}

		/**
		 * @see de.ims.icarus.model.standard.layer.ComplexAnnotationLayer.AnnotationBundle#collectKeys(java.util.Collection)
		 */
		@Override
		public void collectKeys(Collector<String> buffer) {
			for(int i=0; i<data.length-1; i+=2) {
				if(data[i]!=null) {
					buffer.collect((String) data[i]);
				}
			}
		}

		/**
		 * @see de.ims.icarus.model.standard.layer.ComplexAnnotationLayer.AnnotationBundle#collect(java.util.Map)
		 */
		@Override
		public void collect(Map<String, Object> buffer) {
			for(int i=0; i<data.length-1; i+=2) {
				if(data[i]!=null) {
					buffer.put((String) data[i], data[i+1]);
				}
			}
		}

	}
}