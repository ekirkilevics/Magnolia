/**
 * This file Copyright (c) 2003-2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
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
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.admininterface.dialogs;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.gui.dialog.Dialog;
import info.magnolia.module.admininterface.SaveHandler;
import info.magnolia.module.admininterface.config.AclTypeConfiguration;

import java.util.Iterator;

import javax.jcr.RepositoryException;
import javax.jcr.PathNotFoundException;
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
        try {
            saveACLs(role, "uri");

            // for each repository
            Iterator repositoryNames = ContentRepository.getAllRepositoryNames();
            while (repositoryNames.hasNext()) {
                saveACLs(role, (String) repositoryNames.next());
            }

            role.save();
            return true;
        } catch (RepositoryException re) {
            log.error("Failed to update role, reverting all transient modifications made for this node", re);
            try {
                role.refresh(false);
            } catch (RepositoryException e) {
                log.error("Failed to revert transient modifications", e);
            }
        }
        return false;
    }

    protected void saveACLs(Content role, String repository) throws RepositoryException {
        // ######################
        // # acl
        // ######################
        // remove existing
        try {
            role.delete("acl_" + repository); //$NON-NLS-1$
        }
        catch (PathNotFoundException re) {
            // ignore, not existing
        }
        // rewrite
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
                    if (repository.equalsIgnoreCase("uri")) { //$NON-NLS-1$
                        // write ACL as is for URI security
                        accessType = AclTypeConfiguration.TYPE_THIS;
                    } else if (path.equals("/")) { //$NON-NLS-1$
                        accessType = AclTypeConfiguration.TYPE_SUBS;
                        path = StringUtils.EMPTY;
                    }

                    if ((accessType & AclTypeConfiguration.TYPE_THIS) != 0) {
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

                    if ((accessType & AclTypeConfiguration.TYPE_SUBS) != 0) {
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
    }

}
