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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple thread safe collection of handlers for a specific event.
 *
 * @param <H> type of the event handler
 */
public class EventHandlerCollection<H extends EventHandler> {

    private static final Logger log = LoggerFactory.getLogger(EventHandlerCollection.class);

    private final List<H> handlers = new CopyOnWriteArrayList<H>();

    public HandlerRegistration add(final H handler) {
        handlers.add(handler);
        return new HandlerRegistration() {

            @Override
            public void removeHandler() {
                remove(handler);
            }
        };
    }

    public void remove(H handler) {
        handlers.remove(handler);
    }

    public void dispatch(Event<H> event) {
        for (H eventHandler : handlers) {
            log.debug("Dispatching event {} with handler {}", event, eventHandler);
            try {
                event.dispatch(eventHandler);
            } catch (RuntimeException e) {
                log.warn("Exception caught when dispatching event: " + e.getMessage(), e);
            }
        }
    }

    public int size() {
        return handlers.size();
    }

    public boolean isEmpty() {
        return handlers.isEmpty();
    }
}
