package info.magnolia.cms.core.search;

import info.magnolia.cms.security.AccessManager;

import javax.jcr.query.InvalidQueryException;
import javax.jcr.RepositoryException;
import javax.jcr.Node;

/**
 * Date: Mar 29, 2005
 * Time: 2:54:21 PM
 *
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

    public String[] getSupportedQueryLanguages() {
        return this.queryManager.getSupportedQueryLanguages();
    }

}
