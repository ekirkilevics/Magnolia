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
package info.magnolia.jcr.wrapper;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.junit.Before;
import org.junit.Test;

import info.magnolia.context.MgnlContext;
import info.magnolia.test.RepositoryTestCase;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ChannelVisibilitySessionWrapperTest extends RepositoryTestCase {

    private Session session;
    private Session sessionWrapper;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        session = MgnlContext.getJCRSession("website");

        Node included = session.getRootNode().addNode("included");
        included.setProperty(ChannelVisibilityContentDecorator.EXCLUDE_CHANNEL_PROPERTY_NAME, new Value[]{});

        Node excluded = session.getRootNode().addNode("excluded");
        excluded.setProperty(ChannelVisibilityContentDecorator.EXCLUDE_CHANNEL_PROPERTY_NAME, new Value[]{
                session.getValueFactory().createValue("mobile"),
        });
        excluded.addNode("childOfExcluded");

        session.getRootNode().addNode("unspecified");

        sessionWrapper = new ChannelVisibilityContentDecorator("mobile").wrapSession(session);
    }

    @Test
    public void testNodeExists() throws Exception {

        assertTrue(sessionWrapper.nodeExists("/included"));
        assertFalse(sessionWrapper.nodeExists("/excluded"));
        assertFalse(sessionWrapper.nodeExists("/excluded/childOfExcluded"));
        assertTrue(sessionWrapper.nodeExists("/unspecified"));
    }

    @Test
    public void testItemExists() throws Exception {

        assertTrue(sessionWrapper.itemExists("/included"));
        assertTrue(sessionWrapper.itemExists("/included/excludeChannels"));
        assertFalse(sessionWrapper.itemExists("/excluded"));
        assertFalse(sessionWrapper.itemExists("/excluded/excludeChannels"));
        assertFalse(sessionWrapper.itemExists("/excluded/childOfExcluded"));
        assertTrue(sessionWrapper.itemExists("/unspecified"));
    }

    @Test
    public void testPropertyExists() throws Exception {

        assertTrue(sessionWrapper.propertyExists("/included/excludeChannels"));
        assertFalse(sessionWrapper.propertyExists("/excluded/excludeChannels"));
    }

    @Test
    public void testGetNode() throws Exception {

        sessionWrapper.getNode("/included");
        sessionWrapper.getNode("/unspecified");

        try {
            sessionWrapper.getNode("/excluded");
            fail();
        } catch (PathNotFoundException expected) {
        }

        try {
            sessionWrapper.getNode("/excluded/childOfExcluded");
            fail();
        } catch (PathNotFoundException expected) {
        }
    }

    @Test
    public void testGetItem() throws Exception {

        sessionWrapper.getItem("/included");
        sessionWrapper.getItem("/included/excludeChannels");
        sessionWrapper.getItem("/unspecified");

        try {
            sessionWrapper.getItem("/excluded");
            fail();
        } catch (PathNotFoundException expected) {
        }

        try {
            sessionWrapper.getItem("/excluded/excludeChannels");
            fail();
        } catch (PathNotFoundException expected) {
        }
    }

    @Test
    public void testGetProperty() throws Exception {

        sessionWrapper.getProperty("/included/excludeChannels");

        try {
            sessionWrapper.getProperty("/excluded/excludeChannels");
            fail();
        } catch (PathNotFoundException expected) {
        }
    }

    @Test
    public void testCanRemoveVisibleItem() throws Exception {
        sessionWrapper.removeItem("/unspecified");
        sessionWrapper.removeItem("/included/excludeChannels");
        sessionWrapper.removeItem("/included");
    }

    @Test
    public void testFailsToRemoveHiddenItem() throws Exception {
        try {
            sessionWrapper.removeItem("/excluded/childOfExcluded");
            fail();
        } catch (PathNotFoundException expected) {
        }
        try {
            sessionWrapper.removeItem("/excluded/excludeChannels");
            fail();
        } catch (PathNotFoundException expected) {
        }
        try {
            sessionWrapper.removeItem("/excluded");
            fail();
        } catch (PathNotFoundException expected) {
        }
    }

    @Test
    public void testMoveWorksOnVisibleNode() throws Exception {
        sessionWrapper.move("/unspecified", "/foobar");
    }

    @Test
    public void testFailsToMoveHiddenNode() throws Exception {
        try {
            sessionWrapper.move("/excluded", "/foobar");
            fail();
        } catch (PathNotFoundException expected) {
        }
    }

    @Test
    public void testRootNodeHidesExcludedNode() throws Exception {
        NodeIterator iterator = sessionWrapper.getRootNode().getNodes();
        while (iterator.hasNext()) {
            Node node = iterator.nextNode();
            if (node.getName().equals("excluded")) {
                fail();
            }
        }
    }

    @Test
    public void testNavigatingWithNodeParentStillHidesExcludedNode() throws Exception {

        Node unspecified = sessionWrapper.getRootNode().getNode("unspecified");
        Node root = unspecified.getParent();
        assertFalse(root.hasNode("excluded"));
        try {
            root.getNode("excluded");
            fail();
        } catch (PathNotFoundException expected) {
        }
    }
}
