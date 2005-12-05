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
package info.magnolia.cms.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;

import javax.jcr.PathNotFoundException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;


/**
 * Manages the users stored in magnolia itself.
 * @author philipp
 * @version $Revision$ ($Author$)
 */
public class MgnlUserManager implements UserManager {

    public static Logger log = Logger.getLogger(MgnlUserManager.class);

    /**
     * Do not instantiate it!
     */
    public MgnlUserManager() {
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.security.UserManager#getCurrent(javax.servlet.http.HttpServletRequest)
     */
    public User getCurrent(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(Authenticator.ATTRIBUTE_USER);
        if (user == null) {
            // first check if session is authenticated, if yet this is a false call and try to
            // set current user again
            if (SessionAccessControl.isSecuredSession(request)) {
                this.setCurrent(request);
            }
            // if setCurrent failed for some reason or user does not exist
            if ((user = (User)request.getSession().getAttribute(Authenticator.ATTRIBUTE_USER)) == null) {
                user = new DummyUser();
            }
        }
        return user;
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.security.UserManager#setCurrent(javax.servlet.http.HttpServletRequest)
     */
    public void setCurrent(HttpServletRequest request) {
        String name = Authenticator.getUserId(request);
        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.USERS);

        Content node;
        try {
            node = hm.getContent(name);
            request.getSession().setAttribute(Authenticator.ATTRIBUTE_USER, new MgnlUser(node));
        }
        catch (PathNotFoundException e) {
            log.error("user not registered in magnolia itself [" + name + "]");
        }
        catch (Exception e) {
            log.error("can't get jcr-node of current user", e);
        }
    }

   
    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.security.UserManager#findUser(java.lang.String, javax.servlet.http.HttpServletRequest)
     */
    public User getUser(String name, HttpServletRequest request) throws UnsupportedOperationException {
        HierarchyManager hm = SessionAccessControl.getHierarchyManager(request, ContentRepository.USERS);
        return getUser(name, hm);
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.security.UserManager#getUser(java.lang.String)
     */
    public User getUser(String name) throws UnsupportedOperationException {
        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.USERS);
        return getUser(name, hm);
    }

    /**
     * Get the user object form the passed HierarchyManager.
     * @param name
     * @param hm
     * @return the user object
     */
    private User getUser(String name, HierarchyManager hm) {
        try {
            return new MgnlUser(hm.getContent(name));
        }
        catch (Exception e) {
            log.info("can't find user [" + name + "]", e);
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.security.UserManager#getAllUsers()
     */
    public Collection getAllUsers() throws UnsupportedOperationException {
        return getAllUsers(ContentRepository.getHierarchyManager(ContentRepository.USERS));
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.security.UserManager#getAllUsers(javax.servlet.http.HttpServletRequest)
     */
    public Collection getAllUsers(HttpServletRequest request) throws UnsupportedOperationException {
        return getAllUsers(SessionAccessControl.getHierarchyManager(request, ContentRepository.USERS));
    }

    /**
     * @param hm
     * @return
     */
    private Collection getAllUsers(HierarchyManager hm) {
        Collection users = new ArrayList();
        try {
            Collection nodes = hm.getRoot().getChildren(ItemType.CONTENT);
            for (Iterator iter = nodes.iterator(); iter.hasNext();) {
                users.add(new MgnlUser((Content) iter.next()));
            }
        }
        catch (Exception e) {
            log.error("can't find user");
        }
        return users;
    }

    /* (non-Javadoc)
     * @see info.magnolia.cms.security.UserManager#createUser(java.lang.String, java.lang.String, javax.servlet.http.HttpServletRequest)
     */
    public User createUser(String name, String pw, HttpServletRequest request) throws UnsupportedOperationException {
        HierarchyManager hm = SessionAccessControl.getHierarchyManager(request, ContentRepository.USERS);
        return createUser(name, pw, hm);

    }

    /* (non-Javadoc)
     * @see info.magnolia.cms.security.UserManager#createUser(java.lang.String, java.lang.String)
     */
    public User createUser(String name, String pw) throws UnsupportedOperationException {
        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.USERS);
        return createUser(name, pw, hm);
    }

    /**
     * @param name
     * @param pw
     * @param hm
     * @return
     */
    private User createUser(String name, String pw, HierarchyManager hm) {
        try {
            Content node = null;
            node = hm.createContent("/", name, ItemType.CONTENT.getSystemName());
            node.getNodeData("name", true).setValue(name);
            node.getNodeData("pswd", true).setValue(new String(Base64.encodeBase64(pw.getBytes())));
            node.getNodeData("language", true).setValue("en");
            node.save();
            return new MgnlUser(node);
        }
        catch (Exception e) {
            log.info("can't create user [" + name + "]", e);
            return null;
        }
    }
    

}
