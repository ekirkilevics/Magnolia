/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.module;

/**
 * Date: Mar 30, 2004 Time: 10:57:18 AM
 * @author Sameer Charles
 * @version 2.0
 */
/**
 * All external module must implement this interface in order to be initialised by magnolia and have access to module
 * specific / shared repositories
 */
public interface Module {

    /**
     * Initialise module based on the configuration. once repositories are initialised and tickets are created its a
     * responsibility of the module itself to keep this data. magnolia server will release all handles
     * @param moduleConfig
     * @see ModuleConfig#getInitParameters()
     * @see ModuleConfig#getModuleDescription()
     * @see ModuleConfig#getModuleName()
     * @see ModuleConfig#getModuleRepository()
     * @see ModuleConfig#getSharedRepositores()
     */
    void init(ModuleConfig moduleConfig) throws InvalidConfigException;

    /**
     * At this point module is responsible to release all resources
     */
    void destroy();
}
