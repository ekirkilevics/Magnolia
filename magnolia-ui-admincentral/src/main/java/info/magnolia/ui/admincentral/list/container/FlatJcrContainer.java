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
package info.magnolia.ui.admincentral.list.container;


import info.magnolia.ui.admincentral.container.ContainerItemId;
import info.magnolia.ui.admincentral.container.JcrContainer;
import info.magnolia.ui.admincentral.container.JcrContainerSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.util.TraversingItemVisitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A flat implementation of {@link JcrContainer} where relationships are not taken into account.
 * @author fgrilli
 *
 */
public class FlatJcrContainer extends JcrContainer {

    private static final Logger log = LoggerFactory.getLogger(FlatJcrContainer.class);
    private int level = 0;
    /**
     * Constructor for {@link FlatJcrContainer}.
     * @param maxLevel the 0-based level up to which the hierarchy should be traversed (if it's -1, the hierarchy will be traversed until there are no more children of the current item).
     */
    public FlatJcrContainer(JcrContainerSource jcrContainerSource, int maxLevel) {
        super(jcrContainerSource);
        this.level = maxLevel;
    }


    @Override
    protected Collection<ContainerItemId> createContainerIds(Collection<Item> children) throws RepositoryException {
        MagnoliaContentTraversingItemVisitor visitor = new MagnoliaContentTraversingItemVisitor(false, level);
        for (javax.jcr.Item child : children) {
            child.accept(visitor);
        }
        return visitor.getIds();
    }

    public int getLevel() {
        return level;
    }

    /**
     * Creates a list of {@link ContainerItemId} for all <code>magnolia:content</code> node types in a given item hierarchy.
     * The list of item ids can be retrieved with {@link MagnoliaContentTraversingItemVisitor#getIds()}.
     */
    private class MagnoliaContentTraversingItemVisitor extends TraversingItemVisitor {
        private ArrayList<ContainerItemId> ids = new ArrayList<ContainerItemId>();

        public MagnoliaContentTraversingItemVisitor(boolean breadthFirst, int level){
            super(breadthFirst, level);
        }
        @Override
        protected void entering(javax.jcr.Property property, int level) throws RepositoryException {
            //do nothing
        }

        @Override
        protected void entering(Node node, int level) throws RepositoryException {
           //do nothing
        }

        @Override
        protected void leaving(javax.jcr.Property property, int level) throws RepositoryException {
            if(this.maxLevel > -1 && !property.getName().startsWith("jcr:") && !property.getName().startsWith("mgnl:")){
                log.debug("adding property {}", property.getName());
                ids.add(createContainerId(property));
            }
        }

        @Override
        protected void leaving(Node node, int level) throws RepositoryException {
            if(node.getPrimaryNodeType().isNodeType("mgnl:content") || (this.maxLevel > -1 && node.getPrimaryNodeType().isNodeType("mgnl:contentNode")) ){
                log.debug("adding node {}", node.getName());
                ids.add(createContainerId(node));
            }
        }

        public List<ContainerItemId> getIds(){
            return ids;
        }
    }
}