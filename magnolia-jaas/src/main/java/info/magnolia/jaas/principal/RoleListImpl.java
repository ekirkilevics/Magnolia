/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.jaas.principal;

import info.magnolia.cms.security.auth.RoleList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;


/**
 * @author Sameer Charles
 * @version $Revision: $ ($Author: $)
 */
public class RoleListImpl implements RoleList {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * default name for this principal
     */
    private static final String DEFAULT_NAME = "roles";

    /**
     * properties
     */
    private String name;

    /**
     * list of names
     */
    private Collection roles;

    public RoleListImpl() {
        this.roles = new ArrayList();
    }

    /**
     * Get name given to this principal
     * @return name
     */
    public String getName() {
        if (StringUtils.isEmpty(this.name)) {
            return DEFAULT_NAME;
        }
        return this.name;
    }

    /**
     * Set principal name
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Add a role name to the list
     * @param roleName
     */
    public void addRole(String roleName) {
        this.roles.add(roleName);
    }

    /**
     * Gets list of roles as string
     * @return roles
     */
    public Collection getList() {
        return this.roles;
    }

    /**
     * Checks if the role name exist in this list
     */
    public boolean hasRole(String name) {
        Iterator listIterator = this.roles.iterator();
        while (listIterator.hasNext()) {
            String roleName = (String) listIterator.next();
            if (StringUtils.equalsIgnoreCase(name, roleName)) {
                return true;
            }
        }
        return false;
    }

}
