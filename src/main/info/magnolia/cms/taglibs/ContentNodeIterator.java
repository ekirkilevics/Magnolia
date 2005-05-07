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
package info.magnolia.cms.taglibs;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ContentHandler;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.Resource;

import java.util.Collection;
import java.util.Iterator;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * @version $Revision $ ($Author $)
 */
public class ContentNodeIterator extends TagSupport {

    public static final String CONTENT_NODE_COLLECTION_NAME = "contentNodeCollectionName";

    protected static final String CURRENT_INDEX = "currentIndex";

    protected static final String SIZE = "size";

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(ContentNodeIterator.class);

    private String contentNodeCollectionName;

    private int beginIndex;

    private int endIndex;

    private int step = 1;

    private int size;

    private int currentIndex;

    private Iterator contentNodeIterator;

    /**
     * @param name container list name on which this tag will iterate
     * @deprecated
     */
    public void setContainerListName(String name) {
        this.setContentNodeCollectionName(name);
    }

    /**
     * @param name content node name on which this tag will iterate
     */
    public void setContentNodeCollectionName(String name) {
        this.contentNodeCollectionName = name;
    }

    /**
     * @param index index to begin with
     */
    public void setBegin(String index) {
        this.beginIndex = (new Integer(index)).intValue();
    }

    /**
     * @param index index to end at
     */
    public void setEnd(String index) {
        this.endIndex = (new Integer(index)).intValue();
    }

    /**
     * @param step to jump to
     */
    public void setStep(String step) {
        this.step = (new Integer(step)).intValue();
    }

    /**
     * @return end index
     */
    private int getEnd() {
        if (this.endIndex == 0) {
            return this.size;
        }
        return this.endIndex;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        Content page = Resource.getCurrentActivePage(request);
        try {
            Collection children = page.getContent(this.contentNodeCollectionName).getChildren(
                ItemType.CONTENTNODE,
                ContentHandler.SORT_BY_SEQUENCE);
            this.size = children.size();
            if (this.size == 0) {
                return SKIP_BODY;
            }
            pageContext.setAttribute(ContentNodeIterator.SIZE, new Integer(this.getEnd()), PageContext.REQUEST_SCOPE);
            pageContext.setAttribute(
                ContentNodeIterator.CURRENT_INDEX,
                new Integer(this.currentIndex),
                PageContext.REQUEST_SCOPE);
            pageContext.setAttribute(
                ContentNodeIterator.CONTENT_NODE_COLLECTION_NAME,
                this.contentNodeCollectionName,
                PageContext.REQUEST_SCOPE);
            this.contentNodeIterator = children.iterator();
            Resource.setLocalContentNodeCollectionName(request, this.contentNodeCollectionName);
            for (; this.beginIndex > -1; --this.beginIndex) {
                Resource.setLocalContentNode(request, (Content) this.contentNodeIterator.next());
            }
        }
        catch (RepositoryException re) {
            log.debug(re.getMessage());
            return SKIP_BODY;
        }
        return EVAL_BODY_INCLUDE;
    }

    /**
     * @return int
     */
    public int doAfterBody() {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        if (this.contentNodeIterator.hasNext() && (this.currentIndex < this.getEnd())) {
            this.currentIndex++;
            pageContext.setAttribute(
                ContentNodeIterator.CURRENT_INDEX,
                new Integer(this.currentIndex),
                PageContext.REQUEST_SCOPE);
            for (int i = 0; i < this.step; i++) {
                Resource.setLocalContentNode(request, (Content) this.contentNodeIterator.next());
            }
            return EVAL_BODY_AGAIN;
        }
        return SKIP_BODY;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag() {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        Resource.removeLocalContentNode(request);
        Resource.removeLocalContentNodeCollectionName(request);
        pageContext.removeAttribute(ContentNodeIterator.CURRENT_INDEX);
        pageContext.removeAttribute(ContentNodeIterator.SIZE);
        pageContext.removeAttribute(ContentNodeIterator.CONTENT_NODE_COLLECTION_NAME);
        return EVAL_PAGE;
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#release()
     */
    public void release() {
        this.contentNodeCollectionName = null;
        this.contentNodeIterator = null;
        this.beginIndex = 0;
        this.endIndex = 0;
        this.step = 1;
        this.size = 0;
        this.currentIndex = 0;
        super.release();
    }

}
