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
package info.magnolia.jaas.sp.ldap;

import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.codec.binary.Base64;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.security.auth.callback.*;
import javax.jcr.RepositoryException;
import javax.naming.NamingException;
import javax.naming.NamingEnumeration;
import javax.naming.directory.*;
import java.util.Map;
import java.util.Iterator;
import java.util.Properties;
import java.util.List;
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
import info.magnolia.jaas.sp.AbstractLoginModule;

/**
 * This login module gets Authentication and group information from the configured active directory
 * once roles or groups are successfully retrieved it tries to load associated access control list
 * from magnolia repository
 *
 * Date: Aug 10, 2005
 * Time: 4:32:19 PM
 *
 * @author Sameer Charles
 * $Id :$
 */
public class ADSLoginModule extends AbstractLoginModule {

    /**
     * Logger
     * */
    private static Logger log = Logger.getLogger(ADSLoginModule.class);

    /**
     * connection name
     * */
    private static final String CONNECTION_NAME = "adsContext";

    /**
     * jndi config file
     * */
    private static final String JNDI_CONFIG_FILE = "adsConfig";

    /**
     * initial search string attribute
     * */
    private static final String INITIAL_SEARCH_STRING = "initialSearchAttributes";

    /**
     * user id
     * */
    private String name;

    /**
     * user password
     * */
    private char[] pswd;

    /**
     * success flag
     * */
    private boolean success;

    /**
     * Attribute name value map
     * */
    AttributeMap attributeMap;

    /**
     * @param subject
     * @param callbackHandler
     * @param sharedState
     * @param options
     * */
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;
    }

    /**
     * Authenticate against active directory
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
            this.queryLDAP();
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
     * Query ldap and parse all returned attributes
     * */
    private void queryLDAP() {
        DirContext context = ConnectionFactory.getContext(CONNECTION_NAME, JNDI_CONFIG_FILE);
        Attributes matchAttrs = new BasicAttributes(true); // ignore attribute name case
        matchAttrs.put(new BasicAttribute("uid", this.name));

        Properties props = ConnectionFactory.getProperties(CONNECTION_NAME);
        this.attributeMap = new AttributeMap(props);
        // Search for objects with those matching attributes
        try {
            NamingEnumeration answer = context.search(props.getProperty(INITIAL_SEARCH_STRING), matchAttrs);
            // answer should contain all sub attributes
            this.parseSearchResult(answer);
        } catch (NamingException ne) {
            log.debug(ne.getMessage(), ne);
        }
    }

    /**
     * parses result
     * @param result
     * */
    private void parseSearchResult(NamingEnumeration result) {
        try {
            while (result.hasMore()) {
                SearchResult sr = (SearchResult)result.next();
                extractAttributes(sr.getAttributes());
            }
        } catch (NamingException ne) {
            log.error(ne.getMessage(), ne);
        }
    }

    /**
     * extract attributes
     * @param attributes
     * */
    private void extractAttributes(Attributes attributes) {
        if (attributes == null) {
            log.debug("Attribute list is empty");
            return;
        }
        try {
            for (NamingEnumeration enum = attributes.getAll(); enum.hasMore();) {
                Attribute attribute = (Attribute)enum.next();
                for (NamingEnumeration e = attribute.getAll();e.hasMore();) {
                    this.attributeMap.setProperty(attribute.getID(), (String) e.next());
                }
            }
        } catch (NamingException e) {
            e.printStackTrace();
        }
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
        try {
            ConnectionFactory.getContext(CONNECTION_NAME).close();
            this.attributeMap = null;
        } catch (NamingException ne) {
            log.error(ne.getMessage(), ne);
        }
        return false;
    }

    /**
     * checks if the credentials match with ldap
     * @return boolean
     */
    public boolean isValidUser() {
        try {
            String fromLDAP = this.attributeMap.getSingleValueProperty(AttributeMap.PASSWORD);
            if (fromLDAP != null) {
                String encodedPassword = new String(Base64.encodeBase64((new String(this.pswd)).getBytes()));
                if (fromLDAP.equalsIgnoreCase(encodedPassword)) {
                    return true;
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Unable to locate user ["
                + this.name
                + "], authentication failed due to a "
                + e.getClass().getName(), e);
        }
        return false;
    }

    /**
     * set user details from the ldap query result
     * */
    public void setEntity() {
        Entity user = new Entity();
        // todo, set all basic magnolia user property
        String language = this.attributeMap.getSingleValueProperty(AttributeMap.LANGUAGE);
        user.addProperty(Entity.LANGUAGE, language);
        String name = this.attributeMap.getSingleValueProperty(AttributeMap.GIVEN_NAME);
        user.addProperty(Entity.NAME, name);
        this.subject.getPrincipals().add(user);
    }

    /**
     * set access control list from the user, roles and groups
     * */
    public void setACL() {
        HierarchyManager rolesHierarchy = ContentRepository.getHierarchyManager(ContentRepository.USER_ROLES);
        try {
            List roles = this.attributeMap.getProperty(AttributeMap.GROUP_ID);
            PrincipalCollection list = new PrincipalCollection();
            for (int index = 0; index < roles.size(); index++) {
                String rolePath = (String) roles.get(index);
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
            //set principal list
            this.subject.getPrincipals().add(list);
        } catch (RepositoryException re) {
            re.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
