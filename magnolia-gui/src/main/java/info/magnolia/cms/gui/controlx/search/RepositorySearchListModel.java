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
package info.magnolia.cms.gui.controlx.search;

import info.magnolia.cms.core.search.Query;
import info.magnolia.cms.core.search.QueryResult;
import info.magnolia.cms.gui.query.SearchQuery;
import info.magnolia.context.MgnlContext;

import java.util.Collection;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;

import org.apache.log4j.Logger;


/**
 * @author Sameer Charles $Id:RepositorySearchListModel.java 2544 2006-04-04 12:47:32Z philipp $
 */
public class RepositorySearchListModel extends AbstractSearchableListModel {

    /**
     * Logger
     */
    private static Logger log = Logger.getLogger(RepositorySearchListModel.class);

    /**
     * repository id
     */
    private String repositoryId;

    /**
     * workspace Id, if no workspace is defined - default is used which has the same name as repository name
     */
    private String workspaceId;

    /**
     * select from node type (optional)
     */
    private String nodeType = "nt:base";

    private String resultNodeType = "mgnl:content";
    
    /**
     * search path (optional)
     */
    private String searchPath;

    /**
     * search query to be used by sub implementation
     */
    protected SearchQuery query;

    /**
     * default constructor
     */
    public RepositorySearchListModel(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    /**
     * Returns the jcr query statement used by the model.
     */
    protected String buildQuery() {
        QueryBuilder builder = new QueryBuilder(this);
        return builder.getSQLStatement();
    }

    /**
     * Executes the query statement and returns the QueryResult.
     */
    protected QueryResult getResult(String statement) throws InvalidQueryException, RepositoryException {
        Query q = MgnlContext.getQueryManager(this.repositoryId).createQuery(statement, Query.SQL);
        QueryResult result = q.execute();
        return result;
    }

    /**
     * Creates the jcr query and executes it.
     */
    protected Collection getResult() throws Exception {
        String query = buildQuery();
        if(log.isDebugEnabled()){
            log.debug("query: " + query);
        }
        QueryResult result = this.getResult(query);
        Collection items = getResult(result);
        return items;
    }

    /**
     * Gets the items from the query (possibility to post filter)
     */
    protected Collection getResult(QueryResult result) {
        Collection items = result.getContent(this.getResultNodeType());
        return items;
    }

    /**
     * get repository Id
     * @return repository id
     */
    public String getRepositoryId() {
        return repositoryId;
    }

    /**
     * set repository id
     * @param repositoryId
     */
    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    /**
     * get workspace Id
     * @return workspace id
     */
    public String getWorkspaceId() {
        return workspaceId;
    }

    /**
     * set workspace Id
     * @param workspaceId
     */
    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    /**
     * get select node type, query will be executed only on these if set
     * @return nodeType name
     */
    public String getNodeType() {
        return nodeType;
    }

    /**
     * set select node type value, query will be executed only on these if set
     * @param nodeType
     */
    public void setNodeType(String selectNodeType) {
        this.nodeType = selectNodeType;
    }

    /**
     * get jcr path, under which search will be executed
     * @return path
     */
    public String getSearchPath() {
        return searchPath;
    }

    /**
     * set jcr path, under which search will be executed
     * @param searchPath
     */
    public void setSearchPath(String searchPath) {
        this.searchPath = searchPath;
    }

    /**
     * set Query
     * @param query
     */
    public void setQuery(SearchQuery query) {
        this.query = query;
    }

    /**
     * get query
     * @return query
     */
    public SearchQuery getQuery() {
        return this.query;
    }

    public void setResultNodeType(String resultNodeType) {
        this.resultNodeType = resultNodeType;
    }

    public String getResultNodeType() {
        return resultNodeType;
    }

}
