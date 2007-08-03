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
package info.magnolia.module;

import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.module.model.ModuleDefinition;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ModuleLifecycleContextImpl implements ModuleLifecycleContext {
    /**
     * A Map&lt;String,ObservedManager%gt;, mapping nodenames to components
     */
    private final Map components;

    private ModuleDefinition currentModuleDefinition;

    ModuleLifecycleContextImpl() {
        components = new LinkedHashMap();
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
    public void start(Collection moduleNodes) {
        final Iterator managerIt = components.keySet().iterator();
        while (managerIt.hasNext()) {
            final String nodeName = (String) managerIt.next();
            final ObservedManager component = (ObservedManager) components.get(nodeName);
            final Iterator it = moduleNodes.iterator();
            while (it.hasNext()) {
                final Content moduleNode = (Content) it.next();
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
}
