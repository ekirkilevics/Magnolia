/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.module;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import info.magnolia.module.model.ModuleDefinition;

/**
 * Dispatches module lifecycle events to all registered listeners.
 *
 * @version $Id$
 */
public class ModuleLifecycleEventDispatcher {

    /**
     * Represents a registered listener and the name of the module it listens to. A listener can listen to events for
     * all modules in which case moduleName is null.
     */
    private static class EventRegistration {

        private final ModuleLifecycleListener listener;
        private final String moduleName;

        private EventRegistration(ModuleLifecycleListener listener) {
            this.listener = listener;
            this.moduleName = null;
        }

        private EventRegistration(ModuleLifecycleListener listener, String moduleName) {
            this.listener = listener;
            this.moduleName = moduleName;
        }

        public boolean matches(ModuleDefinition moduleDefinition) {
            return moduleName == null || moduleName.equals(moduleDefinition.getName());
        }
    }

    private final Map<Integer, EventRegistration> registrations = new ConcurrentHashMap<Integer, EventRegistration>();

    /**
     * Adds a listener that will receive events for all modules. If the listener has previously been registered to
     * listen to a specific module it will not receive two invocations for that module.
     */
    public void addListener(ModuleLifecycleListener listener) {
        addRegistration(new EventRegistration(listener));
    }

    /**
     * Adds a listener that will receive events only for a specific module. If the listener has previously been
     * registered it will be re-registered and will only receive events for this module.
     */
    public void addListener(String moduleName, ModuleLifecycleListener listener) {
        addRegistration(new EventRegistration(listener, moduleName));
    }

    public void removeListener(ModuleLifecycleListener listener) {
        registrations.remove(System.identityHashCode(listener));
    }

    public void fireStarted(ModuleDefinition moduleDefinition, Object moduleInstance) {
        for (EventRegistration registration : registrations.values()) {
            if (registration.matches(moduleDefinition)) {
                registration.listener.onModuleStarted(moduleDefinition, moduleInstance);
            }
        }
    }

    public void fireRestarted(ModuleDefinition moduleDefinition, Object moduleInstance) {
        for (EventRegistration registration : registrations.values()) {
            if (registration.matches(moduleDefinition)) {
                registration.listener.onModuleRestarted(moduleDefinition, moduleInstance);
            }
        }
    }

    public void fireStopped(ModuleDefinition moduleDefinition, Object moduleInstance) {
        for (EventRegistration registration : registrations.values()) {
            if (registration.matches(moduleDefinition)) {
                registration.listener.onModuleStopped(moduleDefinition, moduleInstance);
            }
        }
    }

    private void addRegistration(EventRegistration registration) {
        registrations.put(System.identityHashCode(registration.listener), registration);
    }
}
