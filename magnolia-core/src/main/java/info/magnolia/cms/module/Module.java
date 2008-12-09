/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
package info.magnolia.cms.module;

import info.magnolia.cms.core.Content;


/**
 * All external module must implement this interface in order to be initialised by magnolia and have access to module
 * specific / shared repositories
 * @author philipp
 * @version $Revision$ ($Author$)
 *
 * @deprecated see info.magnolia.module
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
    void register(info.magnolia.module.model.ModuleDefinition def, Content moduleNode, int registerState) throws RegisterException;

    /**
     * Unregister a module. A module is responsible to undo all the steps done during the registration.
     * @param def the definition of this module
     * @param moduleNode the node in the config repository
     */
    void unregister(info.magnolia.module.model.ModuleDefinition def, Content moduleNode);

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
    info.magnolia.module.model.ModuleDefinition getModuleDefinition();

}
