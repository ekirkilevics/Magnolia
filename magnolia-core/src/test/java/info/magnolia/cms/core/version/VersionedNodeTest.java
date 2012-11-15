/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.cms.core.version;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.version.Version;

import org.junit.Test;

/**
 * @version $Id$
 */
public class VersionedNodeTest {

    @Test
    public void testWrapProperty() throws Exception {
        // GIVEN
        final Version v = mock(Version.class);
        final Node baseNode = mock(Node.class);
        when(baseNode.getPath()).thenReturn("baseNodePath");

        final VersionedNode vn = new VersionedNode(v, baseNode);
        final Property property2Wrap = mock(Property.class);
        when(property2Wrap.getName()).thenReturn("nameOfProperty2Wrap");

        final Property wrapped = vn.wrapProperty(property2Wrap);

        // WHEN
        final String path = wrapped.getPath();

        // THEN
        assertEquals("baseNodePath/nameOfProperty2Wrap", path);
    }
}
