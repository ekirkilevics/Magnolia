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
package info.magnolia.ui.framework.event;

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
 *
 * @author tmattsson
 */
public class SimpleEventBusTest {

    private static class TestEvent implements Event<TestEventHandler> {
        @Override
        public void dispatch(TestEventHandler handler) {
            handler.handleEvent(this);
        }
    }

    private static class TestEventHandler implements EventHandler {

        private int invocationCount = 0;

        public synchronized int getInvocationCount() {
            return invocationCount;
        }

        public synchronized void handleEvent(TestEvent event) {
            invocationCount++;
        }
    }

    private static class RemoveEventHandler extends TestEventHandler {

        HandlerRegistration handlerRegistration;

        @Override
        public void handleEvent(TestEvent event) {
            super.handleEvent(event);
            handlerRegistration.removeHandler();
        }
    }

    private static class InvocationOrderTestingHandler extends TestEventHandler {

        int expectedOrder;
        int actualOrder;
        AtomicInteger counter;

        public InvocationOrderTestingHandler(int expectedOrder, AtomicInteger counter) {
            this.expectedOrder = expectedOrder;
            this.counter = counter;
        }

        @Override
        public void handleEvent(TestEvent event) {
            super.handleEvent(event);
            this.actualOrder = counter.getAndIncrement();
        }
    }

    @Test
    public void testAreHandlersInvokedInOrder() {
        SimpleEventBus eventBus = new SimpleEventBus();
        List<InvocationOrderTestingHandler> handlers = new ArrayList<InvocationOrderTestingHandler>();
        AtomicInteger counter = new AtomicInteger(0);
        for (int i = 0; i < 1000; i++) {
            InvocationOrderTestingHandler handler = new InvocationOrderTestingHandler(i, counter);
            handlers.add(handler);
            eventBus.addHandler(TestEvent.class, handler);
        }
        eventBus.fireEvent(new TestEvent());
        for (InvocationOrderTestingHandler handler : handlers) {
            assertEquals(1, handler.getInvocationCount());
            assertEquals(handler.expectedOrder, handler.actualOrder);
        }
    }

    @Test
    public void testHandlerAddedWhileDispatchingIsNotCalled() {
        final SimpleEventBus eventBus = new SimpleEventBus();
        final TestEventHandler handler2 = new TestEventHandler();
        TestEventHandler handler1 = new TestEventHandler() {
            @Override
            public void handleEvent(TestEvent event) {
                super.handleEvent(event);
                eventBus.addHandler(TestEvent.class, handler2);
            }
        };
        eventBus.addHandler(TestEvent.class, handler1);
        eventBus.fireEvent(new TestEvent());
        assertEquals(1, handler1.getInvocationCount());
        assertEquals(0, handler2.getInvocationCount());
    }

    @Test
    public void testHandlerRemovedWhileDispatchingIsCalled() {
        SimpleEventBus eventBus = new SimpleEventBus();
        RemoveEventHandler handler1 = new RemoveEventHandler();
        TestEventHandler handler2 = new TestEventHandler();

        eventBus.addHandler(TestEvent.class, handler1);
        handler1.handlerRegistration = eventBus.addHandler(TestEvent.class, handler2);

        eventBus.fireEvent(new TestEvent());

        assertEquals(1, handler1.getInvocationCount());
        assertEquals(1, handler2.getInvocationCount());
    }

    @Test
    public void testMultipleRegistrationOfTheSameHandlerWillNotResultInMultipleInvocations() {

        SimpleEventBus eventBus = new SimpleEventBus();
        TestEventHandler handler = new TestEventHandler();
        eventBus.addHandler(TestEvent.class, handler);
        eventBus.addHandler(TestEvent.class, handler);
        eventBus.addHandler(TestEvent.class, handler);
        eventBus.addHandler(TestEvent.class, handler);

        eventBus.fireEvent(new TestEvent());

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

                            List<TestEventHandler> handlers = new ArrayList<TestEventHandler>();
                            List<HandlerRegistration> registrations = new ArrayList<HandlerRegistration>();

                            // Add handlers
                            for (int j = 0; j < numberOfHandlers; j++) {
                                TestEventHandler handler = new TestEventHandler();
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
                            for (TestEventHandler handler : handlers) {
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
