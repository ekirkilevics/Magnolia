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
package info.magnolia.cms.util;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockUtil;
import static org.easymock.classextension.EasyMock.*;

import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

/**
 * @author had
 * @version $Id:$
 *
 */
public class ObservationUtilTest extends MgnlTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testFailRegisterWhenSessionInvalid() throws Exception {
        final HierarchyManager hm = createStrictMock(HierarchyManager.class);
        MockUtil.getSystemMockContext().addHierarchyManager("some-repo", hm);
        final Workspace wks = createStrictMock(Workspace.class);
        final Session session = createStrictMock(Session.class);
        expect(hm.getWorkspace()).andReturn(wks);
        expect(wks.getSession()).andReturn(session);
        expect(session.isLive()).andReturn(false);

        replay( hm, wks);
        try {
            ObservationUtil.registerChangeListener("some-repo", "/parent", new EventListener() {

                @Override
                public void onEvent(EventIterator events) {
                    // do nothing
                }});
            fail("Expected exception not thrown.");
        } catch (IllegalStateException e) {
            assertEquals("Observation manager can't be obtained due to invalid session.", e.getMessage());
        }

        verify( hm, wks);
    }

    public void testRegisterWhenSessionValid() throws Exception {
        final HierarchyManager hm = createStrictMock(HierarchyManager.class);
        MockUtil.getSystemMockContext().addHierarchyManager("some-repo", hm);
        final Workspace wks = createStrictMock(Workspace.class);
        final Session session = createStrictMock(Session.class);
        final ObservationManager observationManager = createStrictMock(ObservationManager.class);
        EventListener listener = new EventListener() {

            @Override
            public void onEvent(EventIterator events) {
                // do nothing
            }};
        expect(hm.getWorkspace()).andReturn(wks);
        expect(wks.getSession()).andReturn(session);
        expect(session.isLive()).andReturn(true);
        expect(wks.getObservationManager()).andReturn(observationManager);
        observationManager.addEventListener(listener, 31, "/parent", true, null, null, false);

        replay( hm, wks, session, observationManager);
        ObservationUtil.registerChangeListener("some-repo", "/parent", listener);
        verify( hm, wks, session, observationManager);
    }
    public void testDontFailUnRegisterWhenSessionInvalid() throws Exception {
        final HierarchyManager hm = createStrictMock(HierarchyManager.class);
        MockUtil.getSystemMockContext().addHierarchyManager("some-repo", hm);
        final Workspace wks = createStrictMock(Workspace.class);
        final Session session = createStrictMock(Session.class);
        expect(hm.getWorkspace()).andReturn(wks);
        expect(wks.getSession()).andReturn(session);
        expect(session.isLive()).andReturn(false);

        replay( hm, wks);
            ObservationUtil.unregisterChangeListener("some-repo", new EventListener() {

                @Override
                public void onEvent(EventIterator events) {
                    // do nothing
                }});
        verify( hm, wks);
    }

    public void testUnRegisterWhenSessionValid() throws Exception {
        final HierarchyManager hm = createStrictMock(HierarchyManager.class);
        MockUtil.getSystemMockContext().addHierarchyManager("some-repo", hm);
        final Workspace wks = createStrictMock(Workspace.class);
        final Session session = createStrictMock(Session.class);
        final ObservationManager observationManager = createStrictMock(ObservationManager.class);
        EventListener listener = new EventListener() {

            @Override
            public void onEvent(EventIterator events) {
                // do nothing
            }};
        expect(hm.getWorkspace()).andReturn(wks);
        expect(wks.getSession()).andReturn(session);
        expect(session.isLive()).andReturn(true);
        expect(wks.getObservationManager()).andReturn(observationManager);
        observationManager.removeEventListener(listener);

        replay( hm, wks, session, observationManager);
        ObservationUtil.unregisterChangeListener("some-repo", listener);
        verify( hm, wks, session, observationManager);
    }
}
