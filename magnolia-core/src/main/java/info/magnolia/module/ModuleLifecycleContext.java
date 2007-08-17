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
     * System is starting up
     */
    public int PHASE_SYSTEM_STARTUP = 1;

    /**
     * A module is restarted. This is triggered through observation (change in the config node).
     */
    public int PHASE_MODULE_RESTART = 2;

    /**
     * The system is shutting down
     */
    public int PHASE_SYSTEM_SHUTDOWN = 3;


    /**
     * Get the current module defintion
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
