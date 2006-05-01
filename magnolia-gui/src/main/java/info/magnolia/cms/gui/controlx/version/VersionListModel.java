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
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionIterator;

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
     * get all versions
     * @return all versions in a collection
     * */
    protected Collection getResult() throws RepositoryException {
        VersionIterator iterator = this.content.getVersionHistory().getAllVersions();
        Collection allVersions = new ArrayList();
        while (iterator.hasNext()) {
            Version version = iterator.nextVersion();
            allVersions.add(this.content.getVersionedContent(version));
        }
        return allVersions;
    }
}
