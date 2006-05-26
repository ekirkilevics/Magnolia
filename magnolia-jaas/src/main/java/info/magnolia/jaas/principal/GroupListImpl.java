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

import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

import info.magnolia.cms.security.auth.GroupList;

/**
 * @author Sameer Charles
 * $Id$
 */
public class GroupListImpl implements GroupList {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * default name for this principal
     */
    protected static final String DEFAULT_NAME = "groups";

    /**
     * properties
     */
    protected String name;

    /**
     * list of names
     */
    protected Collection list;

    public GroupListImpl() {
        this.list = new ArrayList();
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
     * Add a name to the list
     * @param name
     */
    public void add(String name) {
        this.list.add(name);
    }

    /**
     * Gets list of roles as string
     * @return roles
     */
    public Collection getList() {
        return this.list;
    }

    /**
     * Checks if the name exist in this list
     * @param name
     */
    public boolean has(String name) {
        Iterator listIterator = this.list.iterator();
        while (listIterator.hasNext()) {
            String roleName = (String) listIterator.next();
            if (StringUtils.equalsIgnoreCase(name, roleName)) {
                return true;
            }
        }
        return false;
    }
}
