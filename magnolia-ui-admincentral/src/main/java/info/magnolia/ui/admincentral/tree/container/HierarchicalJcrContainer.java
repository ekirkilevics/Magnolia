/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.admincentral.tree.container;

import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.ui.admincentral.container.ContainerItem;
import info.magnolia.ui.admincentral.container.ContainerItemId;
import info.magnolia.ui.admincentral.container.JcrContainer;
import info.magnolia.ui.admincentral.container.JcrContainerSource;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container;


/**
 * Hierarchical implementation of {@link JcrContainer}.
 * @author fgrilli
 *
 */
public class HierarchicalJcrContainer extends JcrContainer implements Container.Hierarchical {

    private static final Logger log = LoggerFactory.getLogger(HierarchicalJcrContainer.class);

    public HierarchicalJcrContainer(JcrContainerSource jcrContainerSource, WorkbenchDefinition workbenchDefinition) {
        super(jcrContainerSource, workbenchDefinition);
    }

    @Override
    public Collection<ContainerItemId> getChildren(Object itemId) {
        try {
            long start = System.currentTimeMillis();
            Collection<Item> children = getJcrContainerSource().getChildren(getJcrItem((ContainerItemId) itemId));
            log.debug("Fetched {} children in {}ms", children.size(), System.currentTimeMillis() - start);
            return createContainerIds(children);
        }
        catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    @Override
    public ContainerItemId getParent(Object itemId) {
        try {
            Item item = getJcrItem((ContainerItemId) itemId);
            if (item instanceof Property) {
                return createContainerId(item.getParent());
            }
            Node node = (Node) item;
            return node.getDepth() > 0 ? createContainerId(node.getParent()) : null;
        }
        catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    @Override
    public Collection<ContainerItemId> rootItemIds() {
        try {
            return createContainerIds(getJcrContainerSource().getRootItemIds());
        }
        catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    @Override
    public boolean setParent(Object itemId, Object newParentId) throws UnsupportedOperationException {
        fireItemSetChange();
        return true;
    }

    @Override
    public boolean areChildrenAllowed(Object itemId) {
        return ((ContainerItemId) itemId).isNode();
    }

    @Override
    public boolean setChildrenAllowed(Object itemId, boolean areChildrenAllowed) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRoot(Object itemId) {
        try {
            return getJcrContainerSource().isRoot(getJcrItem((ContainerItemId) itemId));
        }
        catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    @Override
    public boolean hasChildren(Object itemId) {
        try {
            return getJcrContainerSource().hasChildren(getJcrItem((ContainerItemId) itemId));
        }
        catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    @Override
    public void update(NodeIterator iterator) throws RepositoryException, UnsupportedOperationException {
        throw new UnsupportedOperationException(getClass().getName() + " does not support this operation.");

    }

    protected Collection<ContainerItemId> createContainerIds(Collection<Item> children) throws RepositoryException {
        ArrayList<ContainerItemId> ids = new ArrayList<ContainerItemId>();
        for (javax.jcr.Item child : children) {
            ids.add(createContainerId(child));
        }
        return ids;
    }

    @Override
    public List<String> getSortableContainerPropertyIds() {
        //at present tree view is not sortable
        return Collections.emptyList();
    }

    @Override
    public com.vaadin.data.Item getItem(Object itemId) {
        return new ContainerItem((ContainerItemId) itemId, this);
    }

    @Override
    public boolean containsId(Object itemId) {
        try {
            getJcrItem((ContainerItemId) itemId);
            return true;
        }
        catch (RepositoryException e) {
            return false;
        }
    }
}
