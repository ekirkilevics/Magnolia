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
