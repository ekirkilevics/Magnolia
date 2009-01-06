/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.util;

import info.magnolia.context.MgnlContext;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


/**
 * An implementation of {@link PatternDelegate} that evaluates as condition a request uri patter or a hostname pattern.
 * @author fgiust
 * @version $Id$
 */
public class UrlPatternDelegate implements PatternDelegate {

    private String url;

    private SimpleUrlPattern urlPattern;

    private String host;

    private SimpleUrlPattern hostPattern;

    private Object delegate;

    /**
     * The configured Url
     * @return the configured Url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the Url pattern (using {@link SimpleUrlPattern} internally)
     * @param pattern url pattern
     */
    public void setUrl(String pattern) {
        this.url = pattern;
        this.urlPattern = new SimpleUrlPattern(pattern);
    }

    /**
     * The configured host
     * @return the configured host
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the host pattern (using {@link SimpleUrlPattern} internally)
     * @param host host pattern
     */
    public void setHost(String host) {
        this.host = host;
        this.hostPattern = new SimpleUrlPattern(host);
    }

    /**
     * Compares the reques with the url and host patterns
     * @param request HttpServletRequest
     * @return <code>true</code> if the pattern matches the configured host (if set) and url (if set)
     */
    public boolean match(HttpServletRequest request) {

        boolean match = true;

        if (hostPattern != null) {
            match = hostPattern.match(request.getServerName());
        }

        if (urlPattern != null) {
            match = urlPattern.match(MgnlContext.getAggregationState().getCurrentURI());
        }

        return match;
    }

    /**
     * Sets the delegate.
     * @param delegate the delegate to set
     */
    public void setDelegate(Object delegate) {
        this.delegate = delegate;
    }

    /**
     * Returns the delegate.
     * @return the delegate
     */
    public Object getDelegate() {
        return delegate;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("url", this.url).append(
            "host",
            this.host).toString();
    }

}