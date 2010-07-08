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
package info.magnolia.module.rest.tree.config;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.gui.control.Tree;

import javax.jcr.RepositoryException;
import java.util.Map;

/**
 * Represents the first column, the one with the tree graphics.
 * <p/>
 * The return value is the text to use for the node (and the icon?)
 */
public class JsonTreeColumnLabel extends JsonTreeColumn {

    private Map<String, String> icons;

    public Map<String, String> getIcons() {
        return icons;
    }

    public void setIcons(Map<String, String> icons) {
        this.icons = icons;
    }

    @Override
    public String getType() {
        return "label";
    }

    @Override
    public Object getValue(Content storageNode) throws RepositoryException {

        LabelColumnValue value = new LabelColumnValue();
        value.setLabel(storageNode.getName());
        String icon = getIcon(storageNode.getNodeTypeName());
        value.setIcon(icon);

        return value;
    }

    @Override
    public Object getValue(Content storageNode, NodeData nodeData) throws RepositoryException {

        LabelColumnValue value = new LabelColumnValue();
        value.setLabel(storageNode.getName());
        String icon = getIcon(Tree.ITEM_TYPE_NODEDATA);
        value.setIcon(icon);

        return value;
    }

    private String getIcon(String typeName) {

        // Temp fix since : isnt allowed as a node name, the list of icons is read using content2bean

        String icon = icons.get(typeName.replace(':', '-'));
        if (icon == null)
            icon = Tree.DEFAULT_ICON;
        return icon;
    }

    public static class LabelColumnValue {

        private String label;
        private String icon;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }
    }
}
