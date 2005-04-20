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
package info.magnolia.cms.core.search;

import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.beans.config.ItemType;

import javax.jcr.NodeIterator;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.*;

import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;


/**
 * Date: Apr 1, 2005
 * Time: 1:10:04 PM
 *
 * @author Sameer Charles
 */

public class QueryResultImpl implements QueryResult {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(QueryResultImpl.class);

    /**
     * Unfiltered result object
     * */
    private javax.jcr.query.QueryResult result;

    private AccessManager accessManager;

    /**
     * Resultant iterators
     * */
    private Collection nodeDataCollection = new ArrayList();

    private Collection contentCollection = new ArrayList();

    private Map dirtyHandles = new Hashtable();

    protected QueryResultImpl(javax.jcr.query.QueryResult result, AccessManager accessManager) {
        this.result = result;
        this.accessManager = accessManager;
        try {
            this.doFilter();
        } catch (RepositoryException re) {
            log.error("Failed to filter results " + re.getMessage());
            log.debug(re);
        }
    }

    /**
     * Filters the original result object using specified access manager
     * */
    private void doFilter() throws RepositoryException {
        NodeIterator nodeIterator = this.result.getNodes();
        while (nodeIterator.hasNext()) {
            Node node = nodeIterator.nextNode();
            try {
                boolean isAllowed = this.accessManager.isGranted(Path.getAbsolutePath(node.getPath()), Permission.READ);
                if (isAllowed) {
                    this.build(node);
                }
            } catch (RepositoryException re) {
                log.error(re.getMessage());
                log.debug(re);
            }
        }
    }

    /**
     * Build required result objects
     * */
    private void build(Node node) throws RepositoryException {
        if (node.isNodeType(ItemType.NT_UNSTRUCTRUED)) {
            // ignore, parent will be added as NodeData
        } else if (node.isNodeType(ItemType.NT_NODEDATA)) {
            this.nodeDataCollection.add(new NodeData(node, this.accessManager));
        } else {
            /**
             * All custom node types and mgnl:content
             * */
            if (this.dirtyHandles.get(node.getPath()) == null) {
                this.contentCollection.add(new Content(node, this.accessManager));
                this.dirtyHandles.put(node.getPath(), StringUtils.EMPTY);
            }
            return;
        }
        this.build(node.getParent());
    }

    public Iterator getNodeDataIterator() {
        return this.nodeDataCollection.iterator();
    }

    public Iterator getContentIterator() {
        return this.contentCollection.iterator();
    }

}
