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
import info.magnolia.cms.core.HierarchyManager;

import java.util.Map;


/**
 * The module configuration read from the config repository.
 * @author Sameer Charles
 * @version 2.0
 * @deprecated use the ModuleDefinition where possible. the register node is not used anymore.
 */
public class ModuleConfig {

    /**
     * The HM of the modules
     * @deprecated
     */
    private Map sharedHierarchyManagers; /* id - HierarchyManager map */

    /**
     * Startup parameters
     * @deprecated
     */
    private Map initParameters;

    /**
     * Default HM
     * @deprecated
     */
    private HierarchyManager hierarchyManager;

    /**
     * The node containing the configuration
     */
    private Content localStore;

    /**
     * The registraion definition
     */
    private ModuleDefinition moduleDefinition;

    /**
     * @return module name
     */
    public String getName() {
        return this.getModuleDefinition().getName();
    }

    /**
     * @return description
     */
    public String getDescription() {
        return this.getModuleDefinition().getDisplayName();
    }

    /**
     * @return hms
     */
    public Map getSharedHierarchyManagers() {
        return this.sharedHierarchyManagers;
    }

    /**
     * @return hm
     */
    public HierarchyManager getHierarchyManager() {
        return this.hierarchyManager;
    }

    /**
     * @return the parameters
     */
    public Map getInitParameters() {
        return this.initParameters;
    }

    /**
     * @return content node with the configuration
     */
    public Content getLocalStore() {
        return this.localStore;
    }

    /**
     * @param initParams parameters
     */
    public void setInitParameters(Map initParams) {
        this.initParameters = initParams;
    }

    /**
     * @param manager manager
     */
    public void setHierarchyManager(HierarchyManager manager) {
        this.hierarchyManager = manager;
    }

    /**
     * @param shared shared repositories
     */
    public void setSharedHierarchyManagers(Map shared) {
        this.sharedHierarchyManagers = shared;
    }

    /**
     * @param localStore content node containing the configuration
     */
    public void setLocalStore(Content localStore) {
        this.localStore = localStore;
    }

    /**
     * @return Returns the moduleDefinition.
     */
    public ModuleDefinition getModuleDefinition() {
        return this.moduleDefinition;
    }

    /**
     * @param moduleDefinition The moduleDefinition to set.
     */
    public void setModuleDefinition(ModuleDefinition moduleDefinition) {
        this.moduleDefinition = moduleDefinition;
    }

}
