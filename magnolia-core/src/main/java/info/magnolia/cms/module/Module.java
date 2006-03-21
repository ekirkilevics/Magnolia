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
package info.magnolia.cms.module;

import info.magnolia.cms.core.Content;


/**
 * All external module must implement this interface in order to be initialised by magnolia and have access to module
 * specific / shared repositories
 * @author philipp
 * @version $Revision$ ($Author$)
 */

public interface Module {

    /**
     * No registration needed. Same version is already registered
     */
    int REGISTER_STATE_NONE = 0;

    /**
     * First installation. Node didn't exist in the repository
     */
    int REGISTER_STATE_INSTALLATION = 1;

    /**
     * New version of a already registered module
     */
    int REGISTER_STATE_NEW_VERSION = 2;

    /**
     * Initialise module based on the configuration. once repositories are initialised and tickets are created its a
     * responsibility of the module itself to keep this data. magnolia server will release all handles
     * @throws InvalidConfigException not a valid configuration
     * @throws InitializationException
     */
    void init(Content configNode) throws InvalidConfigException, InitializationException;

    /**
     * This method is always called during the registration phase.
     * @param def the module definition built by the modules xml file
     * @param moduleNode the node in the config repository
     * @param registerState one of the REGISTER_STATE constants
     * @throws RegisterException no update
     */
    void register(ModuleDefinition def, Content moduleNode, int registerState) throws RegisterException;

    /**
     * At this point module is responsible to release all resources
     */
    void destroy();

    /**
     * True if this module is already initialized
     * @return
     */
    boolean isInitialized();

    /**
     * Return true if the module need a system restart after a registration or initialization
     * @return
     */
    boolean isRestartNeeded();

    /**
     * Returns the name of this module
     * @return
     */
    String getName();
    
    /**
     * Get the description of this module
     */
     ModuleDefinition getModuleDefinition();

}