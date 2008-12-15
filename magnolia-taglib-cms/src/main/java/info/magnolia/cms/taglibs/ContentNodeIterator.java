/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.taglibs;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.Resource;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.jstl.core.LoopTagStatus;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;


/**
 * Iterate over contentNode collection. contentNodeIterator is used whenever you want to loop over content, typically paragraphs. A parameter
        contentNodeCollectionName will contain the name of the contentNode you are looping over.
        contentNodeCollectionName is created by providing a newBar with the corresponding name. This will result in
        elements being created within that contentNode, and thus allow you to loop over these.

 * @jsp.tag name="contentNodeIterator" body-content="JSP"
 * @jsp.tag-example
 * <cms:contentNodeIterator contentNodeCollectionName="mainColumnParagraphs">
 *   <cms:adminOnly>
 *     <cms:editBar/>
 *   </cms:adminOnly>
 *   <cms:includeTemplate/>
 * </cms:contentNodeIterator>
 *
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * @author David Smith
 * @version $Revision$ ($Author$)
 */
public class ContentNodeIterator extends BaseContentTag {

    /**
     * @deprecated
     */
    public static final String CONTENT_NODE_COLLECTION_NAME = "contentNodeCollectionName"; //$NON-NLS-1$

    /**
     * @deprecated use the <code>varStatus</code> tag attribute to get a reference to a
     * <code>javax.servlet.jsp.jstl.core.LoopTagStatus</code> instance.
     */
    protected static final String CURRENT_INDEX = "currentIndex"; //$NON-NLS-1$

    /**
     * @deprecated use the <code>varStatus</code> tag attribute to get a reference to a
     * <code>javax.servlet.jsp.jstl.core.LoopTagStatus</code> instance.
     */
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
    protected int begin;

    /**
     * Tag attribute.
     */
    protected Integer end;

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

    protected int count;
    protected int index;

    protected Object current;

    private Iterator contentNodeIterator;

    private LoopTagStatus status;

    /** Size info from outer node iterator, if one exists. */
    private Integer outerSize = null;

    /** Current index the outer node iterator is at. */
    private Integer outerCurrIdx = null;

    /** Collection iterated over by the outer node iterator */
    private String outerCollName = null;

    /** Resource.getLocalContentNodeCollectionName() value set by outer node iterator. */
    private String outerResCollName = null;

    /** Resource.getLocalContentNode set by the outer node iterator. */
    private Content outerLocalContentNode = null;

    /** Flag indicating the previous node state should be restored. */
    private boolean restorePreviousState = false;

    /**
     * Zero-based index of first item to process, inclusive.
     * @jsp.attribute required="false" rtexprvalue="true" type="int"
     */
    public void setBegin(int index) {
        this.begin = index;
    }

    /**
     * Zero-based index of last item to process, inclusive.
     * @jsp.attribute required="false" rtexprvalue="true" type="int"
     */
    public void setEnd(Integer index) {
        this.end = index;
    }

    /**
     * Process every stepth element (e.g 2 = every second element).
     * @jsp.attribute required="false" rtexprvalue="true" type="int"
     */
    public void setStep(int step) {
        this.step = step;
    }

    /**
     * Name of variable to hold the loop status with the following properties: index: position of the current item;
     *  count: number of times through the loop (starting with 1); first: boolean indicator if this is the first
     *  iteration; last: boolean indicator if this is the last iteration.
     *
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setVarStatus(String varStatus) {
        this.varStatus = varStatus;
    }

    /**
     * if this attribute is set, the tag will iterate on the collection directly passed here instead of fetching the
     * collection named by contentNodeCollectionName. This collection must contains info.magnolia.cms.core.Content
     * items.
     * @jsp.attribute required="false" rtexprvalue="true" type="java.util.Collection"
     */
    public void setItems(Collection items) {
        this.items = items;
    }

    /**
     * @jsp.attribute description="nodeDataName is not supported in this tag !" required="false" rtexprvalue="false"
     */
    public void setNodeDataName(String name) {
        throw new UnsupportedOperationException("nodeDataName not supported in this tag");
    }

    /**
     * TODO : provide relevant doc for this attribute -- only overriden to do so.
     */
    public void setContentNodeName(String name) {
        super.setContentNodeName(name);
    }

    private Collection getCollection() throws RepositoryException {
        if (this.items != null) {
            return this.items;
        }
        // If this is a nested iterator, the collection should be from the local content node.
        Content page = super.resolveNode(Resource.getCurrentActivePage());
        return page == null ? Collections.EMPTY_LIST : page.getChildren(ItemType.CONTENTNODE);
    }

    protected LoopTagStatus getLoopStatus() {

        class Status implements LoopTagStatus, Serializable {

            /**
             * Stable serialVersionUID.
             */
            private static final long serialVersionUID = 222L;

            public Object getCurrent() {
                return current;
            }

            public int getIndex() {
                return count + begin - 1;
            }

            public int getCount() {
                return count;
            }

            public boolean isFirst() {
                return (count == 1); // count starts with 1
            }

            public boolean isLast() {
                int lastItem = end != null ? end.intValue() : size;
                return count + begin == lastItem;
            }

            public Integer getBegin() {
                return new Integer(begin);
            }

            public Integer getEnd() {
                return end;
            }

            public Integer getStep() {
                return new Integer(step);
            }
        }

        if (status == null) {
            status = new Status();
        }

        return status;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        index += begin;

        Collection children;
        try {
            children = getCollection();
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

        this.savePrevState();

        pageContext.setAttribute(ContentNodeIterator.SIZE, new Integer(size), PageContext.REQUEST_SCOPE);

        pageContext.setAttribute(ContentNodeIterator.CURRENT_INDEX, new Integer(this.count), PageContext.REQUEST_SCOPE);

        pageContext.setAttribute(ContentNodeIterator.CONTENT_NODE_COLLECTION_NAME, getContentNodeCollectionName(), PageContext.REQUEST_SCOPE);

        Resource.setLocalContentNodeCollectionName(getContentNodeCollectionName());

        this.contentNodeIterator = children.iterator();
        for (int i = 0; i < begin; i++) {
            if (this.contentNodeIterator.hasNext()) {
                this.contentNodeIterator.next();
            }
        }

        return doIteration() ? EVAL_BODY_INCLUDE : SKIP_BODY;
    }

    /**
     * @return int
     */
    public int doAfterBody() {
        return doIteration() ? EVAL_BODY_AGAIN : SKIP_BODY;
    }

    /**
     * @return
     */
    private boolean doIteration() {
        if (this.contentNodeIterator.hasNext()) {

            if (this.end != null && this.count > this.end.intValue()) {
                return false;
            }

            pageContext.setAttribute(
                ContentNodeIterator.CURRENT_INDEX,
                new Integer(this.count),
                PageContext.REQUEST_SCOPE);

            if (StringUtils.isNotEmpty(varStatus)) {
                pageContext.setAttribute(varStatus, getLoopStatus());
            }

            for (int j = 0; j < this.step; j++) {
                current = this.contentNodeIterator.next();
                Resource.setLocalContentNode((Content) current);
            }

            this.count++;

            return true;
        }
        return false;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag() {

        this.restorePrevState();
        this.restorePreviousState = false;
        this.size = 0;
        this.count = 0;
        this.current = null;

        if (varStatus != null) {
            pageContext.removeAttribute(varStatus, PageContext.PAGE_SCOPE);
        }

        reset();

        return EVAL_PAGE;
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#release()
     */
    public void reset() {
        setContentNodeCollectionName(null); // TODO : is this correct - here ?
        this.contentNodeIterator = null;
        this.begin = 0;
        this.end = null;
        this.step = 1;
        this.size = 0;
        this.count = 0;
        this.varStatus = null;
        this.status = null;
        this.items = null;

        this.outerCollName = null;
        this.outerCurrIdx = null;
        this.outerLocalContentNode = null;
        this.outerResCollName = null;
        this.outerSize = null;
    }

    /** Checks if a content node iterator tag is already in operation and saves it's state. */
    private void savePrevState() {
        HttpServletRequest req = (HttpServletRequest) this.pageContext.getRequest();

        // savePrevState() was invoked. Enable restorePrevState()
        this.restorePreviousState = true;

        if (req.getAttribute(ContentNodeIterator.SIZE) != null) {
            this.outerSize = (Integer) pageContext.getAttribute(ContentNodeIterator.SIZE, PageContext.REQUEST_SCOPE);
            this.outerCollName = (String) pageContext.getAttribute(
                ContentNodeIterator.CONTENT_NODE_COLLECTION_NAME,
                PageContext.REQUEST_SCOPE);
            this.outerCurrIdx = (Integer) pageContext.getAttribute(
                ContentNodeIterator.CURRENT_INDEX,
                PageContext.REQUEST_SCOPE);
            this.outerLocalContentNode = Resource.getLocalContentNode();
            this.outerResCollName = Resource.getLocalContentNodeCollectionName();
        }
        else{
            this.outerLocalContentNode = Resource.getLocalContentNode();
        }

    }

    private void restorePrevState() {
        if (this.restorePreviousState) {
            HttpServletRequest req = (HttpServletRequest) this.pageContext.getRequest();
            if (this.outerSize != null) {
                pageContext.setAttribute(ContentNodeIterator.SIZE, this.outerSize);
                pageContext.setAttribute(ContentNodeIterator.CURRENT_INDEX, this.outerCurrIdx);
                pageContext.setAttribute(ContentNodeIterator.CONTENT_NODE_COLLECTION_NAME, this.outerCollName);
                Resource.setLocalContentNode(this.outerLocalContentNode);
                Resource.setLocalContentNodeCollectionName(this.outerResCollName);
            }
            else {
                if(outerLocalContentNode != null){
                    Resource.setLocalContentNode(this.outerLocalContentNode);
                }
                else{
                    Resource.removeLocalContentNode();
                }
                Resource.removeLocalContentNodeCollectionName();
                pageContext.removeAttribute(ContentNodeIterator.CURRENT_INDEX);
                pageContext.removeAttribute(ContentNodeIterator.SIZE);
                pageContext.removeAttribute(ContentNodeIterator.CONTENT_NODE_COLLECTION_NAME);
            }
        }
    }

}
