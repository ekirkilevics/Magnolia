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
package info.magnolia.jaas.sp.jcr;

import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

import javax.security.auth.spi.LoginModule;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.security.auth.callback.*;
import javax.jcr.RepositoryException;
import javax.jcr.PathNotFoundException;
import java.util.Map;
import java.util.Iterator;
import java.io.IOException;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.util.UrlPattern;
import info.magnolia.cms.util.SimpleUrlPattern;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.PermissionImpl;
import info.magnolia.jaas.principal.Entity;
import info.magnolia.jaas.principal.ACL;
import info.magnolia.jaas.principal.PrincipalCollection;
import info.magnolia.jaas.principal.ACLFactory;

/**
 * This is a default login module for magnolia, it uses initialized repository as
 * defined by the provider interface
 *
 * Date: May 30, 2005
 * Time: 4:42:22 PM
 *
 * @author Sameer Charles
 * $Id :$
 */
public class JCRAuthorizationModule implements LoginModule {

    /**
     * Logger
     * */
    private static Logger log = Logger.getLogger(JCRLoginModule.class);

    private Subject subject;

    private CallbackHandler callbackHandler;

    private Map sharedState;

    private Map options;

    private String name;

    private boolean success;

    private Content user;

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;
    }

    /**
     * This must be used as secondary module, no need to check for authentication
     * */
    public boolean login() throws LoginException {
        if(this.callbackHandler == null)
            throw new LoginException( "Error: no CallbackHandler available for JCRLoginModule");

        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback("name");
        callbacks[1] = new PasswordCallback("pswd", false);

        this.success = false;
        try {
            this.callbackHandler.handle(callbacks);
            this.name = ((NameCallback)callbacks[0]).getName();
            this.success = this.isValidUser();
        } catch (IOException ioe) {
            log.debug(ioe);
            throw new LoginException(ioe.toString());
        } catch (UnsupportedCallbackException ce) {
            log.debug(ce.getMessage(), ce);
            throw new LoginException(ce.getCallback().toString() + " not available");
        }

        return this.success;
    }

    /**
     * Update subject with ACL and other properties
     * */
    public boolean commit() throws LoginException {
        if (!this.success)
            return false;
        this.setEntity();
        this.setACL();
        return true;
    }


    public boolean abort() throws LoginException {
        return false;
    }

    public boolean logout() throws LoginException {
        return false;
    }

    /**
     * checks if the user exist in the repository
     * @return boolean
     */
    private boolean isValidUser() {
        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.USERS);
        try {
            this.user = hm.getContent(this.name);
            return true;
        } catch (PathNotFoundException pe) {
            pe.printStackTrace();
            log.info("Unable to locate user [" + this.name + "], authentication failed");
        } catch (RepositoryException re) {
            re.printStackTrace();
            log.error("Unable to locate user ["
                + this.name
                + "], authentication failed due to a "
                + re.getClass().getName(), re);
        }
        return false;
    }

    /**
     * set user details
     * */
    private void setEntity() {
        Entity user = new Entity();
        String language = this.user.getNodeData("language").getString();
        user.addProperty(Entity.LANGUAGE, language);
        String name = this.user.getTitle();
        user.addProperty(Entity.NAME, name);
        // todo, set all basic magnolia user property
        this.subject.getPrincipals().add(user);
    }

    /**
     * set access control list from the user, roles and groups
     * */
    private void setACL() {
        HierarchyManager rolesHierarchy = ContentRepository.getHierarchyManager(ContentRepository.USER_ROLES);
        try {
            Content rolesNode = this.user.getContent("roles");
            Iterator children = rolesNode.getChildren().iterator();
            PrincipalCollection list = new PrincipalCollection();
            while (children.hasNext()) {
                Content child = (Content) children.next();
                String rolePath = child.getNodeData("path").getString();
                Content role = rolesHierarchy.getContent(rolePath);
                Iterator it = role.getChildren(ItemType.CONTENTNODE.getSystemName(),"acl*").iterator();
                while (it.hasNext()) {
                    Content aclEntry = (Content) it.next();
                    String name = StringUtils.substringAfter(aclEntry.getName(),"acl_");
                    if (!StringUtils.contains(name, "_")) {
                        name += ("_default"); // default workspace must be added to the name
                    }
                    ACL acl = ACLFactory.get(name);
                    if (!list.contains(name)) {
                        list.add(acl);
                    }
                    //add acl
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
            /**
             * set principal list
             * */
            this.subject.getPrincipals().add(list);
        } catch (RepositoryException re) {
            re.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
