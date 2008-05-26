/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.context.SystemRepositoryStrategy;
import info.magnolia.context.ThreadReleasingSystemContext;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class ObservationUtil {
    private final static Logger log = LoggerFactory.getLogger(ObservationUtil.class);

    private static SystemRepositoryStrategy repositoryStrategy;

    static {
        SystemContext ctx = (SystemContext)MgnlContext.getSystemContext();
        if(ctx instanceof ThreadReleasingSystemContext){
            repositoryStrategy = new SystemRepositoryStrategy(ctx);
        }
    }

    /**
     * Registers an EventListener for any node type.
     * @see #registerChangeListener(String,String,boolean,String[],javax.jcr.observation.EventListener)
     */
    public static void registerChangeListener(String repository, String observationPath, EventListener listener) {
        registerChangeListener(repository, observationPath, true, listener);
    }

    /**
     * Registers an EventListener for any node type.
     * @see #registerChangeListener(String,String,boolean,String[],javax.jcr.observation.EventListener)
     */
    public static void registerChangeListener(String repository, String observationPath, boolean includeSubnodes, EventListener listener) {
        registerChangeListener(repository, observationPath, includeSubnodes, (String[]) null, listener);
    }

    /**
     * Registers an EventListener for a specific node type.
     * @see #registerChangeListener(String,String,boolean,String[],javax.jcr.observation.EventListener)
     */
    public static void registerChangeListener(String repository, String observationPath, boolean includeSubnodes, String nodeType, EventListener listener) {
        registerChangeListener(repository, observationPath, includeSubnodes, new String[]{nodeType}, listener);
    }

    /**
     * Register a single event listener, bound to the given path.
     * Be careful that if you observe "/", events are going to be generated for jcr:system, which is "shared" accross all workspaces.
     *
     * @param repository
     * @param observationPath repository path
     * @param includeSubnodes the isDeep parameter of ObservationManager.addEventListener()
     * @param nodeTypes the node types to filter events for
     * @param listener event listener
     * @see ObservationManager#addEventListener
     */
    public static void registerChangeListener(String repository, String observationPath, boolean includeSubnodes, String[] nodeTypes, EventListener listener) {
        log.debug("Registering event listener for path [{}]", observationPath); //$NON-NLS-1$

        try {
            ObservationManager observationManager = getObservationManager(repository);

            observationManager.addEventListener(listener, Event.NODE_ADDED
                    | Event.NODE_REMOVED
                    | Event.PROPERTY_ADDED
                    | Event.PROPERTY_CHANGED
                    | Event.PROPERTY_REMOVED, observationPath, includeSubnodes, null, nodeTypes, false);
        }
        catch (RepositoryException e) {
            log.error("Unable to add event listeners for " + observationPath, e); //$NON-NLS-1$
        }
    }

    /**
     * @deprecated since 3.6 - typo - use registerDeferredChangeListener
     */
    public static void registerDefferedChangeListener(String repository, String observationPath, EventListener listener, long delay, long maxDelay) {
        registerDeferredChangeListener(repository,  observationPath, listener, delay, maxDelay);
    }

    public static void registerDeferredChangeListener(String repository, String observationPath, EventListener listener, long delay, long maxDelay) {
        registerDeferredChangeListener(repository, observationPath, true, (String[]) null, listener, delay, maxDelay);
    }

    /**
     * @deprecated since 3.6 - typo - use registerDeferredChangeListener
     */
    public static void registerDefferedChangeListener(String repository, String observationPath, boolean includeSubnodes, EventListener listener, long delay, long maxDelay) {
        registerDeferredChangeListener(repository, observationPath, includeSubnodes, listener, delay, maxDelay);
    }

    public static void registerDeferredChangeListener(String repository, String observationPath, boolean includeSubnodes, EventListener listener, long delay, long maxDelay) {
        registerDeferredChangeListener(repository, observationPath, includeSubnodes, (String[]) null, listener, delay, maxDelay);
    }

    /**
     * @deprecated since 3.6 - typo - use registerDeferredChangeListener
     */
    public static void registerDefferedChangeListener(String repository, String observationPath, boolean includeSubnodes, String nodeType, EventListener listener, long delay, long maxDelay) {
        registerDeferredChangeListener(repository, observationPath, includeSubnodes, nodeType, listener, delay, maxDelay);
    }

    public static void registerDeferredChangeListener(String repository, String observationPath, boolean includeSubnodes, String nodeType, EventListener listener, long delay, long maxDelay) {
        registerDeferredChangeListener(repository, observationPath, includeSubnodes, new String[]{nodeType}, listener, delay, maxDelay);
    }

    /**
     * @deprecated since 3.6 - typo - use registerDeferredChangeListener
     */
    public static void registerDefferedChangeListener(String repository, String observationPath, boolean includeSubnodes, String[] nodeTypes, EventListener listener, long delay, long maxDelay) {
        registerDeferredChangeListener(repository, observationPath, includeSubnodes, nodeTypes, listener, delay, maxDelay);
    }

    /**
     * The event firing is deffered in case there is a serie of fired events
     * @return the wrapped EventListener so that one can unregister it.
     */
    public static void registerDeferredChangeListener(String repository, String observationPath, boolean includeSubnodes, String[] nodeTypes, EventListener listener, long delay, long maxDelay) {
        final EventListener deferedListener = instanciateDeferredEventListener(listener, delay, maxDelay);
        registerChangeListener(repository, observationPath, includeSubnodes, nodeTypes, deferedListener);
    }

    /**
     * Use this and register the returned EventListener with the registerChangeListener() methods, if
     * you need to be able to later unregister your EventListener.
     */
    public static EventListener instanciateDeferredEventListener(EventListener listener, long delay, long maxDelay) {
        return new DeferringEventListener(listener, delay, maxDelay);
    }

    public static void unregisterChangeListener(String repository, EventListener listener) {
        try {
            getObservationManager(repository).removeEventListener(listener);
        } catch (RepositoryException e) {
            log.error("Unable to remove event listener [" + listener + "] from repository " + repository, e);
        }
    }

    private static ObservationManager getObservationManager(String repository) throws RepositoryException {
        return getHierarchyManager(repository)
                .getWorkspace()
                .getObservationManager();
    }

    private static HierarchyManager getHierarchyManager(String repository) {
        if(repositoryStrategy != null){
            // we have our own sessinos
            return repositoryStrategy.getHierarchyManager(repository, ContentRepository.getDefaultWorkspace(repository));
        }
        else{
            return MgnlContext.getSystemContext().getHierarchyManager(repository);
        }
    }

    public static class DeferringEventListener implements EventListener {

        private ObservationBasedDelayedExecutor executor;

        private EventListener listener;

        public DeferringEventListener(EventListener listener, long delay, long maxDelay) {
            this.listener = listener;
            executor = new ObservationBasedDelayedExecutor(listener, delay, maxDelay);
        }

        public void onEvent(EventIterator events) {
            this.executor.consume(events);
        }

        public String toString() {
            return super.toString() + ":" + this.listener;
        }
    }

    /**
     * Defered event handling. Uses the DelayedExecutor class
     */
    public static class ObservationBasedDelayedExecutor {
        private final DelayedExecutor delayedExecutor;
        private final List eventsBuffer = new ArrayList();

        public ObservationBasedDelayedExecutor(final EventListener listener, long delay, long maxDelay) {
            delayedExecutor = new DelayedExecutor(new Runnable(){
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
                while(events.hasNext()) {
                    this.eventsBuffer.add(events.next());
                }
                delayedExecutor.trigger();
            }
        }
    }

    /**
     * List based event iterator. Used to collect events in a list which are later on passed to the listener.
     */
    public static class ListBasedEventIterator implements EventIterator {
        private Iterator iterator;
        private List events;
        private int pos = 0;

        public ListBasedEventIterator(List events) {
            this.events = events;
            this.iterator = events.iterator();
        }

        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        public Object next() {
            pos ++;
            return this.iterator.next();
        }

        public void remove() {
            this.iterator.remove();
        }

        public Event nextEvent() {
            return (Event) next();
        }

        public long getPosition() {
            return pos;
        }

        public long getSize() {
            return events.size();
        }

        public void skip(long skipNum) {
            for(int i=0; i< skipNum; i++){
                next();
            }
        }
    }

}
