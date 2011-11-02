/**
 * This file Copyright (c) 2003-2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.repository;

import info.magnolia.repository.definition.RepositoryDefinition;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import java.io.InputStream;


/**
 * Repository provider. This interface is intended to be implemented by all repository implementations that can be used by Magnolia.
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * @version $Id$
 */
public interface Provider {

    /**
     * Initializes repository, this depends on the underlying repository implementation. Use any available method to get
     * the instance of Repository.
     * @param repositoryDefinition key value pars as define in repository.xml
     * @throws RepositoryNotInitializedException
     */
    void init(RepositoryDefinition repositoryDefinition) throws RepositoryNotInitializedException;

    /**
     * Gets the repository instance initialized on init() call.
     * @throws RepositoryNotInitializedException if init failed to get repository
     */
    Repository getUnderlyingRepository() throws RepositoryNotInitializedException;

    /**
     * @deprecated since 4.0 - typo - use get #getUnderlyingRepository() instead
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
     * Register a new workspace in the current repository.
     * @param workspaceName workspace name
     * @return <code>true</code> true if the workspace is registered now of <code>false</code> if it was already
     * registered
     * @throws RepositoryException if any exception occours during registration
     */
    boolean registerWorkspace(String workspaceName) throws RepositoryException;

    void shutdownRepository();

    Session getSystemSession(String workspaceName) throws RepositoryException;
}
