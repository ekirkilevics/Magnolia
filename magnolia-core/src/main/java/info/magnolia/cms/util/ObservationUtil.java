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

import info.magnolia.context.LifeTimeJCRSessionUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

import org.apache.jackrabbit.core.observation.ObservationManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Util to register JCR observations. Supports delayed execution of the listener to handle event bursts.
 *
 * @version $Id$
 */
public class ObservationUtil {

    private final static Logger log = LoggerFactory.getLogger(ObservationUtil.class);

    private static final int ALL_EVENT_TYPES_MASK = Event.NODE_ADDED | Event.NODE_REMOVED | Event.NODE_MOVED | Event.PROPERTY_ADDED | Event.PROPERTY_CHANGED | Event.PROPERTY_REMOVED;


    /**
     * Unregisters all EventListeners for the given workspace.
     */
    public static void dispose(String workspace) {
        try {
            ObservationManager om = getObservationManager(workspace);

            if(om instanceof ObservationManagerImpl) {
                ((ObservationManagerImpl)om).dispose();
            } else {
                log.warn("Observation can not be disposed ");
            }
        } catch (RepositoryException e) {
            log.error("Unable to Unregisters all EventListeners for the following workspace "+workspace, e);
        }
    }


    /**
     * Registers an EventListener for any node type.
     *
     * @see #registerChangeListener(String,String,boolean,String[],javax.jcr.observation.EventListener)
     */
    public static void registerChangeListener(String workspace, String observationPath, EventListener listener) {
        registerChangeListener(workspace, observationPath, true, listener);
    }

    /**
     * Registers an EventListener for any node type.
     *
     * @see #registerChangeListener(String,String,boolean,String[],javax.jcr.observation.EventListener)
     */
    public static void registerChangeListener(String workspace, String observationPath, boolean includeSubnodes, EventListener listener) {
        registerChangeListener(workspace, observationPath, includeSubnodes, (String[]) null, listener);
    }

    /**
     * Registers an EventListener for a specific node type.
     *
     * @see #registerChangeListener(String,String,boolean,String[],javax.jcr.observation.EventListener)
     */
    public static void registerChangeListener(String workspace, String observationPath, boolean includeSubnodes, String nodeType, EventListener listener) {
        registerChangeListener(workspace, observationPath, includeSubnodes, new String[] { nodeType }, listener);
    }

    /**
     * Registers an EventListener for a specific node type and event types.
     *
     * @see #registerChangeListener(String,String,boolean,String[],int,javax.jcr.observation.EventListener)
     */
    public static void registerChangeListener(String workspace, String observationPath, boolean includeSubnodes, String nodeType, int eventTypesMask, EventListener listener) {
        if (nodeType == null) {
            registerChangeListener(workspace, observationPath, includeSubnodes, (String[]) null, eventTypesMask, listener);
        } else {
            registerChangeListener(workspace, observationPath, includeSubnodes, new String[] { nodeType }, eventTypesMask, listener);
        }
    }

    /**
     * Registers an EventListener for a specific set of node types and event types.
     *
     * @see #registerChangeListener(String, String, boolean, String[], int, javax.jcr.observation.EventListener)
     */
    public static void registerChangeListener(String workspace, String observationPath, boolean includeSubnodes, String[] nodeTypes, EventListener listener) {
        registerChangeListener(workspace, observationPath, includeSubnodes, nodeTypes, ALL_EVENT_TYPES_MASK, listener);
    }

    /**
     * Register a single event listener, bound to the given path. Be careful
     * that if you observe "/", events are going to be generated for jcr:system,
     * which is "shared" across all workspaces.
     *
     * @param workspace
     * @param observationPath workspace path
     * @param includeSubnodes the isDeep parameter of ObservationManager.addEventListener()
     * @param nodeTypes the node types to filter events for
     * @param eventTypesMask an ORed mask of even types (Event constants)
     * @param listener event listener
     * @see ObservationManager#addEventListener
     * @see Event
     */
    public static void registerChangeListener(String workspace, String observationPath, boolean includeSubnodes, String[] nodeTypes, int eventTypesMask, EventListener listener) {
        log.debug("Registering event listener for path [{}]", observationPath); //$NON-NLS-1$

        try {
            ObservationManager observationManager = getObservationManager(workspace);
            if (observationManager == null) {
                log.error("Unable to add event listeners for " + observationPath); //$NON-NLS-1$
                throw new IllegalStateException("Observation manager can't be obtained due to invalid session.");
            }

            observationManager.addEventListener(listener, eventTypesMask, observationPath, includeSubnodes, null, nodeTypes, false);
        } catch (RepositoryException e) {
            log.error("Unable to add event listeners for " + observationPath, e); //$NON-NLS-1$
        }
    }

    public static void registerDeferredChangeListener(String workspace, String observationPath, EventListener listener, long delay, long maxDelay) {
        registerDeferredChangeListener(workspace, observationPath, true, (String[]) null, listener, delay, maxDelay);
    }

    public static void registerDeferredChangeListener(String workspace, String observationPath, boolean includeSubnodes, EventListener listener, long delay, long maxDelay) {
        registerDeferredChangeListener(workspace, observationPath, includeSubnodes, (String[]) null, listener, delay, maxDelay);
    }

    public static void registerDeferredChangeListener(String workspace, String observationPath, boolean includeSubnodes, String nodeType, EventListener listener, long delay, long maxDelay) {
        registerDeferredChangeListener(workspace, observationPath, includeSubnodes, new String[] { nodeType }, listener, delay, maxDelay);
    }

    /**
     * The event firing is deferred in case there is a series of fired events.
     *
     * @return the wrapped EventListener so that one can unregister it.
     */
    public static void registerDeferredChangeListener(String workspace, String observationPath, boolean includeSubnodes, String[] nodeTypes, EventListener listener, long delay, long maxDelay) {
        final EventListener deferredListener = instanciateDeferredEventListener(listener, delay, maxDelay);
        registerChangeListener(workspace, observationPath, includeSubnodes, nodeTypes, deferredListener);
    }

    /**
     * Use this and register the returned EventListener with the
     * registerChangeListener() methods, if you need to be able to later
     * unregister your EventListener.
     */
    public static EventListener instanciateDeferredEventListener(EventListener listener, long delay, long maxDelay) {
        return new DeferringEventListener(listener, delay, maxDelay);
    }

    public static void unregisterChangeListener(String workspace, EventListener listener) {
        try {
            ObservationManager om = getObservationManager(workspace);
            if (om == null) {
                // session have been invalidated and Observation manager is disconnected. Nothing to unregister. Bail out.
                return;
            }
            om.removeEventListener(listener);
        } catch (RepositoryException e) {
            log.error("Unable to remove event listener [" + listener + "] from workspace " + workspace, e);
        }
    }

    private static ObservationManager getObservationManager(String workspace) throws RepositoryException {
        Workspace wks = getSession(workspace).getWorkspace();
        return wks.getSession().isLive() ? wks.getObservationManager() : null;
    }

    private static Session getSession(String workspace) throws RepositoryException {
        return LifeTimeJCRSessionUtil.getSession(workspace);
    }

    /**
     * A listener using an {@link ObservationBasedDelayedExecutor}.
     */
    public static class DeferringEventListener implements EventListener {

        private ObservationBasedDelayedExecutor executor;

        private EventListener listener;

        public DeferringEventListener(EventListener listener, long delay, long maxDelay) {
            this.listener = listener;
            executor = new ObservationBasedDelayedExecutor(listener, delay, maxDelay);
        }

        @Override
        public void onEvent(EventIterator events) {
            this.executor.consume(events);
        }

        @Override
        public String toString() {
            return super.toString() + ":" + this.listener;
        }
    }

    /**
     * Deferred event handling. Uses the DelayedExecutor class.
     */
    public static class ObservationBasedDelayedExecutor {

        private final DelayedExecutor delayedExecutor;
        private final List eventsBuffer = new ArrayList();

        public ObservationBasedDelayedExecutor(final EventListener listener, long delay, long maxDelay) {
            delayedExecutor = new DelayedExecutor(new Runnable() {
                @Override
                public void run() {
                    // during execution consume is blocked
                    synchronized (eventsBuffer) {
                        listener.onEvent(new ListBasedEventIterator(eventsBuffer));
                        eventsBuffer.clear();
                    }
                }
            }, delay, maxDelay);
        }

        protected void consume(EventIterator events) {
            synchronized (this.eventsBuffer) {
                while (events.hasNext()) {
                    this.eventsBuffer.add(events.next());
                }
                delayedExecutor.trigger();
            }
        }
    }

    /**
     * List based event iterator. Used to collect events in a list which are
     * later on passed to the listener.
     */
    public static class ListBasedEventIterator implements EventIterator {

        private Iterator iterator;
        private List events;
        private int pos = 0;

        public ListBasedEventIterator(List events) {
            this.events = events;
            this.iterator = events.iterator();
        }

        @Override
        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        @Override
        public Object next() {
            pos++;
            return this.iterator.next();
        }

        @Override
        public void remove() {
            this.iterator.remove();
        }

        @Override
        public Event nextEvent() {
            return (Event) next();
        }

        @Override
        public long getPosition() {
            return pos;
        }

        @Override
        public long getSize() {
            return events.size();
        }

        @Override
        public void skip(long skipNum) {
            for (int i = 0; i < skipNum; i++) {
                next();
            }
        }
    }

}
