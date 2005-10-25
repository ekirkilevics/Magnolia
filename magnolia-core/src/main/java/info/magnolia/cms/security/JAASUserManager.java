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

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;


/**
 * Manages the JAAS users.
 * @author philipp
 * @version $Revision$ ($Author$)
 */
public class JAASUserManager implements UserManager {

    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.security.UserManager#getCurrent(javax.servlet.http.HttpServletRequest)
     */
    public User getCurrent(HttpServletRequest request) {
        return (new JAASUser(Authenticator.getSubject(request)));
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.security.UserManager#findUser(java.lang.String, javax.servlet.http.HttpServletRequest)
     */
    public User getUser(String name, HttpServletRequest request) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented with JAAS");
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.security.UserManager#getUser(java.lang.String)
     */
    public User getUser(String name) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented with JAAS");
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.security.UserManager#getAllUsers()
     */
    public Collection getAllUsers() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented with JAAS");
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.security.UserManager#getAllUsers(javax.servlet.http.HttpServletRequest)
     */
    public Collection getAllUsers(HttpServletRequest request) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented with JAAS");
    }
}
