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
package info.magnolia.cms.core.search;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Permission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles
 * @author Fabrizio Giustina
 */
public class QueryResultImpl implements QueryResult {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(QueryResultImpl.class);

    /**
     * Unfiltered result object
     */
    private javax.jcr.query.QueryResult result;

    /**
     * caches all previously queried objects
     */
    private Map objectStore = new Hashtable();

    private AccessManager accessManager;

    private Map dirtyHandles = new Hashtable();

    protected QueryResultImpl(javax.jcr.query.QueryResult result, AccessManager accessManager) {
        this.result = result;
        this.accessManager = accessManager;
    }

    /**
     * Build required result objects
     */
    private void build(String nodeType, Collection collection) throws RepositoryException {
        this.objectStore.put(nodeType, collection);
        NodeIterator nodeIterator = this.result.getNodes();
        while (nodeIterator.hasNext()) {
            Node node = nodeIterator.nextNode();
            try {
                build(node, nodeType, collection);
            }
            catch (RepositoryException re) {
                log.error(re.getMessage());
                if (log.isDebugEnabled()) {
                    log.debug(re.getMessage(), re);
                }
            }
        }
    }

    /**
     * Build required result objects
     */
    private void build(Node node, String nodeType, Collection collection) throws RepositoryException {
        /**
         * All custom node types
         */
        if (node.isNodeType(nodeType)) {
            if (this.dirtyHandles.get(node.getPath()) == null) {
                boolean isAllowed = this.accessManager.isGranted(Path.getAbsolutePath(node.getPath()), Permission.READ);
                if (isAllowed) {
                    collection.add(new Content(node, this.accessManager));
                    this.dirtyHandles.put(node.getPath(), StringUtils.EMPTY);
                }
            }
            return;
        }
        if (node.getDepth() > 0) {
            this.build(node.getParent(), nodeType, collection);
        }
    }

    /**
     * @see info.magnolia.cms.core.search.QueryResult#getContent()
     */
    public Collection getContent() {
        return getContent(ItemType.CONTENT.getSystemName());
    }

    /**
     * @see info.magnolia.cms.core.search.QueryResult#getContent(java.lang.String)
     */
    public Collection getContent(String nodeType) {
        Collection resultSet = (Collection) this.objectStore.get(nodeType);
        if (resultSet == null) {
            /* build it first time */
            resultSet = new ArrayList();
            try {
                this.build(nodeType, resultSet);
            }
            catch (RepositoryException re) {
                log.error(re.getMessage());
            }
        }
        return resultSet;
    }

}
