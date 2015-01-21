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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange;
import de.ims.icarus.model.api.layer.AnnotationLayer;
import de.ims.icarus.model.api.layer.LayerGroup;
import de.ims.icarus.model.api.manifest.AnnotationLayerManifest;
import de.ims.icarus.model.api.manifest.AnnotationManifest;
import de.ims.icarus.model.api.members.Container;
import de.ims.icarus.model.api.members.CorpusMember;
import de.ims.icarus.model.api.members.Item;
import de.ims.icarus.model.api.members.MemberType;
import de.ims.icarus.model.util.CorpusMemberUtils;
import de.ims.icarus.model.util.CorpusUtils;
import de.ims.icarus.util.Collector;
import de.ims.icarus.util.CorruptedStateException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SimpleAnnotationLayer extends AbstractLayer<AnnotationLayerManifest> implements AnnotationLayer {

	private final Map<Item, Object> annotations = new WeakHashMap<>();
	private final String key;
	private final AnnotationManifest annotationManifest;

	/**
	 * @param id
	 * @param context
	 * @param manifest
	 */
	public SimpleAnnotationLayer(AnnotationLayerManifest manifest, LayerGroup group) {
		super(manifest, group);

		if(manifest.getAvailableKeys().size()>1 || manifest.isAllowUnknownKeys())
			throw new IllegalArgumentException("This annotation layer implementation does not support more than one annotation key"); //$NON-NLS-1$

		key = manifest.getDefaultKey();

		if(key==null)
			throw new IllegalArgumentException("Missing sole annotation key"); //$NON-NLS-1$

		annotationManifest = manifest.getAnnotationManifest(key);

		if(annotationManifest==null)
			throw new IllegalArgumentException("Missing annotation manifest for sole key: "+key); //$NON-NLS-1$
	}

	protected Map<Item, Object> getDefaultAnnotations() {
		return annotations;
	}

	private void checkKey(String key) {
		if (key == null)
			throw new NullPointerException("Invalid key"); //$NON-NLS-1$
		if(!this.key.equals(key))
			throw new IllegalArgumentException("Unsupported key: "+key); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.model.api.layer.AnnotationLayer#hasAnnotations()
	 */
	@Override
	public boolean hasAnnotations() {
		return !annotations.isEmpty();
	}

//	/**
//	 * @see de.ims.icarus.model.api.layer.AnnotationLayer#getValue(de.ims.icarus.model.api.Markable)
//	 */
//	@Override
//	public Object getValue(Item markable) {
//		return annotations.get(markable);
//	}

	/**
	 * @see de.ims.icarus.model.api.layer.AnnotationLayer#getValue(de.ims.icarus.model.api.members.Item, java.lang.String)
	 */
	@Override
	public Object getValue(Item item, String key) {
		checkKey(key);

		return annotations.get(item);
	}

	/**
	 * @see de.ims.icarus.model.api.layer.AnnotationLayer#collectKeys(de.ims.icarus.model.api.members.Item, de.ims.icarus.util.Collector)
	 */
	@Override
	public boolean collectKeys(Item item, Collector<String> buffer) {
		buffer.collect(key);
		return true;
	}

	/**
	 * @see de.ims.icarus.model.api.layer.AnnotationLayer#removeAllValues()
	 */
	@Override
	public void removeAllValues() {
		execute(new ClearChange());
	}

	/**
	 * @see de.ims.icarus.model.api.layer.AnnotationLayer#removeAllValues(java.lang.String)
	 */
	@Override
	public void removeAllValues(String key) {
		checkKey(key);

		execute(new ClearChange());
	}

	/**
	 * @see de.ims.icarus.model.api.layer.AnnotationLayer#removeAllValues(de.ims.icarus.model.api.members.Item, boolean)
	 */
	@Override
	public void removeAllValues(Item item, boolean recursive) {
		if (item == null)
			throw new NullPointerException("Invalid markable"); //$NON-NLS-1$

		if(getBaseLayers().contains(item.getLayer()) && !getManifest().isDeepAnnotation())
			throw new IllegalArgumentException("Item '"+item+"' is not a member of this layer's base layer"); //$NON-NLS-1$ //$NON-NLS-2$

		List<Item> buffer = new ArrayList<>();

		if(annotations.get(item)!=null) {
			buffer.add(item);
		}

		if(recursive && (item.getMemberType()==MemberType.STRUCTURE
				|| item.getMemberType()==MemberType.CONTAINER)) {
			collectAnnotatedItems((Container) item, buffer);
		}

		if(buffer.isEmpty()) {
			return;
		}

		Item[] markables = new Item[buffer.size()];
		buffer.toArray(markables);

		execute(new AnnotationChange(markables));
	}

	private void collectAnnotatedItems(Container container, List<Item> buffer) {
		int size = container.getMarkableCount();
		for(int i=0; i<size; i++) {
			Item item = container.getItemAt(i);

			if(annotations.get(item)!=null) {
				buffer.add(item);
			}

			if(item.getMemberType()==MemberType.STRUCTURE
					|| item.getMemberType()==MemberType.CONTAINER) {
				collectAnnotatedItems((Container) item, buffer);
			}
		}
	}

//	/**
//	 * @see de.ims.icarus.model.api.layer.AnnotationLayer#setValue(de.ims.icarus.model.api.Markable, java.lang.Object)
//	 */
//	@Override
//	public void setValue(Item markable, Object value) {
//		if (markable == null)
//			throw new NullPointerException("Invalid markable"); //$NON-NLS-1$
//
//		if(getBaseLayers().contains(markable.getLayer()) && !getManifest().isDeepAnnotation())
//			throw new IllegalArgumentException("Item '"+markable+"' is not a member of this layer's base layer"); //$NON-NLS-1$ //$NON-NLS-2$
//
//		if(value!=null && !annotationManifest.getValueType().isValidValue(value))
//			throw new IllegalArgumentException("Invalid annotation value: "+value); //$NON-NLS-1$
//
//		execute(new AnnotationChange(markable, value));
//	}

	/**
	 * Directly saves a given annotation value, bypassing most of the default
	 * sanity checks of the {@link #setValue(Item, Object)} method.
	 *
	 * @param item
	 * @param value
	 */
	public void putValue(Item item, Object value) {
		if (item == null)
			throw new NullPointerException("Invalid markable"); //$NON-NLS-1$
		if (value == null)
			throw new NullPointerException("Invalid value"); //$NON-NLS-1$

		annotations.put(item, value);
	}

	/**
	 * @see de.ims.icarus.model.api.layer.AnnotationLayer#setValue(de.ims.icarus.model.api.members.Item, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setValue(Item item, String key, Object value) {
		if (item == null)
			throw new NullPointerException("Invalid markable"); //$NON-NLS-1$
		checkKey(key);

		if(getBaseLayers().contains(item.getLayer()) && !getManifest().isDeepAnnotation())
			throw new IllegalArgumentException("Item '"+item+"' is not a member of this layer's base layer"); //$NON-NLS-1$ //$NON-NLS-2$

		if(!CorpusUtils.isValidValue(value, annotationManifest))
			throw new IllegalArgumentException("Invalid annotation value: "+value); //$NON-NLS-1$

		execute(new AnnotationChange(item, value));
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

		private Item[] items = null;
		private Object[] values = null;

		private int expectedSize = annotations.size();

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			int size = annotations.size();
			if(expectedSize!=size)
				throw new CorruptedStateException(CorpusMemberUtils.sizeMismatchMessage(
						"Clear failed", expectedSize, annotations.size())); //$NON-NLS-1$

			if(items==null) {
				items = new Item[size];
				values = new Object[size];

				Iterator<Entry<Item, Object>> it = annotations.entrySet().iterator();
				for(int i=0; i<size; i++) {
					Entry<Item, Object> entry = it.next();
					items[i] = entry.getKey();
					values[i] = entry.getValue();
				}

				annotations.clear();
			} else {
				for(int i=0; i<size; i++) {
					annotations.put(items[i], values[i]);
				}

				items = null;
				values = null;
			}

			expectedSize = annotations.size();
		}

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return SimpleAnnotationLayer.this;
		}

	}

	private class AnnotationChange implements AtomicChange {

		private final Item[] items;
		private final Object[] values;

		private int expectedSize = annotations.size();

		private AnnotationChange(Item[] markables, Object[] values) {
			if(markables.length!=values.length)
				throw new IllegalArgumentException("Size mismatch between items and values array"); //$NON-NLS-1$

			this.items = markables;
			this.values = values;
		}

		private AnnotationChange(Item item, Object value) {
			items = new Item[]{item};
			values = new Object[]{value};
		}

		private AnnotationChange(Item[] markables) {
			this.items = markables;
			this.values = new Object[markables.length];
		}

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			int size = annotations.size();
			if(expectedSize!=size)
				throw new CorruptedStateException(CorpusMemberUtils.sizeMismatchMessage(
						"Clear failed", expectedSize, annotations.size())); //$NON-NLS-1$

			for(int i=items.length-1; i>-1; i--) {
				Item item = items[i];
				Object value = values[i];
				Object current = annotations.get(item);
				annotations.put(item, value);

				values[i] = current;
			}

			expectedSize = annotations.size();
		}

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return SimpleAnnotationLayer.this;
		}

	}
}
