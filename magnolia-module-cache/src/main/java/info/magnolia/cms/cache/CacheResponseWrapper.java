package info.magnolia.cms.cache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;


/**
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class CacheResponseWrapper extends HttpServletResponseWrapper {

    private ByteArrayOutputStream cachingStream;

    private PrintWriter cachingWriter = null;

    private CacheableEntry cacheableEntry;

    private int status = SC_OK;

    public CacheResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    public CacheableEntry getCacheableEntry() {
        if (cachingStream == null) {
            return null;
        }
        cacheableEntry = new CacheableEntry(cachingStream.toByteArray());
        cacheableEntry.setContentType(getContentType());
        cacheableEntry.setCharacterEncoding(getCharacterEncoding());
        return cacheableEntry;
    }

    /**
     * @see javax.servlet.ServletResponseWrapper#getOutputStream()
     */
    public ServletOutputStream getOutputStream() throws IOException {
        cachingStream = new ByteArrayOutputStream();
        return new MultiplexServletOutputStream(super.getOutputStream(), cachingStream);
    }

    public PrintWriter getWriter() throws IOException {
        if (cachingWriter == null) {
            String encoding = getCharacterEncoding();
            cachingWriter = encoding != null
                ? new PrintWriter(new OutputStreamWriter(getOutputStream(), encoding))
                : new PrintWriter(new OutputStreamWriter(getOutputStream()));
        }

        return cachingWriter;
    }

    public void flushBuffer() throws IOException {
        super.flushBuffer();

        if (cachingStream != null) {
            cachingStream.flush();
        }

        if (cachingWriter != null) {
            cachingWriter.flush();
        }
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        super.setStatus(status);
        this.status = status;
    }

    public void setStatus(int status, String string) {
        super.setStatus(status, string);
        this.status = status;
    }

    public void sendRedirect(String location) throws IOException {
        this.status = SC_MOVED_TEMPORARILY;
        super.sendRedirect(location);
    }

    public void sendError(int status, String string) throws IOException {
        super.sendError(status, string);
        this.status = status;
    }

    public void sendError(int status) throws IOException {
        super.sendError(status);
        this.status = status;
    }

    public void reset() {
        super.reset();
        if (cachingStream != null) {
            cachingStream.reset();
        }
        cachingWriter = null;
        status = SC_OK;
    }

    public void resetBuffer() {
        super.reset();
        if (cachingStream != null) {
            cachingStream.reset();
        }
        cachingWriter = null;
        status = SC_OK;
    }

}
