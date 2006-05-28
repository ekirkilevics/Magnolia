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


/**
 * @author Sameer Charles $Id$
 */
public class RoleListImpl extends GroupListImpl implements RoleList {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * default name for this principal
     */
    protected static final String DEFAULT_NAME = "roles";

    public RoleListImpl() {
        super();
    }

}
