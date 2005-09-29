/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.gui.control;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.MetaDataUtil;
import info.magnolia.cms.util.NodeDataUtil;

import java.util.Calendar;


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
            html = new MetaDataUtil(content).getPropertyValueString(treeColumn.getName(), treeColumn.getDateFormat());
        }
        else if (treeColumn.getIsLabel()) {
            html = content.getName();
        }
        else if (treeColumn.getIsIcons()) {
            html = getHtmlIcons(treeColumn, content);
        }
        else {
            NodeData data = content.getNodeData(treeColumn.getName());
            html = new NodeDataUtil(data).getValueString(treeColumn.getDateFormat());
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
            MetaData activationMetaData = content.getMetaData(MetaData.ACTIVATION_INFO);
            MetaData generalMetaData = content.getMetaData();
            boolean isActivated = activationMetaData.getIsActivated();
            Calendar actionDate = activationMetaData.getLastActionDate();
            Calendar lastModifiedDate = generalMetaData.getModificationDate();
            String imgSrc;
            if (isActivated) {
                if (lastModifiedDate != null && lastModifiedDate.after(actionDate)) {
                    // node has been modified after last activation
                    imgSrc = Tree.ICONDOCROOT + "indicator_yellow.gif"; //$NON-NLS-1$
                }
                else {
                    // activated and not modified ever since
                    imgSrc = Tree.ICONDOCROOT + "indicator_green.gif"; //$NON-NLS-1$
                }
            }
            else {
                // never activated or deactivated
                imgSrc = Tree.ICONDOCROOT + "indicator_red.gif"; //$NON-NLS-1$
            }
            html.append("<img src=\"" + treeColumn.getRequest().getContextPath() + imgSrc + "\" />"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (treeColumn.getIconsPermission()) {
            if (!content.isGranted(info.magnolia.cms.security.Permission.WRITE)) {
                html.append("<img src=\"" //$NON-NLS-1$
                    + treeColumn.getRequest().getContextPath() + Tree.ICONDOCROOT + "pen_blue_canceled.gif\" />"); //$NON-NLS-1$
            }
        }
        return html.toString();
    }

}
