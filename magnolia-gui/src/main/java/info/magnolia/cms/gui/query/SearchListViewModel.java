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
package info.magnolia.cms.gui.query;

/**
 * @author Sameer Charles
 * $Id :$
 */
public abstract class SearchListViewModel implements ListViewModel {

    /**
     * search query to be used by sub implementation
     * */
    protected SearchQuery query;

    /**
     * sort by field name
     * */
    protected String sortBy;

    /**
     * group by field name
     * */
    protected String groupBy;

    /**
     * set Query
     * @param query
     * */
    public void setQuery(SearchQuery query) {
        this.query = query;
    }

    /**
     * @return Iterator over found records
     * @see ListViewIterator
     * */
    public ListViewIterator iterator() {
        return null;
    }

    /**
     * set sort by field
     * @param name
     * */
    public void setSortBy(String name) {
        this.sortBy = name;
    }

    /**
     * set group by field
     * @param name
     * */
    public void setGroupBy(String name) {
        this.groupBy = name;
    }

    /**
     * get sort on field name
     * @return String field name
     * */
    public String getSortBy() {
        return this.sortBy;
    }

    /**
     * get group on field name
     * @return String field name
     * */
    public String getGroupBy() {
        return this.groupBy;
    }
}
