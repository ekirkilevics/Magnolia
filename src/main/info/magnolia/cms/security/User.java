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

import info.magnolia.cms.core.Content;
import info.magnolia.jaas.principal.Entity;

import java.util.Iterator;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.security.auth.Subject;


/**
 * This class wrapps a user content object to provide some nice methods
 * @author philipp
 * @author Sameer Charles
 * @version $Revision$ ($Author$)
 */
public class User {

    /**
     * user properties
     * */
    private Entity userDetails;

    /**
     * @param subject as created by login module
     */
    public User(Subject subject) {
        Set principalSet = subject.getPrincipals(info.magnolia.jaas.principal.Entity.class);
        Iterator it = principalSet.iterator();
        this.userDetails = (Entity) it.next();
    }

    /**
     * Is this user in a specified role?
     * @param roleName the name of the role
     * @return true if in role
     */
    public boolean hasRole(String roleName) {
        return false;
    }

    /**
     * get user language
     * @return language string
     * */
    public String getLanguage() {
        return (String) this.userDetails.getProperty(Entity.LANGUAGE); //$NON-NLS-1$
    }

    /**
     * get user name
     * @return name string
     * */
    public String getName() {
        return (String) this.userDetails.getProperty(Entity.NAME); //$NON-NLS-1$
    }

}