/**
 * This file Copyright (c) 2003-2007 Magnolia International
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
package info.magnolia.jaas.sp.jcr;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.PermissionImpl;
import info.magnolia.cms.security.auth.ACL;
import info.magnolia.cms.security.auth.GroupList;
import info.magnolia.cms.security.auth.PrincipalCollection;
import info.magnolia.cms.security.auth.RoleList;
import info.magnolia.cms.util.SimpleUrlPattern;
import info.magnolia.cms.util.UrlPattern;
import info.magnolia.jaas.principal.ACLImpl;
import info.magnolia.jaas.principal.GroupListImpl;
import info.magnolia.jaas.principal.PrincipalCollectionImpl;
import info.magnolia.jaas.principal.RoleListImpl;

import java.security.Principal;
import java.util.Iterator;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.security.auth.login.LoginException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is a default login module for magnolia, it uses initialized repository as defined by the provider interface
 * @author Sameer Charles
 * @version $Id$
 */
public class JCRAuthorizationModule extends JCRAuthenticationModule {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(JCRAuthorizationModule.class);

    public void validateUser() throws LoginException {
    }

    /**
     * set access control list from the user, roles and groups
     */
    public void setACL() {
        String[] roles = (String[]) getRoleNames().toArray(new String[getRoleNames().size()]);
        String[] groups = (String[]) getGroupNames().toArray(new String[getGroupNames().size()]);

        if (log.isDebugEnabled()) {
            log.debug("Roles: {}", ArrayUtils.toString(roles));
            log.debug("Groups: {}", ArrayUtils.toString(groups));
        }

        addRoles(roles);
        addGroups(groups);

        PrincipalCollection principalList = new PrincipalCollectionImpl();
        setACLForRoles(roles, principalList);
        setACLForGroups(groups, principalList);

        if (log.isDebugEnabled()) {
            for (Iterator iterator = ((PrincipalCollectionImpl) principalList).iterator(); iterator.hasNext();) {
                Principal principal = (Principal) iterator.next();
                log.debug("ACL: {}", principal);
            }

        }

        // set principal list, a set of info.magnolia.jaas.principal.ACL
        this.subject.getPrincipals().add(principalList);
    }

    // do nothing here, we are only responsible for adding ACL passed on via shared state
    public void setEntity() {}

    /**
     * Set the list of groups, info.magnolia.jaas.principal.GroupList.
     * @param groups array of group names
     */
    protected void addGroups(String[] groups) {
        GroupList groupList = new GroupListImpl();
        for (Iterator iterator = getGroupNames().iterator(); iterator.hasNext();) {
            String group = (String) iterator.next();
            groupList.add(group);
        }
        this.subject.getPrincipals().add(groupList);
    }

    /**
     * Set the list of roles, info.magnolia.jaas.principal.RoleList.
     * @param roles array of role names
     */
    protected void addRoles(String[] roles) {
        RoleList roleList = new RoleListImpl();
        for (Iterator iterator = getRoleNames().iterator(); iterator.hasNext();) {
            String role = (String) iterator.next();
            roleList.add(role);
        }
        this.subject.getPrincipals().add(roleList);
    }

    /**
     * Looks for rolee configured in magnolia repository with the given name, and configures ACLs for it.
     * @param roles array of role names.
     * @param principalList PrincipalCollection
     */
    protected void setACLForRoles(String[] roles, PrincipalCollection principalList) {

        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.USER_ROLES);

        for (int j = 0; j < roles.length; j++) {
            String role = roles[j];
            try {
                setACL(hm.getContent(role), principalList);
            }
            catch (PathNotFoundException e) {
                log.info("Role {} not found", role);
            }
            catch (RepositoryException e) {
                log.warn("Error accessing {} role: {}", role, e.getMessage());
            }
        }
    }

    /**
     * Looks for groups configured in magnolia repository with the given name, and configures ACLs for it.
     * @param groups array of group names.
     * @param principalList PrincipalCollection
     */
    protected void setACLForGroups(String[] groups, PrincipalCollection principalList) {
        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.USER_GROUPS);

        for (int j = 0; j < groups.length; j++) {
            String group = groups[j];
            try {
                setACL(hm.getContent(group), principalList);
            }
            catch (PathNotFoundException e) {
                log.info("Group {} not found", group);
            }
            catch (RepositoryException e) {
                log.warn("Error accessing {} group: {}", group, e.getMessage());
            }
        }
    }

    /**
     * set access control list from a list of roles under the provided content object
     * @param node under which roles and ACL are defined
     */
    private void setACL(Content node, PrincipalCollection principalList) {
        Iterator it = node.getChildren(ItemType.CONTENTNODE.getSystemName(), "acl*").iterator();
        while (it.hasNext()) {
            Content aclEntry = (Content) it.next();
            String name = StringUtils.substringAfter(aclEntry.getName(), "acl_");
            ACL acl;
            String repositoryName;
            String workspaceName;
            if (!StringUtils.contains(name, "_")) {
                repositoryName = name;
                workspaceName = ContentRepository.getDefaultWorkspace(name);
                name += ("_" + workspaceName); // default workspace must be added to the name
            }
            else {
                String[] tokens = StringUtils.split(name, "_");
                repositoryName = tokens[0];
                workspaceName = tokens[1];
            }
            // get the existing acl object if created before with some
            // other role
            if (!principalList.contains(name)) {
                acl = new ACLImpl();
                principalList.add(acl);
            }
            else {
                acl = (ACL) principalList.get(name);
            }
            acl.setName(name);
            acl.setRepository(repositoryName);
            acl.setWorkspace(workspaceName);

            // add acl
            Iterator permissionIterator = aclEntry.getChildren().iterator();
            while (permissionIterator.hasNext()) {
                Content map = (Content) permissionIterator.next();
                String path = map.getNodeData("path").getString();
                UrlPattern p = new SimpleUrlPattern(path);
                Permission permission = new PermissionImpl();
                permission.setPattern(p);
                permission.setPermissions(map.getNodeData("permissions").getLong());
                acl.addPermission(permission);
            }
        }
    }

}
