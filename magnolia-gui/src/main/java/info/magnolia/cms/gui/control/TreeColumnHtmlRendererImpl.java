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
package info.magnolia.cms.gui.control;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.MetaDataUtil;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.context.MgnlContext;


/**
 * @author joshu
 */
public class TreeColumnHtmlRendererImpl implements TreeColumnHtmlRenderer {

    /**
     * @see info.magnolia.cms.gui.control.TreeColumnHtmlRenderer#renderHtml(TreeColumn, Content)
     */
    public String renderHtml(TreeColumn treeColumn, Content content) {
        String html;
        if (treeColumn.getIsMeta()) {
            html = MetaDataUtil.getPropertyValueString(content, treeColumn.getName(), treeColumn.getDateFormat());
        }
        else if (treeColumn.getIsLabel()) {
            html = content.getName();
        }
        else if (treeColumn.getIsIcons()) {
            html = getHtmlIcons(treeColumn, content);
        }
        else {
            NodeData data = content.getNodeData(treeColumn.getName());
            html = NodeDataUtil.getValueString(data, treeColumn.getDateFormat());
        }
        // @todo (value is not shown after saving ...)
        if (treeColumn.getKeyValue().size() != 0) {
            String value = (String) treeColumn.getKeyValue().get(html);
            if (value != null) {
                html = value;
            }
        }
        return html;

    }

    /**
     * Returns the html for the activation info flag and the permission flag.
     * @param treeColumn TreeColumn instance
     * @param content current page
     * @return html snippet for the activation info flag and the permission flag
     */
    private String getHtmlIcons(TreeColumn treeColumn, Content content) {
        StringBuffer html = new StringBuffer();
        if (treeColumn.getIconsActivation()) {
            String imgSrc = Tree.ICONDOCROOT + MetaDataUtil.getActivationStatusIcon(content);
            html.append("<img src=\"").append(MgnlContext.getContextPath()).append(imgSrc).append("\" />"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (treeColumn.getIconsPermission()) {
            if (!content.isGranted(info.magnolia.cms.security.Permission.WRITE)) {
                html.append("<img src=\"") //$NON-NLS-1$
                    .append(treeColumn.getRequest().getContextPath())
                    .append(Tree.ICONDOCROOT)
                    .append("pen_blue_canceled.gif\" />"); //$NON-NLS-1$
            }
        }
        return html.toString();
    }

}
