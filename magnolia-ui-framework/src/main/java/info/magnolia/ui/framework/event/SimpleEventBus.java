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

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * A very simplistic event bus.
 * Check event bus project: http://www.eventbus.org/
 */
public class SimpleEventBus implements EventBus {

    private static final Logger log = LoggerFactory.getLogger(SimpleEventBus.class);

    private final Multimap<Class<? extends Event>, EventHandler> eventHandlers;

    public SimpleEventBus() {
        eventHandlers = ArrayListMultimap.create();
    }

    public synchronized <H extends EventHandler> HandlerRegistration addHandler(final Class<? extends Event<H>> eventClass, final H handler) {
        log.debug("Adding handler {} for events of class {}", handler, eventClass);
        if (eventHandlers.containsEntry(eventClass, handler)) {
            return new HandlerRegistration() {
                public void removeHandler() {
                    synchronized (SimpleEventBus.this) {
                        eventHandlers.remove(eventClass, handler);
                    }
                }
            };
        }

        eventHandlers.put(eventClass, handler);

        return new HandlerRegistration() {
            public void removeHandler() {
                synchronized (SimpleEventBus.this) {
                    eventHandlers.remove(eventClass, handler);
                }
            }
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void fireEvent(Event event) {
        Collection<EventHandler> eventHandlersSnapshot;
        synchronized (this) {
            eventHandlersSnapshot = new ArrayList<EventHandler>(eventHandlers.get(event.getClass()));
        }
        for (EventHandler eventHandler : eventHandlersSnapshot) {
            log.debug("Dispatch event {} with handler {}", event, eventHandler);
            event.dispatch(eventHandler);
        }
    }
}
