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
package de.ims.icarus.language.model.standard.layer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.ims.icarus.language.model.api.Container;
import de.ims.icarus.language.model.api.Context;
import de.ims.icarus.language.model.api.CorpusMember;
import de.ims.icarus.language.model.api.Markable;
import de.ims.icarus.language.model.api.MemberType;
import de.ims.icarus.language.model.api.edit.UndoableCorpusEdit.AtomicChange;
import de.ims.icarus.language.model.api.layer.MarkableLayer;
import de.ims.icarus.language.model.api.manifest.AnnotationLayerManifest;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.collections.LongHashMap;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ComplexAnnotationLayer extends DefaultAnnotationLayer {

	private final Map<String, LongHashMap<Object>> keyAnnotations = new HashMap<>();

	/**
	 * @param id
	 * @param context
	 * @param manifest
	 */
	public ComplexAnnotationLayer(long id, Context context,
			AnnotationLayerManifest manifest) {
		super(id, context, manifest);
	}

	private void checkKey(String key) {
		if (key == null)
			throw new NullPointerException("Invalid annotationKey"); //$NON-NLS-1$
		AnnotationLayerManifest manifest = getManifest();
		if(!manifest.getAvailableKeys().contains(key)
				&& ! manifest.allowUnknownKeys())
			throw new IllegalArgumentException("Key not allowed in annotation layer: "+key); //$NON-NLS-1$
	}

	private LongHashMap<Object> getAnnotations(String key) {
		LongHashMap<Object> annotations = keyAnnotations.get(key);
		if(annotations==null) {
			annotations = new LongHashMap<>();
			keyAnnotations.put(key, annotations);
		}

		return annotations;
	}

	/**
	 * @see de.ims.icarus.language.model.api.standard.layer.DefaultAnnotationLayer#hasAnnotations()
	 */
	@Override
	public boolean hasAnnotations() {
		if(super.hasAnnotations()) {
			return true;
		}

		for(LongHashMap<Object> annotations : keyAnnotations.values()) {
			if(!annotations.isEmpty()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @see de.ims.icarus.language.model.api.standard.layer.DefaultAnnotationLayer#getValue(de.ims.icarus.language.model.api.Markable, java.lang.String)
	 */
	@Override
	public Object getValue(Markable markable, String key) {
		if (markable == null)
			throw new NullPointerException("Invalid markable"); //$NON-NLS-1$
		checkKey(key);

		LongHashMap<Object> annotations = keyAnnotations.get(key);
		return annotations==null ? null : annotations.get(markable.getId());
	}

	/**
	 * @see de.ims.icarus.language.model.api.standard.layer.DefaultAnnotationLayer#removeAllValues()
	 */
	@Override
	public void removeAllValues() {
		if(!hasAnnotations()) {
			return;
		}

		execute(new ClearChange());
	}

	/**
	 * @see de.ims.icarus.language.model.api.standard.layer.DefaultAnnotationLayer#removeAllValues(java.lang.String)
	 */
	@Override
	public void removeAllValues(String key) {
		checkKey(key);

		LongHashMap<Object> annotations = keyAnnotations.get(key);

		if(annotations==null) {
			return;
		}

		execute(new KeyAnnotationChange(key));
	}

	/**
	 * @see de.ims.icarus.language.model.api.standard.layer.DefaultAnnotationLayer#removeAllValues(de.ims.icarus.language.model.api.Markable, boolean)
	 */
	@Override
	public void removeAllValues(Markable markable, boolean recursive) {
		if (markable == null)
			throw new NullPointerException("Invalid markable"); //$NON-NLS-1$

		boolean deepAnnotation = getManifest().isDeepAnnotation();

		MarkableLayer markableLayer = markable.getLayer();
		if(markableLayer!=getBaseLayer() && !deepAnnotation)
			throw new IllegalArgumentException("Markable '"+markable+"' is not a member of this layer's base layer"); //$NON-NLS-1$ //$NON-NLS-2$

		List<Markable> buffer = new ArrayList<>();
		List<String> annotationKeys = new ArrayList<>();

		LongHashMap<?>[] annoMap = new LongHashMap<?>[keyAnnotations.size()];
		String[] annoKeys = new String[keyAnnotations.size()];

		int annoIdx = 0;
		for(Entry<String, LongHashMap<Object>> entry : keyAnnotations.entrySet()) {
			annoKeys[annoIdx] = entry.getKey();
			annoMap[annoIdx] = entry.getValue();
		}

		if(getDefaultAnnotations().get(markable.getId())!=null) {
			buffer.add(markable);
			annotationKeys.add(null);
		}

		if(recursive && annoMap.length>0 && (markable.getMemberType()==MemberType.STRUCTURE
				|| markable.getMemberType()==MemberType.CONTAINER)) {
			collectAnnotatedMarkables((Container) markable, buffer, annotationKeys, annoMap, annoKeys);
		}

		if(buffer.isEmpty()) {
			return;
		}

		int size = buffer.size();
		long[] keys = new long[size];

		for(int i=0; i<size; i++) {
			keys[i] = buffer.get(i).getId();
		}

		execute(new BatchAnnotationChange(annotationKeys.toArray(new String[size]), keys));
	}

	private void collectAnnotatedMarkables(Container container, List<Markable> markableBuffer,
			List<String> keyBuffer, LongHashMap<?>[] maps, String[] keys) {
		int size = container.getMarkableCount();
		for(int i=0; i<size; i++) {
			Markable markable = container.getMarkableAt(i);

			for(int j=0; j<maps.length; j++) {
				LongHashMap<?> annotations = maps[j];
				if(annotations.get(markable.getId())!=null) {
					markableBuffer.add(markable);
					keyBuffer.add(keys[j]);
				}
			}

			if(markable.getMemberType()==MemberType.STRUCTURE
					|| markable.getMemberType()==MemberType.CONTAINER) {
				collectAnnotatedMarkables((Container) markable,
						markableBuffer, keyBuffer, maps, keys);
			}
		}
	}

	/**
	 * @see de.ims.icarus.language.model.api.standard.layer.DefaultAnnotationLayer#setValue(de.ims.icarus.language.model.api.Markable, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setValue(Markable markable, String key, Object value) {
		if (markable == null)
			throw new NullPointerException("Invalid markable"); //$NON-NLS-1$

		checkKey(key);

		execute(new KeyAnnotationChange(key, markable.getId(), value));
	}

	protected void setValue0(Markable markable, String key, Object value) {
		getAnnotations(key).put(markable.getId(), value);
	}

	private class ClearChange implements AtomicChange {

		private final List<long[]> keyList = new ArrayList<>();
		private final List<String> annotationKeyList = new ArrayList<>();
		private final List<Object[]> valueList = new ArrayList<>();

		/**
		 * @see de.ims.icarus.language.model.api.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			if(keyList.isEmpty()) {

				LongHashMap<Object> annotations = getDefaultAnnotations();

				if(!annotations.isEmpty()) {
					int size = annotations.size();
					long[] keys = new long[size];
					Object[] values = new Object[size];

					annotations.save(keys, values);
					annotations.clear();

					keyList.add(keys);
					annotationKeyList.add(null);
					valueList.add(values);
				}

				for(Entry<String, LongHashMap<Object>> entry : keyAnnotations.entrySet()) {
					String anootationKey = entry.getKey();
					annotations = entry.getValue();

					if(annotations.isEmpty()) {
						continue;
					}

					int size = annotations.size();
					long[] keys = new long[size];
					Object[] values = new Object[size];

					annotations.save(keys, values);
					annotations.clear();

					keyList.add(keys);
					annotationKeyList.add(anootationKey);
					valueList.add(values);
				}

			} else {

				for(int i=keyList.size()-1; i>-1; i--) {
					String annotationKey = annotationKeyList.get(i);
					long[] keys = keyList.get(i);
					Object[] values = valueList.get(i);

					LongHashMap<Object> annotations = annotationKey==null ?
							getDefaultAnnotations() : keyAnnotations.get(annotationKey);

					if(annotations==null)
						throw new CorruptedStateException("Missing annotation storage for key: "+annotationKey); //$NON-NLS-1$

					annotations.load(keys, values);
				}

				keyList.clear();
				annotationKeyList.clear();
				valueList.clear();
			}
		}

		/**
		 * @see de.ims.icarus.language.model.api.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return ComplexAnnotationLayer.this;
		}

	}

	private class KeyAnnotationChange implements AtomicChange {

		private final String annotationKey;
		private final long[] keys;
		private final Object[] values;

		/**
		 * Models a set of changes to annotation entries
		 */
		private KeyAnnotationChange(String annotationKey, long[] keys, Object[] values) {
			if(keys.length!=values.length)
				throw new IllegalArgumentException("Size mismatch between keys ad values array"); //$NON-NLS-1$

			this.annotationKey = annotationKey;
			this.keys = keys;
			this.values = values;
		}

		/**
		 * Models the change of a single annotation entry
		 */
		private KeyAnnotationChange(String annotationKey, long key, Object value) {
			this.annotationKey = annotationKey;
			keys = new long[]{key};
			values = new Object[]{value};
		}

		/**
		 * Models removal of a subset of annotations for a single
		 * annotation key
		 */
		private KeyAnnotationChange(String annotationKey, long[] keys) {
			this.annotationKey = annotationKey;
			this.keys = keys;
			this.values = new Object[keys.length];
		}

		/**
		 * Models the removal of an entire annotation key
		 */
		private KeyAnnotationChange(String annotationKey) {
			this.annotationKey = annotationKey;

			LongHashMap<Object> annotations = keyAnnotations.get(annotationKey);
			if(annotations==null)
				throw new IllegalArgumentException("No annotations for key: "+annotationKey); //$NON-NLS-1$

			int size = annotations.size();
			keys = new long[size];
			values = new Object[size];

			annotations.save(keys, values);
			Arrays.fill(values, null);
		}

		/**
		 * @see de.ims.icarus.language.model.api.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			LongHashMap<Object> annotations = getAnnotations(annotationKey);

			for(int i=keys.length-1; i>-1; i--) {
				long key = keys[i];
				Object value = values[i];
				Object current = annotations.get(key);
				annotations.put(key, value);

				values[i] = current;
			}
		}

		/**
		 * @see de.ims.icarus.language.model.api.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return ComplexAnnotationLayer.this;
		}

	}

	private class BatchAnnotationChange implements AtomicChange {

		private final String[] annotationKeys;
		private final long[] keys;
		private final Object[] values;

		/**
		 * Models a set of changes to annotation entries
		 */
		private BatchAnnotationChange(String[] annotationKeys, long[] keys, Object[] values) {
			if(keys.length!=values.length || annotationKeys.length!=keys.length)
				throw new IllegalArgumentException("Size mismatch in input arrays"); //$NON-NLS-1$

			this.annotationKeys = annotationKeys;
			this.keys = keys;
			this.values = values;
		}

		/**
		 * Models removal of a subset of annotations for a single
		 * annotation key
		 */
		private BatchAnnotationChange(String[] annotationKeys, long[] keys) {
			if(annotationKeys.length!=keys.length)
				throw new IllegalArgumentException("Size mismatch in input arrays"); //$NON-NLS-1$

			this.annotationKeys = annotationKeys;
			this.keys = keys;
			this.values = new Object[keys.length];
		}

		/**
		 * @see de.ims.icarus.language.model.api.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			for(int i=keys.length-1; i>-1; i--) {
				String annotationKey = annotationKeys[i];

				LongHashMap<Object> annotations = annotationKey==null ?
						getDefaultAnnotations() : keyAnnotations.get(annotationKeys[i]);

				if(annotations==null)
					throw new CorruptedStateException("Missing annotation storage for key: "+annotationKeys[i]); //$NON-NLS-1$

				long key = keys[i];
				Object value = values[i];
				Object current = annotations.get(key);
				annotations.put(key, value);

				values[i] = current;
			}
		}

		/**
		 * @see de.ims.icarus.language.model.api.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return ComplexAnnotationLayer.this;
		}

	}
}
