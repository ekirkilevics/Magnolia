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
package info.magnolia.module.dms;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.module.InitializationException;
import info.magnolia.cms.module.ModuleUtil;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.RoleManager;
import info.magnolia.cms.security.Security;
import info.magnolia.cms.security.UserManager;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.module.admininterface.AbstractAdminModule;

import org.apache.log4j.Logger;


/**
 * The module. Configures users and roles. Most of the code will disapeare.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 *
 */
public class DMSModule extends AbstractAdminModule {

    public static Logger log = Logger.getLogger(DMSModule.class);

    private static DMSModule instance;
    
    /**
     * The repository name used by this dms
     */
    private String repository;
    
    /**
     * The dialog used to edit a document, also used to build the search interface
     */
    private String baseDialog;

    
    /**
     * Configure the DMS module
     */
    public DMSModule() {
        setRepository("dms");
        setBaseDialog("/modules/dms/dialogs/dmsEdit");
    }
    
    public void onRegister(int registerState) {
        try {
            HierarchyManager hmRoles = ContentRepository.getHierarchyManager(ContentRepository.USER_ROLES);
            HierarchyManager hmUsers = ContentRepository.getHierarchyManager(ContentRepository.USERS);

            ContentUtil.createPath(hmRoles, DMSConfig.ROLE, ItemType.CONTENT);
            ContentUtil.createPath(hmRoles, DMSConfig.LICENSE_ROLE, ItemType.CONTENT);
            ModuleUtil.registerProperties(hmRoles, "info.magnolia.module.dms.roles");
            ContentUtil.createPath(hmUsers, DMSConfig.USER, ItemType.CONTENT);
            ModuleUtil.registerProperties(hmUsers, "info.magnolia.module.dms.users");

            hmUsers.save();
            hmRoles.save();

            // grant superuser
            log.info("Update security");
            configureUsers();

            // provide the files
            log.info("Install files");

        }
        catch (Exception e) {
            log.error("Can't register dms module", e);
        }
    }

    protected void onInit() throws InitializationException {
        instance = this;
    }

    /**
     * @return Returns the instance.
     */
    public static DMSModule getInstance() {
        return instance;
    }

    protected void configureUsers() {
        UserManager userManager = Security.getUserManager();
        RoleManager roleManager = Security.getRoleManager();
        userManager.getUser("superuser").addRole(DMSConfig.LICENSE_ROLE);
        roleManager.getRole("superuser").addPermission(DMSConfig.REPOSITORY, "/*", Permission.ALL);
    }

    
    /**
     * @return Returns the repository.
     */
    public String getRepository() {
        return repository;
    }

    
    /**
     * @param repository The repository to set.
     */
    protected void setRepository(String repository) {
        this.repository = repository;
    }

    
    /**
     * @return Returns the baseDialog.
     */
    public String getBaseDialog() {
        return this.baseDialog;
    }

    
    /**
     * @param baseDialog The baseDialog to set.
     */
    public void setBaseDialog(String baseDialog) {
        this.baseDialog = baseDialog;
    }

}