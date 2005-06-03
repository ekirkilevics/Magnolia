package info.magnolia.cms.util;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.search.QueryResult;
import info.magnolia.cms.core.search.Query;
import info.magnolia.cms.core.search.QueryManager;

import javax.jcr.RepositoryException;

import org.apache.log4j.Logger;

import java.util.Iterator;

/**
 * This is a temporary class to search for content object based on mgnl:UUID property
 * it will be replaced later by HierarchyManager.getContentByUUID(String)
 *
 * @author Sameer Charles
 * @version $Revision $ ($Author $)
 */
public class Search {

    /**
     * Logger
     * */
    private static Logger log = Logger.getLogger(Search.class);

    /**
     * Using JCR search to get content object associated with the given UUID
     * @param queryManager
     * @param uuid
     * */
    public static Content getContentByUUID(QueryManager queryManager, String uuid) {
        try {
            String statement = "SELECT * FROM nt:base where mgnl:uuid like '"+uuid+"'";
            Query q = queryManager.createQuery(statement, Query.SQL);
            QueryResult result = q.execute();
            Iterator it = result.getContent().iterator();
            while (it.hasNext()) {
                Content foundObject = (Content) it.next();
                return foundObject;
            }
        } catch (RepositoryException e) {
            log.error(e);
        }
        return null;
    }


}
