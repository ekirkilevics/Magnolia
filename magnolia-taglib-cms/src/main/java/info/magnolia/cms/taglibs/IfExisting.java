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
import info.magnolia.cms.i18n.DefaultI18NSupport;
import info.magnolia.cms.i18n.I18NSupportFactory;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Michael Aemisegger
 * @version $Revision $ ($Author $)
 */
public class IfExisting extends BaseConditionalContentTag {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(IfExisting.class);

    private String nodeDataName = StringUtils.EMPTY;

    /**
     * @param name , antom name to evaluate
     */
    public void setNodeDataName(String name) {
        this.nodeDataName = name;
    }

    /**
     * @see javax.servlet.jsp.jstl.core.ConditionalTagSupport#condition()
     */
    protected boolean condition() {
        Content node = this.getFirstMatchingNode();

        // if the tag is checking for a nodes existance
        if (StringUtils.isEmpty(this.nodeDataName)) {
            return node != null;
        }
        else{
            NodeData nodeData = I18NSupportFactory.getI18nSupport().getNodeData(node, this.nodeDataName);
            return (nodeData != null) && nodeData.isExist();
        }
    }

}
