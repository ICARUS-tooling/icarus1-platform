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
package de.ims.icarus.language.model.test.member;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import junit.framework.Assert;

import org.junit.Test;

import de.ims.icarus.language.model.api.CorpusMember;
import de.ims.icarus.language.model.standard.LookupList;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LookupListTest {

	private static final CorpusMember dummy1 = new MemberDummy(1);
	private static final CorpusMember dummy2 = new MemberDummy(2);
	private static final CorpusMember dummy3 = new MemberDummy(3);

	@Test
	public void testInit() throws Exception {
		LookupList<CorpusMember> list = new LookupList<>();

		assertEquals(list.size(), 0L);

		list = new LookupList<>(100);

		assertEquals(list.size(), 0L);
	}

	@Test
	public void testBasic() throws Exception {
		LookupList<CorpusMember> list = new LookupList<>();

		// Test size

		list.add(dummy1);
		Assert.assertEquals("Size after adding 1 item must be 1", list.size(), 1);

		list.add(dummy2);
		Assert.assertEquals("Size after adding 2 items must be s", list.size(), 2);

		// Test indexOf()

		Assert.assertEquals("Index of dummy1 must be 0", list.indexOf(dummy1), 0);
		Assert.assertEquals("Index of dummy2 must be 1", list.indexOf(dummy2), 1);
		Assert.assertEquals("Index of dummy3 must be -1", list.indexOf(dummy3), -1);

		// Test contains()

		Assert.assertTrue("List must contain dummy1", list.contains(dummy1));
		Assert.assertTrue("List must contain dummy2", list.contains(dummy2));
		Assert.assertTrue("List must not contain dummy3", !list.contains(dummy3));

		// Test insert

		list.add(1, dummy3);

		Assert.assertEquals("Index of dummy3 must be 1", list.indexOf(dummy3), 1);
		Assert.assertEquals("Index of dummy2 must be 2", list.indexOf(dummy2), 2);
		Assert.assertTrue("List must contain dummy3", list.contains(dummy3));

		// Test clear
		list = new LookupList<>();
		list .add(dummy1);
		list.add(dummy2);
		list.add(dummy3);

		Assert.assertEquals(list.size(), 3);
		list.clear();
		Assert.assertTrue("List must be empty after clearing", list.isEmpty());

		// Test remove

		list = new LookupList<>();
		list.add(dummy1);

		list.remove(dummy1);
		Assert.assertTrue("List must not contain dummy1 after removal", !list.contains(dummy1));

		list.add(dummy2);
		list.remove(0);
		Assert.assertTrue("List must not contain dummy2 after removal", !list.contains(dummy2));
	}

	@Test
	public void testIterator() throws Exception {
		LookupList<CorpusMember> list = new LookupList<>();

		list.add(dummy1);
		list.add(dummy2);
		Assert.assertEquals(list.size(), 2);

		Iterator<CorpusMember> it = list.iterator();
		Assert.assertNotNull(it);
		Assert.assertTrue(it.hasNext());

		CorpusMember item = it.next();
		Assert.assertEquals(item, dummy1);
		Assert.assertTrue(it.hasNext());

		item = it.next();
		Assert.assertEquals(item, dummy2);
		Assert.assertTrue(!it.hasNext());
	}

	@Test
	public void testLookup() throws Exception {
		LookupList<CorpusMember> list = new LookupList<>();

		CorpusMember[] items = new CorpusMember[20];
		for(int i=0; i<items.length; i++) {
			items[i] = new MemberDummy(i+1);
			list.add(items[i]);
		}

		Assert.assertEquals(list.size(), items.length);

		for(int i=0; i<items.length; i++) {
			Assert.assertTrue(list.contains(items[i]));
			Assert.assertEquals(i, list.indexOf(items[i]));
		}
	}
}
