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
package info.magnolia.test;

import info.magnolia.cms.beans.config.URI2RepositoryManager;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.link.LinkResolver;
import info.magnolia.cms.link.LinkResolverImpl;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;


import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import java.io.IOException;
import java.util.Locale;

import com.mockrunner.mock.web.MockPageContext;
import com.mockrunner.mock.web.MockServletConfig;
import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockJspWriter;
import static org.easymock.EasyMock.*;

/**
 * A base class to simplify the testing of tag library output.
 * @author ryangardner
 * Date: Jan 3, 2008
 */
public abstract class MgnlTagTestCase extends MgnlTestCase  {
    protected MockWebContext webContext;
    protected MockPageContext pageContext;
    private SystemContext sysContext;

    protected void setUp() throws Exception {
        super.setUp();

        HierarchyManager hm = initWebsiteData();
        webContext = new MockWebContext();
        webContext.addHierarchyManager(ContentRepository.WEBSITE, hm);

        MgnlContext.setInstance(webContext);

        sysContext = createMock(SystemContext.class);
        expect(sysContext.getLocale()).andReturn(Locale.ENGLISH).anyTimes();
        replay(sysContext);
        ComponentsTestUtil.setInstance(SystemContext.class, sysContext);

        // set up necessary items not configured in the repository
        ComponentsTestUtil.setImplementation(URI2RepositoryManager.class, URI2RepositoryManager.class);
        ComponentsTestUtil.setInstance(I18nContentSupport.class, new DefaultI18nContentSupport());
        ComponentsTestUtil.setInstance(LinkResolver.class, new LinkResolverImpl());

        setupPageContext();
    }

    protected void setupPageContext() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        pageContext = new MockPageContext(new MockServletConfig(), req, new MockHttpServletResponse());
    }

    protected void tearDown() throws Exception {
        verify(sysContext);
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
        super.tearDown();
    }


    /**
     * Clears the JSP buffer to allow for future tests to have a clean slate
     */
    protected void clearJsp() {
        MockJspWriter jspWriter = (MockJspWriter) pageContext.getOut();
        try {
            jspWriter.clear();
        } catch (IOException e) {
            assertNull("Our Jsp Writer should never throw an IO Exception ", e);
        }
    }

    /**
     * Test the value of the jsp document, clearing the JSP buffer after the check is done.
     *  - Delegates to {@link #assertJspContent(String, String)}
     */
    protected void assertJspContent(String expectedResult) throws JspException {
        assertJspContent(null, expectedResult);
    }

    /**
     * Test the value of the jsp document, clearing the JSP buffer after the check is done.
     *  - Delegates to {@link #assertJspContent(String, String, boolean)}
     */
    protected void assertJspContent(String explanation, String expectedResult) throws JspException {
        assertJspContent(explanation, expectedResult, false);
    }

    /**
     * Tests that the doEndTag() method outputs a value that matches the result parameter
     * After testing this, the JspWriter buffer is cleared to allow for further tests
     *
     * @param explanation Explanation of what is being tested 
     * @param expectedResult The expected result that the tag should write to the jsp page as a string
     * @param leaveJspIntact by default, the JSP buffer will be cleared after each call to this method 
     *                       if this value is true, the jsp will be left untouched after the assertion
     * @throws JspException  Thrown if there is a JSP Exception thrown by the tag
     */
    protected void assertJspContent(String explanation,  String expectedResult, boolean leaveJspIntact ) throws JspException {
         assertEquals(explanation, expectedResult, getJspOutput());
         if (!leaveJspIntact) {
            clearJsp();
         }
    }

    /**
     * Get the current value stored in the JSP buffer.
     * @return whatever is stored in the JSP buffer
     */
    protected String getJspOutput() {
        MockJspWriter jspWriter = (MockJspWriter) pageContext.getOut();
        return jspWriter.getOutputAsString();
    }


    /**
     * This method is responsible for initializing a HM repository for use in the testing.
     *
     * It is called durring the setUp() procedure.
     *
     * One suggested way to implement this is to create a properties file resource with the contents
     * you wish to initialize this with, and then read in the properties file here like this:
     *
     *  return MockUtil.createHierarchyManager(this.getClass().getResourceAsStream("mytagtest.properties"));
     *
     * (Obviously, mytagtest is a placeholder name, and in your implementation you would change this)
     *
     * @return A HierarchyManager initialized with the appropriate data needed to run the tests
     * @throws IOException
     * @throws RepositoryException
     */
    abstract protected HierarchyManager initWebsiteData() throws IOException, RepositoryException;
        //return MockUtil.createHierarchyManager(this.getClass().getResourceAsStream("outtest.properties"));


}
