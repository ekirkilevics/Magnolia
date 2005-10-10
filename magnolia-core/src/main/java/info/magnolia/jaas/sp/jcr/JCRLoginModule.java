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
import org.apache.commons.codec.binary.Base64;

import javax.security.auth.login.LoginException;
import javax.security.auth.callback.*;
import javax.jcr.RepositoryException;
import javax.jcr.PathNotFoundException;
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
import info.magnolia.cms.security.PrincipalCollection;
import info.magnolia.cms.security.ACL;
import info.magnolia.jaas.principal.*;
import info.magnolia.jaas.sp.AbstractLoginModule;

/**
 * This is a default login module for magnolia, it uses initialized repository as
 * defined by the provider interface
 *
 * @author Sameer Charles
 * @version $Revision$ ($Author$)
 */
public class JCRLoginModule extends AbstractLoginModule {

    /**
     * Logger
     * */
    private static Logger log = Logger.getLogger(JCRLoginModule.class);

    protected String name;

    protected char[] pswd;

    protected boolean success;

    protected Content user;

    /**
     * Authenticate against magnolia/jcr user repository
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
            this.pswd = ((PasswordCallback)callbacks[1]).getPassword();
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
    public boolean isValidUser() {
        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.USERS);
        try {
            this.user = hm.getContent(this.name);
            String fromRepository = this.user.getNodeData("pswd").getString().trim();
            String encodedPassword = new String(Base64.encodeBase64((new String(this.pswd)).getBytes()));
            if (fromRepository.equalsIgnoreCase(encodedPassword)) {
                return true;
            }
            return false;
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
    public void setEntity() {
        EntityImpl user = new EntityImpl();
        String language = this.user.getNodeData("language").getString();
        user.addProperty(EntityImpl.LANGUAGE, language);
        String name = this.user.getTitle();
        user.addProperty(EntityImpl.NAME, name);
        // todo, set all basic magnolia user property
        this.subject.getPrincipals().add(user);
    }

    /**
     * set access control list from the user, roles and groups
     * */
    public void setACL() {
        HierarchyManager rolesHierarchy = ContentRepository.getHierarchyManager(ContentRepository.USER_ROLES);
        try {
            Content rolesNode = this.user.getContent("roles");
            Iterator children = rolesNode.getChildren().iterator();
            RoleListImpl roleList = new RoleListImpl();
            PrincipalCollection principalList = new PrincipalCollectionImpl();
            while (children.hasNext()) {
                Content child = (Content) children.next();
                String rolePath = child.getNodeData("path").getString();
                roleList.addRole(rolePath);
                Content role = rolesHierarchy.getContent(rolePath);
                Iterator it = role.getChildren(ItemType.CONTENTNODE.getSystemName(),"acl*").iterator();
                while (it.hasNext()) {
                    Content aclEntry = (Content) it.next();
                    String name = StringUtils.substringAfter(aclEntry.getName(),"acl_");
                    ACL acl = new ACLImpl();
                    if (!StringUtils.contains(name, "_")) {
                        String defaultWorkspace
                                = ContentRepository.getDefaultWorkspace(StringUtils.substringBefore(name,"_"));
                        acl.setRepository(name);
                        acl.setWorkspace(defaultWorkspace);
                        name += ("_"+defaultWorkspace); // default workspace must be added to the name
                    } else {
                        String[] tokens = StringUtils.split(name,"_");
                        acl.setRepository(tokens[0]);
                        acl.setRepository(tokens[1]);
                    }
                    acl.setName(name);

                    if (!principalList.contains(name)) {
                        principalList.add(acl);
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
             * set principal list, a set of info.magnolia.jaas.principal.ACL
             * */
            this.subject.getPrincipals().add(principalList);
            /**
             * set list of role names, info.magnolia.jaas.principal.RoleList
             * */
            this.subject.getPrincipals().add(roleList);
        } catch (RepositoryException re) {
            re.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
