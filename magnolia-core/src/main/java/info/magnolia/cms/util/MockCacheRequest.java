package info.magnolia.cms.util;

import info.magnolia.cms.core.Content;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/**
 * Fake request created from a real request used to generate cache content.
 * @author niko
 */
public class MockCacheRequest implements HttpServletRequest {

    private HttpServletRequest request;

    private Content node;

    public MockCacheRequest(HttpServletRequest request, Content node) {
        this.request = request;
        this.node = node;
    }

    public String getAuthType() {
        return request.getAuthType();
    }

    public Cookie[] getCookies() {
        return request.getCookies();
    }

    public long getDateHeader(String arg0) {
        return request.getDateHeader(arg0);
    }

    public String getHeader(String arg0) {
        return request.getHeader(arg0);
    }

    public Enumeration getHeaders(String arg0) {
        return request.getHeaders(arg0);
    }

    public Enumeration getHeaderNames() {
        return request.getHeaderNames();
    }

    public int getIntHeader(String arg0) {
        return request.getIntHeader(arg0);
    }

    public String getMethod() {
        return "GET";
    }

    public String getPathInfo() {
        return request.getPathInfo();
    }

    public String getPathTranslated() {
        return null;
    }

    public String getContextPath() {
        return request.getContextPath();
    }

    public String getQueryString() {
        return request.getQueryString();
    }

    public String getRemoteUser() {
        return request.getRemoteUser();
    }

    public boolean isUserInRole(String arg0) {
        return request.isUserInRole(arg0);
    }

    public Principal getUserPrincipal() {
        return request.getUserPrincipal();
    }

    public String getRequestedSessionId() {
        return request.getRequestedSessionId();
    }

    public String getRequestURI() {
        // TODO Auto-generated method stub
        return request.getContextPath() + getServletPath();
    }

    public StringBuffer getRequestURL() {
        // TODO Auto-generated method stub
        return new StringBuffer("http://" + request.getRemoteAddr() + getRequestURI());
    }

    public String getServletPath() {
        // TODO Auto-generated method stub
        try {
            return node.getJCRNode().getPath() + ".html";
        }
        catch (Exception e) {
            return null;
        }
    }

    public HttpSession getSession(boolean arg0) {
        return request.getSession(arg0);
    }

    public HttpSession getSession() {
        return request.getSession();
    }

    public boolean isRequestedSessionIdValid() {
        return request.isRequestedSessionIdValid();
    }

    public boolean isRequestedSessionIdFromCookie() {
        return request.isRequestedSessionIdFromCookie();
    }

    public boolean isRequestedSessionIdFromURL() {
        return request.isRequestedSessionIdFromURL();
    }

    /**
     * @deprecated
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
     */
    public boolean isRequestedSessionIdFromUrl() {
        return request.isRequestedSessionIdFromUrl();
    }

    public Object getAttribute(String arg0) {
        return request.getAttribute(arg0);
    }

    public Enumeration getAttributeNames() {
        return request.getAttributeNames();
    }

    public String getCharacterEncoding() {
        return request.getCharacterEncoding();
    }

    public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
        request.setCharacterEncoding(arg0);
    }

    public int getContentLength() {
        return 0;
    }

    public String getContentType() {
        // TODO Auto-generated method stub
        return null;
    }

    public ServletInputStream getInputStream() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public String getParameter(String arg0) {
        return request.getParameter(arg0);
    }

    public Enumeration getParameterNames() {
        return request.getParameterNames();
    }

    public String[] getParameterValues(String arg0) {
        return request.getParameterValues(arg0);
    }

    public static final Hashtable EMPTY_MAP = new Hashtable(0);

    public Map getParameterMap() {
        return EMPTY_MAP;
    }

    public String getProtocol() {
        return request.getProtocol();
    }

    public String getScheme() {
        return request.getScheme();
    }

    public String getServerName() {
        return request.getServerName();
    }

    public int getServerPort() {
        return request.getServerPort();
    }

    public BufferedReader getReader() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public String getRemoteAddr() {
        return request.getRemoteAddr();
    }

    public String getRemoteHost() {
        return request.getRemoteHost();
    }

    public void setAttribute(String arg0, Object arg1) {
        // TODO Auto-generated method stub

    }

    public void removeAttribute(String arg0) {
        // TODO Auto-generated method stub

    }

    public Locale getLocale() {
        return request.getLocale();
    }

    public Enumeration getLocales() {
        return request.getLocales();
    }

    public boolean isSecure() {
        return request.isSecure();
    }

    public RequestDispatcher getRequestDispatcher(String arg0) {
        return null;
    }

    /**
     * @deprecated
     * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
     */
    public String getRealPath(String arg0) {
        return request.getRealPath(arg0);
    }

    public int getRemotePort() {
        return request.getRemotePort();
    }

    public String getLocalName() {
        return request.getLocalName();
    }

    public String getLocalAddr() {
        return request.getLocalAddr();
    }

    public int getLocalPort() {
        return request.getLocalPort();
    }

}
