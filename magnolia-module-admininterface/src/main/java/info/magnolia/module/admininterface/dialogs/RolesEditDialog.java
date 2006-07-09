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
package info.magnolia.module.admininterface.dialogs;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.gui.dialog.Dialog;
import info.magnolia.module.admininterface.SaveHandler;
import info.magnolia.module.admininterface.pages.RolesACLPage;

import java.util.Iterator;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class RolesEditDialog extends ConfiguredDialog {

    protected static Logger log = LoggerFactory.getLogger("roles dialog"); //$NON-NLS-1$

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * @param name
     * @param request
     * @param response
     * @param configNode
     */
    public RolesEditDialog(String name, HttpServletRequest request, HttpServletResponse response, Content configNode) {
        super(name, request, response, configNode);
    }

    public String getRepository() {
        String repository = super.getRepository();
        if (repository == null) {
            repository = ContentRepository.USER_ROLES;
        }
        return repository;
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.module.admininterface.DialogMVCHandler#createDialog(info.magnolia.cms.core.Content,
     * info.magnolia.cms.core.Content)
     */
    protected Dialog createDialog(Content configNode, Content storageNode) throws RepositoryException {

        Dialog dialog = super.createDialog(configNode, storageNode);

        dialog.setJavascriptSources(request.getContextPath() + "/.resources/admin-js/dialogs/DynamicTable.js"); //$NON-NLS-1$
        dialog.setJavascriptSources(request.getContextPath() + "/.resources/admin-js/dialogs/pages/rolesACLPage.js"); //$NON-NLS-1$
        dialog.setCssSources(request.getContextPath() + "/.resources/admin-css/dialogs/pages/rolesEditPage.css"); //$NON-NLS-1$
        return dialog;
    }

    /**
     * @see info.magnolia.module.admininterface.DialogMVCHandler#configureSaveHandler(info.magnolia.module.admininterface.SaveHandler)
     */
    protected void configureSaveHandler(SaveHandler save) {
        super.configureSaveHandler(save);
        save.setPath(path);
    }

    protected boolean onPostSave(SaveHandler saveControl) {
        Content role = this.getStorageNode();

        // for each repository
        Iterator repositoryNames = ContentRepository.getAllRepositoryNames();
        while (repositoryNames.hasNext()) {
            String repository = (String) repositoryNames.next();

            // ######################
            // # acl
            // ######################
            // remove existing
            try {
                role.delete("acl_" + repository); //$NON-NLS-1$
            }
            catch (RepositoryException re) {
                // ignore, not existing
            }
            // rewrite
            try {
                Content acl = role.createContent("acl_" + repository, ItemType.CONTENTNODE); //$NON-NLS-1$
                String aclValueStr = form.getParameter("acl" + repository + "List"); //$NON-NLS-1$ //$NON-NLS-2$
                if (StringUtils.isNotEmpty(aclValueStr)) {
                    String[] aclEntries = aclValueStr.split(";"); //$NON-NLS-1$
                    for (int i = 0; i < aclEntries.length; i++) {
                        String path = StringUtils.EMPTY;
                        long accessRight = 0;
                        int accessType = 0;

                        String[] aclValuePairs = aclEntries[i].split(","); //$NON-NLS-1$
                        for (int j = 0; j < aclValuePairs.length; j++) {
                            String[] aclValuePair = aclValuePairs[j].split(":"); //$NON-NLS-1$
                            String aclName = aclValuePair[0].trim();
                            String aclValue = StringUtils.EMPTY;
                            if (aclValuePair.length > 1) {
                                aclValue = aclValuePair[1].trim();
                            }

                            if (aclName.equals("path")) { //$NON-NLS-1$
                                path = aclValue;
                            }
                            else if (aclName.equals("accessType")) { //$NON-NLS-1$
                                accessType = Integer.valueOf(aclValue).intValue();
                            }
                            else if (aclName.equals("accessRight")) { //$NON-NLS-1$
                                try {
                                    accessRight = Long.parseLong(aclValue);
                                }
                                catch (NumberFormatException e) {
                                    accessRight = 0;
                                }
                            }
                        }

                        if (StringUtils.isNotEmpty(path)) {
                            if (path.equals("/")) { //$NON-NLS-1$
                                accessType = RolesACLPage.TYPE_SUBS;
                                path = StringUtils.EMPTY;
                            }

                            if ((accessType & RolesACLPage.TYPE_THIS) != 0) {
                                try {
                                    String newLabel = Path.getUniqueLabel(hm, acl.getHandle(), "0"); //$NON-NLS-1$
                                    Content r = acl.createContent(newLabel, ItemType.CONTENTNODE);
                                    r.createNodeData("path").setValue(path); //$NON-NLS-1$
                                    r.createNodeData("permissions").setValue(accessRight); //$NON-NLS-1$
                                }
                                catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                }
                            }

                            if ((accessType & RolesACLPage.TYPE_SUBS) != 0) {
                                try {
                                    String newLabel = Path.getUniqueLabel(hm, acl.getHandle(), "0"); //$NON-NLS-1$
                                    Content r = acl.createContent(newLabel, ItemType.CONTENTNODE);
                                    r.createNodeData("path").setValue(path + "/*"); //$NON-NLS-1$ //$NON-NLS-2$
                                    r.createNodeData("permissions").setValue(accessRight); //$NON-NLS-1$
                                }
                                catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                }
                            }
                        }
                    }
                }
                hm.save();
            }
            catch (RepositoryException re) {
                log.error(re.getMessage(), re);
            }
        }
        return true;
    }

}