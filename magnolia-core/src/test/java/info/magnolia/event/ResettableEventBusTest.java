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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Test case for {@link info.magnolia.event.ResettableEventBus}.
 */
public class ResettableEventBusTest {

    @Test
    public void testDoesNotDispatchToRemovedHandler() {

        // GIVEN
        ResettableEventBus eventBus = new ResettableEventBus(new SimpleEventBus());
        InvocationCountingTestEventHandler handler = new InvocationCountingTestEventHandler();
        HandlerRegistration registration = eventBus.addHandler(TestEvent.class, handler);
        registration.removeHandler();

        // WHEN
        eventBus.fireEvent(new TestEvent());

        // THEN
        assertEquals(0, handler.getInvocationCount());
    }

    @Test
    public void testCanReset() {

        // GIVEN
        ResettableEventBus eventBus = new ResettableEventBus(new SimpleEventBus());

        List<InvocationCountingTestEventHandler> handlers = new ArrayList<InvocationCountingTestEventHandler>();

        for (int i = 0; i < 50; i++) {
            InvocationCountingTestEventHandler handler = new InvocationCountingTestEventHandler();
            handlers.add(handler);
            eventBus.addHandler(TestEvent.class, handler);
        }

        eventBus.fireEvent(new TestEvent());

        for (InvocationCountingTestEventHandler handler : handlers) {
            assertEquals(1, handler.getInvocationCount());
        }

        // WHEN
        eventBus.reset();

        // THEN
        eventBus.fireEvent(new TestEvent());

        // verifies that the second event fired after resetting the event bus did not result in any more invocations
        for (InvocationCountingTestEventHandler handler : handlers) {
            assertEquals(1, handler.getInvocationCount());
        }
    }
}
