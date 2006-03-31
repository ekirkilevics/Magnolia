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

/**
 * @author Sameer Charles
 * $Id$
 */
public interface ListModel {

    /**
     * get ListModelIterator
     * @return iterator
     * */
    public ListModelIterator iterator();

    /**
     * set sort on field name
     * @param name
     * */
    public void setSortBy(String name);

    /**
     * set sort on field name
     * @param name
     * @param order
     * */
    public void setSortBy(String name, String order);

    /**
     * set group on field name
     * @param name
     * */
    public void setGroupBy(String name);

    /**
     * set group on field name
     * @param name
     * @param order
     * */
    public void setGroupBy(String name, String order);

    /**
     * get sort on field name
     * @return String field name
     * */
    public String getSortBy();

    /**
     * get sort by ordering
     * @return order ('ASCENDING' | 'DESCENDING')
     * */
    public String getSortByOrder();

    /**
     * get group on field name
     * @return String field name
     * */
    public String getGroupBy();

    /**
     * get group by ordering
     * @return order ('ASCENDING' | 'DESCENDING')
     * */
    public String getGroupByOrder();

}
