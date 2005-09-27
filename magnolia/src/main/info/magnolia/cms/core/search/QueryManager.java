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
