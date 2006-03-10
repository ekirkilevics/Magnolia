package info.magnolia.cms.cache;

import info.magnolia.cms.core.Path;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;


/**
 * @author Andreas Brenk
 * @since 06.02.2006
 */
public class CacheRequest implements Serializable {

    // ~ Instance fields
    // --------------------------------------------------------------------------------------------------------------------

    private String authorization;

    private String extension;

    private boolean gzip;

    private String method;

    private int parameterCount;

    private String uri;

    // ~ Constructors
    // -----------------------------------------------------------------------------------------------------------------------

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
    }

    // ~ Methods
    // ----------------------------------------------------------------------------------------------------------------------------

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
}
