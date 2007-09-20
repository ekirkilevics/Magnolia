/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.taglibs;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.i18n.I18nContentSupportFactory;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Marcel Salathe
 * @author Fabrizio Giustina
 * @version $Revision $ ($Author $)
 */
public class IfEmpty extends BaseConditionalContentTag {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(IfEmpty.class);

    private String nodeDataName = StringUtils.EMPTY;

    /**
     * @deprecated
     */
    public void setAtomName(String name) {
        this.setNodeDataName(name);
    }

    /**
     * @param name , antom name to evaluate
     */
    public void setNodeDataName(String name) {
        this.nodeDataName = name;
    }

    /**
     * @deprecated
     */
    public void setContainerName(String name) {
        this.setContentNodeName(name);
    }

    /**
     * @param name , contentNode collection name
     * @deprecated
     */
    public void setContainerListName(String name) {
        this.setContentNodeCollectionName(name);
    }

    /**
     * @see javax.servlet.jsp.jstl.core.ConditionalTagSupport#condition()
     */
    protected boolean condition() {
        Content node = this.getFirstMatchingNode();

        if (node == null) {
            return true;
        }

        // checking for node existence
        if(StringUtils.isEmpty(this.nodeDataName) && !node.hasChildren()){
            return true;
        }

        NodeData nodeData = I18nContentSupportFactory.getI18nSupport().getNodeData(node, this.nodeDataName);

        if ((nodeData == null) || !nodeData.isExist() || StringUtils.isEmpty(nodeData.getString())) {
            return true;
        }

        return false;
    }

}
