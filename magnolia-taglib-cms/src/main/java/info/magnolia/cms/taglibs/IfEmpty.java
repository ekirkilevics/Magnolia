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
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.jstl.core.ConditionalTagSupport;


/**
 * @author Marcel Salathe
 * @author Fabrizio Giustina
 * @version $Revision $ ($Author $)
 */
public class IfEmpty extends ConditionalTagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(IfEmpty.class);

    private String nodeDataName = StringUtils.EMPTY;

    private String contentNodeName = StringUtils.EMPTY;

    private String contentNodeCollectionName = StringUtils.EMPTY;

    private transient Content contentNodeCollection;

    private transient Content contentNode;

    private transient NodeData nodeData;

    private boolean actpage;

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
     * @param contentNodeName , contentNodeName to check
     */
    public void setContentNodeName(String contentNodeName) {
        this.contentNodeName = contentNodeName;
    }

    /**
     * @param name , contentNode collection name
     * @deprecated
     */
    public void setContainerListName(String name) {
        this.setContentNodeCollectionName(name);
    }

    /**
     * @param name contentNodeCollectionName to check
     */
    public void setContentNodeCollectionName(String name) {
        this.contentNodeCollectionName = name;
    }

    /**
     * Set the actpage.
     *
     * @param set
     */
    public void setActpage(boolean set) {
        this.actpage = set;
    }

    /**
     * @see javax.servlet.jsp.jstl.core.ConditionalTagSupport#condition()
     */
    protected boolean condition() {
        HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
        // in the case where a contentNodeCollectionName is provided
        if (StringUtils.isNotEmpty(this.contentNodeCollectionName)) {
            try {
                this.contentNodeCollection = Resource.getCurrentActivePage(req).getContent(
                        this.contentNodeCollectionName);
            }
            catch (RepositoryException e) {
                if (log.isDebugEnabled())
                    log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
            }
            if (this.contentNodeCollection == null) {
                return true;
            }
            if (!this.contentNodeCollection.hasChildren()) {
                return true;
            }
            return false;
        }
        // if only contentNodeName is provided, it checks if this contentNode exists
        if (StringUtils.isNotEmpty(this.contentNodeName) && StringUtils.isEmpty(this.nodeDataName)) {
            try {
                this.contentNode = Resource.getCurrentActivePage(req).getContent(this.contentNodeName);
            }
            catch (RepositoryException re) {
                log.error(re.getMessage());
            }
            if (this.contentNode == null) {
                // contentNode doesn't exist, evaluate body
                return true;
            }
        }
        // if both contentNodeName and nodeDataName are set, it checks if that nodeData of that contentNode exitsts
        // and is not empty
        else if (StringUtils.isNotEmpty(this.contentNodeName) && StringUtils.isNotEmpty(this.nodeDataName)) {
            try {
                this.contentNode = Resource.getCurrentActivePage(req).getContent(this.contentNodeName);
            }
            catch (RepositoryException re) {
                if (log.isDebugEnabled())
                    log.debug("Repository exception while reading " + this.contentNodeName + ": " + re.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (this.contentNode == null) {
                return true;
            }
            if (this.contentNode != null) {

                this.nodeData = this.contentNode.getNodeData(this.nodeDataName);

                if ((this.nodeData == null)
                        || !this.nodeData.isExist()
                        || StringUtils.isEmpty(this.nodeData.getString())) {
                    return true;
                }
            }
        }
        // if only nodeDataName is provided, it checks if that nodeData of the current contentNode exists and is not
        // empty
        else if (StringUtils.isEmpty(this.contentNodeName) && StringUtils.isNotEmpty(this.nodeDataName)) {
            if (this.actpage) {
                this.contentNode = Resource.getCurrentActivePage((HttpServletRequest) pageContext.getRequest());
            } else {
                this.contentNode = Resource.getLocalContentNode((HttpServletRequest) pageContext.getRequest());
                if (this.contentNode == null) {
                    this.contentNode = Resource.getGlobalContentNode((HttpServletRequest) pageContext.getRequest());
                }
            }
            if (this.contentNode == null) {
                return true;
            }
            if (this.contentNode != null) {

                this.nodeData = this.contentNode.getNodeData(this.nodeDataName);

                if ((this.nodeData == null)
                        || !this.nodeData.isExist()
                        || StringUtils.isEmpty(this.nodeData.getString())) {
                    return true;
                }
            }
        }
        // if both contentNodeName and nodeDataName are not provided, it checks if the current contentNode exists
        else {
            this.contentNode = Resource.getLocalContentNode((HttpServletRequest) pageContext.getRequest());
            if (this.contentNode == null) {
                this.contentNode = Resource.getGlobalContentNode((HttpServletRequest) pageContext.getRequest());
            }
            if (this.contentNode == null) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see javax.servlet.jsp.tagext.BodyTagSupport#doEndTag()
     */
    public int doEndTag() throws JspException {
        this.contentNodeCollection = null;
        this.contentNode = null;
        this.nodeData = null;
        return super.doEndTag();
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {
        this.nodeDataName = StringUtils.EMPTY;
        this.contentNodeName = StringUtils.EMPTY;
        this.contentNodeCollectionName = StringUtils.EMPTY;
        this.actpage = false;
    }

}
