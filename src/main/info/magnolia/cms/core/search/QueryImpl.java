package info.magnolia.cms.core.search;

import info.magnolia.cms.security.AccessManager;

import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;
import javax.jcr.*;

/**
 * Date: Mar 29, 2005
 * Time: 2:57:55 PM
 *
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

    public String getPersistentQueryPath() throws ItemNotFoundException, RepositoryException {
        return this.query.getPersistentQueryPath();
    }

    public void save(String s) throws ItemExistsException, PathNotFoundException, VersionException,
    ConstraintViolationException, LockException, UnsupportedRepositoryOperationException, RepositoryException {
        this.query.save(s);
    }
}
