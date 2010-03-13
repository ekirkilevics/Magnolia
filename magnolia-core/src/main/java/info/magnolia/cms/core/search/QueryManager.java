/**
 * This file Copyright (c) 2003-2010 Magnolia International
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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;


/**
 * Date: Apr 4, 2005 Time: 11:00:02 AM
 * @author Sameer Charles
 */

public interface QueryManager {

    /**
     * <i>Description inherited from javax.jcr.query.QueryManager#createQuery(String, String)</i><br>
     * Creates a new query by specifying the query <code>statement</code> itself and the <code>language</code> in
     * which the query is stated. If the query <code>statement</code> is syntactically invalid, given the language
     * specified, an <code>InvalidQueryException</code> is thrown. The <code>language</code> must be a string from
     * among those returned by QueryManager.getSupportedQueryLanguages(); if it is not, then an
     * <code>InvalidQueryException</code> is thrown.
     * @throws InvalidQueryException if statement is invalid or language is unsupported.
     * @throws RepositoryException if another error occurs
     * @return A <code>Query</code> object.
     */
    Query createQuery(String statement, String language) throws InvalidQueryException, RepositoryException;

    /**
     * <i>Description inherited from javax.jcr.query.QueryManager#getQuery(javax.jcr.Node)</i><br>
     * Retrieves an existing persistent query. If <code>node</code> is not a valid persisted query (that is, a node of
     * type <code>nt:query</code>), an <code>InvalidQueryException</code> is thrown. <p/> Persistent queries are
     * created by first using <code>QueryManager.createQuery</code> to create a <code>Query</code> object and then
     * calling <code>Query.save</code> to persist the query to a location in the workspace.
     * @param node a persisted query (that is, a node of type <code>nt:query</code>).
     * @throws InvalidQueryException If <code>node</code> is not a valid persisted query (that is, a node of type
     * <code>nt:query</code>).
     * @throws RepositoryException if another error occurs
     * @return a <code>Query</code> object.
     */
    Query getQuery(Node node) throws InvalidQueryException, RepositoryException;

    /**
     * <i>Description inherited from javax.jcr.query.QueryManager#getSupportedQueryLanguages()</i><br>
     * Returns an array of strings representing all query languages supported by this repository. In level 1 this set
     * must include the string represented by the constant {@link Query#XPATH}. If SQL is supported it must
     * additionally include the string represented by the constant {@link Query#SQL}. An implementation may also
     * support other languages as well. See {@link Query}.
     * @return An string array.
     * @throws RepositoryException if an error occurs.
     */
    String[] getSupportedQueryLanguages() throws RepositoryException;

}
