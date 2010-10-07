/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.cms.core.search;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.DefaultContent;
import info.magnolia.cms.core.HierarchyManager;
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
 * Wrapping a JCR {@link javax.jcr.query.QueryResult}. This class will filter
 * the result according to the user's ACLs. You can use
 * {@link #getContent(String)} to retrieve nodes of a certain type. If the
 * node's type doesn't match the nearest matching ancestors is add instead. This
 * allows to search in paragraph content while retrieving  a list of pages.
 *
 * @author Sameer Charles
 * @author Fabrizio Giustina
 */
public class QueryResultImpl implements QueryResult {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(QueryResultImpl.class);

    /**
     * Unfiltered result object.
     */
    protected javax.jcr.query.QueryResult result;

    /**
     * caches all previously queried objects.
     */
    protected Map<String, Collection<Content>> objectStore = new Hashtable<String, Collection<Content>>();

    /**
     * @deprecated
     */
    private AccessManager accessManager;

    protected HierarchyManager hm;

    protected Map<String, String> dirtyHandles = new Hashtable<String, String>();

    protected QueryResultImpl(javax.jcr.query.QueryResult result, HierarchyManager hm) {
        this.result = result;
        this.hm = hm;
        this.accessManager = hm.getAccessManager();
    }

    /**
     * @deprecated
     * @return
     */
    public AccessManager getAccessManager() {
        return accessManager;
    }

    public javax.jcr.query.QueryResult getJcrResult() {
        return result;
    }

    /**
     * Adds all found nodes of a certain type. If the type doesn't match it will traverse the ancestors and add them instead.
     */
    protected void build(String nodeType, Collection<Content> collection) throws RepositoryException {
        this.objectStore.put(nodeType, collection);
        NodeIterator nodeIterator = this.result.getNodes();

        // whitespace separated list (can't hurt since a single nodetype name can't contain a space)
        String[] nodeTypes = StringUtils.split(nodeType);

        while (nodeIterator.hasNext()) {
            Node node = nodeIterator.nextNode();
            try {
                build(node, nodeTypes, collection);
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
     * Traverses the hierarchy from the current node to the root until the node's type matches.
     */
    protected void build(Node node, String[] nodeType, Collection<Content> collection) throws RepositoryException {
        /**
         * All custom node types
         */
        if ((nodeType== null || nodeType.length == 0) || isNodeType(node, nodeType) && !node.isNodeType(ItemType.NT_RESOURCE)) {
            if (this.dirtyHandles.get(node.getPath()) == null) {
                boolean isAllowed = this.hm.getAccessManager().isGranted(Path.getAbsolutePath(node.getPath()), Permission.READ);
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

    public Collection<Content> getContent() {
        return getContent(ItemType.CONTENT.getSystemName());
    }

    public Collection<Content> getContent(String nodeType) {
        Collection<Content> resultSet = this.objectStore.get(nodeType);
        if (resultSet == null) {
            /* build it first time */
            resultSet = new ArrayList<Content>();
            try {
                this.build(nodeType, resultSet);
            }
            catch (RepositoryException re) {
                log.error(re.getMessage());
            }
        }
        return resultSet;
    }

    private boolean isNodeType(Node node, String[] nodeType) throws RepositoryException {

        for (String nt : nodeType) {
            if (node.isNodeType(nt)) {
                return true;
            }
        }
        return false;
    }
}
