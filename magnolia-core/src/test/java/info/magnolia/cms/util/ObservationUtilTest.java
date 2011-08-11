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

import static org.junit.Assert.*;

import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

import org.junit.Test;

import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.test.mock.jcr.MockWorkspace;
import static org.mockito.Mockito.*;

/**
 * @version $Id$
 */
public class ObservationUtilTest extends MgnlTestCase {

    @Test
    public void testFailRegisterWhenSessionInvalid() throws Exception {

        setupMockSession(false);

        try {
            ObservationUtil.registerChangeListener("some-repo", "/parent", new EventListener() {

                @Override
                public void onEvent(EventIterator events) {
                    // do nothing
                }
            });
            fail("Expected exception not thrown.");
        } catch (IllegalStateException e) {
            assertEquals("Observation manager can't be obtained due to invalid session.", e.getMessage());
        }
    }

    @Test
    public void testRegisterWhenSessionValid() throws Exception {

        MockSession session = setupMockSession(true);
        ObservationManager observationManager = setupMockObservationManager(session);

        EventListener listener = new EventListener() {

            @Override
            public void onEvent(EventIterator events) {
                // do nothing
            }
        };

        ObservationUtil.registerChangeListener("some-repo", "/parent", listener);

        verify(observationManager).addEventListener(listener, 63, "/parent", true, null, null, false);
    }

    @Test
    public void testDontFailUnregisterWhenSessionInvalid() throws Exception {

        MockSession session = setupMockSession(false);
        ObservationManager observationManager = setupMockObservationManager(session);

        EventListener listener = new EventListener() {

            @Override
            public void onEvent(EventIterator events) {
                // do nothing
            }
        };

        ObservationUtil.unregisterChangeListener("some-repo", listener);

        verify(observationManager, times(0)).removeEventListener(listener);
    }

    @Test
    public void testUnregisterWhenSessionValid() throws Exception {

        MockSession session = setupMockSession(true);
        ObservationManager observationManager = setupMockObservationManager(session);

        EventListener listener = new EventListener() {

            @Override
            public void onEvent(EventIterator events) {
                // do nothing
            }
        };

        ObservationUtil.unregisterChangeListener("some-repo", listener);

        verify(observationManager).removeEventListener(listener);
    }

    private MockSession setupMockSession(boolean live) {
        MockSession session = new MockSession("some-repo");
        session.setLive(live);
        MockUtil.getSystemMockContext().addSession(session.getWorkspace().getName(), session);
        return session;
    }

    private ObservationManager setupMockObservationManager(MockSession session) {
        ObservationManager observationManager = mock(ObservationManager.class);
        MockWorkspace workspace = (MockWorkspace) session.getWorkspace();
        workspace.setObservationManager(observationManager);
        return observationManager;
    }
}
