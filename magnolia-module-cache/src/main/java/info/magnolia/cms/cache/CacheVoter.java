/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2007 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.cache;

import javax.servlet.http.HttpServletRequest;


/**
 * Interface for pluggable cache voters.
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public interface CacheVoter {

    /**
     * If the implementation will return <code>false</code>, this resource will not be cached
     * @param request HttpServletRequest
     * @return <code>false</code> if the request must not be cached, <code>true</code> otherwise
     */
    boolean allowCaching(HttpServletRequest request);

    /**
     * Returns true if this cache voter is enabled.
     * @return <code>true</code> if this cache voter is enabled
     */
    boolean isEnabled();

}
