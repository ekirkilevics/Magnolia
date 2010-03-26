/**
 * This file Copyright (c) 2010-2010 Magnolia International
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
package info.magnolia.module.templatingcomponents.jsp;

import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.meterware.httpunit.WebResponse;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.context.WebContext;
import info.magnolia.test.mock.MockHierarchyManager;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class NewBarTagTest extends AbstractJspTest {
    private static final String EXPECTED_ONCLICK_FMT = "mgnlShiftPushButtonClick(this);mgnlOpenDialog('/foo/bar','paragraphs','mgnlNew','%s','null','.magnolia/dialogs/selectParagraph.html', null, null, 'en_US');";

    @Override
    protected void setupAggregationState(AggregationState aggState) throws RepositoryException {
        aggState.setMainContent(websiteHM.getContent("/foo/bar"));
        aggState.setCurrentContent(websiteHM.getContent("/foo/bar"));
    }

    @Override
    protected void setupExpectations(WebContext ctx, MockHierarchyManager hm, HttpServletRequest req, AccessManager accessManager) {
    }

    @Override
    void check(WebResponse response, HtmlPage page) throws Exception {
        //prettyPrint(response, System.out);

        {
            final HtmlDivision div = page.getFirstByXPath("//div[@id='asList']");
            final DomNodeList<HtmlElement> spans = div.getElementsByTagName("span");
            assertEquals(1, spans.size());
            assertEquals("New", spans.get(0).getTextContent());

            // entities in attribute values are already decoded
            final String onclick = spans.get(0).getOnClickAttribute();
            // this is the ugly bit
            final String expected = String.format(EXPECTED_ONCLICK_FMT, "abc,def");
            assertEquals(expected, onclick);
        }

        {
            final HtmlDivision div = page.getFirstByXPath("//div[@id='customLabel']");
            final DomNodeList<HtmlElement> spans = div.getElementsByTagName("span");
            assertEquals(1, spans.size());
            assertEquals("My custom new label", spans.get(0).getTextContent());

            // entities in attribute values are already decoded
            final String onclick = spans.get(0).getOnClickAttribute();
            // this is the ugly bit
            final String expected = String.format(EXPECTED_ONCLICK_FMT, "abc,def");
            assertEquals(expected, onclick);
        }

        {
            final HtmlDivision div = page.getFirstByXPath("//div[@id='emptyParagraphList']");
            final DomNodeList<HtmlElement> spans = div.getElementsByTagName("span");
            assertEquals(0, spans.size());
            /* assertEquals("No paragraph is available ...", spans.get(0).getTextContent());

            // entities in attribute values are already decoded
            final String onclick = spans.get(0).getOnClickAttribute();
            // this is the ugly bit
            final String expected = "mgnlShiftPushButtonClick(this);";
            assertEquals(expected, onclick);
            */
        }

    }

}
