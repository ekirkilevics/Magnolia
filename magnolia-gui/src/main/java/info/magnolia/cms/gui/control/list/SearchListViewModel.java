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
package info.magnolia.cms.gui.control.list;

import info.magnolia.cms.gui.query.SearchQuery;

/**
 * @author Sameer Charles
 * $Id$
 */
public abstract class SearchListViewModel implements ListViewModel {

    /**
     * sort or group by order
     * */
    public static final String DESCENDING = "DESC";

    /**
     * sort or group by order
     * */
    public static final String ASCENDING = "ASC";

    /**
     * search query to be used by sub implementation
     * */
    protected SearchQuery query;

    /**
     * sort by field name
     * */
    protected String sortBy;

    /**
     * sort by order
     * */
    protected String sortByOrder;

    /**
     * group by field name
     * */
    protected String groupBy;

    /**
     * group by order
     * */
    protected String groupByOrder;

    /**
     * set Query
     * @param query
     * */
    public void setQuery(SearchQuery query) {
        this.query = query;
    }

    /**
     * get query
     * @return query
     * */
    public SearchQuery getQuery() {
        return this.query;
    }

    /**
     * this must be implemented by implementing classes
     * @return Iterator over found records
     * @see ListViewIterator
     * */
    public abstract ListViewIterator iterator();

    /**
     * set sort by field
     * @param name
     * */
    public void setSortBy(String name) {
        this.sortBy = name;
    }

    /**
     * set sort by field and order ('ASCENDING' | 'DESCENDING')
     * @param name
     * @param order
     * */
    public void setSortBy(String name, String order) {
        this.sortBy = name;
        this.sortByOrder = order;
    }

    /**
     * set group by field
     * @param name
     * */
    public void setGroupBy(String name) {
        this.groupBy = name;
    }

    /**
     * set group by field and order ('ASCENDING' | 'DESCENDING')
     * @param name
     * @param order
     * */
    public void setGroupBy(String name, String order) {
        this.groupBy = name;
        this.groupByOrder = order;
    }

    /**
     * get sort on field name
     * @return String field name
     * */
    public String getSortBy() {
        return this.sortBy;
    }

    /**
     * get sort by ordering
     * @return order ('ASCENDING' | 'DESCENDING')
     * */
    public String getSortByOrder() {
        return this.sortByOrder;
    }

    /**
     * get group on field name
     * @return String field name
     * */
    public String getGroupBy() {
        return this.groupBy;
    }

    /**
     * get group by ordering
     * @return order ('ASCENDING' | 'DESCENDING')
     * */
    public String getGroupByOrder() {
        return this.groupByOrder;
    }

}
