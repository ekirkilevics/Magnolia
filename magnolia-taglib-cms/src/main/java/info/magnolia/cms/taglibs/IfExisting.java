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
import info.magnolia.cms.util.Resource;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.jstl.core.ConditionalTagSupport;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Michael Aemisegger
 * @version $Revision $ ($Author $)
 */
public class IfExisting extends ConditionalTagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(IfExisting.class);

    private String nodeDataName = StringUtils.EMPTY;

    private String contentNodeName = StringUtils.EMPTY;

    private String contentNodeCollectionName = StringUtils.EMPTY;

    private transient Content contentNode;

    private boolean actpage;

    /**
     * @param name , antom name to evaluate
     */
    public void setNodeDataName(String name) {
        this.nodeDataName = name;
    }

    /**
     * @param contentNodeName , contentNodeName to check
     */
    public void setContentNodeName(String contentNodeName) {
        this.contentNodeName = contentNodeName;
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
                if (Resource.getCurrentActivePage(req).hasContent(this.contentNodeCollectionName)) {
                    return true;
                }
            }
            catch (RepositoryException re) {
                return false;
            }
            return false;
        }
        // if only contentNodeName is provided, it checks if this contentNode exists
        if (StringUtils.isNotEmpty(this.contentNodeName) && StringUtils.isEmpty(this.nodeDataName)) {
            try {
                if (Resource.getCurrentActivePage(req).hasContent(this.contentNodeName)) {
                    return true;
                }
            }
            catch (RepositoryException re) {
                return false;
            }
            return false;
        }
        // if both contentNodeName and nodeDataName are set, it checks if that nodeData of that contentNode exitsts
        else if (StringUtils.isNotEmpty(this.contentNodeName) && StringUtils.isNotEmpty(this.nodeDataName)) {
            try {
                this.contentNode = Resource.getCurrentActivePage(req).getContent(this.contentNodeName);
            }
            catch (RepositoryException re) {
                if (log.isDebugEnabled()) {
                    log.debug("Repository exception while reading " + this.contentNodeName + ": " + re.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            if (this.contentNode == null) {
                return false;
            }
            if (this.contentNode != null) {

                try {
                    if (this.contentNode.hasNodeData(this.nodeDataName)) {
                        return true;
                    }
                }
                catch (RepositoryException e) {
                    return false;
                }
            }
        }
        // if only nodeDataName is provided, it checks if that nodeData of the current contentNode exists
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
                return false;
            }
            if (this.contentNode != null) {

                try {
                    if (this.contentNode.hasNodeData(this.nodeDataName)) {
                        return true;
                    }
                }
                catch (RepositoryException e) {
                    return false;
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
                return false;
            }
        }
        return false;
    }

    /**
     * @see javax.servlet.jsp.tagext.BodyTagSupport#doEndTag()
     */
    public int doEndTag() throws JspException {
        this.contentNode = null;
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
