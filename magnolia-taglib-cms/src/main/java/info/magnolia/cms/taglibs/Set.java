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
import javax.servlet.jsp.tagext.TagSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles
 * @version $Revision$ ($Author$)
 */
public class Set extends TagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(Set.class);

    private transient Content contentNode;

    private String contentNodeName;

    /**
     * @param contentNode to be set
     */
    public void setContentNode(Content contentNode) {
        this.contentNode = contentNode;
    }

    /**
     * @param name , contentNode name to be set
     */
    public void setContentNodeName(String name) {
        this.contentNodeName = name;
    }

    /**
     * @deprecated
     */
    public void setContainer(Content contentNode) {
        this.setContentNode(contentNode);
    }

    /**
     * @deprecated
     */
    public void setContainerName(String name) {
        this.setContentNodeName(name);
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() {
        HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
        Resource.removeGlobalContentNode(req);
        if (this.contentNodeName == null) {
            Resource.setGlobalContentNode(req, this.contentNode);
        }
        else {
            try {
                this.contentNode = Resource.getCurrentActivePage(req).getContent(this.contentNodeName);
                Resource.setGlobalContentNode(req, this.contentNode);
            }
            catch (RepositoryException re) {
                log.error(re.getMessage());
            }
        }
        return SKIP_BODY;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag() {
        return EVAL_PAGE;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {
        super.release();
        this.contentNode = null;
        this.contentNodeName = null;
    }
}
