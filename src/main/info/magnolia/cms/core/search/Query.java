package info.magnolia.cms.core.search;

import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;
import javax.jcr.*;

/**
 * Date: Apr 4, 2005
 * Time: 11:02:35 AM
 *
 * @author Sameer Charles
 */

public interface Query {

    String XPATH = "xpath";
    String SQL = "sql";

    QueryResult execute() throws RepositoryException;

    String getStatement();

    String getLanguage();

    String getPersistentQueryPath() throws ItemNotFoundException, RepositoryException;

    void save(String s) throws ItemExistsException, PathNotFoundException, VersionException,
    ConstraintViolationException, LockException, UnsupportedRepositoryOperationException, RepositoryException;

}
