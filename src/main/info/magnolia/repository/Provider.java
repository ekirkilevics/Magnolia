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
package info.magnolia.repository;

import java.util.Map;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Workspace;


/**
 * Date: Nov 25, 2004 Time: 4:14:11 PM
 * @author Sameer Charles
 * @version 2.1
 */
public interface Provider {

    /**
     * <p>
     * Initializes repository, this depends on the underlying repository implementation. Use any available method to get
     * the instance of Repository
     * </p>
     * @param repositoryMapping key value pars as define in repository.xml
     * @throws RepositoryNotInitializedException
     */
    public void init(RepositoryMapping repositoryMapping) throws RepositoryNotInitializedException;

    /**
     * <p>
     * gets the repository instance initialized on init() call
     * </p>
     * @throws RepositoryNotInitializedException if init failed to get repository
     */
    public Repository getUnderlineRepository() throws RepositoryNotInitializedException;

    /**
     * <p>
     * register namespace with the repository. <br>
     * refer JCR-170 specifications
     * </p>
     * @param prefix namespace prefix
     * @param uri namespace URI
     * @param workspace session workspace instance
     * @throws RepositoryException
     */
    public void registerNamespace(String prefix, String uri, Workspace workspace) throws RepositoryException;

    /**
     * <p>
     * unregister namespace with the repository
     * </p>
     * @param prefix as registered previously
     * @param workspace session workspace instance
     * @throws RepositoryException
     */
    public void unregisterNamespace(String prefix, Workspace workspace) throws RepositoryException;

    /**
     * <p>
     * Node type registration is entirely dependent on the implementation. refer JSR-170 specifications
     * </p>
     * @param definition key/value pair of nodetype definition, dependent on how repository implementation expose its
     * methods
     * @throws RepositoryException
     */
    public void registerNodeType(Map definition) throws RepositoryException;
}
