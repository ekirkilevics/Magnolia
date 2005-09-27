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

import info.magnolia.cms.security.AccessManager;

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
 * Date: Mar 29, 2005 Time: 2:57:55 PM
 * @author Sameer Charles
 */

public class QueryImpl implements Query {

    private javax.jcr.query.Query query;

    private AccessManager accessManager;

    protected QueryImpl(javax.jcr.query.Query query, AccessManager accessManager) {
        this.query = query;
        this.accessManager = accessManager;
    }

    public QueryResult execute() throws RepositoryException {
        javax.jcr.query.QueryResult result = this.query.execute();
        QueryResultImpl filteredResult = new QueryResultImpl(result, this.accessManager);
        return filteredResult;
    }

    public String getStatement() {
        return this.query.getStatement();
    }

    public String getLanguage() {
        return this.query.getLanguage();
    }

    public String getStoredQueryPath() throws ItemNotFoundException, RepositoryException {
        return this.query.getStoredQueryPath();
    }

    public Node storeAsNode(String s) throws ItemExistsException, PathNotFoundException, VersionException,
        ConstraintViolationException, LockException, UnsupportedRepositoryOperationException, RepositoryException {
        return this.query.storeAsNode(s);
    }
}
