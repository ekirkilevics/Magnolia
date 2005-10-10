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

import java.security.Principal;
import java.io.Serializable;
import java.util.Collection;

/**
 *
 * @author Sameer Charles
 * @version $Revision: $ ($Author: $)
 */
public interface RoleList extends Principal, Serializable {

    /**
     * Get name given to this principal
     * @return name
     */
    public String getName();

    /**
     * Set principal name
     * @param name
     * */
    public void setName(String name);
    /**
     * Add a role name to the list
     * @param roleName
     * */
    public void addRole(String roleName);
    /**
     * Gets list of roles as string
     * @return roles
     * */
    public Collection getList();
    /**
     * Checks if the role name exist in this list
     * */
    public boolean hasRole(String name);

}
