/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.gui.controlx.list;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles $Id$
 */
public abstract class AbstractListModel implements ListModel {

    private static Logger log = LoggerFactory.getLogger(AbstractListModel.class);

    /**
     * sort or group by order
     */
    public static final String DESCENDING = "DESC";

    /**
     * sort or group by order
     */
    public static final String ASCENDING = "ASC";

    /**
     * sort by field name
     */
    protected String sortBy;

    /**
     * sort by order
     */
    protected String sortByOrder;

    /**
     * group by field name
     */
    protected String groupBy;

    /**
     * group by order
     */
    protected String groupByOrder;

    /**
     * Used to get values out of the nodes/objects handled by the list
     */
    private ValueProvider valueProvider;

    /**
     * this must be implemented by implementing classes
     * @return Iterator over found records
     * @see ListModelIterator
     */
    public ListModelIterator getListModelIterator() {
        try {
            Collection items = getResult();
            items = doSort(items);
            return createIterator(items);
        }
        catch (Exception re) {
            log.error("can't create the list model iterator, will return an empty list", re);
            return new ListModelIteratorImpl(this, new ArrayList());
        }
    }

    public Iterator iterator() {
        return getListModelIterator();
    }

    /**
     * @return the collection of the items passed to the iterator
     */
    protected abstract Collection getResult() throws Exception;

    /**
     * Create the iterator
     * @param items
     * @return
     */
    protected ListModelIterator createIterator(Collection items) {
        if(!(items instanceof List)){
            throw new RuntimeException("items must be a List");
        }
        return new ListModelIteratorImpl(this, (List) items);
    }

    /**
     * set sort by field
     * @param name
     */
    public void setSortBy(String name) {
        this.sortBy = name;
    }

    /**
     * set sort by field and order ('ASCENDING' | 'DESCENDING')
     * @param name
     * @param order
     */
    public void setSortBy(String name, String order) {
        this.sortBy = name;
        this.sortByOrder = order;
    }

    /**
     * set group by field
     * @param name
     */
    public void setGroupBy(String name) {
        this.groupBy = name;
    }

    /**
     * set group by field and order ('ASCENDING' | 'DESCENDING')
     * @param name
     * @param order
     */
    public void setGroupBy(String name, String order) {
        this.groupBy = name;
        this.groupByOrder = order;
    }

    /**
     * get sort on field name
     * @return String field name
     */
    public String getSortBy() {
        return this.sortBy;
    }

    /**
     * get sort by ordering
     * @return order ('ASCENDING' | 'DESCENDING')
     */
    public String getSortByOrder() {
        return this.sortByOrder;
    }

    /**
     * get group on field name
     * @return String field name
     */
    public String getGroupBy() {
        return this.groupBy;
    }

    /**
     * get group by ordering
     * @return order ('ASCENDING' | 'DESCENDING')
     */
    public String getGroupByOrder() {
        return this.groupByOrder;
    }

    /**
     * sort
     * @param collection
     * @return sorted collection
     */
    protected Collection doSort(Collection collection) {
        if(!(collection instanceof List)){
            log.warn("can sort only collections of type {} but got a {}", List.class, collection.getClass());
            return collection;
        }
        List list = (List) collection;
        if (StringUtils.isNotEmpty(this.getGroupBy())) {
            ListComparator comparator = newComparator();
            comparator.setSortBy(this.getGroupBy());
            comparator.setOrder(this.getGroupByOrder());
            Collections.sort(list, comparator);
        }
        if (StringUtils.isNotEmpty(this.getGroupBy()) && StringUtils.isNotEmpty(this.getSortBy())) { // sub sort
            ListComparator comparator = newComparator();
            comparator.setPreSort(this.getGroupBy());
            comparator.setSortBy(this.getSortBy());
            comparator.setOrder(this.getSortByOrder());
            Collections.sort(list, comparator);
        }
        if (StringUtils.isEmpty(this.getGroupBy()) && StringUtils.isNotEmpty(this.getSortBy())) {
            ListComparator comparator = newComparator();
            comparator.setSortBy(this.getSortBy());
            comparator.setOrder(this.getSortByOrder());
            Collections.sort(list, comparator);
        }
        return list;
    }

    protected ListComparator newComparator() {
        return new ListComparator();
    }

    /**
     * @param valueProvider the valueProvider to set
     */
    public void setValueProvider(ValueProvider valueProvider) {
        this.valueProvider = valueProvider;
    }

    /**
     * @return the valueProvider
     */
    public ValueProvider getValueProvider() {
        if (valueProvider == null) {
            valueProvider = DefaultValueProvider.getInstance();
        }
        return valueProvider;
    }

    /**
     * Use by the list iterator to resolve the id
     */
    protected String resolveId(int index, Object value){
        return Integer.toString(index);
    }

    /**
     * Does simple or sub ordering
     */
    protected class ListComparator implements Comparator {

        private String preSort;

        private String sortBy;

        private String order;

        public int compare(Object object, Object object1) {
            if (StringUtils.isNotEmpty(this.sortBy) && StringUtils.isEmpty(this.preSort)) {
                return this.sort(object, object1);
            }
            else if (StringUtils.isNotEmpty(this.sortBy) && StringUtils.isNotEmpty(this.preSort)) {
                return this.subSort(object, object1);
            }
            return 0;
        }

        /**
         * group by
         * @param object to be compared
         * @param object1 to be compared
         */
        protected int sort(Object object, Object object1) {
            Comparable firstKey = (Comparable) getValueProvider().getValue(this.sortBy, object);
            Comparable secondKey = (Comparable) getValueProvider().getValue(this.sortBy, object1);
            if (this.getOrder().equalsIgnoreCase(ASCENDING)) {
                return firstKey.compareTo(secondKey);
            }

            return secondKey.compareTo(firstKey);
        }

        /**
         * sub sort
         * @param object to be compared
         * @param object1 to be compared
         */
        protected int subSort(Object object, Object object1) {
            String firstKey = (String) getValueProvider().getValue(this.preSort, object);
            String secondKey = (String) getValueProvider().getValue(this.preSort, object1);
            Comparable subSortFirstKey = (Comparable) getValueProvider().getValue(this.sortBy, object);
            Comparable subSortSecondKey = (Comparable) getValueProvider().getValue(this.sortBy, object1);
            if (firstKey.equalsIgnoreCase(secondKey)) {
                if (this.getOrder().equalsIgnoreCase(ASCENDING)) {
                    return subSortFirstKey.compareTo(subSortSecondKey);
                }
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
            if (order == null) {
                return ASCENDING;
            }
            return order;
        }

        public void setOrder(String order) {
            this.order = order;
        }

    }
}
