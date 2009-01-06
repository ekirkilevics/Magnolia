/**
 * This file Copyright (c) 2007-2009 Magnolia International
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

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.MockContent;
import info.magnolia.test.mock.MockNodeData;
import info.magnolia.test.MgnlTagTestCase;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

/**
 * @author Ryan Gardner
 * @version $Revision$ ($Author$)
 */
public class OutTest extends MgnlTagTestCase {
    private AggregationState agState;

    protected void setUp() throws Exception {
        super.setUp();
        agState = initAgState();
        webContext.setAggregationState(agState);
        final ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setDefaultExtension("html");
        FactoryUtil.setInstance(ServerConfiguration.class, serverConfiguration);
    }

    // UUID - related output tests
    protected Out basicUUID_test_instance(String linkResolvingType) throws RepositoryException {
       HierarchyManager hm = webContext.getHierarchyManager(ContentRepository.WEBSITE);
       pageContext.setAttribute("actPage", hm.getContentByUUID("1"));
       Out out = new Out();
       //2 is the node of the uuid link stored in the repository
       out.setContentNode(hm.getContentByUUID("2"));
       out.setPageContext(pageContext);
       out.setNodeDataName("link1");
       out.setUuidToLink(linkResolvingType);
       return out;
    }

    public void testUUIDLinkOutRelative() throws IOException, RepositoryException, JspException {
        Out out = basicUUID_test_instance(Out.LINK_RESOLVING_RELATIVE);
        out.doEndTag();
        assertJspContent("UUID link is resolved as a relative link", "main/linkTarget");
        assertJspContent("JSP Content should be cleared after each test", "");
    }

    public void testUUIDLinkOutAbsolute() throws IOException, RepositoryException, JspException {
        Out out = basicUUID_test_instance(Out.LINK_RESOLVING_ABSOLUTE);
        out.doEndTag();
        assertJspContent("UUID link is resolved as an absolute link", "/main/linkTarget.html");
    }

    public void testUUIDLinkOutNone() throws IOException, RepositoryException, JspException {
        Out out = basicUUID_test_instance(Out.LINK_RESOLVING_NONE);
        out.doEndTag();
        assertJspContent("UUID link is output unresolved", "3");
    }

    public void testDateFormattingShouldUseAggregationStateLanguageIfNotSpecified() throws Exception {
        doTestForDateProperty(null, null, "Jul 8, 2008");
        agState.setLocale(Locale.GERMAN);
        doTestForDateProperty(null, null, "08.07.2008");
    }

    public void testDateFormattingShouldUseGivenLanguageIfSpecified() throws Exception {
        doTestForDateProperty("fr", null, "8 juil. 2008");
    }

    public void testDateFormattingShouldUseGivenPatternIfSpecified() throws Exception {
        doTestForDateProperty(null, "EEEE, d. MMMM yyyy", "Tuesday, 8. July 2008");
    }

    public void testDateFormattingShouldUseGivenPatternAndLanguageIfSpecified() throws Exception {
        doTestForDateProperty("fr", "EEEE, d. MMMM yyyy", "mardi, 8. juillet 2008");
    }

    private void doTestForDateProperty(String lang, String pattern, String expectedResult) throws JspException {
        final Calendar cal = Calendar.getInstance();
        cal.set(2008, 6, 8, 18, 55, 20);
        final MockContent testNode = new MockContent("test");
        testNode.addNodeData(new MockNodeData("myDate", cal));

        final Out out = new Out();
        out.setContentNode(testNode);
        out.setPageContext(pageContext);
        out.setNodeDataName("myDate");
        if (lang != null) {
            out.setDateLanguage(lang);
        }
        if (pattern != null) {
            out.setDatePattern(pattern);
        }
        out.doEndTag();
        assertJspContent(expectedResult);
    }


    // setup methods and utilities
    protected AggregationState initAgState() throws RepositoryException {
        AggregationState agState = new AggregationState();
        agState.setCharacterEncoding("UTF-8");
        agState.setCurrentURI("http://www.test.org/");
        agState.setRepository(ContentRepository.WEBSITE);
        agState.setHandle("/main");
        agState.setMainContent(webContext.getHierarchyManager(ContentRepository.WEBSITE).getContentByUUID("1"));
        agState.setLocale(Locale.US);
        return agState;
    }

    protected HierarchyManager initWebsiteData() throws IOException, RepositoryException {
        return MockUtil.createHierarchyManager(this.getClass().getResourceAsStream("outtest.properties"));
    }
}
