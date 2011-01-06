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
package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.util.ObservationUtil;
import info.magnolia.cms.util.SystemContentWrapper;
import info.magnolia.context.MgnlContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * A lot of "manager" objects are observed. Will mean that they reload the registered content after the content was changed. To
 * centralize this code we use this abstract manager. A subclass will implement onRegister and onClear.
 * @author philipp
 */
public abstract class ObservedManager {
    protected final Logger log = LoggerFactory.getLogger(getClass());

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
            log.warn("Tried to register a non-existing node!");
            return;
        }

        ObservationUtil.registerDeferredChangeListener(ContentRepository.CONFIG, node.getHandle(), new EventListener() {

            public void onEvent(EventIterator events) {
                reload();
            }
        }, 1000, 5000);

        try {
            registeredUUIDs.add(node.getUUID());
            onRegister(new SystemContentWrapper(node));
        }
        catch (Exception e) {
            log.warn("Was not able to register [" + node.getHandle() + "]", e);
        }
    }

    /**
     * Calls onClear and reregister the nodes by calling onRegister.
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
                log.warn("Can't reload the the node [" + uuid + "]");
            }
        }
        return;
    }

    /**
     * Reload a specific node.
     * @param node
     */
    protected void reload(Content node) {
        onRegister(node);
    }

    /**
     * Clears the registered UUIDs and calls onClear().
     */
    public void clear() {
        this.registeredUUIDs.clear();
        onClear();
    }

    /**
     * Registers a node.
     * @param node
     */
    protected abstract void onRegister(Content node);

    /**
     * The implementor should clear everthing. If needed the nodes will get registered.
     */
    protected abstract void onClear();

}
