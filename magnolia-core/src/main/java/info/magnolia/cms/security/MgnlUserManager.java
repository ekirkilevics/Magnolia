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

import org.apache.log4j.Logger;


/**
 * Manages the users stored in magnolia itself.
 * @author philipp
 * @version $Revision$ ($Author$)
 */
public class MgnlUserManager implements UserManager {

    public static Logger log = Logger.getLogger(MgnlUserManager.class);

    /**
     * name for the stored user object in the session.
     */
    public final static String ATTRIBUTE_CURRENT_JCR_USER = "info.magnolia.cms.security.JCRUserManager.currentUser";

    /**
     * Do not instantiate it!
     */
    protected MgnlUserManager() {
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.security.UserManager#getCurrent(javax.servlet.http.HttpServletRequest)
     */
    public User getCurrent(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(MgnlUserManager.ATTRIBUTE_CURRENT_JCR_USER);
        if (user != null) {
            String name = user.getName();
            HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.USERS);

            Content node;
            try {
                node = hm.getContent(name);
                user = new MgnlUser(node);
                request.getSession().setAttribute(MgnlUserManager.ATTRIBUTE_CURRENT_JCR_USER, user);
            }
            catch (PathNotFoundException e) {
                log.error("user not registered in magnolia itself [" + name + "]");
            }
            catch (Exception e) {
                log.error("can't get jcr-node of current user", e);
            }

        }
        return user;
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

}
