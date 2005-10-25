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

import java.util.Iterator;
import java.util.Set;

import javax.security.auth.Subject;

import org.apache.log4j.Logger;


/**
 * This class wraps a user content object to provide some nice methods
 * @author philipp
 * @author Sameer Charles
 * @version $Revision$ ($Author$)
 */
public class JAASUser implements User {

    public static Logger log = Logger.getLogger(JAASUser.class);

    /**
     * user properties
     */
    private Entity userDetails;

    /**
     * user roles
     */
    private RoleList roleList;

    /**
     * @param subject as created by login module
     */
    protected JAASUser(Subject subject) {
        Set principalSet = subject.getPrincipals(Entity.class);
        Iterator entityIterator = principalSet.iterator();
        this.userDetails = (Entity) entityIterator.next();
        principalSet = subject.getPrincipals(RoleList.class);
        Iterator roleListIterator = principalSet.iterator();
        this.roleList = (RoleList) roleListIterator.next();
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.security.UserInterface#hasRole(java.lang.String)
     */
    public boolean hasRole(String roleName) {
        return this.roleList.hasRole(roleName);
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.security.UserInterface#removeRole(java.lang.String)
     */
    public void removeRole(String roleName) {
        throw new UnsupportedOperationException("not implemented with JAAS");
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.security.UserInterface#addRole(java.lang.String)
     */
    public void addRole(String roleName) {
        throw new UnsupportedOperationException("not implemented with JAAS");
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.security.UserInterface#getLanguage()
     */

    public String getLanguage() {
        return (String) this.userDetails.getProperty(Entity.LANGUAGE); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.security.UserInterface#getName()
     */
    public String getName() {
        return (String) this.userDetails.getProperty(Entity.NAME); //$NON-NLS-1$
    }
}