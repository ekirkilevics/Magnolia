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
package info.magnolia.repository;

import java.io.InputStream;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Workspace;


/**
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * @version $Id$
 */
public interface Provider {

    /**
     * Initializes repository, this depends on the underlying repository implementation. Use any available method to get
     * the instance of Repository.
     * @param repositoryMapping key value pars as define in repository.xml
     * @throws RepositoryNotInitializedException
     */
    void init(RepositoryMapping repositoryMapping) throws RepositoryNotInitializedException;

    /**
     * Gets the repository instance initialized on init() call.
     * @throws RepositoryNotInitializedException if init failed to get repository
     */
    Repository getUnderlineRepository() throws RepositoryNotInitializedException;

    /**
     * Register namespace with the repository. Refer JCR-170 specifications.
     * @param prefix namespace prefix
     * @param uri namespace URI
     * @param workspace session workspace instance
     * @throws RepositoryException
     */
    void registerNamespace(String prefix, String uri, Workspace workspace) throws RepositoryException;

    /**
     * Unregister namespace with the repository.
     * @param prefix as registered previously
     * @param workspace session workspace instance
     * @throws RepositoryException
     */
    void unregisterNamespace(String prefix, Workspace workspace) throws RepositoryException;

    /**
     * Node type registration is entirely dependent on the implementation. Refer JSR-170 specifications.
     * @throws RepositoryException
     */
    void registerNodeTypes() throws RepositoryException;

    /**
     * Node type registration is entirely dependent on the implementation. Refer JSR-170 specifications.
     * @throws RepositoryException
     */
    void registerNodeTypes(String configuration) throws RepositoryException;

    /**
     * Node type registration is entirely dependent on the implementation. Refer JSR-170 specifications.
     * @param stream , stream type depends on the implementation of this method
     * @throws RepositoryException
     */
    void registerNodeTypes(InputStream stream) throws RepositoryException;

    /**
     * Register a new workspace in the current repository
     * @param workspaceName workspace name
     * @return <code>true</code> true if the workspace is registered now of <code>false</code> if it was already
     * registered
     * @throws RepositoryException if any exception occours during registration
     */
    boolean registerWorkspace(String workspaceName) throws RepositoryException;
}
