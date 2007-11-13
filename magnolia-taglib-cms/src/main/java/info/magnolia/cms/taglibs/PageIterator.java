/**
 * This file Copyright (c) 2003-2007 Magnolia International
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
import info.magnolia.cms.util.Resource;

import java.util.Collection;
import java.util.Iterator;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;


/**
 * Iterates over subpages.
 * @author Andreas Brenk
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class PageIterator extends TagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private Iterator contentIterator;

    private String hiddenAttribute = "hidden";

    /**
     * s{@inheritDoc}
     */
    public int doAfterBody() throws JspException {
        boolean hasNext = nextContent();

        if (hasNext) {
            return EVAL_BODY_AGAIN;
        }

        return SKIP_BODY;
    }

    /**
     * {@inheritDoc}
     */
    public int doEndTag() throws JspException {
        Resource.restoreCurrentActivePage();
        return EVAL_PAGE;
    }

    /**
     * {@inheritDoc}
     */
    public int doStartTag() throws JspException {
        initContentIterator();

        boolean hasNext = nextContent();
        if (hasNext) {
            return EVAL_BODY_INCLUDE;
        }

        return SKIP_BODY;
    }

    /**
     * {@inheritDoc}
     */
    public void release() {
        this.contentIterator = null;
        hiddenAttribute = "hidden";
        super.release();
    }

    public void setHiddenAttribute(String hiddenAttribute) {
        this.hiddenAttribute = hiddenAttribute;
    }

    private void initContentIterator() {
        Content activePage = Resource.getCurrentActivePage();
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
     * @return <code>true</code> if the next page was activated, <code>false</code> if there are no pages left
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
