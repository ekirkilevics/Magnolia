/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.module.admincentral.tree.action;

import info.magnolia.module.admincentral.views.AbstractTreeTableView;

import javax.jcr.Node;
import javax.jcr.RepositoryException;


/**
 * Tree action for adding a page to the website repository.
 * 
 * TODO this class is a mock up that only adds an item to the tree table. It will need to be changed
 * into accessing the JCR.
 */
public class AddAction extends TreeAction {

    private static final long serialVersionUID = 7745378363506148188L;

    @Override
    protected void handleAction(AbstractTreeTableView treeTableView, Node node) throws RepositoryException {
        // TODO_ shouldn't this be implemented calling the JcrContainers methodes (if so - how to
        // access the JcrContainer?)?
        // add to JCR
        Node newChild = node.addNode(node.getPath() + "/untitled2");
        // force reading and therefore realizing there's a new Node - eventually better implemented
        // by using treeTable.addItem(String)
        treeTableView.getTreeTable().getItem(newChild.getPath());
    }
}
