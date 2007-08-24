/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.cms.security;

/**
 * Provides the name for the default realm.
 * @author philipp
 * @version $Id$
 */
public interface Realm {

    /**
     * The realm for the admin interface
     */
    public static final String REALM_ADMIN = "admin";

    /**
     * No realm --> all users
     */
    public static final String REALM_ALL = "all";

    /**
     * The default realm is REALM_ADMIN
     */
    public static final String DEFAULT_REALM = REALM_ALL;
}
