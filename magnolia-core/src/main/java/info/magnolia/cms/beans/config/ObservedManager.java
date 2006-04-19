/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.beans.config;

import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.util.ObservationUtil;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A lot of manager are observed. Will mean that they reload the registered content after the content was changed. To
 * centralize this code we use this abstract manager. A subclass will implement onRegister and onClear.
 * @author philipp
 */
public abstract class ObservedManager {

    /**
     * Logger
     */
    protected Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Count of reload request
     */
    private int reloadRequestCount = 0;

    /**
     * True if this manager is realoading. used to avoid cycles.
     */
    private boolean reloading = false;

    /**
     * milli second the Reloader Thread sleeps
     */
	private static final long SLEEP_MILLIS = 1000;
    /**
     * UUIDs of the registered main nodes. They will get registered again after a change.
     */
    protected Set registeredUUIDs = new HashSet();

    /**
     * Register a node. The uuid is cached and then onRegister() called.
     * @param node the node to register
     */
    public synchronized void register(Content node) {
        if (node == null) {
            log.warn("tried to register a not existing node!");
            return;
        }

        ObservationUtil.registerChangeListener(ContentRepository.CONFIG, node.getHandle(), new EventListener() {

            public void onEvent(EventIterator events) {
                reload();
            }
        });

        try {
            registeredUUIDs.add(node.getUUID());
            onRegister(node);
        }
        catch (Exception e) {
            Paragraph.log.warn("Was not able to register [" + node.getHandle() + "]", e);
        }
    }

    /**
     * Calls onClear and reregister the nodes by calling onRegister
     */
    public synchronized void reload() {
        // if recalled in the same thread only
        if (this.reloading == true) {
            log.debug("this manager waiting for reloading: [{}]", this.getClass().getName());
            this.reloadRequestCount++;
            return;
        }
        setReloading(true);
        this.reloadRequestCount = 0;
        Reloader reloader = new Reloader(this, this.reloadRequestCount);
        new Thread(reloader).start();
    }

    /**
     * Reload a specifig node
     * @param node
     */
    private final void reload(Content node) {
        onRegister(node);
    }

    /**
     * Clears the registered uuids and calls onClear().
     */
    public final void clear() {
        this.registeredUUIDs.clear();
        onClear();
    }

    /**
     * Registers a node
     * @param node
     */
    protected abstract void onRegister(Content node);

    /**
     * The implementor should clear everthing. If needed the nodes will get registered.
     */
    protected abstract void onClear();

    /**
     * @return Returns the reloading.
     */
    public boolean isReloading() {
        return reloading;
    }

    /**
     * Sets the reloading flag
     * @param reloading boolean
     */
	private void setReloading(boolean reloading) {
		this.reloading = reloading;
	}

    /**
     *
     * @return Returns the reloadRequestCount
     */
	protected int getReloadRequestCount() {
		return reloadRequestCount;
	}

	/**
	 * Reloading is done in a separate thread. The thread sleeps for SLEEP_MILLIS milliseconds and
	 * checks if the  reloadRequestCount of the observedManager has changed. If true it will remain
	 * sleeping. If false the real reloading starts.
	 *
	 *
	 * @author Ralf Hirning
	 *
	 */
    private class Reloader implements Runnable {
    	/**
    	 *  reloadRequestCount before sleeping
    	 */
    	private int lastReloadRequestCount = 0;

    	/**
    	 * the ObservedManager
    	 */
    	private ObservedManager observedManager;

    	/**
    	 * Constructor
    	 *
    	 * @param observedManager ObservedManager
    	 * @param reloadRequestCount reloadRequestCount of the observedManager
    	 */
    	private Reloader(ObservedManager observedManager, int reloadRequestCount) {
    		this.observedManager = observedManager;
    		this.lastReloadRequestCount = reloadRequestCount;
    	}

    	/**
    	 * The Reloader thread's run method
    	 */
		public void run() {
			while (true) {
				try {
					Thread.sleep(SLEEP_MILLIS);
				} catch (InterruptedException e) {
					// ok, go on
					;
				}

				// check if the  reloadRequestCount of the observedManager has changed
				int currentReloadRequestCount = this.observedManager.getReloadRequestCount();
				if (currentReloadRequestCount > lastReloadRequestCount) {
					lastReloadRequestCount = currentReloadRequestCount;
				} else {
					// allow creation of a new Reloader
					this.observedManager.setReloading(false);

					// Call onClear and reregister the nodes by calling onRegister
			        this.observedManager.onClear();

			        HierarchyManager hm = MgnlContext.getSystemContext().getHierarchyManager(ContentRepository.CONFIG);

			        for (Iterator iter = this.observedManager.registeredUUIDs.iterator(); iter.hasNext();) {
			            String uuid = (String) iter.next();
			            try {
			                Content node = hm.getContentByUUID(uuid);
			                reload(node);
			            }
			            catch (Exception e) {
			            	this.observedManager.registeredUUIDs.remove(uuid);
			                log.warn("can't reload the the node [" + uuid + "]");
			            }
			        }
			        return;
				}
			}
		}
    }
}
