/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.util;

import info.magnolia.cms.core.Content;
import info.magnolia.test.mock.MockHierarchyManager;
import info.magnolia.test.mock.MockUtil;
import junit.framework.TestCase;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class SiblingsHelperTest extends TestCase {
    private MockHierarchyManager hm;

    protected void setUp() throws Exception {
        super.setUp();
        hm = MockUtil.createHierarchyManager(TEST_CONTENT);
    }

    public void testCanTellPosition() throws Exception {
        final Content n = hm.getContent("/parent/sub2");
        final SiblingsHelper s = SiblingsHelper.of(n);
        assertEquals("Positions are 0-based indexes, so /parent/sub2 should be in position 1.", 1, s.getPosition());
        s.next();
        s.next();
        assertEquals("Should have skipped nodes of different type.", 3, s.getPosition());
    }

    public void testCanTellIfFirstWhenInitializingWithFirst() throws Exception {
        final Content n = hm.getContent("/parent/sub1");
        final SiblingsHelper s = SiblingsHelper.of(n);
        assertEquals(0, s.getPosition());
        assertEquals(true, s.isFirst());
        assertEquals(false, s.isLast());
    }

    public void testCanTellIfFirstAfterNavigating() throws Exception {
        final Content n = hm.getContent("/parent/sub2");
        final SiblingsHelper s = SiblingsHelper.of(n);
        s.prev();
        assertEquals(0, s.getPosition());
        assertEquals(true, s.isFirst());
        assertEquals(false, s.isLast());
    }

    public void testCanTellIfLastWhenInitializingWith() throws Exception {
        final Content n = hm.getContent("/parent/sub6");
        final SiblingsHelper s = SiblingsHelper.of(n);
        // sub6 is of type mgnl:other, there are only 2 nodes of that type
        assertEquals(1, s.getPosition());
        assertEquals(true, s.isLast());
        assertEquals(false, s.isFirst());
        assertEquals("ID-6", s.getCurrent().getUUID());
    }

    public void testCanTellIfLastAfterNavigating() throws Exception {
        final Content n = hm.getContent("/parent/sub4");
        final SiblingsHelper s = SiblingsHelper.of(n);
        assertEquals(2, s.getPosition());
        s.next();
        assertEquals(3, s.getPosition());
        assertEquals(true, s.isLast());
        assertEquals(false, s.isFirst());
        assertEquals("ID-5", s.getCurrent().getUUID());
    }

    public void testCanTellIfLastEvenIfThereAreOtherNodesOfAnotherType() throws Exception {
        final Content n = hm.getContent("/parent/sub5");
        final SiblingsHelper s = SiblingsHelper.of(n);
        assertEquals(3, s.getPosition());
        assertEquals(true, s.isLast());
        assertEquals(false, s.isFirst());
    }

    public void testFactoryMethodProperlyInstanciatesWithFirstChildOfParent() throws Exception {
        final Content parent = hm.getContent("/parent");
        final SiblingsHelper s = SiblingsHelper.childrenOf(parent);
        assertEquals(0, s.getPosition());
        assertEquals(true, s.isFirst());
        assertEquals(false, s.isLast());
        assertEquals("ID-1", s.getCurrent().getUUID());
    }

    public static final String TEST_CONTENT = "" +
            "parent@type = mgnl:test\n" +
            "parent@uuid = ID-x\n" +

            "parent/sub1@type = mgnl:test\n" +
            "parent/sub1@uuid = ID-1\n" +

            "parent/sub2@type = mgnl:test\n" +
            "parent/sub2@uuid = ID-2\n" +

            "parent/subother@type = mgnl:other\n" +
            "parent/subother@uuid = ID-3\n" +

            "parent/sub4@type = mgnl:test\n" +
            "parent/sub4@uuid = ID-4\n" +

            "parent/sub4/sub4_1@type = mgnl:test\n" +
            "parent/sub4/sub4_1@uuid = ID-41\n" +

            "parent/sub4/sub4_2@type = mgnl:test\n" +
            "parent/sub4/sub4_2@uuid = ID-42\n" +

            "parent/sub5@type = mgnl:test\n" +
            "parent/sub5@uuid = ID-5\n" +

            "parent/sub6@type = mgnl:other\n" +
            "parent/sub6@uuid = ID-6\n";

}
