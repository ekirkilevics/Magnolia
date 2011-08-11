/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.module.model;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @version $Id$
 */
public class VersionComparatorTest {
    private static final Version V100 = Version.parseVersion("1.0.0");
    private static final Version V101 = Version.parseVersion("1.0.1");
    private static final Version V110 = Version.parseVersion("1.1.0");
    private static final Version V200 = Version.parseVersion("2.0.0");

    @Test
    public void testBasic() {
        final VersionComparator vc = new VersionComparator();
        assertTrue(vc.compare(V101, V100) > 0);
        assertTrue(vc.compare(V110, V100) > 0);
        assertTrue(vc.compare(V200, V100) > 0);
        assertTrue(vc.compare(V110, V101) > 0);
        assertTrue(vc.compare(V200, V101) > 0);
        assertTrue(vc.compare(V200, V110) > 0);

        assertTrue(vc.compare(V100, V100) == 0);
        assertTrue(vc.compare(V101, V101) == 0);
        assertTrue(vc.compare(V110, V110) == 0);
        assertTrue(vc.compare(V200, V200) == 0);

        assertTrue(vc.compare(V100, V101) < 0);
        assertTrue(vc.compare(V100, V110) < 0);
        assertTrue(vc.compare(V100, V200) < 0);
        assertTrue(vc.compare(V101, V110) < 0);
        assertTrue(vc.compare(V101, V200) < 0);
        assertTrue(vc.compare(V110, V200) < 0);
    }

    @Test
    public void testClassifiersAreIgnored() {
        final VersionComparator vc = new VersionComparator();
        assertTrue(vc.compare(Version.parseVersion("1.0-foo"), V100) == 0);
        assertTrue(vc.compare(V100, Version.parseVersion("1.0-bar")) == 0);
        assertTrue(vc.compare(V100, Version.parseVersion("2.0-bar")) < 0);
        assertTrue(vc.compare(Version.parseVersion("2.0.5-foo"), V101) > 0);
    }
}
