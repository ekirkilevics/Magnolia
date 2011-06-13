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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.ModuleRegistry;


/**
 * Base class for managers that monitor configuration within modules and react when its changed to reload. Configuration
 * is read from a sub path of each modules configuration node. Subclasses override either reload(List<Node>) or the pair
 * onClear() and onRegister().
 *
 * @vesion $Id$
 */
public abstract class ModuleConfigurationObservingManager {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final Object monitor = new Object();
    private final String pathWithinModule;
    private final ModuleRegistry moduleRegistry;
    private final List<String> observedPaths = new ArrayList<String>();

    protected ModuleConfigurationObservingManager(String pathWithinModule, ModuleRegistry moduleRegistry) {
        this.pathWithinModule = pathWithinModule;
        this.moduleRegistry = moduleRegistry;
    }

    public void start() {

        for (String moduleName : moduleRegistry.getModuleNames()) {
            String path = "/modules/" + moduleName + "/" + pathWithinModule;
            observedPaths.add(path);
        }

        for (String observedPath : observedPaths) {
            ObservationUtil.registerDeferredChangeListener(ContentRepository.CONFIG, observedPath, new EventListener() {

                @Override
                public void onEvent(EventIterator events) {
                    synchronized (monitor) {
                        reload();
                    }
                }
            }, 1000, 5000);
        }

        synchronized (monitor) {
            reload();
        }
    }

    protected void reload() {
        List<Node> nodes;
        try {
            nodes = getObservedNodes();
        } catch (RepositoryException e) {
            log.error("Failed to acquire nodes", e);
            return;
        }
        try {
            reload(nodes);
        } catch (RepositoryException e) {
            log.error("Reload of observed nodes failed", e);
        }
    }

    protected void reload(List<Node> nodes) throws RepositoryException {
        onClear();
        for (Node node : nodes) {
            try {
                onRegister(node);
            } catch (Exception e) {
                log.warn("Failed to reload the node [" + node.getPath() + "]");
            }
        }
    }

    protected void onClear() throws RepositoryException {
    }

    protected void onRegister(Node node) throws RepositoryException {
    }

    private List<Node> getObservedNodes() throws RepositoryException {

        Session session = MgnlContext.getJCRSession(ContentRepository.CONFIG);

        List<Node> nodes = new ArrayList<Node>();
        for (String observedPath : observedPaths) {
            try {
                if (session.nodeExists(observedPath)) {
                    nodes.add(session.getNode(observedPath));
                }
            } catch (RepositoryException e) {
                log.error("Failed to acquire node for observed path [" + observedPath + "]", e);
            }
        }
        return nodes;
    }

    protected List<String> getObservedPaths() {
        return Collections.unmodifiableList(observedPaths);
    }
}
