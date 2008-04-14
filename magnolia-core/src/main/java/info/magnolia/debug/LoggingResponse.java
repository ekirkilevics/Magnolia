package info.magnolia.debug;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Wrapps a response and records the set headers and http status
 * @author philipp
 * @version $Id$
 */
class LoggingResponse extends HttpServletResponseWrapper {

    private Map headers = new HashMap();

    private int length;

    private int status;

    LoggingResponse(HttpServletResponse response) {
        super(response);
    }

    public Map getHeaders() {
        return this.headers;
    }


    public int getLength() {
        return this.length;
    }


    public int getStatus() {
        return this.status;
    }


    public void setDateHeader(String name, long date) {
        super.setDateHeader(name, date);
        headers.put(name, String.valueOf(date));
    }

    public void setIntHeader(String name, int value) {
        super.setIntHeader(name, value);
        headers.put(name, String.valueOf(value));
    }

    public void setContentLength(int len) {
        this.length = len;
        super.setContentLength(len);
    }

    public void setHeader(String name, String value) {
        super.setHeader(name, value);
        headers.put(name, value);
    }

    public void sendRedirect(String location) throws IOException {
        this.status = HttpServletResponse.SC_MOVED_TEMPORARILY;
        super.sendRedirect(location);
    }

    public void sendError(int sc) throws IOException {
        this.status = sc;
        super.sendError(sc);
    }

    public void sendError(int sc, String msg) throws IOException {
        this.status = sc;
        super.sendError(sc, msg);
    }

    public void setStatus(int sc) {
        this.status = sc;
        super.setStatus(sc);
    }

    public void setStatus(int sc, String sm) {
        this.status = sc;
        super.setStatus(sc, sm);
    }

}