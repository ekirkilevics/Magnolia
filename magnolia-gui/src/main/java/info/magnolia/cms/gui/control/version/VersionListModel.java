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
package info.magnolia.cms.gui.control.version;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.control.list.AbstractListModel;
import info.magnolia.cms.gui.control.list.ListModelIterator;
import info.magnolia.cms.gui.control.list.ListModelIteratorImpl;

import javax.jcr.version.VersionIterator;
import javax.jcr.version.Version;
import javax.jcr.RepositoryException;
import java.util.*;

import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 * $Id$
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
     * constructor
     * */
    public VersionListModel(Content content) {
        this.content = content;
    }

    /**
     * @return Iterator over found records
     * @see info.magnolia.cms.gui.control.list.ListModelIterator
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
        Collections.sort((List) collection, new ListComparator());
        return collection;
    }

    /**
     * Does simple or sub ordering
     * */
    private class ListComparator implements Comparator {

        public int compare(Object object, Object object1) {
            return 0;
        }
    }
    
}
