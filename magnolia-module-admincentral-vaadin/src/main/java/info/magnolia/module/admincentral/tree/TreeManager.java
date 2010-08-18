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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.cms.core.Content;
import info.magnolia.objectfactory.Components;

/**
 * Maintains a registry of configured tree definitions.
 */
public class TreeManager extends ObservedManager {

    @Override
    protected void onRegister(Content node) {
    }

    @Override
    protected void onClear() {
    }

    public TreeDefinition getTree(String name) {

        if (name.equals("website")) {
            TreeDefinition tree = new TreeDefinition();
            tree.setName("website");
            tree.setFlatMode(false);
            tree.setRepository(ContentRepository.WEBSITE);
            tree.setPath("/");
            tree.setIncludeNodeData(false);

            LabelColumn column1 = new LabelColumn();
            column1.setLabel("Page");
            tree.addColumn(column1);

            NodeDataColumn column2 = new NodeDataColumn();
            column2.setLabel("Title");
            column2.setNodeDataName("title");
            tree.addColumn(column2);

            IconColumn column5 = new IconColumn();
            column5.setLabel("Status");
            tree.addColumn(column5);

            TemplateColumn column4 = new TemplateColumn();
            column4.setLabel("Template");
            tree.addColumn(column4);

            MetaDataColumn column3 = new MetaDataColumn();
            column3.setLabel("Mod. Date");
            tree.addColumn(column3);

            return tree;
        }

        if (name.equals("config")) {
            TreeDefinition tree = new TreeDefinition();
            tree.setName("config");
            tree.setFlatMode(false);
            tree.setRepository(ContentRepository.CONFIG);
            tree.setPath("/");
            tree.setIncludeNodeData(true);

            LabelColumn column1 = new LabelColumn();
            column1.setLabel("");
            tree.addColumn(column1);

            NodeDataValueColumn column2 = new NodeDataValueColumn();
            column2.setLabel("Value");
            tree.addColumn(column2);

            NodeDataTypeColumn column4 = new NodeDataTypeColumn();
            column4.setLabel("Type");
            tree.addColumn(column4);

            IconColumn column5 = new IconColumn();
            column5.setLabel("Status");
            tree.addColumn(column5);

            MetaDataColumn column3 = new MetaDataColumn();
            column3.setLabel("Mod. Date");
            tree.addColumn(column3);

            return tree;
        }

        return null;
    }

    public static TreeManager getInstance() {
        return Components.getSingleton(TreeManager.class);
    }
}
