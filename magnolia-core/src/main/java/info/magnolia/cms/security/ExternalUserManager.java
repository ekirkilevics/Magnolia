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

import info.magnolia.cms.beans.runtime.MgnlContext;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;


/**
 * Manages the JAAS users.
 * @author philipp
 * @version $Revision$ ($Author$)
 */
public class ExternalUserManager implements UserManager {

    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.security.UserManager#getCurrent(javax.servlet.http.HttpServletRequest)
     */
    public User getCurrent() {
        HttpServletRequest request = MgnlContext.getRequest();
        User user = (User) request.getSession().getAttribute(Authenticator.ATTRIBUTE_USER);
        if (user == null) {
            // first check if session is authenticated, if yet this is a false call and try to
            // set current user again
            if (SessionAccessControl.isSecuredSession(request)) {
                this.setCurrent(new JAASUser(Authenticator.getSubject(request)));
            }
            // if setCurrent failed for some reason or user does not exist
            if ((user = (User) request.getSession().getAttribute(Authenticator.ATTRIBUTE_USER)) == null) {
                user = new DummyUser();
            }
        }
        return user;
    }

    public void setCurrent(User user) {
        MgnlContext.getSession().setAttribute(Authenticator.ATTRIBUTE_USER, user);
    }

    public User getUser(String name) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented with JAAS");
    }

    public Collection getAllUsers() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented with JAAS");
    }

    public User createUser(String name, String pw) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented with JAAS");
    }
}
