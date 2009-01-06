/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.core.search;

import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;


/**
 * Date: Apr 4, 2005 Time: 11:02:35 AM
 * @author Sameer Charles
 */

public interface Query {

    String XPATH = "xpath"; //$NON-NLS-1$

    String SQL = "sql"; //$NON-NLS-1$

    /**
     * <i>Description inherited from javax.jcr.query.Query#execute()</i><br>
     * Executes this query and returns a <code>{@link QueryResult}</code>.
     * @return a <code>QueryResult</code>
     * @throws RepositoryException if an error occurs
     */
    QueryResult execute() throws RepositoryException;

    /**
     * <i>Description inherited from javax.jcr.query.Query#getStatement()</i><br>
     * Returns the statement set for this query.
     * @return the query statement.
     */
    String getStatement();

    /**
     * <i>Description inherited from javax.jcr.query.Query#getLanguage()</i><br>
     * Returns the language set for this query. This will be one of the query language constants returned by
     * {@link QueryManager#getSupportedQueryLanguages}.
     * @return the query language.
     */
    String getLanguage();

    /**
     * <i>Description inherited from javax.jcr.query.Query#getStoredQueryPath()</i><br>
     * If this is a <code>Query</code> object that has been stored using {@link Query#storeAsNode} (regardless of
     * whether it has been <code>save</code>d yet) or retrieved using {@link QueryManager#getQuery}), then this
     * method returns the path of the <code>nt:query</code> node that stores the query. If this is a transient query
     * (that is, a <code>Query</code> object created with {@link QueryManager#createQuery} but not yet stored) then
     * this method throws an <code>ItemNotFoundException</code>.
     * @return path of the node representing this query.
     * @throws ItemNotFoundException if this query is not a stored query.
     * @throws RepositoryException if another error occurs.
     */
    String getStoredQueryPath() throws ItemNotFoundException, RepositoryException;

    /**
     * <i>Description inherited from javax.jcr.query.Query#storeAsNode()</i><br>
     * Creates a node representing this <code>Query</code> in content. <p/> In a level 1 repository this method throws
     * an <code>UnsupportedRepositoryOperationException</code>. <p/> In a level 2 repository it creates a node of
     * type <code>nt:query</code> at <code>absPath</code> and returns that node. <p/> In order to persist the newly
     * created node, a <code>save</code> must be performed that includes <i>the parent</i> of this new node within
     * its scope. In other words, either a <code>Session.save</code> or an <code>Item.save</code> on the parent or
     * higher-degree ancestor of <code>absPath</code> must be performed. <p/> An <code>ItemExistsException</code>
     * will be thrown either immediately (by this method), or on <code>save</code>, if an item at the specified path
     * already exists and same-name siblings are not allowed. Implementations may differ on when this validation is
     * performed. <p/> A <code>PathNotFoundException</code> will be thrown either immediately , or on
     * <code>save</code>, if the specified path implies intermediary nodes that do not exist. Implementations may
     * differ on when this validation is performed. <p/> A <code>ConstraintViolationException</code>will be thrown
     * either immediately or on <code>save</code>, if adding the node would violate a node type or
     * implementation-specific constraintor if an attempt is made to add a node as the child of a property.
     * Implementations may differ on when this validation is performed. <p/> A <code>VersionException</code> will be
     * thrown either immediately (by this method), or on <code>save</code>, if the node to which the new child is
     * being added is versionable and checked-in or is non-versionable but its nearest versionable ancestor is
     * checked-in. Implementations may differ on when this validation is performed. <p/> A <code>LockException</code>
     * will be thrown either immediately (by this method), or on <code>save</code>, if a lock prevents the addition
     * of the node. Implementations may differ on when this validation is performed.
     * @return the newly created node.
     * @throws ItemExistsException if an item at the specified path already exists, same-name siblings are not allowed
     * and this implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws PathNotFoundException if the specified path implies intermediary <code>Node</code>s that do not exist
     * or the last element of <code>relPath</code> has an index, and this implementation performs this validation
     * immediately instead of waiting until <code>save</code>.
     * @throws ConstraintViolationException if a node type or implementation-specific constraint is violated or if an
     * attempt is made to add a node as the child of a property and this implementation performs this validation
     * immediately instead of waiting until <code>save</code>.
     * @throws VersionException if the node to which the new child is being added is versionable and checked-in or is
     * non-versionable but its nearest versionable ancestor is checked-in and this implementation performs this
     * validation immediately instead of waiting until <code>save</code>.
     * @throws LockException if a lock prevents the addition of the node and this implementation performs this
     * validation immediately instead of waiting until <code>save</code>.
     * @throws UnsupportedRepositoryOperationException in a level 1 implementation.
     * @throws RepositoryException if another error occurs or if the <code>relPath</code> provided has an index on its
     * final element.
     */
    Node storeAsNode(String s) throws ItemExistsException, PathNotFoundException, VersionException,
        ConstraintViolationException, LockException, UnsupportedRepositoryOperationException, RepositoryException;

}
