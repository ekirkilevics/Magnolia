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
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.Resource;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.jstl.core.LoopTagStatus;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * @version $Revision $ ($Author $)
 */
public class ContentNodeIterator extends TagSupport {

    public static final String CONTENT_NODE_COLLECTION_NAME = "contentNodeCollectionName"; //$NON-NLS-1$

    protected static final String CURRENT_INDEX = "currentIndex"; //$NON-NLS-1$

    protected static final String SIZE = "size"; //$NON-NLS-1$

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(ContentNodeIterator.class);

    /**
     * Tag attribute.
     */
    private String contentNodeCollectionName;

    /**
     * Tag attribute.
     */
    protected int begin;

    /**
     * Tag attribute.
     */
    protected int end;

    /**
     * Tag attribute.
     */
    protected int step = 1;

    /**
     * Tag attribute.
     */
    private String varStatus;

    /**
     * Tag attribute.
     */
    private Collection items;

    protected int size;

    protected int index;

    private Iterator contentNodeIterator;

    private LoopTagStatus status;

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
        this.begin = (new Integer(index)).intValue();
    }

    /**
     * @param index index to end at
     */
    public void setEnd(int index) {
        this.end = index;
    }

    /**
     * @param step to jump to
     */
    public void setStep(int step) {
        this.step = step;
    }

    /**
     * Setter for <code>varStatus</code>.
     * @param varStatus The varStatus to set.
     */
    public void setVarStatus(String varStatus) {
        this.varStatus = varStatus;
    }

    /**
     * Setter for <code>items</code>.
     * @param items The items to set.
     */
    public void setItems(Collection items) {
        this.items = items;
    }

    /**
     * @return end index
     */
    private int getEnd() {
        if (this.end == 0) {
            return this.size;
        }
        return this.end;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();

        Collection children;
        try {
            children = getCollection(request);
        }
        catch (PathNotFoundException e) {
            // ok, this is normal
            return SKIP_BODY;
        }
        catch (AccessDeniedException e) {
            log.debug(e.getMessage());
            return SKIP_BODY;
        }
        catch (RepositoryException e) {
            log.error(e.getMessage(), e);
            return SKIP_BODY;
        }

        this.size = children.size();
        if (this.size == 0) {
            return SKIP_BODY;
        }

        pageContext.setAttribute(ContentNodeIterator.SIZE, new Integer(this.getEnd()), PageContext.REQUEST_SCOPE);
        pageContext.setAttribute(ContentNodeIterator.CURRENT_INDEX, new Integer(this.index), PageContext.REQUEST_SCOPE);
        pageContext.setAttribute(
            ContentNodeIterator.CONTENT_NODE_COLLECTION_NAME,
            this.contentNodeCollectionName,
            PageContext.REQUEST_SCOPE);
        this.contentNodeIterator = children.iterator();
        Resource.setLocalContentNodeCollectionName(request, this.contentNodeCollectionName);
        for (; this.begin > -1; --this.begin) {
            Resource.setLocalContentNode(request, (Content) this.contentNodeIterator.next());
        }

        if (StringUtils.isNotEmpty(varStatus)) {
            pageContext.setAttribute(varStatus, getLoopStatus());
        }
        return EVAL_BODY_INCLUDE;
    }

    /**
     * @param request
     * @return
     * @throws PathNotFoundException
     * @throws RepositoryException
     * @throws AccessDeniedException
     */
    private Collection getCollection(HttpServletRequest request) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {

        if (this.items != null) {
            return this.items;
        }
        else if (StringUtils.isNotEmpty(this.contentNodeCollectionName)) {
            Content page = Resource.getCurrentActivePage(request);
            return page.getContent(this.contentNodeCollectionName).getChildren(ItemType.CONTENTNODE);
        }

        return Collections.EMPTY_LIST;
    }

    protected LoopTagStatus getLoopStatus() {

        class Status implements LoopTagStatus, Serializable {

            /**
             * Stable serialVersionUID.
             */
            private static final long serialVersionUID = 222L;

            public Object getCurrent() {
                return (this.getCurrent());
            }

            public int getIndex() {
                return index;
            }

            public int getCount() {
                return size;
            }

            public boolean isFirst() {
                return (index == 0);
            }

            public boolean isLast() {
                return (index == (size - 1));
            }

            public Integer getBegin() {
                return new Integer(begin);
            }

            public Integer getEnd() {
                return end != 0 ? new Integer(end) : null;
            }

            public Integer getStep() {
                return new Integer(size);
            }
        }
        if (status == null) {
            status = new Status();
        }

        return status;
    }

    /**
     * @return int
     */
    public int doAfterBody() {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        if (this.contentNodeIterator.hasNext() && (this.index < this.getEnd())) {
            this.index++;
            pageContext.setAttribute(
                ContentNodeIterator.CURRENT_INDEX,
                new Integer(this.index),
                PageContext.REQUEST_SCOPE);

            if (StringUtils.isNotEmpty(varStatus)) {
                pageContext.setAttribute(varStatus, getLoopStatus());
            }

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
        this.begin = 0;
        this.end = 0;
        this.step = 1;
        this.size = 0;
        this.index = 0;

        if (varStatus != null) {
            pageContext.removeAttribute(varStatus, PageContext.PAGE_SCOPE);
        }

        return EVAL_PAGE;
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#release()
     */
    public void release() {
        this.contentNodeCollectionName = null;
        this.contentNodeIterator = null;
        this.begin = 0;
        this.end = 0;
        this.step = 1;
        this.size = 0;
        this.index = 0;
        this.varStatus = null;
        this.status = null;
        this.items = null;
        super.release();
    }

}
