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
package info.magnolia.cms.gui.control.search;

import info.magnolia.cms.core.search.Query;
import info.magnolia.cms.core.search.QueryResult;
import info.magnolia.cms.gui.control.list.ListModelIterator;
import info.magnolia.cms.gui.control.list.ListModelIteratorImpl;
import info.magnolia.cms.gui.control.list.AbstractListModel;
import info.magnolia.cms.beans.runtime.MgnlContext;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;


/**
 * @author Sameer Charles
 * $Id$
 */
public class RepositorySearchListModel extends AbstractListModel {

    /**
     * Logger
     * */
    private static Logger log = Logger.getLogger(RepositorySearchListModel.class);

    /**
     * repository id
     * */
    private String repositoryId;

    /**
     * workspace Id, if no workspace is defined - default is used which has the same name as repository name
     * */
    private String workspaceId;

    /**
     * select from node type (optional)
     * */
    private String selectNodeType;

    /**
     * search path (optional)
     * */
    private String searchPath;

    /**
     * default constructor
     * */
    public RepositorySearchListModel(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    /**
     * this must be implemented by implementing classes
     *
     * @return Iterator over found records
     * @see ListModelIterator
     */
    public ListModelIterator iterator() {
        QueryBuilder builder = new QueryBuilder(this);
        System.out.println("SQL : "+builder.getSQLStatement());
        //return this.getResult(builder.getSQLStatement());
        return this.dummyIterator();
    }

    /**
     * execute statement
     * @param statement
     * */
    private ListModelIteratorImpl getResult(String statement) {
        try {
            Query q = MgnlContext.getQueryManager(this.repositoryId).createQuery(statement, Query.SQL);
            QueryResult result = q.execute();
            return new ListModelIteratorImpl((List)result.getContent(), this.getGroupBy());
        } catch (RepositoryException re) {
            log.error(re);
        }
        return new ListModelIteratorImpl(new ArrayList(), this.getGroupBy());
    }


    /**
     * dummy iterator, will be removed
     * this does not respect <code>SearchQuery</code>
     * */
    private ListModelIterator dummyIterator() {
        String statement = "SELECT * FROM nt:base where title like '%'";
        if (StringUtils.isNotEmpty(this.getGroupBy()) && StringUtils.isNotEmpty(this.getSortBy())) {
            statement += (" order by "+this.getGroupBy()+" "+this.getGroupByOrder()+", "+this.getSortBy()+" "+this.getSortByOrder());
        } else if (StringUtils.isNotEmpty(this.getGroupBy()) && StringUtils.isEmpty(this.getSortBy())) {
            statement += (" order by "+this.getGroupBy() + " " + this.getGroupByOrder());
        } else if (StringUtils.isEmpty(this.getGroupBy()) && StringUtils.isNotEmpty(this.getSortBy())) {
            statement += (" order by "+this.getSortBy() + " " + this.getSortByOrder());
        }
        try {
            Query q = MgnlContext.getQueryManager(this.repositoryId).createQuery(statement, Query.SQL);
            QueryResult result = q.execute();
            return new ListModelIteratorImpl((List)result.getContent(), this.getGroupBy());
        } catch (RepositoryException re) {
            log.error(re);
        }
        return new ListModelIteratorImpl(new ArrayList(), this.getGroupBy());
    }

    /**
     * get repository Id
     * @return repository id
     * */
    public String getRepositoryId() {
        return repositoryId;
    }

    /**
     * set repository id
     * @param repositoryId
     * */
    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    /**
     * get workspace Id
     * @return workspace id
     * */
    public String getWorkspaceId() {
        return workspaceId;
    }

    /**
     * set workspace Id
     * @param workspaceId
     * */
    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    /**
     * get select node type, query will be executed only on these if set
     * @return nodeType name
     * */
    public String getSelectNodeType() {
        return selectNodeType;
    }

    /**
     * set select node type value, query will be executed only on these if set
     * @param selectNodeType
     * */
    public void setSelectNodeType(String selectNodeType) {
        this.selectNodeType = selectNodeType;
    }

    /**
     * get jcr path, under which search will be executed
     * @return path
     * */
    public String getSearchPath() {
        return searchPath;
    }

    /**
     * set jcr path, under which search will be executed
     * @param searchPath
     * */
    public void setSearchPath(String searchPath) {
        this.searchPath = searchPath;
    }

}
