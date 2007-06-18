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
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.security.auth.Entity;
import info.magnolia.jaas.principal.EntityImpl;
import info.magnolia.jaas.sp.AbstractLoginModule;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.ItemNotFoundException;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.FailedLoginException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;


/**
 * @author Sameer Charles $Id$
 */
public class JCRAuthenticationModule extends AbstractLoginModule {

    private static final Logger log = LoggerFactory.getLogger(JCRAuthenticationModule.class);

    protected Content user;

    /**
     * Releases all associated memory
     */
    public boolean release() {
        return true;
    }

    /**
     * checks is the credentials exist in the repository
     * @return boolean
     */
    public boolean validateUser() throws FailedLoginException ,LoginException {
        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.USERS);
        try {
            this.user = hm.getContent(this.name);
            String serverPassword = this.user.getNodeData("pswd").getString().trim();
            // we do not allow users with no password
            if (StringUtils.isEmpty(serverPassword)) return false;
            // plain text server password
            serverPassword = new String(Base64.decodeBase64(serverPassword.getBytes()));
            return StringUtils.equals(serverPassword, new String(this.pswd));
        }
        catch (PathNotFoundException pe) {
            log.debug("Unable to locate user [{}], authentication failed", this.name);
            throw new FailedLoginException(pe.getMessage());
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
            throw new LoginException(re.getMessage());
        }
    }

    /**
     * set user details
     */
    public void setEntity() {
        EntityImpl user = new EntityImpl();
        String language = this.user.getNodeData("language").getString();
        user.addProperty(Entity.LANGUAGE, language);
        user.addProperty(Entity.NAME, this.user.getName());
        user.addProperty(Entity.FULL_NAME, this.user.getTitle());
        user.addProperty(Entity.PASSWORD, new String(this.pswd));
        this.subject.getPrincipals().add(user);
        collectGroupNames(this.user);
        collectRoleNames(this.user);
    }

    /**
     * set access control list from the user, roles and groups
     */
    public void setACL() {
    }

    /**
     * Extract all the configured roles from the given node (which can be the user node or a group node)
     */
    public void collectRoleNames(Content node) {

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
                addRoleName(role.getName());
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
     */
    public void collectGroupNames(Content node) {
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

                addGroupName(group.getName());
                collectRoleNames(group);
                // check for any sub groups
                collectGroupNames(group);
            }
        }
        catch (PathNotFoundException e) {
            log.debug(e.getMessage(), e);
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
    }



}
