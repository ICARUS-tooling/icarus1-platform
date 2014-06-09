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

import de.ims.icarus.model.api.Container;
import de.ims.icarus.model.api.CorpusMember;
import de.ims.icarus.model.api.Markable;
import de.ims.icarus.model.api.MemberType;
import de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange;
import de.ims.icarus.model.api.layer.AnnotationLayer;
import de.ims.icarus.model.api.layer.LayerGroup;
import de.ims.icarus.model.api.manifest.AnnotationLayerManifest;
import de.ims.icarus.model.api.manifest.AnnotationManifest;
import de.ims.icarus.model.util.CorpusMemberUtils;
import de.ims.icarus.model.util.CorpusUtils;
import de.ims.icarus.util.Consumer;
import de.ims.icarus.util.CorruptedStateException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultAnnotationLayer extends AbstractLayer<AnnotationLayerManifest> implements AnnotationLayer {

	private final Map<Markable, Object> annotations = new WeakHashMap<>();

	/**
	 * @param id
	 * @param context
	 * @param manifest
	 */
	public DefaultAnnotationLayer(AnnotationLayerManifest manifest, LayerGroup group) {
		super(manifest, group);
	}

	protected Map<Markable, Object> getDefaultAnnotations() {
		return annotations;
	}

	/**
	 * @see de.ims.icarus.model.api.layer.AnnotationLayer#hasAnnotations()
	 */
	@Override
	public boolean hasAnnotations() {
		return !annotations.isEmpty();
	}

	/**
	 * @see de.ims.icarus.model.api.layer.AnnotationLayer#getValue(de.ims.icarus.model.api.Markable)
	 */
	@Override
	public Object getValue(Markable markable) {
		return annotations.get(markable);
	}

	/**
	 * @see de.ims.icarus.model.api.layer.AnnotationLayer#getValue(de.ims.icarus.model.api.Markable, java.lang.String)
	 */
	@Override
	public Object getValue(Markable markable, String key) {
		throw new UnsupportedOperationException("Additional keys not supported"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.model.api.layer.AnnotationLayer#collectKeys(de.ims.icarus.model.api.Markable, de.ims.icarus.util.Consumer)
	 */
	@Override
	public boolean collectKeys(Markable markable, Consumer<String> buffer) {
		throw new UnsupportedOperationException("Additional keys not supported"); //$NON-NLS-1$
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
		throw new UnsupportedOperationException("Additional keys not supported"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.model.api.layer.AnnotationLayer#removeAllValues(de.ims.icarus.model.api.Markable, boolean)
	 */
	@Override
	public void removeAllValues(Markable markable, boolean recursive) {
		if (markable == null)
			throw new NullPointerException("Invalid markable"); //$NON-NLS-1$

		if(getBaseLayers().contains(markable.getLayer()) && !getManifest().isDeepAnnotation())
			throw new IllegalArgumentException("Markable '"+markable+"' is not a member of this layer's base layer"); //$NON-NLS-1$ //$NON-NLS-2$

		List<Markable> buffer = new ArrayList<>();

		if(annotations.get(markable)!=null) {
			buffer.add(markable);
		}

		if(recursive && (markable.getMemberType()==MemberType.STRUCTURE
				|| markable.getMemberType()==MemberType.CONTAINER)) {
			collectAnnotatedMarkables((Container) markable, buffer);
		}

		if(buffer.isEmpty()) {
			return;
		}

		Markable[] markables = new Markable[buffer.size()];
		buffer.toArray(markables);

		execute(new AnnotationChange(markables));
	}

	private void collectAnnotatedMarkables(Container container, List<Markable> buffer) {
		int size = container.getMarkableCount();
		for(int i=0; i<size; i++) {
			Markable markable = container.getMarkableAt(i);

			if(annotations.get(markable)!=null) {
				buffer.add(markable);
			}

			if(markable.getMemberType()==MemberType.STRUCTURE
					|| markable.getMemberType()==MemberType.CONTAINER) {
				collectAnnotatedMarkables((Container) markable, buffer);
			}
		}
	}

	/**
	 * @see de.ims.icarus.model.api.layer.AnnotationLayer#setValue(de.ims.icarus.model.api.Markable, java.lang.Object)
	 */
	@Override
	public void setValue(Markable markable, Object value) {
		if (markable == null)
			throw new NullPointerException("Invalid markable"); //$NON-NLS-1$

		if(getBaseLayers().contains(markable.getLayer()) && !getManifest().isDeepAnnotation())
			throw new IllegalArgumentException("Markable '"+markable+"' is not a member of this layer's base layer"); //$NON-NLS-1$ //$NON-NLS-2$

		AnnotationManifest annotationManifest = getManifest().getDefaultAnnotationManifest();
		if(annotationManifest==null)
			throw new UnsupportedOperationException("No keyless annotation defined"); //$NON-NLS-1$

		if(value!=null && !annotationManifest.getValueType().isValidValue(value))
			throw new IllegalArgumentException("Invalid annotation value: "+value); //$NON-NLS-1$

		execute(new AnnotationChange(markable, value));
	}

	/**
	 * Directly saves a given annotation value, bypassing most of the default
	 * sanity checks of the {@link #setValue(Markable, Object)} method.
	 *
	 * @param markable
	 * @param value
	 */
	public void putValue(Markable markable, Object value) {
		if (markable == null)
			throw new NullPointerException("Invalid markable"); //$NON-NLS-1$
		if (value == null)
			throw new NullPointerException("Invalid value"); //$NON-NLS-1$

		annotations.put(markable, value);
	}

	/**
	 * @see de.ims.icarus.model.api.layer.AnnotationLayer#setValue(de.ims.icarus.model.api.Markable, java.lang.String, java.lang.Object)
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
		CorpusUtils.dispatchChange(this, change);
	}

	private class ClearChange implements AtomicChange {

		private Markable[] markables = null;
		private Object[] values = null;

		int expectedSize = annotations.size();

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			int size = annotations.size();
			if(expectedSize!=size)
				throw new CorruptedStateException(CorpusMemberUtils.sizeMismatchMessage(
						"Clear failed", expectedSize, annotations.size())); //$NON-NLS-1$

			if(markables==null) {
				markables = new Markable[size];
				values = new Object[size];

				Iterator<Entry<Markable, Object>> it = annotations.entrySet().iterator();
				for(int i=0; i<size; i++) {
					Entry<Markable, Object> entry = it.next();
					markables[i] = entry.getKey();
					values[i] = entry.getValue();
				}

				annotations.clear();
			} else {
				for(int i=0; i<size; i++) {
					annotations.put(markables[i], values[i]);
				}

				markables = null;
				values = null;
			}

			expectedSize = annotations.size();
		}

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return DefaultAnnotationLayer.this;
		}

	}

	private class AnnotationChange implements AtomicChange {

		private final Markable[] markables;
		private final Object[] values;

		private AnnotationChange(Markable[] markables, Object[] values) {
			if(markables.length!=values.length)
				throw new IllegalArgumentException("Size mismatch between markables and values array"); //$NON-NLS-1$

			this.markables = markables;
			this.values = values;
		}

		private AnnotationChange(Markable markable, Object value) {
			markables = new Markable[]{markable};
			values = new Object[]{value};
		}

		private AnnotationChange(Markable[] markables) {
			this.markables = markables;
			this.values = new Object[markables.length];
		}

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#execute()
		 */
		@Override
		public void execute() {
			for(int i=markables.length-1; i>-1; i--) {
				Markable markable = markables[i];
				Object value = values[i];
				Object current = annotations.get(markable);
				annotations.put(markable, value);

				values[i] = current;
			}
		}

		/**
		 * @see de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange#getAffectedMember()
		 */
		@Override
		public CorpusMember getAffectedMember() {
			return DefaultAnnotationLayer.this;
		}

	}
}
