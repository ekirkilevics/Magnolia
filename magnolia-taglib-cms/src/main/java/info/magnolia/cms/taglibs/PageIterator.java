/**
 * This file Copyright (c) 2003-2010 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
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
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.taglibs;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.context.MgnlContext;

import java.util.Collection;
import java.util.Iterator;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;


/**
 * Iterates over a Content (page) collection.
 * @jsp.tag name="pageIterator" body-content="JSP"
 *
 * @author Andreas Brenk
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class PageIterator extends TagSupport {

    private Iterator contentIterator;

    private String hiddenAttribute = "hidden";

    public int doAfterBody() throws JspException {
        boolean hasNext = nextContent();

        if (hasNext) {
            return EVAL_BODY_AGAIN;
        }

        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        Resource.restoreCurrentActivePage();
        return EVAL_PAGE;
    }

    public int doStartTag() throws JspException {
        initContentIterator();

        boolean hasNext = nextContent();
        if (hasNext) {
            return EVAL_BODY_INCLUDE;
        }

        return SKIP_BODY;
    }

    public void release() {
        this.contentIterator = null;
        hiddenAttribute = "hidden";
        super.release();
    }

    /**
     * If a page contains a boolean property with this name and it is set to true, the page is skipped by the iterator.
     * Defaults to "hidden".
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setHiddenAttribute(String hiddenAttribute) {
        this.hiddenAttribute = hiddenAttribute;
    }

    private void initContentIterator() {
        Content activePage = MgnlContext.getAggregationState().getCurrentContent();
        // the current content can be a paragraph
        try {
          while (!activePage.getItemType().equals(ItemType.CONTENT)) {
            activePage = activePage.getParent();
          }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Collection children = activePage.getChildren(ItemType.CONTENT);

        for (Iterator i = children.iterator(); i.hasNext();) {
            Content c = (Content) i.next();
            boolean hidden = c.getNodeData(this.hiddenAttribute).getBoolean();
            if (hidden) {
                i.remove();
            }
        }

        this.contentIterator = children.iterator();
    }

    /**
     * Returns <code>true</code> if the next page was activated, <code>false</code> if there are no pages left.
     */
    private boolean nextContent() {
        if (this.contentIterator.hasNext()) {
            Content page = (Content) this.contentIterator.next();
            Resource.setCurrentActivePage(page);
            return true;
        }

        return false;
    }
}
