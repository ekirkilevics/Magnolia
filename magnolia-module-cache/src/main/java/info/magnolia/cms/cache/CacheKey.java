package info.magnolia.cms.cache;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;


/**
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class CacheKey implements Serializable {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private String key;

    public CacheKey(HttpServletRequest request) {
        key = request.getRequestURI();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return key.equals(((CacheKey) obj).key);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return key.hashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        // just return the key (path), as it's used for the filepath in the simple implementation
        return key;
    }

}
