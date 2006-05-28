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

import info.magnolia.cms.security.auth.ACL;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;


/**
 * This class represents access control list as a principal
 * @author Sameer Charles $Id$
 */
public class ACLImpl implements ACL {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private static final String NAME = "acl";

    /**
     * properties
     */
    private String name;

    private List list;

    private String repository;

    private String workspace;

    /**
     * Constructor
     */
    public ACLImpl() {
        this.list = new ArrayList();
    }

    /**
     * Get name given to this principal
     * @return name
     */
    public String getName() {
        if (StringUtils.isEmpty(this.name)) {
            return NAME;
        }
        return this.name;
    }

    /**
     * Set this principal name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get repository ID for which this ACL has been constructed
     * @return repository ID
     */
    public String getRepository() {
        return this.repository;
    }

    /**
     * Set repository ID for which this ACL will be constructed
     * @param repository
     */
    public void setRepository(String repository) {
        this.repository = repository;
    }

    /**
     * Get workspace ID for which this ACL has been contructed
     * @return workspace ID
     */
    public String getWorkspace() {
        return this.workspace;
    }

    /**
     * Set workspace ID for which this ACL will be constructed
     * @param workspace
     */
    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    /**
     * add permission in to an existing list
     * @param permission
     */
    public void addPermission(Object permission) {
        this.list.add(permission);
    }

    /**
     * Initialize access control list with provided permissions it will overwrite any existing permissions set before.
     * @param list
     */
    public void setList(List list) {
        this.list.clear();
        this.list.addAll(list);
    }

    /**
     * Returns list of permissions for this principal
     */
    public List getList() {
        return this.list;
    }

}
