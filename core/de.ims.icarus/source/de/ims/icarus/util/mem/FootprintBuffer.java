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
package de.ims.icarus.util.mem;

import java.lang.reflect.Array;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import de.ims.icarus.util.collections.CollectionUtils;
import de.ims.icarus.util.strings.StringUtil;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class FootprintBuffer implements MemoryFootprint {

	private static final int INTEGER = 0;
	private static final int SHORT = 1;
	private static final int LONG = 2;
	private static final int BYTE = 3;
	private static final int BOOLEAN = 4;
	private static final int CHARACTER = 5;
	private static final int FLOAT = 6;
	private static final int DOUBLE = 7;

	private static final TObjectIntMap<Class<?>> typeLookup = new TObjectIntHashMap<>();
	static {
		typeLookup.put(int.class, INTEGER);
		typeLookup.put(short.class, SHORT);
		typeLookup.put(long.class, LONG);
		typeLookup.put(byte.class, BYTE);
		typeLookup.put(boolean.class, BOOLEAN);
		typeLookup.put(char.class, CHARACTER);
		typeLookup.put(float.class, FLOAT);
		typeLookup.put(double.class, DOUBLE);
	}

	private static final int[] primitiveFootprints = new int[8];
	static {
		primitiveFootprints[INTEGER] = INT_FIELD_SIZE;
		primitiveFootprints[SHORT] = SHORT_FIELD_SIZE;
		primitiveFootprints[LONG] = LONG_FIELD_SIZE;
		primitiveFootprints[BYTE] = BYTE_FIELD_SIZE;
		primitiveFootprints[BOOLEAN] = BOOLEAN_FIELD_SIZE;
		primitiveFootprints[CHARACTER] = CHAR_FIELD_SIZE;
		primitiveFootprints[FLOAT] = FLOAT_FIELD_SIZE;
		primitiveFootprints[DOUBLE] = DOUBLE_FIELD_SIZE;
	}

	private static final Class<?>[] primitiveClasses = new Class[8];
	static {
		primitiveClasses[INTEGER] = int.class;
		primitiveClasses[SHORT] = short.class;
		primitiveClasses[LONG] = long.class;
		primitiveClasses[BYTE] = byte.class;
		primitiveClasses[BOOLEAN] = boolean.class;
		primitiveClasses[CHARACTER] = char.class;
		primitiveClasses[FLOAT] = float.class;
		primitiveClasses[DOUBLE] = double.class;
	}

	private final Object rootObject;

	private long primitivesCount = 0;
	private long[] primitiveCounts = new long[8];

	private long objectCount = 0;
	private long referenceCount = 0;
	private long uplinkCount = 0;
	private long downlinkCount = 0;

	private long arrayCount = 0;

	private static class ClassInfo {
		long footprint;
		long count;
	}

	private Map<Class<?>, ClassInfo> classInfos = new LinkedHashMap<>();

	private long footprint;

	private AtomicBoolean active = new AtomicBoolean();
	private volatile boolean finished = false;

	private final long refsize;

	FootprintBuffer(Object rootObject) {
		if (rootObject == null)
			throw new NullPointerException("Invalid rootObject"); //$NON-NLS-1$

		this.rootObject = rootObject;

		// Init some stuff
		boolean is64Arch = System.getProperty("os.arch").contains("64"); //$NON-NLS-1$ //$NON-NLS-2$
		int refsize = is64Arch ? OBJREF_SIZE_64 : OBJREF_SIZE_32;

		//FIXME find out when exactly does ref pointer compression occur
		refsize = OBJREF_SIZE_32;

		this.refsize = refsize;
	}

	synchronized void start() {
		if(finished)
			throw new IllegalStateException("Calculation already finished"); //$NON-NLS-1$
		if(!active.compareAndSet(false, true))
			throw new IllegalStateException("Calculation already active"); //$NON-NLS-1$
	}

	synchronized void finalizeFootprint() {
		checkActive();

		long footprint = 0;
		try {
			// Now sum up the footprint
			footprint += objectCount*OBJECT_SHELL_SIZE;
			footprint += referenceCount*refsize;

			for(int type =0; type<primitiveFootprints.length; type++) {
				int size = primitiveFootprints[type];
				long count = primitiveCounts[type];

				footprint += size*count;
			}
		} finally {
			this.footprint = footprint;
			finished = true;
			active.set(false);
		}
	}

	private ClassInfo ensureClassInfo(Class<?> clazz) {
		if (clazz == null)
			throw new NullPointerException("Invalid clazz"); //$NON-NLS-1$

		ClassInfo info = classInfos.get(clazz);
		if(info==null) {
			info = new ClassInfo();
			classInfos.put(clazz, info);
		}

		return info;
	}


	public long addObject() {
		checkActive();

		objectCount++;

		return OBJECT_SHELL_SIZE;
	}

	public void addFootprint(Class<?> clazz, long footprint) {
		checkActive();

		ClassInfo info = ensureClassInfo(clazz);
		info.count++;
		info.footprint += footprint;
	}

	public long addReference() {
		checkActive();

		referenceCount++;

		return refsize;
	}

	public long addUplink() {
		checkActive();

		referenceCount++;
		uplinkCount++;

		return refsize;
	}

	public long addDownlink() {
		checkActive();

		referenceCount++;
		downlinkCount++;

		return refsize;
	}

//	private int stringFootprint(String s) {
//		return 8 * (int) ((((s.length()) * 2) + 45) / 8);
//	}

	public long addArray(Object array) {
		if (array == null)
			throw new NullPointerException("Invalid array"); //$NON-NLS-1$

		long footprint = addObject();

		int size = Array.getLength(array);
		long fieldsize = refsize;
		int type = typeLookup.get(array.getClass().getComponentType());
		if(type!=-1) {
			// For primitive array directly calculate memory for fields
			fieldsize = primitiveFootprints[type];
			primitivesCount += size;
			primitiveCounts[type] += size;
		} else {
			// For complexe array only calculate memory for references
			referenceCount += size;
		}

		footprint += fieldsize*size;

//		addFootprint(array.getClass(), footprint);

		arrayCount++;

		return footprint;
	}

	private long addPrimitive(int type) {
		checkActive();

		primitivesCount++;
		primitiveCounts[type]++;

		return primitiveFootprints[type];
	}

	public static boolean isPrimitiveClass(Class<?> clazz) {
		return typeLookup.containsKey(clazz);
	}

	public long addPrimitive(Class<?> clazz) {
		int type = typeLookup.get(clazz);
		if(type==-1)
			throw new IllegalArgumentException("Not a primitive class: "+clazz); //$NON-NLS-1$

		return addPrimitive(type);
	}

	public long addInteger() {
		return addPrimitive(INTEGER);
	}

	public long addLong() {
		return addPrimitive(LONG);
	}

	public long addShort() {
		return addPrimitive(SHORT);
	}

	public long addByte() {
		return addPrimitive(BYTE);
	}

	public long addDouble() {
		return addPrimitive(DOUBLE);
	}

	public long addFloat() {
		return addPrimitive(FLOAT);
	}

	public long addBoolean() {
		return addPrimitive(BOOLEAN);
	}

	public long addCharacter() {
		return addPrimitive(CHARACTER);
	}

//	public void merge(MemoryFootprint other) {
//		checkFinished();
//
//		footprint += other.getFootprint();
//		primitivesCount += other.getPrimitiveCount();
//		primitiveCounts[INTEGER] += other.getIntegerCount();
//		primitiveCounts[LONG] += other.getLongCount();
//		primitiveCounts[SHORT] += other.getShortCount();
//		primitiveCounts[FLOAT] += other.getFloatCount();
//		primitiveCounts[DOUBLE] += other.getDoubleCount();
//		primitiveCounts[BOOLEAN] += other.getBooleanCount();
//		primitiveCounts[BYTE] += other.getByteCount();
//		primitiveCounts[CHARACTER] += other.getCharacterCount();
//
//		arrayCount += other.getArrayCount();
//		objectCount += other.getObjectCount();
//		referenceCount += other.getReferenceCount();
//		uplinkCount += other.getUplinkCount();
//		downlinkCount += other.getDownlinkCount();
//
//		//TODO
//	}

	private void checkFinished() {
		if(active.get())
			throw new IllegalStateException("Calculation still active"); //$NON-NLS-1$
		if(!finished)
			throw new IllegalStateException("Calculations not finished yet"); //$NON-NLS-1$
	}

	private void checkActive() {
		if(finished)
			throw new IllegalStateException("Footprint already calculated"); //$NON-NLS-1$
		if(!active.get())
			throw new IllegalStateException("Calculation not in progress"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.util.mem.MemoryFootprint#getRootObject()
	 */
	@Override
	public Object getRootObject() {
		return rootObject;
	}

	/**
	 * @see de.ims.icarus.util.mem.MemoryFootprint#getFootprint()
	 */
	@Override
	public long getFootprint() {
		checkFinished();

		return footprint;
	}

	/**
	 * @see de.ims.icarus.util.mem.MemoryFootprint#getObjectCount()
	 */
	@Override
	public long getObjectCount() {
		checkFinished();

		return objectCount;
	}

	/**
	 * @see de.ims.icarus.util.mem.MemoryFootprint#getReferenceCount()
	 */
	@Override
	public long getReferenceCount() {
		checkFinished();

		return referenceCount;
	}

	/**
	 * @see de.ims.icarus.util.mem.MemoryFootprint#getDownlinkCount()
	 */
	@Override
	public long getDownlinkCount() {
		checkFinished();

		return downlinkCount;
	}

	/**
	 * @see de.ims.icarus.util.mem.MemoryFootprint#getUplinkCount()
	 */
	@Override
	public long getUplinkCount() {
		checkFinished();

		return uplinkCount;
	}

	/**
	 * @see de.ims.icarus.util.mem.MemoryFootprint#getPrimitiveCount()
	 */
	@Override
	public long getPrimitiveCount() {
		checkFinished();

		return primitivesCount;
	}

	/**
	 * @see de.ims.icarus.util.mem.MemoryFootprint#getArrayCount()
	 */
	@Override
	public long getArrayCount() {
		checkFinished();

		return arrayCount;
	}

	/**
	 * @see de.ims.icarus.util.mem.MemoryFootprint#getIntegerCount()
	 */
	@Override
	public long getIntegerCount() {
		checkFinished();

		return primitiveCounts[INTEGER];
	}

	/**
	 * @see de.ims.icarus.util.mem.MemoryFootprint#getLongCount()
	 */
	@Override
	public long getLongCount() {
		checkFinished();

		return primitiveCounts[LONG];
	}

	/**
	 * @see de.ims.icarus.util.mem.MemoryFootprint#getShortCount()
	 */
	@Override
	public long getShortCount() {
		checkFinished();

		return primitiveCounts[SHORT];
	}

	/**
	 * @see de.ims.icarus.util.mem.MemoryFootprint#getByteCount()
	 */
	@Override
	public long getByteCount() {
		checkFinished();

		return primitiveCounts[BYTE];
	}

	/**
	 * @see de.ims.icarus.util.mem.MemoryFootprint#getBooleanCount()
	 */
	@Override
	public long getBooleanCount() {
		checkFinished();

		return primitiveCounts[BOOLEAN];
	}

	/**
	 * @see de.ims.icarus.util.mem.MemoryFootprint#getCharacterCount()
	 */
	@Override
	public long getCharacterCount() {
		checkFinished();

		return primitiveCounts[CHARACTER];
	}

	/**
	 * @see de.ims.icarus.util.mem.MemoryFootprint#getFloatCount()
	 */
	@Override
	public long getFloatCount() {
		checkFinished();

		return primitiveCounts[FLOAT];
	}

	/**
	 * @see de.ims.icarus.util.mem.MemoryFootprint#getDoubleCount()
	 */
	@Override
	public long getDoubleCount() {
		checkFinished();

		return primitiveCounts[DOUBLE];
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if(finished) {
			StringBuilder sb = new StringBuilder();

			sb.append('[');
			sb.append("Memory Footprint: root=").append(rootObject) //$NON-NLS-1$
				.append(" totalBytes=").append(StringUtil.formatDecimal(footprint)) //$NON-NLS-1$
				.append(" references=").append(StringUtil.formatDecimal(referenceCount)) //$NON-NLS-1$
				.append(" uplinks=").append(StringUtil.formatDecimal(uplinkCount)) //$NON-NLS-1$
				.append(" downlinks=").append(StringUtil.formatDecimal(downlinkCount)) //$NON-NLS-1$
				.append(" objects=").append(StringUtil.formatDecimal(objectCount)) //$NON-NLS-1$
				.append(" primitives=").append(StringUtil.formatDecimal(primitivesCount)) //$NON-NLS-1$
				.append(" arrays=").append(StringUtil.formatDecimal(arrayCount)); //$NON-NLS-1$
			sb.append(']');

			return sb.toString();
		} else {
			return super.toString();
		}
	}

	private ClassInfo getClassInfo(Class<?> clazz) {
		if (clazz == null)
			throw new NullPointerException("Invalid clazz"); //$NON-NLS-1$

		ClassInfo info = classInfos.get(clazz);
		if(info==null)
			throw new IllegalArgumentException("No data available for class: "+clazz); //$NON-NLS-1$

		return info;
	}

	/**
	 * @see de.ims.icarus.util.mem.MemoryFootprint#getClasses()
	 */
	@Override
	public Set<Class<?>> getClasses() {
		return CollectionUtils.getSetProxy(classInfos.keySet());
	}

	/**
	 * @see de.ims.icarus.util.mem.MemoryFootprint#getInstanceCount(java.lang.Class)
	 */
	@Override
	public long getInstanceCount(Class<?> clazz) {
		return getClassInfo(clazz).count;
	}

	/**
	 * @see de.ims.icarus.util.mem.MemoryFootprint#getInstanceFootprint(java.lang.Class)
	 */
	@Override
	public long getInstanceFootprint(Class<?> clazz) {
		return getClassInfo(clazz).footprint;
	}
}
