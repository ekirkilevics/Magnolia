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
import info.magnolia.cms.core.NodeData;
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
import info.magnolia.api.HierarchyManager;

import java.security.Principal;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.jcr.ItemNotFoundException;
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
    private static Logger log = LoggerFactory.getLogger(JCRAuthorizationModule.class);

    /**
     * Set of role names.
     */
    protected Set rolesNames = new LinkedHashSet();

    /**
     * Set of group names.
     */
    protected Set groupsNames = new LinkedHashSet();

    /**
     * {@inheritDoc}
     */
    public boolean validateUser() throws FailedLoginException ,LoginException {
        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.USERS);
        try {
            this.user = hm.getContent(this.name);
            return true;
        }
        catch (PathNotFoundException pe) {
            log.info("Unable to locate user '{}', authentication failed", this.name);
        }
        catch (RepositoryException re) {
            log.error("Unable to locate user ["
                + this.name
                + "], authentication failed due to a "
                + re.getClass().getName(), re);
        }
        return false;
    }

    /**
     * set access control list from the user, roles and groups
     */
    public void setACL() {
        collectGroups(this.user);
        collectRoles(this.user);

        String[] roles = (String[]) rolesNames.toArray(new String[rolesNames.size()]);
        String[] groups = (String[]) groupsNames.toArray(new String[groupsNames.size()]);

        if (log.isDebugEnabled()) {
            log.debug("Roles: {}", ArrayUtils.toString(roles));
            log.debug("Groups: {}", ArrayUtils.toString(groups));
        }

        addRoles(roles);
        addGroups(groups);

        PrincipalCollection principalList = new PrincipalCollectionImpl();
        setACL(this.user, principalList);
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

    /**
     * Set the list of groups, info.magnolia.jaas.principal.GroupList.
     * @param groups array of group names
     */
    protected void addGroups(String[] groups) {
        GroupList groupList = new GroupListImpl();
        for (Iterator iterator = groupsNames.iterator(); iterator.hasNext();) {
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
        for (Iterator iterator = rolesNames.iterator(); iterator.hasNext();) {
            String role = (String) iterator.next();
            roleList.add(role);
        }
        this.subject.getPrincipals().add(roleList);
    }

    /**
     * Extract all the configured roles from the given node (which can be the user node or a group node)
     * @param node user or group node
     */
    protected void collectRoles(Content node) {

        HierarchyManager rolesHierarchy = ContentRepository.getHierarchyManager(ContentRepository.USER_ROLES);
        try {
            if (!node.hasContent("roles")) {
                return;
            }
            Content rolesNode = node.getContent("roles");
            Iterator children = rolesNode.getNodeDataCollection().iterator();
            while (children.hasNext()) {
                String roleUUID = ((NodeData) children.next()).getString();
                Content role;
                try {
                    role = rolesHierarchy.getContentByUUID(roleUUID);
                }
                catch (ItemNotFoundException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Role does not exist", e);
                    }
                    continue;
                }
                catch (IllegalArgumentException e) {
                    // this can happen if the roleUUID is not a valid uuid string
                    if (log.isDebugEnabled()) {
                        log.debug("Exception caught", e);
                    }
                    continue;
                }
                rolesNames.add(role.getName());
            }
        }
        catch (PathNotFoundException e) {
            log.debug(e.getMessage(), e);
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }

    }

    /**
     * Extract all the configured groups from the given node (which can be the user node or a group node)
     * @param node user or group node
     */
    protected void collectGroups(Content node) {
        HierarchyManager groupsHierarchy = ContentRepository.getHierarchyManager(ContentRepository.USER_GROUPS);
        try {
            if (!node.hasContent("groups")) {
                return;
            }
            Content groupNode = node.getContent("groups");
            Iterator children = groupNode.getNodeDataCollection().iterator();
            while (children.hasNext()) {
                String groupUUID = ((NodeData) children.next()).getString();
                Content group;
                try {
                    group = groupsHierarchy.getContentByUUID(groupUUID);

                }
                catch (ItemNotFoundException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Group does not exist", e);
                    }
                    continue;
                }

                // ignore if this groups is already in a list to avoid infinite recursion
                if (!groupsNames.contains(group.getName())) {
                    groupsNames.add(group.getName());
                    collectRoles(group);
                    // check for any sub groups
                    collectGroups(group);
                }

            }
        }
        catch (PathNotFoundException e) {
            log.debug(e.getMessage(), e);
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
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
