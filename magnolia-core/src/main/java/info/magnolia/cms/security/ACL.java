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
import java.util.List;

/**
 *
 * @author Sameer Charles
 * @version $Revision: $ ($Author: $)
 */
public interface ACL extends Principal, Serializable {

    /**
     * Get name given to this principal
     * @return name
     */
    public String getName();
    /**
     * Set this principal name
     */
    public void setName(String name);

    /**
     * Get repository ID for which this ACL has been constructed
     * @return repository ID
     */
    public String getRepository();

    /**
     * Set repository ID for which this ACL will be constructed
     * @param repository
     */
    public void setRepository(String repository);

    /**
     * Get workspace ID for which this ACL has been contructed
     * @return workspace ID
     */
    public String getWorkspace();

    /**
     * Set workspace ID for which this ACL will be constructed
     * @param workspace
     */
    public void setWorkspace(String workspace);

    /**
     * add permission in to an existing list
     * @param permission
     */
    public void addPermission(Object permission);

    /**
     * Initialize access control list with provided permissions it will overwrite any existing permissions set before.
     * @param list
     */
    public void setList(List list);

    /**
     * Returns list of permissions for this principal
     */
    public List getList();

}
