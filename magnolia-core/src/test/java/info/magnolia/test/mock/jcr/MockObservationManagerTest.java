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
package info.magnolia.test.mock.jcr;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class MockObservationManagerTest {

    /**
     * @version $Id$
     */
    private static class SimpleEventListener implements EventListener {

        private int invocationCount = 0;

        @Override
        public void onEvent(EventIterator events) {
            invocationCount++;
        }

        public int getInvocationCount() {
            return invocationCount;
        }

        public void setInvocationCount(int invocationCount) {
            this.invocationCount = invocationCount;
        }
    }

    @Test
    public void canAddRemoveAndSeeAddedListeners() throws RepositoryException {

        MockObservationManager om = new MockObservationManager();

        assertEquals(0, om.getRegisteredEventListeners().getSize());

        // Add a listener
        SimpleEventListener listener = new SimpleEventListener();
        om.addEventListener(listener, Event.NODE_ADDED, "/foo", false, null, null, false);
        assertEquals(1, om.getRegisteredEventListeners().getSize());
        assertSame(listener, om.getRegisteredEventListeners().nextEventListener());

        om.fireEvent(MockEvent.nodeAdded("/foo"));
        assertEquals(1, listener.getInvocationCount());
        om.fireEvent(MockEvent.nodeAdded("/foo/bar"));
        assertEquals(1, listener.getInvocationCount());

        // Add the same listener again and make sure it's not registered twice
        om.addEventListener(listener, Event.NODE_ADDED, "/bar", true, null, null, false);
        assertEquals(1, om.getRegisteredEventListeners().getSize());

        // Add another listener
        SimpleEventListener listener2 = new SimpleEventListener();
        om.addEventListener(listener2, Event.NODE_ADDED, "/bar", true, null, null, false);
        assertEquals(2, om.getRegisteredEventListeners().getSize());

        // Remove the first listener
        om.removeEventListener(listener);
        assertEquals(1, om.getRegisteredEventListeners().getSize());
        assertSame(listener2, om.getRegisteredEventListeners().nextEventListener());

        // Remove the second listener
        om.removeEventListener(listener2);
        assertEquals(0, om.getRegisteredEventListeners().getSize());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void willRefuseToFilterOnUuid() throws RepositoryException {
        MockObservationManager om = new MockObservationManager();
        om.addEventListener(new SimpleEventListener(), Event.NODE_ADDED, null, false, new String[]{}, null, false);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void willRefuseToFilterOnNodeType() throws RepositoryException {
        MockObservationManager om = new MockObservationManager();
        om.addEventListener(new SimpleEventListener(), Event.NODE_ADDED, null, false, null, new String[]{}, false);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void willRefuseToFilterLocalChanges() throws RepositoryException {
        MockObservationManager om = new MockObservationManager();
        om.addEventListener(new SimpleEventListener(), Event.NODE_ADDED, null, false, null, null, true);
    }

    @Test
    public void canFilterOnNodeType() throws RepositoryException {
        MockObservationManager om = new MockObservationManager();
        SimpleEventListener addListener = new SimpleEventListener();
        SimpleEventListener removeListener = new SimpleEventListener();

        om.addEventListener(addListener, Event.NODE_ADDED, null, false, null, null, false);
        om.addEventListener(removeListener, Event.NODE_REMOVED, null, false, null, null, false);

        // Event without node type
        om.fireEvent(new MockEvent());
        assertEquals(0, addListener.getInvocationCount());
        assertEquals(0, removeListener.getInvocationCount());

        om.fireEvent(MockEvent.nodeAdded("/foo"));
        assertEquals(1, addListener.getInvocationCount());
        assertEquals(0, removeListener.getInvocationCount());

        om.fireEvent(MockEvent.nodeRemoved("/foo"));
        assertEquals(1, addListener.getInvocationCount());
        assertEquals(1, removeListener.getInvocationCount());
    }

    @Test
    public void canFilterOnPath() throws RepositoryException {
        MockObservationManager om = new MockObservationManager();
        SimpleEventListener exact = new SimpleEventListener();
        SimpleEventListener sub = new SimpleEventListener();

        om.addEventListener(exact, Event.NODE_ADDED, "/foo/bar", false, null, null, false);
        om.addEventListener(sub, Event.NODE_ADDED, "/foo/bar", true, null, null, false);

        // Event without path
        MockEvent event = new MockEvent();
        event.setType(Event.NODE_ADDED);
        om.fireEvent(event);
        assertEquals(0, exact.getInvocationCount());
        assertEquals(0, sub.getInvocationCount());

        om.fireEvent(MockEvent.nodeAdded("/foo/bar"));
        assertEquals(1, exact.getInvocationCount());
        assertEquals(1, sub.getInvocationCount());

        om.fireEvent(MockEvent.nodeAdded("/foo/bar/zed"));
        assertEquals(1, exact.getInvocationCount());
        assertEquals(2, sub.getInvocationCount());

        om.fireEvent(MockEvent.nodeAdded("/foo"));
        assertEquals(1, exact.getInvocationCount());
        assertEquals(2, sub.getInvocationCount());

        om.fireEvent(MockEvent.nodeAdded("/somewhere/else"));
        assertEquals(1, exact.getInvocationCount());
        assertEquals(2, sub.getInvocationCount());
    }

    @Test
    public void setsUserDataInEvent() throws RepositoryException {
        MockObservationManager om = new MockObservationManager();
        final AtomicBoolean listenerTriggered = new AtomicBoolean();
        EventListener listener = new EventListener() {
            @Override
            public void onEvent(EventIterator events) {
                try {
                    assertEquals("magic", events.nextEvent().getUserData());
                    listenerTriggered.set(true);
                } catch (RepositoryException e) {
                    fail(e.getMessage());
                }
            }
        };

        om.setUserData("magic");
        om.addEventListener(listener, Event.NODE_ADDED, "/foo", false, null, null, false);
        om.fireEvent(MockEvent.nodeAdded("/foo"));
        assertTrue(listenerTriggered.get());
    }
}
