package info.magnolia.cms.core.search;

import info.magnolia.cms.security.AccessManager;


/**
 * Date: Apr 1, 2005
 * Time: 11:12:49 AM
 *
 * @author Sameer Charles
 */

public abstract class SearchFactory {

    public static QueryManager getAccessControllableQueryManager(javax.jcr.query.QueryManager queryManager,
    AccessManager accessManager) {
        return (new QueryManagerImpl(queryManager, accessManager));
    }

}
