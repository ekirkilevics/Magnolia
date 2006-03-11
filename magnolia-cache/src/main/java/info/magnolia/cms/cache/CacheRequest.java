package info.magnolia.cms.cache;

import info.magnolia.cms.core.Path;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;


/**
 * @author Andreas Brenk
 * @since 3.0
 */
public class CacheRequest implements Serializable {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private String authorization;

    private String extension;

    private boolean gzip;

    private String method;

    private int parameterCount;

    private String uri;

    /**
     * Srver url for the web application extracted from the request. Used when a cache domain is not configured. Root
     * webapp url in the form [scheme]://[server]:[port]/[context]
     */
    private String domain;

    public CacheRequest() {
        // do nothing
    }

    public CacheRequest(HttpServletRequest request) {
        this.uri = Path.getURI(request);
        this.extension = StringUtils.substringAfterLast(this.uri, ".");
        this.method = request.getMethod();
        this.gzip = StringUtils.contains(StringUtils.lowerCase(request.getHeader("Accept-Encoding")), "gzip");
        this.parameterCount = request.getParameterMap().size();
        this.authorization = request.getHeader("Authorization");
        this.domain = getAppURL(request);
    }

    /**
     * Returns the server url for the web application. Used when a cache domain is not configured.
     * @param request HttpServletRequest
     * @return the root webapp url [scheme]://[server]:[port]/[context]
     */
    private static String getAppURL(HttpServletRequest request) {
        StringBuffer url = new StringBuffer();
        int port = request.getServerPort();

        String scheme = request.getScheme();
        url.append(scheme);
        url.append("://"); // $NON-NLS-1$
        url.append(request.getServerName());
        if ((scheme.equals("http") && (port != 80 && port > 0)) // $NON-NLS-1$
            || (scheme.equals("https") && (port != 443))) { // $NON-NLS-1$
            url.append(':'); // $NON-NLS-1$
            url.append(port);
        }

        url.append(request.getContextPath());

        return url.toString();
    }

    /**
     * @todo add other properties
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof CacheRequest)) {
            return false;
        }

        return this.uri.equals(((CacheRequest) obj).uri);
    }

    public String getAuthorization() {
        return this.authorization;
    }

    public String getExtension() {
        return this.extension;
    }

    public String getMethod() {
        return this.method;
    }

    public int getParameterCount() {
        return this.parameterCount;
    }

    /**
     * Return the requested URI without context path information.
     */
    public String getURI() {
        return this.uri;
    }

    public int hashCode() {
        return this.uri.hashCode();
    }

    public String toString() {
        return this.uri;
    }

    public boolean useGZIP() {
        return this.gzip;
    }

    /**
     * Getter for <code>domain</code>.
     * @return Returns the domain.
     */
    public String getDomain() {
        return this.domain;
    }
}
