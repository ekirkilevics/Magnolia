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
package info.magnolia.event;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

/**
 * Test case for {@link info.magnolia.event.EventHandlerCollection}.
 */
public class EventHandlerCollectionTest {

    @Test
    public void testCanDispatchEvent() {

        // GIVEN
        EventHandlerCollection<TestEventHandler> collection = new EventHandlerCollection<TestEventHandler>();
        InvocationCountingTestEventHandler eventHandler = new InvocationCountingTestEventHandler();

        assertTrue(collection.isEmpty());
        assertEquals(0, collection.size());

        collection.add(eventHandler);

        assertFalse(collection.isEmpty());
        assertEquals(1, collection.size());

        // WHEN
        collection.dispatch(new TestEvent());

        // THEN
        assertEquals(1, eventHandler.getInvocationCount());
    }

    @Test
    public void testCanRemoveHandler() {

        // GIVEN
        EventHandlerCollection<TestEventHandler> collection = new EventHandlerCollection<TestEventHandler>();
        InvocationCountingTestEventHandler eventHandler = new InvocationCountingTestEventHandler();
        collection.add(eventHandler);
        collection.dispatch(new TestEvent());

        assertEquals(1, eventHandler.getInvocationCount());

        collection.remove(eventHandler);

        // WHEN
        collection.dispatch(new TestEvent());

        // THEN (still 1)
        assertEquals(1, eventHandler.getInvocationCount());
    }

    @Test
    public void testCanRemoveHandlerViaReturnedRegistrationObject() {

        // GIVEN
        EventHandlerCollection<TestEventHandler> collection = new EventHandlerCollection<TestEventHandler>();
        InvocationCountingTestEventHandler eventHandler = new InvocationCountingTestEventHandler();
        HandlerRegistration registration = collection.add(eventHandler);

        collection.dispatch(new TestEvent());

        assertEquals(1, eventHandler.getInvocationCount());

        registration.removeHandler();

        // WHEN
        collection.dispatch(new TestEvent());

        // THEN (still 1)
        assertEquals(1, eventHandler.getInvocationCount());
    }

    @Test
    public void testHandlersAreInvokedInOrder() {

        // GIVEN
        EventHandlerCollection<TestEventHandler> collection = new EventHandlerCollection<TestEventHandler>();

        List<InvocationOrderTestingEventHandler> handlers = new ArrayList<InvocationOrderTestingEventHandler>();
        AtomicInteger counter = new AtomicInteger(0);
        for (int i = 0; i < 1000; i++) {
            InvocationOrderTestingEventHandler handler = new InvocationOrderTestingEventHandler(i, counter);
            handlers.add(handler);
            collection.add(handler);
        }

        // WHEN
        collection.dispatch(new TestEvent());

        // THEN
        for (InvocationOrderTestingEventHandler handler : handlers) {
            assertEquals(1, handler.getInvocationCount());
            assertEquals(handler.expectedOrder, handler.actualOrder);
        }
    }

    @Test
    public void testHandlersAddedWhileDispatchedAreNotInvoked() {

        // GIVEN
        final EventHandlerCollection<TestEventHandler> collection = new EventHandlerCollection<TestEventHandler>();
        final InvocationCountingTestEventHandler secondHandler = new InvocationCountingTestEventHandler();
        InvocationCountingTestEventHandler firstHandler = new InvocationCountingTestEventHandler() {
            @Override
            public synchronized void handleEvent(TestEvent event) {
                super.handleEvent(event);
                collection.add(secondHandler);
            }
        };
        collection.add(firstHandler);

        // WHEN
        collection.dispatch(new TestEvent());

        // THEN
        assertEquals(1, firstHandler.getInvocationCount());
        assertEquals(0, secondHandler.getInvocationCount());
    }

    @Test
    public void testHandlersRemovedWhileDispatchedAreStillInvoked() {

        // GIVEN
        final EventHandlerCollection<TestEventHandler> collection = new EventHandlerCollection<TestEventHandler>();
        final InvocationCountingTestEventHandler secondHandler = new InvocationCountingTestEventHandler();
        InvocationCountingTestEventHandler firstHandler = new InvocationCountingTestEventHandler() {
            @Override
            public synchronized void handleEvent(TestEvent event) {
                super.handleEvent(event);
                collection.remove(secondHandler);
            }
        };
        collection.add(firstHandler);
        collection.add(secondHandler);

        // WHEN
        collection.dispatch(new TestEvent());

        // THEN
        assertEquals(1, collection.size());
        assertEquals(1, firstHandler.getInvocationCount());
        assertEquals(1, secondHandler.getInvocationCount());
    }
}
