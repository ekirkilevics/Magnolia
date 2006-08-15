/**
 * 
 */
package info.magnolia.cms.util;

import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.search.Query;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.core.search.QueryResult;
import info.magnolia.context.MgnlContext;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Util to execute queries as simple as possible.
 * @author Philipp Bracher
 * @version $Id$
 *
 */
public class QueryUtil {
    private static Logger log = LoggerFactory.getLogger(QueryUtil.class);

    /**
     * Execute a query
     * @param repository
     * @param statement
     * @return
     */
    public static Collection query(String repository, String statement){
        return query(repository,statement, Query.SQL);
    }

    /**
     * Execute a query
     * @param repository
     * @param statement
     * @param language
     * @return
     */
    public static Collection query(String repository, String statement, String language){
        return query(repository,statement,language, ItemType.NT_BASE);
    }

    /**
     * Execute a query
     * @param repository
     * @param statement
     * @param language
     * @param returnItemType
     * @return
     */
    public static Collection query(String repository, String statement, String language, String returnItemType){
        try {
            QueryManager qm = MgnlContext.getQueryManager(repository);
            Query query= qm.createQuery(statement, language);
            QueryResult result = query.execute();
            return result.getContent(returnItemType);
        }
        catch (Exception e) {
            log.error("can't query, will return empty collection", e);
        }
        
        return new ArrayList();
    }
    
}
