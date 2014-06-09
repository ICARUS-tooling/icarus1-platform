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
package de.ims.icarus.model.standard.driver.file;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Implements a simple 16 bytes checksum to encapsulate the last modification time
 * and size of a file. Since reading in an entire file to compute an actual checksum
 * is way to expensive for large files, those 2 informations are considered enough
 * to make a very quick check whether or not a file has changed since the last time it
 * was accessed by the framework.
 * <p>
 * The checksum internally uses an {@link UUID} object to simplify storage and serialization.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class FileChecksum {

	private final UUID uuid;

	public static FileChecksum compute(Path file) throws IOException {
		if (file == null)
			throw new NullPointerException("Invalid file"); //$NON-NLS-1$
		if(Files.notExists(file))
			throw new FileNotFoundException("File does not exist: "+file); //$NON-NLS-1$

		long timestamp = Files.getLastModifiedTime(file).toMillis();
		long size = Files.size(file);

//		return compute(timestamp, size);

		return new FileChecksum(timestamp, size);
	}

	public static FileChecksum compute(long timestamp, long size) {
		if(timestamp<0)
			throw new IllegalArgumentException("Timestamp cannot be negative: "+timestamp); //$NON-NLS-1$
		if(size<0)
			throw new IllegalArgumentException("Size cannot be negative: "+size); //$NON-NLS-1$

//		byte[] data = new byte[SIZE];
//
//		for(int i=0; i<8; i++) {
//			data[i] = (byte)((timestamp & mask) - Byte.MIN_VALUE);
//			data[i+8] = (byte) ((size & mask) - Byte.MIN_VALUE);
//
//			timestamp >>= 8;
//			size >>= 8;
//		}
//
//
//		return new FileChecksum(data);

		return new FileChecksum(timestamp, size);
	}

	public static FileChecksum parse(String s) {
//		if (s == null)
//			throw new NullPointerException("Invalid string");
//		if(s.isEmpty())
//			throw new IllegalArgumentException("String is empty");
//		if(s.length()!=SIZE)
//			throw new IllegalArgumentException("String is of illegal size (16 characters required): "+s);
//
//		byte[] data = new byte[SIZE];
//		for(int i=0; i<SIZE; i++) {
//			char c = s.charAt(i);
//			byte b =
//		}
//
//		UUID
//
//		return new FileChecksum(data);

		UUID uuid = UUID.fromString(s);

		return new FileChecksum(uuid);
	}

//	public FileChecksum(byte[] data) {
//		if (data == null)
//			throw new NullPointerException("Invalid data"); //$NON-NLS-1$
//		if(data.length!=SIZE)
//			throw new IllegalArgumentException("Checksum needs 16 bytes, only got "+data.length); //$NON-NLS-1$
//
//		this.data = data;
//	}

	private FileChecksum(UUID uuid) {
		this.uuid = uuid;
	}

	public FileChecksum(long timestamp, long size) {
		uuid = new UUID(timestamp, size);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return uuid.hashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof FileChecksum) {
			FileChecksum other = (FileChecksum)obj;
//			return Arrays.equals(data, other.data);
			return uuid.equals(other.uuid);
		}
		return false;
	}

	/**
	 * Converts this checksum in a {@code String} representation by
	 * converting each byte into a character and creating a new
	 * string object from them.
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
//		char[] tmp = new char[16];
//		for(int i=0; i<SIZE; i++) {
//			tmp[i] = (char) (data[i]+1);
//		}
//
//		return new String(tmp);
		return uuid.toString();
	}


}
