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
package info.magnolia.cms.core.search;

import info.magnolia.cms.security.AccessManager;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;


/**
 * Date: Mar 29, 2005 Time: 2:54:21 PM
 * @author Sameer Charles
 */

public class QueryManagerImpl implements QueryManager {

    private javax.jcr.query.QueryManager queryManager;

    private AccessManager accessManager;

    protected QueryManagerImpl(javax.jcr.query.QueryManager queryManager, AccessManager accessManager) {
        this.queryManager = queryManager;
        this.accessManager = accessManager;
    }

    public Query createQuery(String s, String s1) throws InvalidQueryException, RepositoryException {
        javax.jcr.query.Query query = this.queryManager.createQuery(s, s1);
        return (new QueryImpl(query, this.accessManager));
    }

    public Query getQuery(Node node) throws InvalidQueryException, RepositoryException {
        javax.jcr.query.Query query = this.queryManager.getQuery(node);
        return (new QueryImpl(query, this.accessManager));
    }

    public String[] getSupportedQueryLanguages() throws RepositoryException {
        return this.queryManager.getSupportedQueryLanguages();
    }

}
