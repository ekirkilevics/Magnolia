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
import info.magnolia.cms.util.Resource;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class BaseContentTag extends TagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    protected Logger log = LoggerFactory.getLogger(getClass());

    protected String nodeDataName;

    protected String contentNodeName;

    protected String contentNodeCollectionName;

    protected boolean inherit;

    /**
     * Set the node data name, e.g. "mainText".
     * @param name
     */
    public void setNodeDataName(String name) {
        this.nodeDataName = name;
    }

    /**
     * Set the content node name name, e.g. "01".
     * @param name
     */
    public void setContentNodeName(String name) {
        this.contentNodeName = name;
    }

    /**
     * Set the content node collection name name, e.g. "mainColumnParagraphs".
     * @param name
     */
    public void setContentNodeCollectionName(String name) {
        this.contentNodeCollectionName = name;
    }

    /**
     * Setter for <code>inherit</code>.
     * @param inherit <code>true</code> to inherit from parent pages if value is not set.
     */
    public void setInherit(boolean inherit) {
        this.inherit = inherit;
    }

    /**
     * Get the first matching node containing a NodeData named <code>nodeDataName</code>
     * @return the active node, or the first matching one if nodedata is null and inherit is set.
     * @deprecated Use {@link #getFirstMatchingNode()} instead
     */
    protected Content getFirtMatchingNode() {
        return getFirstMatchingNode();
    }

    /**
     * Get the first matching node containing a NodeData named <code>nodeDataName</code>
     * @return the active node, or the first matching one if nodedata is null and inherit is set.
     */
    protected Content getFirstMatchingNode() {
        Content currentPage = Resource.getCurrentActivePage();
        Content contentNode = resolveNode(currentPage);
        if (contentNode == null) {
            return null;
        }

        if(StringUtils.isNotEmpty(this.nodeDataName)){
            NodeData nodeData = contentNode.getNodeData(this.nodeDataName);

            try {
                while (inherit && currentPage.getLevel() > 0 && !nodeData.isExist()) {
                    currentPage = currentPage.getParent();
                    contentNode = resolveNode(currentPage);
                    nodeData = contentNode.getNodeData(this.nodeDataName);
                }
            }
            catch (RepositoryException e) {
                log.error(e.getMessage(), e);
            }
        }

        return contentNode;
    }

    protected Content resolveNode(Content currentPage) {
        Content currentParagraph = Resource.getLocalContentNode();

        if (StringUtils.isNotEmpty(contentNodeName)) {
            // contentNodeName is defined
            try {
                if (StringUtils.isEmpty(contentNodeCollectionName)) {
                    // e.g. <cms:out nodeDataName="title" contentNodeName="footer"/>
                    return currentPage.getContent(contentNodeName);
                }

                // e.g. <cms:out nodeDataName="title" contentNodeName="01" contentNodeCollectionName="mainPars"/>
                // e.g. <cms:out nodeDataName="title" contentNodeName="footer" contentNodeCollectionName=""/>
                return currentPage.getContent(contentNodeCollectionName).getContent(contentNodeName);

            }
            catch (RepositoryException re) {
                if (log.isDebugEnabled()) {
                    log.debug(re.getMessage());
                }
            }
        }
        else {
            if (currentParagraph == null) {
                // outside collection iterator
                if (StringUtils.isEmpty(contentNodeCollectionName)) {
                    // e.g. <cms:out nodeDataName="title"/>
                    // e.g. <cms:out nodeDataName="title" contentNodeName=""/>
                    // e.g. <cms:out nodeDataName="title" contentNodeCollectionName=""/>
                    return currentPage;
                }
                // ERROR: no content node assignable because contentNodeName is empty
                // e.g. <cms:out nodeDataName="title" contentNodeCollectionName="mainPars"/>
            }
            else {
                // inside collection iterator
                if (contentNodeName == null && contentNodeCollectionName == null) {
                    // e.g. <cms:out nodeDataName="title"/>
                    return currentParagraph;
                }
                else if ((contentNodeName != null && StringUtils.isEmpty(contentNodeName))
                    || (contentNodeCollectionName != null && StringUtils.isEmpty(contentNodeCollectionName))) {
                    // empty collection name -> use actpage
                    // e.g. <cms:out nodeDataName="title" contentNodeCollectionName=""/>
                    return currentPage;
                }
            }
        }
        return null;
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#release()
     */
    public void release() {
        super.release();

        this.nodeDataName = null;
        this.contentNodeName = null;
        this.contentNodeCollectionName = null;
        this.inherit = false;
    }

}
