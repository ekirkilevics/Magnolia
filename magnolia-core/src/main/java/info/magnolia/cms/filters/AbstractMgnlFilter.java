/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
package info.magnolia.cms.filters;

import info.magnolia.voting.Voter;
import info.magnolia.voting.Voting;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;


/**
 * A magnolia filter configured in the config repository. This filter is bypassable.
 * @author philipp
 * @version $Id$
 */
public abstract class AbstractMgnlFilter implements MgnlFilter {

    private String name;

    private Voter[] bypasses = new Voter[0];

    private boolean enabled = true;

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    public abstract void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException;

    public boolean bypasses(HttpServletRequest request) {
        if(!isEnabled()){
            return true;
        }
        Voting voting = Voting.Factory.getDefaultVoting();
        return voting.vote(bypasses, request) > 0;
    }

    public void destroy() {
        // nothing to do here
    }

    public Voter[] getBypasses() {
        return this.bypasses;
    }

    public void addBypass(Voter voter) {
        this.bypasses = (Voter[]) ArrayUtils.add(this.bypasses, voter);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public boolean isEnabled() {
        return this.enabled;
    }


    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    //---- utility methods -----
    protected boolean acceptsGzipEncoding(HttpServletRequest request) {
        return acceptsEncoding(request, "gzip");
    }

    protected boolean acceptsEncoding(final HttpServletRequest request, final String name) {
        return headerContains(request, "Accept-Encoding", name);
    }

    protected boolean headerContains(final HttpServletRequest request, final String header, final String value) {
        final Enumeration accepted = request.getHeaders(header);
        while (accepted.hasMoreElements()) {
            final String headerValue = (String) accepted.nextElement();
            if (headerValue.indexOf(value) != -1) {
                return true;
            }
        }
        return false;
    }

    protected void addAndVerifyHeader(HttpServletResponse response, String name, String value) {
        response.addHeader(name, value);
        final boolean containsHeader = response.containsHeader(name);
        if (!containsHeader) {
            throw new IllegalStateException("Failure when attempting to set response header " + name + ": " + value);
        }
    }
}
