package info.magnolia.cms.cache;

import info.magnolia.context.MgnlContext;

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

    /**
     * Used for special subclasses not meant to be used in general
     */
    public CacheKey() {
    }

    /**
     * Contstuctor used if the key is already known
     */
    public CacheKey(String key) {
        this.key = key;
    }

    /**
     * override actual key value
     * */
    public void setValue(String key) {
        this.key = key;
    }

    /**
     * The constructor used by default
     * @param request the request from which we get the path
     *
     * TODO : we should probably use the AggregationState instead of the HttpServletRequest
     */
    public CacheKey(HttpServletRequest request) {
        key = MgnlContext.getAggregationState().getOriginalURI();
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
