/**
 * This file Copyright (c) 2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.cache.executor;

import info.magnolia.module.cache.BrowserCachePolicy;
import info.magnolia.module.cache.BrowserCachePolicyResult;
import info.magnolia.module.cache.Cache;
import info.magnolia.module.cache.CachePolicyResult;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.time.FastDateFormat;

/**
 * TODO : avoid duplication with CacheHeadersFilter ???
 * 
 * @author pbracher
 */
public class SetExpirationHeaders extends AbstractExecutor {
    private final FastDateFormat formatter = FastDateFormat.getInstance("EEE, d MMM yyyy HH:mm:ss zzz", TimeZone.getTimeZone("GMT"), Locale.ENGLISH);

    public void processCacheRequest(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain, Cache cache,
            CachePolicyResult cachePolicyResult) throws IOException, ServletException {

        BrowserCachePolicy browserCachePolicy = this.getCacheConfiguration().getBrowserCachePolicy();
        BrowserCachePolicyResult clientCacheResult = browserCachePolicy.canCacheOnClient(cachePolicyResult);

        if (clientCacheResult == BrowserCachePolicyResult.NO_CACHE) {
            response.setHeader("Pragma", "No-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setHeader("Expires", "Fri, 30 Oct 1998 14:19:41 GMT");
        } else {
            response.setHeader("Pragma", "");
            response.setHeader("Cache-Control", "max-age=" + clientCacheResult.getExpirationDate() / 1000 + ", public");
            // TODO : use setDateHeader ?
            response.setHeader("Expires", formatter.format(new Date(clientCacheResult.getExpirationDate())));
        }
    }
}
