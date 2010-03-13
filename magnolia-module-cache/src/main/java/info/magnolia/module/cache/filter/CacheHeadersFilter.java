/**
 * This file Copyright (c) 2008-2010 Magnolia International
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
package info.magnolia.module.cache.filter;

import info.magnolia.cms.filters.AbstractMgnlFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>
 * Filter that sets cache headers, allowing or dening cache at client-side. By default the filter adds the
 * "Cache-Control: public" and expire directives to resources so that everything can be cached by the browser. Setting
 * the <code>nocache</code> property to <code>true</code> has the opposite effect, forcing browsers to avoid
 * caching.
 * </p>
 * <p>
 * The following example shows how to configure the filter so that static resources (images, css, js) gets cached by the
 * browser, and deny cache for html pages.
 * </p>
 *
 * <pre>
 * + server
 *    + filters
 *      + ...
 *      + headers-cache
 *        - class                  info.magnolia.module.cache.filter.CacheHeadersFilter
 *        - expirationMinutes      1440 <em>(default)</em>
 *        + bypasses
 *          + extensions
 *            - class              info.magnolia.voting.voters.ExtensionVoter
 *            - allow              gif,jpg,png,swf,css,js
 *            - not                true
 *      + headers-nocache
 *        - class                  info.magnolia.module.cache.filter.CacheHeadersFilter
 *        - nocache                true
 *        + bypasses
 *          + extensions
 *            - class              info.magnolia.voting.voters.ExtensionVoter
 *            - allow              html
 *            - not                true
 * </pre>
 *
 * @author Fabrizio Giustina
 * @version $Id: $
 */
public class CacheHeadersFilter extends AbstractMgnlFilter {

    /**
     * Number of minutes this item must be kept in cache.
     */
    private long expirationMinutes = 1440;

    /**
     * Cache should be avoided for filtered items.
     */
    private boolean nocache;

    /**
     * Sets the expirationMinutes.
     * @param expirationMinutes the expirationMinutes to set
     */
    public void setExpirationMinutes(long expirationMinutes) {
        this.expirationMinutes = expirationMinutes;
    }

    /**
     * Sets the nocache.
     * @param nocache the nocache to set
     */
    public void setNocache(boolean nocache) {
        this.nocache = nocache;
    }

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (nocache) {
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0");
            response.setDateHeader("Expires", 0L);
        } else {
            response.setHeader("Pragma", "");
            response.setHeader("Cache-Control", "max-age=" + expirationMinutes * 60 + ", public");
            final long expiration = System.currentTimeMillis() + expirationMinutes * 60000;
            response.setDateHeader("Expires", expiration);
        }

        chain.doFilter(request, response);
    }
}
