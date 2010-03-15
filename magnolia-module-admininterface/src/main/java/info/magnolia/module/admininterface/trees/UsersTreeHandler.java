/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.module.admininterface.trees;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.Security;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.cms.util.ExclusiveWrite;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.AdminTreeMVCHandler;

/**
 * @author had
 * @version $Id:$
 */
public class UsersTreeHandler extends AdminTreeMVCHandler {

    private final SecuritySupport securitySupport;

    public UsersTreeHandler(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
        securitySupport = Security.getSecuritySupport();
    }

    @Override
    public String createNode() {
        getTree().setPath(path);
        synchronized (ExclusiveWrite.getInstance()) {
            final UserManager userMan = securitySupport.getUserManager(StringUtils.substringAfterLast(path, "/"));
            final User user = userMan.createUser(this.getNewNodeName(), "");
            setNewNodeName(user.getName());
        }
        return VIEW_TREE;
    }

    @Override
    public String renameNode(String newName) throws AccessDeniedException, ExchangeException, PathNotFoundException, RepositoryException {
        newName = super.renameNode(newName);
        // update ACLs after rename
        String path = this.getPath();
        String newPath = StringUtils.substringBeforeLast(path, "/") + "/" + newName;
        Content acls = getHierarchyManager().getContent(newPath + "/acl_users");
        for (Content acl : acls.getChildren()) {
            NodeData pathND = acl.getNodeData("path");
            String aclPath = pathND.getString();
            if (aclPath.startsWith(path + "/")) {
                pathND.setValue(newPath + "/" + StringUtils.substringAfter(aclPath, path + "/"));
            }
            if (path.equals(aclPath)) {
                pathND.setValue(newPath);
            }
        }
        acls.save();
        return newName;
    }

    // TODO: copy/move ACL update if we ever allow moving
}
