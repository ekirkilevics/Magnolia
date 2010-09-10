/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.module.admincentral.tree.container;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.ContentWrapper;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admincentral.tree.TreeDefinition;

import java.util.ArrayList;
import java.util.Collection;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;


/**
 * NodeItem.
 * @author daniellipp
 * @version $Id$
 */
public class NodeItem extends ContentWrapper implements Item {

    private static final long serialVersionUID = -6540682899828148456L;

    private static Logger log = LoggerFactory.getLogger(NodeItem.class);

    private TreeDefinition definition;

    Content node;

    String handle;

    public NodeItem(Content node, TreeDefinition definition)
            throws RepositoryException {
        super(node);
        this.handle = node.getHandle();
        this.node = node;
        this.definition = definition;
    }

    public boolean addItemProperty(Object id, Property property)
            throws UnsupportedOperationException {
        assertIdIsString(id);
        try {
            this.setNodeData((String) id, property.getValue());
        }
        catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    protected void assertIdIsString(Object id) {
        if (!(id instanceof String)) {
            throw new UnsupportedOperationException(
                    "JCR requires all property id's to be String");
        }
    }

    /**
     *
     * @return absolute path as vaadin item id
     * @throws RepositoryException
     */
    public String getItemId() throws RepositoryException {
        return getHandle();
    }

    public Property getItemProperty(Object id) {
        assertIdIsString(id);
        return new NodeProperty(this, (String) id, definition);
    }

    public Collection< ? > getItemPropertyIds() {
        ArrayList<String> idlist = new ArrayList<String>();
        for (NodeData nd : getNodeDataCollection()) {
            idlist.add(nd.getName());
        }
        return idlist;
    }

    public boolean removeItemProperty(Object id)
            throws UnsupportedOperationException {
        assertIdIsString(id);
        try {
            getNodeData((String) id).delete();
        }
        catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public synchronized Content getWrappedContent() {
        try {
            if( node == null || !node.getJCRNode().getSession().isLive()){
                node = getHierarchyManager().getContent(getHandle());
            }
        }
        catch (RepositoryException e) {
            log.error("can't reinitialize node " + getHandle(), e);
        }
        return node;
    }

    public HierarchyManager getHierarchyManager() {
        return MgnlContext.getSystemContext().getHierarchyManager(definition.getRepository());
    }
}
