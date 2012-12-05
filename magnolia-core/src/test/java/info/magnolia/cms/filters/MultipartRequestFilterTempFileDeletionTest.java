/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.cms.filters;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockWebContext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.mockrunner.mock.web.MockFilterChain;

/**
 * Tests deletion of temp files created by MultipartRequestFilter and CosMultipartRequestFilter.
 */
public class MultipartRequestFilterTempFileDeletionTest {

    private File testFile;
    private HttpServletRequest req;
    private HttpServletResponse res;
    private FilterChain filterChain;
    private MockWebContext webCtx;
    private File file;

    @Before
    public void setUp(){
        ComponentsTestUtil.setImplementation(WebContainerResources.class, WebContainerResourcesImpl.class);

        SystemProperty.setProperty(SystemProperty.MAGNOLIA_UPLOAD_TMPDIR, System.getProperty("java.io.tmpdir"));
        SystemProperty.setProperty("info.magnolia.cms.util.UnicodeNormalizer$Normalizer", "info.magnolia.cms.util.UnicodeNormalizer$AutoDetectNormalizer");
        SystemProperty.setProperty("magnolia.utf8.enabled", "true");
        testFile = new File("pom.xml");
        assertTrue(testFile.getAbsolutePath() + " can't be found.", testFile.exists());
        req = mock(HttpServletRequest.class);
        when(req.getAttribute(Mockito.<String>anyObject())).thenReturn(null);
        res = mock(HttpServletResponse.class);
        webCtx = new MockWebContext();
        MgnlContext.setInstance(webCtx);
        file = mock(File.class);
    }

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        SystemProperty.clear();
        MgnlContext.setInstance(null);
    }

    @Test
    public void testMultipartFilterFileDeletion() throws Throwable {
        //GIVEN
        OncePerRequestAbstractMgnlFilter testFilter = getTestFilter();
        MultipartRequestFilter multiFilter = new MultipartRequestFilter();
        filterChain = new MockFilterChain();
        ((MockFilterChain)filterChain).addFilter(testFilter);

        //WHEN
        doTest(multiFilter, "text/xml; charset=UTF-8");

        //THEN
        verify(file).delete();
    }

    @Test
    public void testCOSMultipartFilterFileDeletion() throws Throwable {
        //GIVEN
        OncePerRequestAbstractMgnlFilter testFilter = getTestFilter();
        CosMultipartRequestFilter multiFilter = new CosMultipartRequestFilter();
        filterChain = new MockFilterChain();
        ((MockFilterChain)filterChain).addFilter(testFilter);

        //WHEN
        doTest(multiFilter, "text/xml");

        //THEN
        verify(file).delete();
    }

    //filter to pass mock file into multipart form
    private OncePerRequestAbstractMgnlFilter getTestFilter(){
        return new OncePerRequestAbstractMgnlFilter() {

            @Override
            public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
                assertNotNull(MgnlContext.getPostedForm());
                assertNotNull(MgnlContext.getPostedForm().getDocument("document").getFile());
                MgnlContext.getPostedForm().addDocument("document", "testFile", "text/xml", file);
            }
        };
    }
    
    public void doTest(Filter filter, final String expectedDocumentType) throws Throwable {
        //GIVEN
        final MultipartRequestEntity multipart = newMultipartRequestEntity();
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        multipart.writeRequest(output);
        final byte[] bytes = output.toByteArray();
        final ByteArrayInputStream delegateStream = new ByteArrayInputStream(bytes);
        final ServletInputStream servletInputStream = new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return delegateStream.read();
            }
        };

        //WHEN
        req.setAttribute(isA(String.class), isA(Boolean.class));
        when(req.getContentType()).thenReturn(multipart.getContentType());
        when(req.getHeader("Content-Type")).thenReturn(multipart.getContentType());
        when(req.getCharacterEncoding()).thenReturn("UTF-8");
        when(req.getQueryString()).thenReturn("");
        when(req.getContentLength()).thenReturn(Integer.valueOf((int) multipart.getContentLength()));
        when(req.getInputStream()).thenReturn(servletInputStream);

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final Object args = invocation.getArguments()[1];
                checkMultipartForm((MultipartForm) args, expectedDocumentType);
                webCtx.setPostedForm((MultipartForm) args);
                return null;
            }
        }).when(req).setAttribute(eq(MultipartForm.REQUEST_ATTRIBUTE_NAME), isA(MultipartForm.class));
        when(file.exists()).thenReturn(true);

        webCtx.pop();

        //THEN
        filter.doFilter(req, res, filterChain);
    }

    private MultipartRequestEntity newMultipartRequestEntity() throws Exception {
        PostMethod method = new PostMethod();
        Part[] parts = {
                new StringPart("param1", "value1", "UTF-8"),
                new StringPart("param2", "àèìòù", "UTF-8"),
                new StringPart("param3", "value3a", "UTF-8"),
                new StringPart("param3", "value3b", "UTF-8"),
                new FilePart("document", testFile, "text/xml", "UTF-8")};

        return new MultipartRequestEntity(parts, method.getParams());
    }

    private void checkMultipartForm(MultipartForm form, String expectedDocumentType) throws IOException {
        assertNotNull("MultipartForm request attribute expected", form);
        assertEquals(3, form.getParameters().size());
        assertEquals("value1", form.getParameter("param1"));
        assertEquals("àèìòù", form.getParameter("param2"));

        String[] value3 = form.getParameterValues("param3");
        assertNotNull("multi-value parameter has not been parsed", value3);
        assertEquals(2, value3.length);

        assertEquals(1, form.getDocuments().size());

        Document document = form.getDocument("document");
        assertNotNull("expected non-null Document", document);
        assertEquals("document", document.getAtomName());
        assertEquals("xml", document.getExtension());
        assertEquals("pom", document.getFileName());
        assertEquals("pom.xml", document.getFileNameWithExtension());
        assertEquals(testFile.length(), document.getLength());

        assertEquals(expectedDocumentType, document.getType());

        assertTrue(document.getType().startsWith("text/xml"));

        File documentFile = document.getFile();
        assertTrue(documentFile.exists());
        assertTrue(documentFile.canRead());
        InputStream stream1 = document.getStream();
        assertEquals(testFile.length(), stream1.available());
        assertEquals(testFile.length(), stream1.skip(testFile.length()));
        assertEquals(0, stream1.available());
        documentFile.deleteOnExit();
    }
}
