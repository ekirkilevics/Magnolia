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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.DelayedExecutor;
import info.magnolia.cms.util.ObservationUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.cms.core.HierarchyManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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

        ObservationUtil.registerDefferedChangeListener(ContentRepository.CONFIG, node.getHandle(), new EventListener() {

            public void onEvent(EventIterator events) {
                reload();
            }
        }, 1000, 5000);

        try {
            registeredUUIDs.add(node.getUUID());
            onRegister(node);
        }
        catch (Exception e) {
            log.warn("Was not able to register [" + node.getHandle() + "]", e);
        }
    }

    /**
     * Calls onClear and reregister the nodes by calling onRegister
     */
    public synchronized void reload() {
        // Call onClear and reregister the nodes by calling onRegister
        onClear();

        HierarchyManager hm = MgnlContext.getSystemContext().getHierarchyManager(ContentRepository.CONFIG);

        // copy to avoid ConcurrentModificationException since the list get changed during iteration
        List uuids = new ArrayList(registeredUUIDs);

        for (Iterator iter = uuids.iterator(); iter.hasNext();) {
            String uuid = (String) iter.next();
            try {
                Content node = hm.getContentByUUID(uuid);
                reload(node);
            }
            catch (Exception e) {
                registeredUUIDs.remove(uuid);
                log.warn("can't reload the the node [" + uuid + "]");
            }
        }
        return;
    }

    /**
     * Reload a specifig node
     * @param node
     */
    protected final void reload(Content node) {
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

}
