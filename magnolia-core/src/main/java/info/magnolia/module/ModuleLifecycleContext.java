/**
 * This file Copyright (c) 2003-2009 Magnolia International
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

import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.module.model.ModuleDefinition;

/**
 * This interface currently provides a hook allowing modules to register ObserverManager
 * instances, which can observer other modules' nodes. In the future, it might provide
 * other callback methods relevant to the lifecycle management of a module.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public interface ModuleLifecycleContext {

    /**
     * System is starting up.
     */
    public int PHASE_SYSTEM_STARTUP = 1;

    /**
     * A module is restarted. This is triggered through observation (change in the config node).
     */
    public int PHASE_MODULE_RESTART = 2;

    /**
     * The system is shutting down.
     */
    public int PHASE_SYSTEM_SHUTDOWN = 3;


    /**
     * Gets the current module definition.
     */
    ModuleDefinition getCurrentModuleDefinition();


    /**
     * Returns the phase the lifecycle is in. This is one of the phase constants.
     */
    int getPhase();

    /**
     * Registers a component which will observe other modules' config node with
     * the given nodeName.
     */
    void registerModuleObservingComponent(String nodeName, ObservedManager component);

}
