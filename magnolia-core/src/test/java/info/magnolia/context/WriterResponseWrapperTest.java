package info.magnolia.context;

import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import java.io.StringWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class WriterResponseWrapperTest extends TestCase {
    public void testCantUseWriterAfterOutputStream() throws IOException {
        final HttpServletResponse response = createStrictMock(HttpServletResponse.class);
        final StringWriter out = new StringWriter();
        final WriterResponseWrapper wrw = new WriterResponseWrapper(response, out);
        final ServletOutputStream os = wrw.getOutputStream();
        try {
            wrw.getWriter();
            fail("should have failed");
        } catch (IllegalStateException e) {
            assertEquals("According to the ServletResponse javadoc, either getWriter or getOutputStream may be called to write the body, not both.", e.getMessage());
        }
        os.print("boo");
        assertEquals("boo", out.toString());
    }

    public void testCantUseOutputStreamAfterWriter() {
        final HttpServletResponse response = createStrictMock(HttpServletResponse.class);
        final StringWriter out = new StringWriter();
        final WriterResponseWrapper wrw = new WriterResponseWrapper(response, out);
        final PrintWriter w = wrw.getWriter();
        try {
            wrw.getOutputStream();
            fail("should have failed");
        } catch (IllegalStateException e) {
            assertEquals("According to the ServletResponse javadoc, either getWriter or getOutputStream may be called to write the body, not both.", e.getMessage());
        }
        w.print("boo");
        assertEquals("boo", out.toString());
    }
}
