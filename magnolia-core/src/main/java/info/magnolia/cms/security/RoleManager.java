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

/**
 * 
 * @author philipp
 * @version $Revision$ ($Author$)
 *
 */
public interface RoleManager {
    /**
     * Create a role withour any security restrictions.
     * @param name
     * @return the new role
     * @throws UnsupportedOperationException
     * @throws Exception 
     */
    public Role createRole(String name) throws UnsupportedOperationException, Exception;

    /**
     * Get the specifig role without any security restrictions
     * @param name
     * @param request
     * @return the role object
     * @throws UnsupportedOperationException
     */
    public Role getRole(String name) throws UnsupportedOperationException;

}