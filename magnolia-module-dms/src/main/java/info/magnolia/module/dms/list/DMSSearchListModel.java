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
package info.magnolia.module.dms.list;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.search.QueryResult;
import info.magnolia.cms.gui.controlx.search.RepositorySearchListModel;
import info.magnolia.module.dms.beans.Document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * Returns only contenNodes which are documents.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class DMSSearchListModel extends RepositorySearchListModel {

    /**
     * Default contstructor.
     */
    public DMSSearchListModel(String repositoryId) {
        super(repositoryId);
    }

    /**
     * Return the nodes of type contentnode and check if the node is a document.
     */
    protected Collection getResult(QueryResult result) {

        Collection all = result.getContent(ItemType.CONTENTNODE.getSystemName());
        List items = new ArrayList();
        for (Iterator iter = all.iterator(); iter.hasNext();) {
            Content item = (Content) iter.next();
            if (Document.isDocument(item)) {
                items.add(item);
            }
        }
        return items;
    }
}
