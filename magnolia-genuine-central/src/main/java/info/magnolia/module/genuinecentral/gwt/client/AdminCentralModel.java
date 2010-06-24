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
package info.magnolia.module.genuinecentral.gwt.client;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeModel;

public class AdminCentralModel extends BaseTreeModel {

    protected List<TabEntry> entries = new ArrayList<TabEntry>();

    public AdminCentralModel() {

        add(createTab("Website", "website", "/"));
        add(createTab("Config", "config", "/"));
        loadEntries(this);
    }

    private Tab createTab(String name, String treeName, String treePath) {
        Tab treeGrids = new Tab(name);
        MgnlTreeGrid tree = new MgnlTreeGrid();
        tree.setTree(treeName);
        tree.setPath(treePath);
        tree.setHeading(name);
        treeGrids.add(name, tree, null);
        return treeGrids;
    }

    public TabEntry findEntry(String name) {
        if (get(name) != null) {
            return (TabEntry) get(name);
        }
        for (TabEntry entry : getEntries()) {
            if (name.equals(entry.getId())) {
                return entry;
            }
        }
        return null;
    }

    public List<TabEntry> getEntries() {
        return entries;
    }

    private void loadEntries(TreeModel model) {
        for (ModelData child : model.getChildren()) {
            if (child instanceof TabEntry) {
                entries.add((TabEntry) child);
            } else if (child instanceof Tab) {
                loadEntries((Tab) child);
            }
        }
    }
}