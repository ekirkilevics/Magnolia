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
package info.magnolia.module;

import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.module.model.ModuleDefinition;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This default implementation of ModuleLifecycleContext allows the ModuleManager
 * to set the current "phase" of installation; other ModuleLifecycleContext clients
 * have read-only access to the phase. 
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ModuleLifecycleContextImpl implements ModuleLifecycleContext {
    /**
     * A Map&lt;String,ObservedManager%gt;, mapping nodenames to components.
     */
    private final Map<String, ObservedManager> components;

    private int phase;

    private ModuleDefinition currentModuleDefinition;

    public ModuleLifecycleContextImpl() {
        components = new LinkedHashMap<String, ObservedManager>();
    }

    public void registerModuleObservingComponent(String nodeName, ObservedManager component) {
        if (components.containsKey(nodeName)) {
            final Object currentObservedManager = components.get(nodeName);
            throw new IllegalStateException("ObservedManager " + currentObservedManager + " was already registered for nodes of name " + nodeName + ", " + component + " can't be registered.");
        } else {
            components.put(nodeName, component);
        }
    }

    /**
     * @param moduleNodes a Collection&lt;Content&gt; of module nodes.
     */
    public void start(Collection<Content> moduleNodes) {
        for (String nodeName : components.keySet()) {
            final ObservedManager component = components.get(nodeName);
            for (Content moduleNode : moduleNodes) {
                initEntry(moduleNode, nodeName, component);
            }
        }
    }

    private void initEntry(Content moduleNode, String nodeName, ObservedManager observedManager) {
        final Content node = ContentUtil.getCaseInsensitive(moduleNode, nodeName);
        if (node != null) {
            observedManager.register(node);
        }
    }

    public ModuleDefinition getCurrentModuleDefinition() {
        return this.currentModuleDefinition;
    }

    public void setCurrentModuleDefinition(ModuleDefinition currentModuleDefinition) {
        this.currentModuleDefinition = currentModuleDefinition;
    }


    public int getPhase() {
        return this.phase;
    }


    public void setPhase(int phase) {
        this.phase = phase;
    }
}
