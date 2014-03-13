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
import java.util.List;

import de.ims.icarus.language.model.api.Container;
import de.ims.icarus.language.model.api.Context;
import de.ims.icarus.language.model.api.Corpus;
import de.ims.icarus.language.model.api.CorpusMember;
import de.ims.icarus.language.model.api.Markable;
import de.ims.icarus.language.model.api.MemberType;
import de.ims.icarus.language.model.api.edit.UndoableCorpusEdit.AtomicChange;
import de.ims.icarus.language.model.api.layer.AnnotationLayer;
import de.ims.icarus.language.model.api.layer.MarkableLayer;
import de.ims.icarus.language.model.api.manifest.AnnotationLayerManifest;
import de.ims.icarus.language.model.api.manifest.AnnotationManifest;
import de.ims.icarus.language.model.standard.CorpusMemberUtils;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.collections.LongHashMap;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultAnnotationLayer extends AbstractLayer<AnnotationLayerManifest> implements AnnotationLayer {

	private final LongHashMap<Object> annotations = new LongHashMap<>();

	/**
	 * @param id
	 * @param context
	 * @param manifest
	 */
	public DefaultAnnotationLayer(long id, Context context,
			AnnotationLayerManifest manifest) {
		super(id, context, manifest);
	}

	protected LongHashMap<Object> getDefaultAnnotations() {
		return annotations;
	}

	/**
	 * @see de.ims.icarus.language.model.api.layer.AnnotationLayer#hasAnnotations()
	 */
	@Override
	public boolean hasAnnotations() {
		return !annotations.isEmpty();
	}

	/**
	 * @see de.ims.icarus.language.model.api.layer.AnnotationLayer#getValue(de.ims.icarus.language.model.api.Markable)
	 */
	@Override
	public Object getValue(Markable markable) {
		return annotations.get(markable.getId());
	}

	/**
	 * @see de.ims.icarus.language.model.api.layer.AnnotationLayer#getValue(de.ims.icarus.language.model.api.Markable, java.lang.String)
	 */
	@Override
	public Object getValue(Markable markable, String key) {
		throw new UnsupportedOperationException("Additional keys not supported"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.language.model.api.layer.AnnotationLayer#removeAllValues()
	 */
	@Override
	public void removeAllValues() {
		execute(new ClearChange());
	}

	/**
	 * @see de.ims.icarus.language.model.api.layer.AnnotationLayer#removeAllValues(java.lang.String)
	 */
	@Override
	public void removeAllValues(String key) {
		throw new UnsupportedOperationException("Additional keys not supported"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.language.model.api.layer.AnnotationLayer#removeAllValues(de.ims.icarus.language.model.api.Markable, boolean)
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

		if(annotations.get(markable.getId())!=null) {
			buffer.add(markable);
		}

		if(recursive && (markable.getMemberType()==MemberType.STRUCTURE
				|| markable.getMemberType()==MemberType.CONTAINER)) {
			collectAnnotatedMarkables((Container) markable, buffer);
		}

		if(buffer.isEmpty()) {
			return;
		}

		int size = buffer.size();
		long[] keys = new long[size];

		for(int i=0; i<size; i++) {
			keys[i] = buffer.get(i).getId();
		}

		execute(new AnnotationChange(keys));
	}

	private void collectAnnotatedMarkables(Container container, List<Markable> buffer) {
		int size = container.getMarkableCount();
		for(int i=0; i<size; i++) {
			Markable markable = container.getMarkableAt(i);

			if(annotations.get(markable.getId())!=null) {
				buffer.add(markable);
			}

			if(markable.getMemberType()==MemberType.STRUCTURE
					|| markable.getMemberType()==MemberType.CONTAINER) {
				collectAnnotatedMarkables((Container) markable, buffer);
			}
		}
	}

	/**
	 * @see de.ims.icarus.language.model.api.layer.AnnotationLayer#setValue(de.ims.icarus.language.model.api.Markable, java.lang.Object)
	 */
	@Override
	public void setValue(Markable markable, Object value) {
		if (markable == null)
			throw new NullPointerException("Invalid markable"); //$NON-NLS-1$

		MarkableLayer markableLayer = markable.getLayer();
		if(markableLayer!=getBaseLayer() && !getManifest().isDeepAnnotation())
			throw new IllegalArgumentException("Markable '"+markable+"' is not a member of this layer's base layer"); //$NON-NLS-1$ //$NON-NLS-2$

		AnnotationManifest annotationManifest = getManifest().getDefaultAnnotationManifest();
		if(annotationManifest==null)
			throw new UnsupportedOperationException("No keyless annotation defined"); //$NON-NLS-1$

		if(value!=null && !annotationManifest.getValueType().isValidValue(value))
			throw new IllegalArgumentException("Invalid annotation value: "+value); //$NON-NLS-1$

		execute(new AnnotationChange(markable.getId(), value));
	}

	protected void setValue0(Markable markable, Object value) {
		annotations.put(markable.getId(), value);
	}

	/**
	 * @see de.ims.icarus.language.model.api.layer.AnnotationLayer#setValue(de.ims.icarus.language.model.api.Markable, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setValue(Markable markable, String key, Object value) {
		throw new UnsupportedOperationException("Additional keys not supported"); //$NON-NLS-1$
	}

	/**
	 * Helper method to check whether or not the enclosing corpus is editable
	 * and to forward an atomic change to the edit model.
	 *
	 * @param change
	 * @throws UnsupportedOperationException if the corpus is not editable
	 */
	protected void execute(AtomicChange change) {
		Corpus corpus = getCorpus();

		if(!corpus.getManifest().isEditable())
			throw new UnsupportedOperationException("Corpus does not support modifications"); //$NON-NLS-1$

		corpus.getEditModel().execute(change);
	}

	private class ClearChange implements AtomicChange {

		private long[] keys = null;
		private Object[] values = null;

		int expectedSize = annotations.size();

		/**
		 * @see de.ims.icarus.language.model.api.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			int size = annotations.size();
			if(expectedSize!=size)
				throw new CorruptedStateException(CorpusMemberUtils.sizeMismatchMessage(
						"Clear failed", expectedSize, annotations.size())); //$NON-NLS-1$

			if(keys==null) {
				keys = new long[size];
				values = new Object[size];

				annotations.save(keys, values);
				annotations.clear();
			} else {
				annotations.load(keys, values);

				keys = null;
				values = null;
			}

			expectedSize = annotations.size();
		}

		/**
		 * @see de.ims.icarus.language.model.api.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return DefaultAnnotationLayer.this;
		}

	}

	private class AnnotationChange implements AtomicChange {

		private final long[] keys;
		private final Object[] values;

		private AnnotationChange(long[] keys, Object[] values) {
			if(keys.length!=values.length)
				throw new IllegalArgumentException("Size mismatch between keys and values array"); //$NON-NLS-1$

			this.keys = keys;
			this.values = values;
		}

		private AnnotationChange(long key, Object value) {
			keys = new long[]{key};
			values = new Object[]{value};
		}

		private AnnotationChange(long[] keys) {
			this.keys = keys;
			this.values = new Object[keys.length];
		}

		/**
		 * @see de.ims.icarus.language.model.api.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
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
			return DefaultAnnotationLayer.this;
		}

	}
}
