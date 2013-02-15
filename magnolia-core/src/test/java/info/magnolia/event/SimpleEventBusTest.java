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
package info.magnolia.event;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for SimpleEventBus.
 */
public class SimpleEventBusTest {

    private static class RemoveEventHandler extends InvocationCountingTestEventHandler {

        HandlerRegistration handlerRegistration;

        @Override
        public void handleEvent(TestEvent event) {
            super.handleEvent(event);
            handlerRegistration.removeHandler();
        }
    }

    @Test
    public void testAreHandlersInvokedInOrder() {

        // GIVEN
        SimpleEventBus eventBus = new SimpleEventBus();
        List<InvocationOrderTestingEventHandler> handlers = new ArrayList<InvocationOrderTestingEventHandler>();
        AtomicInteger counter = new AtomicInteger(0);
        for (int i = 0; i < 1000; i++) {
            InvocationOrderTestingEventHandler handler = new InvocationOrderTestingEventHandler(i, counter);
            handlers.add(handler);
            eventBus.addHandler(TestEvent.class, handler);
        }

        // WHEN
        eventBus.fireEvent(new TestEvent());

        // THEN
        for (InvocationOrderTestingEventHandler handler : handlers) {
            assertEquals(1, handler.getInvocationCount());
            assertEquals(handler.expectedOrder, handler.actualOrder);
        }
    }

    @Test
    public void testHandlerAddedWhileDispatchingIsNotCalled() {

        // GIVEN
        final SimpleEventBus eventBus = new SimpleEventBus();
        final InvocationCountingTestEventHandler handler2 = new InvocationCountingTestEventHandler();
        InvocationCountingTestEventHandler handler1 = new InvocationCountingTestEventHandler() {
            @Override
            public void handleEvent(TestEvent event) {
                super.handleEvent(event);
                eventBus.addHandler(TestEvent.class, handler2);
            }
        };
        eventBus.addHandler(TestEvent.class, handler1);

        // WHEN
        eventBus.fireEvent(new TestEvent());

        // THEN
        assertEquals(1, handler1.getInvocationCount());
        assertEquals(0, handler2.getInvocationCount());
    }

    @Test
    public void testHandlerRemovedWhileDispatchingIsCalled() {

        // GIVEN
        SimpleEventBus eventBus = new SimpleEventBus();
        RemoveEventHandler handler1 = new RemoveEventHandler();
        InvocationCountingTestEventHandler handler2 = new InvocationCountingTestEventHandler();

        eventBus.addHandler(TestEvent.class, handler1);
        handler1.handlerRegistration = eventBus.addHandler(TestEvent.class, handler2);

        // WHEN
        eventBus.fireEvent(new TestEvent());

        // THEN
        assertEquals(1, handler1.getInvocationCount());
        assertEquals(1, handler2.getInvocationCount());
    }

    @Test
    public void testMultipleRegistrationOfTheSameHandlerWillNotResultInMultipleInvocations() {

        // GIVEN
        SimpleEventBus eventBus = new SimpleEventBus();
        InvocationCountingTestEventHandler handler = new InvocationCountingTestEventHandler();
        eventBus.addHandler(TestEvent.class, handler);
        eventBus.addHandler(TestEvent.class, handler);
        eventBus.addHandler(TestEvent.class, handler);
        eventBus.addHandler(TestEvent.class, handler);

        // WHEN
        eventBus.fireEvent(new TestEvent());

        // THEN
        assertEquals(1, handler.getInvocationCount());
    }

    @Test
    public void testMultipleThreads() throws InterruptedException {

        final int numberOfThreads = 20;
        final int numberOfIterations = 50;
        final int numberOfHandlers = 200;

        final SimpleEventBus eventBus = new SimpleEventBus();
        final CountDownLatch startSignal = new CountDownLatch(1);
        final AtomicBoolean threadEncounteredException = new AtomicBoolean(false);

        List<Thread> threads = new ArrayList<Thread>();
        for (int i = 0; i < numberOfThreads; i++) {
            threads.add(new Thread() {
                @Override
                public void run() {
                    try {
                        startSignal.await();
                        for (int i = 0; i < numberOfIterations; i++) {

                            List<InvocationCountingTestEventHandler> handlers = new ArrayList<InvocationCountingTestEventHandler>();
                            List<HandlerRegistration> registrations = new ArrayList<HandlerRegistration>();

                            // Add handlers
                            for (int j = 0; j < numberOfHandlers; j++) {
                                InvocationCountingTestEventHandler handler = new InvocationCountingTestEventHandler();
                                handlers.add(handler);
                                registrations.add(eventBus.addHandler(TestEvent.class, handler));
                            }

                            // Fire event
                            eventBus.fireEvent(new TestEvent());

                            // Remove handlers
                            for (HandlerRegistration handler : registrations) {
                                handler.removeHandler();
                            }

                            // Make sure that every handler was invoked at least once
                            for (InvocationCountingTestEventHandler handler : handlers) {
                                Assert.assertTrue(handler.getInvocationCount() > 0);
                            }
                        }
                    } catch (Throwable e) {
                        threadEncounteredException.set(true);
                        e.printStackTrace();
                    }
                }
            });
        }
        for (Thread thread : threads) {
            thread.start();
        }
        startSignal.countDown();
        for (Thread thread : threads) {
            thread.join();
        }
        assertFalse(threadEncounteredException.get());
    }
}
