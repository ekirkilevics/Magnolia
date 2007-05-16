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
package info.magnolia.jaas.sp.jcr;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.PermissionImpl;
import info.magnolia.cms.security.auth.*;
import info.magnolia.cms.util.SimpleUrlPattern;
import info.magnolia.cms.util.UrlPattern;
import info.magnolia.jaas.principal.*;
import info.magnolia.api.HierarchyManager;

import java.security.Principal;
import java.util.Iterator;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.FailedLoginException;

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

    /**
     * {@inheritDoc}
     */
    public boolean validateUser() throws FailedLoginException ,LoginException {
        return true;
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
