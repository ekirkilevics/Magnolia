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
package info.magnolia.cms.gui.controlx.version;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.controlx.list.AbstractListModel;
import info.magnolia.cms.gui.controlx.list.ListModelIterator;
import info.magnolia.cms.gui.controlx.list.ListModelIteratorImpl;
import info.magnolia.cms.gui.query.SearchQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionIterator;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 * $Id:VersionListModel.java 2544 2006-04-04 12:47:32Z philipp $
 */
public class VersionListModel extends AbstractListModel {

    /**
     * Logger
     * */
    private static final Logger log = Logger.getLogger(VersionListModel.class);

    /**
     * versioned node
     * */
    private Content content;

    /**
     * search query to be used by sub implementation
     * */
    protected SearchQuery query;

    /**
     * constructor
     * */
    public VersionListModel(Content content) {
        this.content = content;
    }

    /**
     * @return Iterator over found records
     * @see info.magnolia.cms.gui.controlx.list.ListModelIterator
     */
    public ListModelIterator iterator() {
        try {
            return new ListModelIteratorImpl((List) this.doSort(this.getAllVersions()), this.getGroupBy());
        } catch (RepositoryException re) {
            log.error("Failed to get ListModelIterator, returning blank Iterator");
            log.error(re.getMessage(), re);
        }
        return new ListModelIteratorImpl(new ArrayList(), this.getGroupBy());
    }

    /**
     * get all versions
     * @return all versions in a collection
     * */
    private Collection getAllVersions() throws RepositoryException {
        VersionIterator iterator = this.content.getVersionHistory().getAllVersions();
        Collection allVersions = new ArrayList();
        while (iterator.hasNext()) {
            Version version = iterator.nextVersion();
            allVersions.add(this.content.getVersionedContent(version));
        }
        return allVersions;
    }

    /**
     * sort
     * @param collection
     * @return sorted collection
     * */
    private Collection doSort(Collection collection) {
        if (StringUtils.isNotEmpty(this.getGroupBy())) {
            Collections.sort((List) collection, new ListComparator(this.getGroupBy(), this.getSortBy()));
        }
        if (StringUtils.isNotEmpty(this.getGroupBy()) && StringUtils.isNotEmpty(this.getSortBy())) { // sub sort
            Collections.sort((List) collection, new ListComparator("", this.getSortBy()));
        }
        return collection;
    }

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
     * Does simple or sub ordering
     * */
    private class ListComparator implements Comparator {

        private String groupBy;

        private String sortBy;

        ListComparator(String groupBy, String sortBy) {
            this.groupBy = groupBy;
            this.sortBy = sortBy;
        }

        public int compare(Object object, Object object1) {
            if (StringUtils.isNotEmpty(this.groupBy)) {
                return this.group(object, object1);
            } else if (StringUtils.isNotEmpty(this.sortBy)) {
                return this.subSort(object, object1);
            }
            return 0;
        }

        /**
         * group by
         * @param object to be compared
         * @param object1 to be compared
         * */
        private int group(Object object, Object object1) {
            String firstKey = ((Content) object).getNodeData(this.groupBy).getString();
            String secondKey = ((Content) object1).getNodeData(this.groupBy).getString();
            return firstKey.compareTo(secondKey);
        }

        /**
         * sub sort
         * @param object to be compared
         * @param object1 to be compared
         * */
        private int subSort(Object object, Object object1) {
            String firstKey = ((Content) object).getNodeData(this.groupBy).getString();
            String secondKey = ((Content) object1).getNodeData(this.groupBy).getString();
            String subSortFirstKey = ((Content) object).getNodeData(this.sortBy).getString();
            String subSortSecondKey = ((Content) object1).getNodeData(this.sortBy).getString();
            if (firstKey.equalsIgnoreCase(secondKey)) {
                return subSortFirstKey.compareTo(subSortSecondKey);
            }
            return -1;
        }

    }

}
