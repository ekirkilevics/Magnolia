/**
 * This file Copyright (c) 2007-2011 Magnolia International
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

import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.test.MgnlTagTestCase;
import info.magnolia.test.mock.MockContent;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;

import junit.framework.Assert;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class PageIteratorTest extends MgnlTagTestCase {

    private AggregationState aggregationState;

    /**
     * {@inheritDoc}
     */
    @Override
    protected HierarchyManager initWebsiteData() throws IOException, RepositoryException {
        return null;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        MockNode mainContent = new MockNode("main");
        MockNode currentContent = new MockNode("active");
        MockSession session = new MockSession("test");
        currentContent.setSession(session);

        MockNode mainContentChild = new MockNode("mainchild");

        MockNode currentContentChild = new MockNode("activechild");

        mainContent.addNode(mainContentChild);
        currentContent.addNode(currentContentChild);

        aggregationState = new AggregationState();
        aggregationState.setMainContent(mainContent);

        aggregationState.setCurrentContent(currentContent);

        webContext.setAggregationState(aggregationState);
    }

    /**
     * Test for MAGNOLIA-3007
     * @throws JspException
     */
    public void testIterateOnCurrentPage() throws JspException, RepositoryException {
        PageIterator tag = new PageIterator();

        tag.doStartTag();
        Assert.assertEquals("/active/activechild", aggregationState.getCurrentContent().getPath());
    }

    /**
     * Test for MAGNOLIA-3209
     */
    public void testIterateOnPagesNotOnParagraphs() throws JspException, RepositoryException {
        PageIterator tag = new PageIterator();

        // create a paragraph and set it as the current paragraph
        MockContent currentContent = (MockContent) aggregationState.getCurrentContent();
        MockContent paragraph = new MockContent("pargraph", ItemType.CONTENTNODE);

        currentContent.addContent(paragraph);

        aggregationState.setCurrentContent(paragraph.getJCRNode());

        tag.doStartTag();

        Assert.assertEquals("Must be the first child of the current page.", "/active/activechild", aggregationState.getCurrentContent().getPath());
    }


}
