package info.magnolia.cms.core.search;

import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.core.Content;
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

    private Collection contentNodeCollection = new ArrayList();

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
                boolean isAllowed = this.accessManager.isGranted(node.getPath(), Permission.READ);
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
        if (node.isNodeType(ItemType.NT_NODEDATA)) {
            this.nodeDataCollection.add(new NodeData(node, this.accessManager));
        } else if (node.isNodeType(ItemType.NT_CONTENTNODE)) {
            if (this.dirtyHandles.get(node.getPath()) == null) {
                this.nodeDataCollection.add(new ContentNode(node, this.accessManager));
                this.dirtyHandles.put(node.getPath(), StringUtils.EMPTY);
            }
        } else {
            /**
             * All custom node types and mgnl:content
             * */
            if (this.dirtyHandles.get(node.getPath()) == null) {
                this.nodeDataCollection.add(new Content(node, this.accessManager));
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

    public Iterator getContentNodeIterator() {
        return this.contentNodeCollection.iterator();
    }

}
