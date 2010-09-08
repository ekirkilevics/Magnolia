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
package info.magnolia.module.admincentral.tree;

import info.magnolia.objectfactory.Components;

import javax.jcr.RepositoryException;
import java.util.HashMap;
import java.util.Map;

/**
 * Maintains a registry of configured tree providers by name.
 */
public class TreeRegistry {

    private final Map<String, TreeProvider> providers = new HashMap<String, TreeProvider>();

    public void registerTree(String treeName, TreeProvider provider) {
        synchronized (providers) {
            if (providers.containsKey(treeName))
                throw new IllegalStateException("Tree already registered for name [" + treeName + "]");
            providers.put(treeName, provider);
        }
    }

    public void unregisterTree(String treeName) {
        synchronized (providers) {
            providers.remove(treeName);
        }
    }

    public TreeDefinition getTree(String name) throws RepositoryException {

        TreeProvider treeProvider;
        synchronized (providers) {
            treeProvider = providers.get(name);
        }
        if (treeProvider == null) {
            return null;
        }
        return treeProvider.getTreeDefinition();
    }

    public static TreeRegistry getInstance() {
        return Components.getSingleton(TreeRegistry.class);
    }
}
