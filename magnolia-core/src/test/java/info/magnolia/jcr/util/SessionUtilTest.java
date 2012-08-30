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
package info.magnolia.jcr.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import info.magnolia.cms.core.version.MgnlVersioningSession;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockSession;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 */
public class SessionUtilTest {

    private final String WEBSITE = "website";

    @Before
    public void setUp() throws RepositoryException{
        MockUtil.initMockContext();
        MockSession session = new MockSession(WEBSITE);
        MockUtil.setSessionAndHierarchyManager(session);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void testHasSameUnderlyingSessionWithTwoWrappersOnSameSession() {
        // GIVEN
        final Session session = mock(Session.class);
        final Session wrapperOne = new MgnlVersioningSession(session);
        final Session wrapperTwo = new MgnlVersioningSession(session);

        // WHEN
        boolean result = SessionUtil.hasSameUnderlyingSession(wrapperOne, wrapperTwo);

        // THEN
        assertTrue(result);
    }

    @Test
    public void testHasSameUnderlyingSessionWithOneWrapperOnDifferentSession() {
        // GIVEN
        final Session jcrSession = mock(Session.class);
        final Session otherSession = mock(Session.class);

        final Session wrapperOne = new MgnlVersioningSession(jcrSession);

        // WHEN
        boolean result = SessionUtil.hasSameUnderlyingSession(otherSession, wrapperOne);

        // THEN
        assertFalse(result);
    }

    @Test
    public void testHasSameUnderlyingSessionWithTwoUnwrappedSessions() {
        // GIVEN
        final Session jcrSession = mock(Session.class);
        final Session otherSession = mock(Session.class);

        // WHEN
        boolean result = SessionUtil.hasSameUnderlyingSession(jcrSession, otherSession);

        // THEN
        assertFalse(result);
    }

    @Test
    public void testGetNode() throws RepositoryException {
        // GIVEN
        Node addedNode = MgnlContext.getJCRSession(WEBSITE).getRootNode().addNode("1");

        // WHEN
        Node returnedNode = SessionUtil.getNode(WEBSITE, addedNode.getPath());

        // THEN
        assertEquals(addedNode, returnedNode);
    }

    @Test
    public void testGetNodeNoSessionPassed() throws RepositoryException {
        // GIVEN
        Node addedNode = MgnlContext.getJCRSession(WEBSITE).getRootNode().addNode("1");

        // WHEN
        Node returnedNode = SessionUtil.getNode(null, addedNode.getPath());

        // THEN
        assertEquals(null, returnedNode);
    }

    @Test
    public void testGetNodeBadPath() throws RepositoryException {
        // GIVEN
        Node addedNode = MgnlContext.getJCRSession(WEBSITE).getRootNode().addNode("1");

        // WHEN
        Node returnedNode = SessionUtil.getNode(WEBSITE, addedNode.getPath() + 1);

        // THEN
        assertEquals(null, returnedNode);
    }

    public void testGetNodeBadSession() throws RepositoryException {
        // GIVEN
        Node addedNode = MgnlContext.getJCRSession(WEBSITE).getRootNode().addNode("1");

        // WHEN
        Node returnedNode = SessionUtil.getNode("dms", addedNode.getPath());

        // THEN
        assertEquals(null, returnedNode);
    }

    @Test
    public void testGetNodeByIdentifier() throws RepositoryException {
        // GIVEN
        Node addedNode = MgnlContext.getJCRSession(WEBSITE).getRootNode().addNode("1");

        // WHEN
        Node returnedNode = SessionUtil.getNodeByIdentifier(WEBSITE, addedNode.getIdentifier());

        // THEN
        assertEquals(addedNode, returnedNode);
    }

    @Test
    public void testGetNodeByIdentifierNoSessionPassed() throws RepositoryException {
        // GIVEN
        Node addedNode = MgnlContext.getJCRSession(WEBSITE).getRootNode().addNode("1");

        // WHEN
        Node returnedNode = SessionUtil.getNodeByIdentifier(null, addedNode.getIdentifier());

        // THEN
        assertEquals(null, returnedNode);
    }

    @Test
    public void testGetNodeByIdentifierBadId() throws RepositoryException {
        // GIVEN
        Node addedNode = MgnlContext.getJCRSession(WEBSITE).getRootNode().addNode("1");

        // WHEN
        Node returnedNode = SessionUtil.getNodeByIdentifier(WEBSITE, addedNode.getIdentifier() + 1);

        // THEN
        assertEquals(null, returnedNode);
    }

    public void testGetNodeByIdentifierBadSession() throws RepositoryException {
        // GIVEN
        Node addedNode = MgnlContext.getJCRSession(WEBSITE).getRootNode().addNode("1");

        // WHEN
        Node returnedNode = SessionUtil.getNodeByIdentifier(WEBSITE, addedNode.getIdentifier());

        // THEN
        assertEquals(null, returnedNode);
    }
}
