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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;

import junit.framework.TestCase;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class LazyNodeDataWrapperTest extends TestCase {

    public void testDoesNotCallHierarchyManagerUntilNeeded() {
        final HierarchyManager hm = createStrictMock(HierarchyManager.class);
        final NodeData nd = createMock(NodeData.class);
        final Workspace wks = createMock(Workspace.class);
        // just initialization
        expect(nd.getHierarchyManager()).andReturn(hm);
        expect(hm.getWorkspace()).andReturn(wks);
        expect(wks.getName()).andReturn("blah");
        expect(nd.getHandle()).andReturn("/baz/bar");
        replay(hm, nd, wks);
        final LazyNodeDataWrapper lazy = withHierarchyManager(hm, nd);
        // well we can't do much yet
        verify(hm, nd, wks);
    }

    public void testCallHierarchyManagerOnlyFirstTime() throws RepositoryException {
        final HierarchyManager hm = createMock(HierarchyManager.class);
        final NodeData nd = createMock(NodeData.class);
        final Workspace wks = createMock(Workspace.class);
        // just initialization
        expect(nd.getHierarchyManager()).andReturn(hm);
        expect(hm.getWorkspace()).andReturn(wks);
        expect(wks.getName()).andReturn("blah");
        expect(nd.getHandle()).andReturn("/baz/bar");

        // given the mocks below, we should not try to retrieve that prop from HM
        // but get the actual value twice
        expect(nd.getString()).andReturn("hello").times(2);

        // irrelevant mocks -- but let's pretend our nodedata's session is always live
        final Property p = createNiceMock(Property.class);
        final Session s = createNiceMock(Session.class);
        expect(nd.isExist()).andReturn(true).anyTimes();
        expect(s.isLive()).andReturn(true).anyTimes();
        expect(p.getSession()).andReturn(s).anyTimes();
        expect(nd.getJCRProperty()).andReturn(p).anyTimes();

        replay(hm, nd, p, s, wks);
        final LazyNodeDataWrapper lazy = withHierarchyManager(hm, nd);
        assertEquals("hello", lazy.getString());
        // let's call it a second time
        assertEquals("hello", lazy.getString());
        verify(hm, nd, p, s, wks);
    }

    public void testWorkOnDeadSession() throws RepositoryException {
        final HierarchyManager systemHM = createMock(HierarchyManager.class);
        final NodeData nd = createMock(NodeData.class);
        final Workspace wks = createMock(Workspace.class);
        // just initialization
        expect(nd.getHierarchyManager()).andReturn(systemHM);
        expect(systemHM.getWorkspace()).andReturn(wks);
        expect(wks.getName()).andReturn("blah");
        expect(nd.getHandle()).andReturn("/bar/baz");

        // but get the actual value twice
        expect(nd.getString()).andReturn("hello").times(3);

        final Property p = createNiceMock(Property.class);
        final Session propSession = createNiceMock(Session.class);
        final Session systemSession = createNiceMock(Session.class);
        expect(nd.isExist()).andReturn(true).anyTimes();

        // get the property from system because internal node data is null
        expect(systemHM.getNodeData("/bar/baz")).andReturn(nd);
        // session is NOT live for the first time it's called (2nd attempt to retrieve property) - this is why we need a lazy wrapper
        expect(p.getSession()).andReturn(propSession);
        expect(propSession.isLive()).andReturn(false);
        // get the property from system because internal node data claim to have invalid session
        expect(systemHM.getNodeData("/bar/baz")).andReturn(nd);

        // next time it was actually refreshed from the system HM so it should be live.
        expect(p.getSession()).andReturn(systemSession).anyTimes();
        expect(systemSession.isLive()).andReturn(true);
        expect(nd.getJCRProperty()).andReturn(p).anyTimes();


        Object[] mocks = new Object[] { systemHM, nd, p, propSession, systemSession, wks };
        replay(mocks);
        final LazyNodeDataWrapper lazy = withHierarchyManager(systemHM, nd);
        // first time the nodeData was null
        assertEquals("hello", lazy.getString());
        // let's call it a second time - the node data session is dead
        assertEquals("hello", lazy.getString());
        // let's call it a third time - the node data sessionshould be alive
        assertEquals("hello", lazy.getString());
        verify(mocks);
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
