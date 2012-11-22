/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.jcr.util;

import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Calendar;

import static org.junit.Assert.*;

/**
 * Tests.
 */
public class NodeTypesTest {

    private static final String FIRST_CHILD = "1";
    private static final String SECOND_CHILD = "2";
    private static final String THIRD_CHILD = "3";

    private MockNode root;
    private Node first;
    private Node second;
    private Node third;

    @Before
    public void setUpTestStructure() throws RepositoryException {
        root = new MockNode(new MockSession("test"));
        first = root.addNode(FIRST_CHILD);
        first.addMixin(NodeTypes.CreatedMixin.NAME);
        first.addMixin(NodeTypes.LastModifiedMixin.NAME);
        second = root.addNode(SECOND_CHILD);
        third = root.addNode(THIRD_CHILD);
    }

    @Test
    public void testGetCreated() throws RepositoryException {
        // GIVEN
        final Calendar now =  Calendar.getInstance();
        first.setProperty(NodeTypes.CreatedMixin.CREATED, now);

        // WHEN
        final Calendar result = NodeTypes.CreatedMixin.getCreated(first);

        // THEN
        assertEquals(now, result);
    }

    @Test
    public void testGetCreatedWhenNotSet() throws RepositoryException {
        assertNull("Should not be set", NodeTypes.CreatedMixin.getCreated(first));
    }

    @Test
    public void testGetCreatedBy() throws RepositoryException {
        // GIVEN
        final String userName = "Junit";
        first.setProperty(NodeTypes.CreatedMixin.CREATED_BY, userName);

        // WHEN
        final String result = NodeTypes.CreatedMixin.getCreatedBy(first);

        // THEN
        assertEquals(userName, result);
    }

    @Test
    public void testGetCreatedByWhenNotSet() throws RepositoryException {
        assertNull("Should not be set", NodeTypes.CreatedMixin.getCreatedBy(first));
    }

    @Test
    public void testSetCreation() throws RepositoryException {
        // GIVEN
        final String userName = "Junit";

        // WHEN
        NodeTypes.CreatedMixin.setCreation(first, userName);

        // THEN
        assertTrue("Created should just have been set", (Calendar.getInstance().getTimeInMillis() - first.getProperty(NodeTypes.CreatedMixin.CREATED).getDate().getTimeInMillis()) < 1000);
        assertEquals(first.getProperty(NodeTypes.CreatedMixin.CREATED).getString(), first.getProperty(NodeTypes.LastModifiedMixin.LAST_MODIFIED).getString());
        assertEquals(userName, first.getProperty(NodeTypes.CreatedMixin.CREATED_BY).getString());
        assertEquals(userName, first.getProperty(NodeTypes.LastModifiedMixin.LAST_MODIFIED_BY).getString());
    }

    @Test
    public void testUpdateModification() throws Exception {
        // GIVEN
        final String oldUser = "someone";
        final Calendar longAgo = Calendar.getInstance();
        longAgo.set(Calendar.YEAR, 1924);
        first.setProperty(NodeTypes.LastModifiedMixin.LAST_MODIFIED, longAgo);
        first.setProperty(NodeTypes.LastModifiedMixin.LAST_MODIFIED_BY, oldUser);

        final String newUser = "Junit";

        // WHEN
        NodeTypes.LastModifiedMixin.updateModification(first, newUser);

        // THEN
        assertTrue("Lastmodified should just have been set", (Calendar.getInstance().getTimeInMillis() - first.getProperty(NodeTypes.LastModifiedMixin.LAST_MODIFIED).getDate().getTimeInMillis()) < 1000);
        assertEquals(newUser, first.getProperty(NodeTypes.LastModifiedMixin.LAST_MODIFIED_BY).getString());
    }
}
