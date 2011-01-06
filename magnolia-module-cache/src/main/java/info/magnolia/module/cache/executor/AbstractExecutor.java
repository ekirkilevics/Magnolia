/**
 * This file Copyright (c) 2008-2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.cache.executor;

import javax.servlet.http.HttpServletRequest;

import info.magnolia.module.cache.CacheConfiguration;
import info.magnolia.module.cache.CachePolicyExecutor;

/**
 * The cache configuration is passed to the executor.
 *
 * @author pbracher
 * @version $Revision$ ($Author$)
 */
public abstract class AbstractExecutor implements CachePolicyExecutor {

    private CacheConfiguration cacheConfiguration;

    public CacheConfiguration getCacheConfiguration() {
        return cacheConfiguration;
    }

    public void setCacheConfiguration(CacheConfiguration cacheConfiguration) {
        this.cacheConfiguration = cacheConfiguration;
    }

    /**
     * Checks if server cache is newer than the client cache.
     * @param request The servlet request we are processing
     * @return boolean true if the server resource is newer
     */
    protected boolean ifModifiedSince(HttpServletRequest request, long cacheStorageTimestamp) {
        try {
            long headerValue = request.getDateHeader("If-Modified-Since");
            if (headerValue != -1) {
                // If an If-None-Match header has been specified, If-Modified-Since is ignored.
                // The header defines only seconds, so we ignore the milliseconds.
                final long cacheStorageTimestampSeconds = cacheStorageTimestamp - (cacheStorageTimestamp % 1000);
                if (request.getHeader("If-None-Match") == null && cacheStorageTimestamp > 0 && (cacheStorageTimestampSeconds <= headerValue)) {
                    return false;
                }
            }
        } catch (IllegalArgumentException e) {
            // can happen per spec if the header value can't be converted to a date ...
            return true;
        }
        return true;
    }


}
