/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.core.search;

import info.magnolia.cms.core.DefaultContent;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.context.MgnlContext;

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

    private HierarchyManager hm;

    private Map dirtyHandles = new Hashtable();

    protected QueryResultImpl(javax.jcr.query.QueryResult result, AccessManager accessManager, HierarchyManager hm) {
        this.result = result;
        this.accessManager = accessManager;
        this.hm = hm;
    }

    public AccessManager getAccessManager() {
        return accessManager;
    }

    public javax.jcr.query.QueryResult getJcrResult() {
        return result;
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
                log.error("{} caught while iterating on query results: {}", re.getClass().getName(), re.getMessage());
                if (log.isDebugEnabled()) {
                    log.debug(
                        re.getClass().getName() + " caught while iterating on query results: " + re.getMessage(),
                        re);
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
        if ((node.isNodeType(nodeType) || StringUtils.isEmpty(nodeType)) && !node.isNodeType(ItemType.NT_RESOURCE)) {
            if (this.dirtyHandles.get(node.getPath()) == null) {
                boolean isAllowed = this.accessManager.isGranted(Path.getAbsolutePath(node.getPath()), Permission.READ);
                if (isAllowed) {
                    collection.add(new DefaultContent(node, this.hm));
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
