package info.magnolia.cms.filters;

import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.test.MagnoliaTestUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.log4j.Logger;

import com.mockrunner.mock.web.MockFilterChain;
import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;


/**
 * @author Andreas Brenk
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class MultipartRequestFilterTest extends TestCase {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(MultipartRequestFilterTest.class);

    private Filter filter;

    protected void setUp() throws Exception {
        super.setUp();

        SystemProperty.setProperty(SystemProperty.MAGNOLIA_UPLOAD_TMPDIR, MagnoliaTestUtils.getProjectRoot()
            + "/target");

    }

    protected void tearDown() throws Exception {
        super.tearDown();
        this.filter = null;
    }

    public void testFilterCOS() throws Throwable {
        this.filter = new MultipartRequestFilter();
        doTest();
    }

    public void testFilterCommonsFileUpload() throws Throwable {
        this.filter = new CommonsFileUploadMultipartRequestFilter();
        doTest();
    }

    public void doTest() throws Throwable {
        HttpServletRequest request = newHttpServletRequest();
        HttpServletResponse response = newHttpServletResponse();
        FilterChain chain = newFilterChain();

        try {
            this.filter.doFilter(request, response, chain);
        }
        catch (ServletException e) {
            throw e.getRootCause();
        }

        MultipartForm form = (MultipartForm) request.getAttribute(MultipartForm.REQUEST_ATTRIBUTE_NAME);
        assertNotNull("MultipartForm request attribute expected", form);
        assertEquals(3, form.getParameters().size());
        assertEquals("value1", form.getParameter("param1"));
        assertEquals("àèìòù", form.getParameter("param2"));

        String[] value3 = form.getParameterValues("param3");
        assertNotNull("multi-value parameter has not been parsed", value3);
        assertEquals(2, value3.length);

        assertEquals(1, form.getDocuments().size());

        int expectedDocumentSize = 1495;
        Document document = form.getDocument("document");
        assertNotNull("expected non-null Document", document);
        assertEquals("document", document.getAtomName());
        assertEquals("xml", document.getExtension());
        assertEquals("log4j", document.getFileName());
        assertEquals("log4j.xml", document.getFileNameWithExtension());
        assertEquals(expectedDocumentSize, document.getLength());

        // commons file upload
        // assertEquals("text/xml; charset=UTF-8", document.getType());

        // COS
        // assertEquals("text/xml", document.getType());

        assertTrue(document.getType().startsWith("text/xml"));

        File file1 = document.getFile();
        assertTrue(file1.exists());
        assertTrue(file1.canRead());
        InputStream stream1 = document.getStream();
        assertEquals(expectedDocumentSize, stream1.available());
        assertEquals(expectedDocumentSize, stream1.skip(expectedDocumentSize));
        assertEquals(0, stream1.available());
    }

    private FilterChain newFilterChain() {
        MockFilterChain chain = new MockFilterChain();

        return chain;
    }

    private HttpServletRequest newHttpServletRequest() throws Exception {
        MultipartRequestEntity multipart = newMultipartRequestEntity();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        multipart.writeRequest(output);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType(multipart.getContentType());
        request.setHeader("Content-type", multipart.getContentType());
        request.setContentLength((int) multipart.getContentLength());
        request.setBodyContent(output.toByteArray());
        request.setCharacterEncoding("UTF-8");

        return request;
    }

    private HttpServletResponse newHttpServletResponse() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        return response;
    }

    private MultipartRequestEntity newMultipartRequestEntity() throws Exception {
        File f1 = new File(MagnoliaTestUtils.getProjectRoot() + "/src/test-resources/log4j.xml");

        PostMethod method = new PostMethod();
        Part[] parts = {
            new StringPart("param1", "value1", "UTF-8"),
            new StringPart("param2", "àèìòù", "UTF-8"),
            new StringPart("param3", "value3a", "UTF-8"),
            new StringPart("param3", "value3b", "UTF-8"),
            new FilePart("document", f1, "text/xml", "UTF-8")};

        return new MultipartRequestEntity(parts, method.getParams());
    }
}
