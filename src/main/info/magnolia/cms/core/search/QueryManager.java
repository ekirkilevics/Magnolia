package info.magnolia.cms.core.search;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;


/**
 * Date: Apr 4, 2005
 * Time: 11:00:02 AM
 *
 * @author Sameer Charles
 */


public interface QueryManager {

    Query createQuery(String s, String s1) throws InvalidQueryException, RepositoryException;

    Query getQuery(Node node) throws InvalidQueryException, RepositoryException;

    String[] getSupportedQueryLanguages();

}
