/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 * */


package info.magnolia.cms.module;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;

import java.util.Hashtable;




/**
 * Date: Mar 30, 2004
 * Time: 10:59:07 AM
 *
 * @author Sameer Charles
 * @version 2.0
 */



public class ModuleConfig {



    /* module specific */
    private Hashtable sharedHierarchyManagers; /* id - HierarchyManager map */
    private Hashtable initParameters;
    private String moduleName;
    private String moduleDescription;
    private HierarchyManager hierarchyManager;


    /* local store */
    private Content localStore;




    public String getModuleName() {
        return this.moduleName;
    }



    public String getModuleDescription() {
        return this.moduleDescription;
    }



    public Hashtable getSharedHierarchyManagers() {
        return this.sharedHierarchyManagers;
    }


    public HierarchyManager getHierarchyManager() {
        return this.hierarchyManager;
    }


    public Hashtable getInitParameters() {
        return this.initParameters;
    }



    public Content getLocalStore() {
        return this.localStore;
    }


    public void setModuleName(String value) {
        this.moduleName = value;
    }



    public void setModuleDescription(String value) {
        this.moduleDescription = value;
    }



    public void setInitParameters(Hashtable initParams) {
        this.initParameters = initParams;
    }



    public void setHierarchyManager(HierarchyManager manager) {
        this.hierarchyManager = manager;
    }



    public void setSharedHierarchyManagers(Hashtable shared){
        this.sharedHierarchyManagers = shared;
    }



    public void setLocalStore(Content localStore) {
        this.localStore = localStore;
    }





}
