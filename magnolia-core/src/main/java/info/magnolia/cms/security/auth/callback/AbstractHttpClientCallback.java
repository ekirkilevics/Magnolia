/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.cms.security.auth.callback;

import info.magnolia.cms.util.SimpleUrlPattern;
import info.magnolia.context.MgnlContext;
import info.magnolia.voting.Voter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A base class for {@link HttpClientCallback} implementations, providing a default set of configurable filters to
 * accept the request.
 *
 * $Id$
 */
public abstract class AbstractHttpClientCallback implements HttpClientCallback {

    // TODO use regexes ? Or both ?
    private SimpleUrlPattern originalUrlPattern;
    private SimpleUrlPattern urlPattern;
    private SimpleUrlPattern hostPattern;
    private Voter voters;

    @Override
    public boolean accepts(HttpServletRequest request) {
        boolean accepted = true;

        if (accepted && hostPattern != null) {
            accepted = hostPattern.match(request.getServerName());
        }

        if (accepted && originalUrlPattern != null) {
            accepted = originalUrlPattern.match(MgnlContext.getAggregationState().getOriginalURI());
        }

        if (accepted && urlPattern != null) {
            accepted = urlPattern.match(MgnlContext.getAggregationState().getCurrentURI());
        }

        if (accepted && voters != null) {
            accepted = voters.vote(request) > 0;
        }

        // TODO we could also accept based on site or whatever or other param
        //      * add a simple interface similar to PatternDelegate, without the getDelegate() method ?
        //      * on the other hand we already have Voters
        //        ** propose a revision of Voters ?

        return accepted;
    }

    /**
     * Override this method to provide the specific functionality.
     * This implementation is only kept for backwards compatibility with versions prior to 4.5, and merely delegates
     * the call to {@link #doCallback(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) {
        this.doCallback(request, response);
    }

    /**
     * Since 4.5, one can simply implement {@link #handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}
     * when subclassing AbstractHttpClientCallback.
     * @deprecated since 4.5
     */
    protected void doCallback(HttpServletRequest request, HttpServletResponse response) {
        throw new IllegalStateException("This method should not be called ! See Javadoc.");
    }

    // ------- configuration methods

    protected SimpleUrlPattern getOriginalUrlPattern() {
        return originalUrlPattern;
    }

    public void setOriginalUrlPattern(SimpleUrlPattern originalUrlPattern) {
        this.originalUrlPattern = originalUrlPattern;
    }

    protected SimpleUrlPattern getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(SimpleUrlPattern urlPattern) {
        this.urlPattern = urlPattern;
    }

    protected SimpleUrlPattern getHostPattern() {
        return hostPattern;
    }

    public void setHostPattern(SimpleUrlPattern hostPattern) {
        this.hostPattern = hostPattern;
    }

    public Voter getVoters() {
        return voters;
    }

    public void setVoters(Voter voters) {
        this.voters = voters;
    }
}
