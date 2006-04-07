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
package info.magnolia.cms.gui.controlx.list;

import org.apache.commons.lang.StringUtils;

import java.util.Comparator;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.controlx.list.util.ValueProvider;


/**
 * @author Sameer Charles
 * $Id$
 */
public abstract class AbstractListModel implements ListModel {


    /**
     * sort or group by order
     * */
    public static final String DESCENDING = "DESC";

    /**
     * sort or group by order
     * */
    public static final String ASCENDING = "ASC";

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
     * this must be implemented by implementing classes
     * @return Iterator over found records
     * @see ListModelIterator
     * */
    public abstract ListModelIterator iterator();

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

    /**
     * sort
     * @param collection
     * @return sorted collection
     * */
    protected Collection doSort(Collection collection) {
        if (StringUtils.isNotEmpty(this.getGroupBy())) {
            ListComparator comparator = new ListComparator();
            comparator.setSortBy(this.getGroupBy());
            comparator.setOrder(this.getGroupByOrder());
            Collections.sort((List) collection, comparator);
        }
        if (StringUtils.isNotEmpty(this.getGroupBy()) && StringUtils.isNotEmpty(this.getSortBy())) { // sub sort
            ListComparator comparator = new ListComparator();
            comparator.setPreSort(this.getGroupBy());
            comparator.setSortBy(this.getSortBy());
            comparator.setOrder(this.getSortByOrder());
            Collections.sort((List) collection, comparator);
        }
        if (StringUtils.isEmpty(this.getGroupBy()) && StringUtils.isNotEmpty(this.getSortBy())) {
            ListComparator comparator = new ListComparator();
            comparator.setSortBy(this.getSortBy());
            comparator.setOrder(this.getSortByOrder());
            Collections.sort((List) collection, comparator);
        }
        return collection;
    }

    /**
     * Does simple or sub ordering
     * */
    protected class ListComparator implements Comparator {

        private String preSort;

        private String sortBy;

        private String order;

        private ValueProvider valueProvider;

        ListComparator() {
            this.valueProvider = ValueProvider.getInstance();
        }

        public int compare(Object object, Object object1) {
            if (StringUtils.isNotEmpty(this.sortBy) && StringUtils.isEmpty(this.preSort)) {
                return this.sort(object, object1);
            } else if (StringUtils.isNotEmpty(this.sortBy) && StringUtils.isNotEmpty(this.preSort)) {
                return this.subSort(object, object1);
            }
            return 0;
        }

        /**
         * group by
         * @param object to be compared
         * @param object1 to be compared
         * */
        private int sort(Object object, Object object1) {
            String firstKey = (String) this.valueProvider.getValue(this.sortBy, (Content) object);
            String secondKey = (String) this.valueProvider.getValue(this.sortBy, (Content) object1);
            if (this.getOrder().equalsIgnoreCase(ASCENDING))
                return firstKey.compareTo(secondKey);
            else
                return secondKey.compareTo(firstKey);
        }

        /**
         * sub sort
         * @param object to be compared
         * @param object1 to be compared
         * */
        private int subSort(Object object, Object object1) {
            String firstKey = (String) this.valueProvider.getValue(this.preSort, (Content) object);
            String secondKey = (String) this.valueProvider.getValue(this.preSort, (Content) object1);
            String subSortFirstKey = (String) this.valueProvider.getValue(this.sortBy, (Content) object);
            String subSortSecondKey = (String) this.valueProvider.getValue(this.sortBy, (Content) object1);
            if (firstKey.equalsIgnoreCase(secondKey)) {
                if (this.getOrder().equalsIgnoreCase(ASCENDING))
                    return subSortFirstKey.compareTo(subSortSecondKey);
                else
                    return subSortSecondKey.compareTo(subSortFirstKey);
            }
            return -1;
        }

        public String getPreSort() {
            return preSort;
        }

        public void setPreSort(String preSort) {
            this.preSort = preSort;
        }

        public String getSortBy() {
            return sortBy;
        }

        public void setSortBy(String sortBy) {
            this.sortBy = sortBy;
        }

        public String getOrder() {
            if (order == null)
                return ASCENDING;
            return order;
        }

        public void setOrder(String order) {
            this.order = order;
        }

    }
}
