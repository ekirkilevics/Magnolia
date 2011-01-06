/**
 * This file Copyright (c) 2003-2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.gui.controlx.search;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.search.Query;
import info.magnolia.cms.core.search.QueryResult;
import info.magnolia.cms.gui.query.SearchQuery;
import info.magnolia.context.MgnlContext;

import java.util.Collection;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


/**
 * @author Sameer Charles $Id:RepositorySearchListModel.java 2544 2006-04-04 12:47:32Z philipp $
 */
public class RepositorySearchListModel extends AbstractSearchableListModel {
    private static final Logger log = LoggerFactory.getLogger(RepositorySearchListModel.class);

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
        QueryBuilder builder = getQueryBuilder();
        return builder.getSQLStatement();
    }

    protected QueryBuilder getQueryBuilder() {
        return new QueryBuilder(this);
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
        return getResult(result);
    }

    /**
     * Gets the items from the query (possibility to post filter)
     */
    protected Collection getResult(QueryResult result) {
        return result.getContent(this.getResultNodeType());
    }

    /**
     * Returns the uuid of the node
     */
    protected String resolveId(int index, Object value) {
        if(value instanceof Content){
            return ((Content)value).getUUID();
        }
        return super.resolveId(index, value);
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
        // this is needed in case the list page is not a searchable list
        if(this.query == null){
            this.query = new SearchQuery();
        }
        return this.query;
    }

    public void setResultNodeType(String resultNodeType) {
        this.resultNodeType = resultNodeType;
    }

    public String getResultNodeType() {
        return resultNodeType;
    }

}
