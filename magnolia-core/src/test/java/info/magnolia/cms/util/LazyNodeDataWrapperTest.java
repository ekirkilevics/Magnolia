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

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;

import org.junit.Test;

/**
 * @version $Id$
 */
public class LazyNodeDataWrapperTest {

    @Test
    public void testDoesNotCallHierarchyManagerUntilNeeded() throws Exception {
        // GIVEN
        final HierarchyManager hm = mock(HierarchyManager.class);
        final NodeData nd = mock(NodeData.class);
        final Content content = mock(Content.class);
        final Workspace wks = mock(Workspace.class);
        // just initialization
        when(nd.getParent()).thenReturn(content);
        when(content.getWorkspace()).thenReturn(wks);
        when(wks.getName()).thenReturn("blah");
        when(nd.getHandle()).thenReturn("/baz/bar");

        // WHEN
        final LazyNodeDataWrapper lazy = withHierarchyManager(hm, nd);

        // THEN - well we can't do much yet
    }

    @Test
    public void testCallHierarchyManagerOnlyFirstTime() throws RepositoryException {
        // GIVEN
        final HierarchyManager hm = mock(HierarchyManager.class);
        final NodeData nd = mock(NodeData.class);
        final Workspace wks = mock(Workspace.class);
        // irrelevant mocks -- but let's pretend our nodedata's session is always live
        final Content content = mock(Content.class);
        final Session s = mock(Session.class);
        final Property p = mock(Property.class);
        // just initialization
        when(nd.getParent()).thenReturn(content);
        when(content.getWorkspace()).thenReturn(wks);
        when(wks.getName()).thenReturn("blah");
        when(wks.getSession()).thenReturn(s);
        when(nd.getHandle()).thenReturn("/baz/bar");

        // given the mocks below, we should not try to retrieve that prop from HM
        // but get the actual value twice
        when(nd.getString()).thenReturn("hello");

        when(nd.isExist()).thenReturn(true);
        when(s.isLive()).thenReturn(true);
        when(nd.getJCRProperty()).thenReturn(p);
        when(p.getSession()).thenReturn(s);

        // WHEN
        final LazyNodeDataWrapper lazy = withHierarchyManager(hm, nd);

        // THEN
        assertEquals("hello", lazy.getString());
        // let's call it a second time
        assertEquals("hello", lazy.getString());
    }

    @Test
    public void testWorkOnDeadSession() throws RepositoryException {
        // GIVEN
        final HierarchyManager systemHM = mock(HierarchyManager.class);
        final NodeData nd = mock(NodeData.class);
        final Content content = mock(Content.class);
        final Property p = mock(Property.class);
        final Session propSession = mock(Session.class);
        final Session systemSession = mock(Session.class);
        final Workspace wks = mock(Workspace.class);
        // just initialization
        when(nd.getParent()).thenReturn(content);
        when(content.getWorkspace()).thenReturn(wks);
        when(p.getSession()).thenReturn(propSession);
        when(wks.getName()).thenReturn("blah");
        when(nd.getHandle()).thenReturn("/bar/baz");

        // but get the actual value twice
        when(nd.getString()).thenReturn("hello");

        when(nd.isExist()).thenReturn(true);

        // get the property from system because internal node data is null
        when(systemHM.getNodeData("/bar/baz")).thenReturn(nd);
        // session is NOT live for the first time it's called (2nd attempt to retrieve property) - this is why we need a lazy wrapper
        when(p.getSession()).thenReturn(propSession);
        when(propSession.isLive()).thenReturn(false);
        // get the property from system because internal node data claim to have invalid session
        when(systemHM.getNodeData("/bar/baz")).thenReturn(nd);

        // next time it was actually refreshed from the system HM so it should be live.
        when(p.getSession()).thenReturn(systemSession);
        when(systemSession.isLive()).thenReturn(true);
        when(nd.getJCRProperty()).thenReturn(p);

        // WHEN
        final LazyNodeDataWrapper lazy = withHierarchyManager(systemHM, nd);

        // THEN
        // first time the nodeData was null
        assertEquals("hello", lazy.getString());
        // let's call it a second time - the node data session is dead
        assertEquals("hello", lazy.getString());
        // let's call it a third time - the node data session should be alive
        assertEquals("hello", lazy.getString());
    }

    private LazyNodeDataWrapper withHierarchyManager(final HierarchyManager hm, final NodeData nd) {
        return new LazyNodeDataWrapper(nd) {
            @Override
            public HierarchyManager getHierarchyManager() {
                return hm;
            }
        };
    }
}
