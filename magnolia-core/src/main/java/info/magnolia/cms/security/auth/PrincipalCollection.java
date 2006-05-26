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
package info.magnolia.cms.security.auth;

import java.io.Serializable;
import java.security.Principal;
import java.util.Collection;


/**
 * @author Sameer Charles
 * $Id$
 */
public interface PrincipalCollection extends Principal, Serializable {

    public String getName();

    /**
     * Set this principal name
     */
    public void setName(String name);

    /**
     * Set collection
     * @param collection
     */
    public void set(Collection collection);

    /**
     * Add to collection
     * @param principal to be added to the collection
     */
    public void add(Principal principal);

    /**
     * Remove from the collection
     * @param principal to be removed from the collection
     */
    public void remove(Principal principal);

    /**
     * Clear collection
     */
    public void clearAll();

    /**
     * Check if this collection contains specified object
     * @param principal
     * @return true if the specified object exist in the collection
     */
    public boolean contains(Principal principal);

    /**
     * Checks if this collection contains object with the specified name
     * @param name
     * @return true if the collection contains the principal by the specified name
     */
    public boolean contains(String name);

    /**
     * Get principal associated to the specified name from the collection
     * @param name
     * @return principal object associated to the specified name
     */
    public Principal get(String name);

}
