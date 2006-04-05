/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.gui.controlx.search;

import info.magnolia.cms.gui.controlx.list.AbstractListModel;
import info.magnolia.cms.gui.query.SearchQuery;


/**
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 *
 */
public abstract class AbstractSearchableListModel extends AbstractListModel implements SearchableListModel {

    /**
     * search query to be used by sub implementation
     * */
    protected SearchQuery query;

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


}
