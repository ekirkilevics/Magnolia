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
package info.magnolia.ui.model;

import info.magnolia.jcr.util.JCRUtil;
import info.magnolia.ui.model.tree.definition.TreeDefinition;
import info.magnolia.ui.model.tree.registry.TreeRegistry;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;


/**
 * The UI model provides all the definition for the trees, dialogs, commands and so on.
 */
// TODO drop this class, depend on the registries
public class UIModel {

    private TreeRegistry treeRegistry;

    public UIModel(TreeRegistry treeRegistry) {
        this.treeRegistry = treeRegistry;
    }

    public TreeDefinition getTreeDefinition(String treeName) {
        try {
            return treeRegistry.getTree(treeName);
        } catch (RepositoryException e) {
            throw new IllegalStateException(e);
        }
    }

    // TODO drop or move method
    public Item getItem(String treeName, String path) throws RepositoryException {
        return getItem(getTreeDefinition(treeName), path);
    }

    // TODO drop or move method
    public Item getItem(TreeDefinition treeDefinition, String path) throws RepositoryException {
        String base = treeDefinition.getPath();
        if (!base.equals("/"))
            path = base + path;
        return JCRUtil.getSession(treeDefinition.getRepository()).getItem(path);
    }

    // TODO drop or move method
    public String getPathInTree(String treeName, Item item) throws RepositoryException {
        TreeDefinition treeDefinition = getTreeDefinition(treeName);
        String base = treeDefinition.getPath();
        if (base.equals("/"))
            return item.getPath();
        else
            return StringUtils.substringAfter(item.getPath(), base);
    }
}
