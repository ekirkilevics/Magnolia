/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
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
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.util;

import static org.junit.Assert.*;
import info.magnolia.test.mock.MockContent;

import org.junit.Test;

/**
 * @version $Id$
 */
public class NodeUuidComparatorTest {
    @Test
    public void testBasic() {
        final MockContent a = new MockContent("a");
        a.setUUID("a-uuid");
        final MockContent b = new MockContent("b");
        b.setUUID("b-uuid");
        final MockContent otherA = new MockContent("other-a");
        otherA.setUUID("a-uuid"); // yes, this is on purpose, to ensure we don't accidentally compare the instances, but only their uuid

        final NodeUuidComparator comparator = new NodeUuidComparator();
        assertEquals(0, comparator.compare(a, otherA));
        assertTrue(comparator.compare(a, b) < 0);
        assertTrue(comparator.compare(b, a) > 0);
    }
}
