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
package info.magnolia.cms.security;

/**
 * @author Sameer Charles
 * $Id$
 */
public interface GroupManager {

    /**
     * Create a group
     * @param name
     * @return newly created group
     * @throws UnsupportedOperationException if the implementation does not support writing
     * @throws AccessDeniedException if logged in repository user does not sufficient rights
     */
    public Group createGroup(String name) throws UnsupportedOperationException, AccessDeniedException;

    /**
     * Get group by the given name
     * @param name
     * @return group
     * @throws UnsupportedOperationException if the implementation does not support writing
     * @throws AccessDeniedException if logged in repository user does not sufficient rights
     */
    public Group getGroup(String name) throws UnsupportedOperationException, AccessDeniedException;


}
