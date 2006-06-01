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
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.security.auth.PrincipalCollection;
import info.magnolia.cms.security.auth.ACL;
import info.magnolia.cms.security.auth.GroupList;
import info.magnolia.cms.security.auth.RoleList;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.PermissionImpl;
import info.magnolia.cms.util.UrlPattern;
import info.magnolia.cms.util.SimpleUrlPattern;
import info.magnolia.jaas.principal.RoleListImpl;
import info.magnolia.jaas.principal.PrincipalCollectionImpl;
import info.magnolia.jaas.principal.ACLImpl;
import info.magnolia.jaas.principal.GroupListImpl;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.ItemNotFoundException;
import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.StringUtils;

import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;


/**
 * This is a default login module for magnolia, it uses initialized repository as defined by the provider interface
 * @author Sameer Charles $Id$
 */
public class JCRAuthorizationModule extends JCRAuthenticationModule {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(JCRAuthorizationModule.class);

    /**
     * checks if the user exist in the repository
     * @return boolean
     */
    public boolean isValidUser() {
        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.USERS);
        try {
            this.user = hm.getContent(this.name);
            return true;
        }
        catch (PathNotFoundException pe) {
            log.info("Unable to locate user [{}], authentication failed", this.name);
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
     * Update subject with ACL and other properties
     */
    public boolean commit() throws LoginException {
        if (!this.success) {
            throw new LoginException("failed to authenticate " + this.name);
        }
        this.setEntity();
        this.setACL();
        return true;
    }

    /**
     * set access control list from the user, roles and groups
     */
    public void setACL() {
        RoleList roleList = new RoleListImpl();
        PrincipalCollection principalList = new PrincipalCollectionImpl();
        this.setACL(this.user, principalList, roleList);
        Iterator groupsIterator = this.getGroups().iterator();
        HierarchyManager groupsHierarchy = ContentRepository.getHierarchyManager(ContentRepository.USER_GROUPS);
        GroupList groupList = new GroupListImpl();
        while (groupsIterator.hasNext()) {
            String groupPath = ((NodeData) groupsIterator.next()).getString();
            try {
                Content groupNode = groupsHierarchy.getContent(groupPath);
                groupList.add(groupNode.getName());
                this.setACL(groupNode, principalList, roleList);
            }
            catch (PathNotFoundException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Group node on path " + groupPath + " does not exist");
                }
            }
            catch (RepositoryException re) {
                log.error("Failed to get group node " + groupPath, re);
            }
        }
        /**
         * set list of group names, info.magnolia.jaas.principal.GroupList
         */
        this.subject.getPrincipals().add(groupList);
        /**
         * set principal list, a set of info.magnolia.jaas.principal.ACL
         */
        this.subject.getPrincipals().add(principalList);
        /**
         * set list of role names, info.magnolia.jaas.principal.RoleList
         */
        this.subject.getPrincipals().add(roleList);
    }

    /**
     * get all groups
     * @return collection of groups <code>NodeData</code>
     */
    private Collection getGroups() {
        try {
            return this.user.getContent("groups").getNodeDataCollection();
        }
        catch (RepositoryException re) {
            if (log.isDebugEnabled()) {
                log.debug(this.user.getName() + "do not belong to any group");
            }
            log.debug(re.getMessage());
        }
        return new ArrayList();
    }

    /**
     * set access control list from a list of roles under the provided content object
     * @param aclNode under which roles and ACL are defined
     */
    private void setACL(Content aclNode, PrincipalCollection principalList, RoleList roleList) {
        HierarchyManager rolesHierarchy = ContentRepository.getHierarchyManager(ContentRepository.USER_ROLES);
        try {
            Content rolesNode = aclNode.getContent("roles");
            Iterator children = rolesNode.getNodeDataCollection().iterator();
            while (children.hasNext()) {
                String roleUUID = ((NodeData) children.next()).getString();
                Content role;
                try {
                    role = rolesHierarchy.getContentByUUID(roleUUID);
                } catch (ItemNotFoundException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Role does not exist", e);
                    }
                    continue;
                }
                roleList.add(role.getName());
                Iterator it = role.getChildren(ItemType.CONTENTNODE.getSystemName(), "acl*").iterator();
                while (it.hasNext()) {
                    Content aclEntry = (Content) it.next();
                    String name = StringUtils.substringAfter(aclEntry.getName(), "acl_");
                    ACL acl;
                    String repositoryName;
                    String workspaceName;
                    if (!StringUtils.contains(name, "_")) {
                        workspaceName = ContentRepository.getDefaultWorkspace(StringUtils.substringBefore(name, "_"));
                        repositoryName = name;
                        name += ("_" + workspaceName); // default workspace
                        // must be added to the
                        // name
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
        catch (PathNotFoundException e) {
            log.debug(e.getMessage(), e);
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

}
