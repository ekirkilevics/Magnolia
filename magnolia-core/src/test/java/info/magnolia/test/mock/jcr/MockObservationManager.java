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

import java.util.IdentityHashMap;
import java.util.Map;
import javax.jcr.RepositoryException;
import javax.jcr.observation.EventJournal;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.EventListenerIterator;
import javax.jcr.observation.ObservationManager;

/**
 * @version $Id$
 */
public class MockObservationManager implements ObservationManager {

    private static class EventFilter {

        private int eventTypes;
        private String absPath;
        private boolean isDeep;

        private EventFilter(int eventTypes, String absPath, boolean isDeep) {
            this.eventTypes = eventTypes;
            this.absPath = absPath;
            this.isDeep = isDeep;
        }

        public boolean matches(MockEvent event) {
            if ((event.getType() & this.eventTypes) == 0) {
                return false;
            }
            if (this.absPath != null) {
                if (event.getPath() == null) {
                    return false;
                }
                if (isDeep && !event.getPath().startsWith(this.absPath)) {
                    return false;
                }
                if (!isDeep && !event.getPath().equals(this.absPath)) {
                    return false;
                }
            }
            return true;
        }
    }

    private IdentityHashMap<EventListener, EventFilter> listeners = new IdentityHashMap<EventListener, EventFilter>();
    private String userData;

    @Override
    public void addEventListener(EventListener listener, int eventTypes, String absPath, boolean isDeep, String[] uuid, String[] nodeTypeName, boolean noLocal) throws RepositoryException {
        if (uuid != null) {
            throw new UnsupportedOperationException("Filtering on uuid is not supported");
        }
        if (nodeTypeName != null) {
            throw new UnsupportedOperationException("Filtering on nodeTypeName is not supported");
        }
        if (noLocal) {
            throw new UnsupportedOperationException("Excluding events from the same session is not supported");
        }
        listeners.put(listener, new EventFilter(eventTypes, absPath, isDeep));
    }

    @Override
    public void removeEventListener(EventListener listener) throws RepositoryException {
        listeners.remove(listener);
    }

    @Override
    public EventListenerIterator getRegisteredEventListeners() throws RepositoryException {
        return new MockEventListenerIterator(listeners.keySet());
    }

    public void fireEvent(MockEvent event) {
        event.setUserData(this.userData);
        for (Map.Entry<EventListener, EventFilter> entry : listeners.entrySet()) {
            EventListener listener = entry.getKey();
            EventFilter filter = entry.getValue();
            if (filter.matches(event)) {
                listener.onEvent(new MockEventIterator(event));
            }
        }
    }

    @Override
    public void setUserData(String userData) throws RepositoryException {
        this.userData = userData;
    }

    @Override
    public EventJournal getEventJournal() throws RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }

    @Override
    public EventJournal getEventJournal(int eventTypes, String absPath, boolean isDeep, String[] uuid, String[] nodeTypeName) throws RepositoryException {
        throw new UnsupportedOperationException("Not implemented. This is a fake class.");
    }
}
