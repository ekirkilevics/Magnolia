/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.cms.filters;

import info.magnolia.cms.util.RequestHeaderUtil;
import info.magnolia.cms.util.ServletUtils;
import info.magnolia.objectfactory.Components;
import info.magnolia.voting.Voter;
import info.magnolia.voting.Voting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A magnolia filter configured in the config repository. This filter is bypassable.
 * @author philipp
 * @version $Id$
 */
public abstract class AbstractMgnlFilter implements MgnlFilter {

    private static Logger log =  LoggerFactory.getLogger(AbstractMgnlFilter.class);

    private String name;

    private Voter[] bypasses = new Voter[0];

    private boolean enabled = true;

    private DispatchRules dispatchRules = new DispatchRules();

    private Mapping mapping = new Mapping();

    private WebContainerResources webContainerResources = Components.getSingleton(WebContainerResources.class);

    protected AbstractMgnlFilter() {
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    public abstract void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException;

    public boolean matches(HttpServletRequest request) {
        return isEnabled() && matchesDispatching(request) && mapsTo(request) && !bypasses(request);
    }

    protected boolean mapsTo(HttpServletRequest request) {
        if(getMapping().getMappings().isEmpty()){
            return true;
        }
        return getMapping().match(request).isMatching();
    }

    protected boolean matchesDispatching(HttpServletRequest request) {
        if(webContainerResources == null){
            return true;
        }
        boolean toWebContainerResource = webContainerResources.isWebContainerResource(request);
        boolean toMagnoliaResource = !toWebContainerResource;

        DispatchRule dispatchRule = getDispatchRules().getDispatchRule(ServletUtils.getDispatcherType(request));
        if (toMagnoliaResource && dispatchRule.isToMagnoliaResources())
            return true;
        if (toWebContainerResource && dispatchRule.isToWebContainerResources())
            return true;
        return false;
    }

    protected boolean bypasses(HttpServletRequest request) {
        Voting voting = Voting.HIGHEST_LEVEL;
        if (voting.vote(bypasses, request) > 0)
            return true;

        return false;
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

    public DispatchRules getDispatchRules() {
        return dispatchRules;
    }

    public void setDispatchRules(DispatchRules dispatching) {
        this.dispatchRules = dispatching;
    }

    public Collection<String> getMappings() {
        ArrayList<String> result = new ArrayList<String>();
        for (Pattern pattern : getMapping().getMappings()) {
            result.add(pattern.pattern());
        }
        return result;
    }

    protected Mapping getMapping() {
        return mapping;
    }

    public void addMapping(String mapping){
        this.getMapping().addMapping(mapping);
    }

    //---- utility methods -----
    protected boolean acceptsGzipEncoding(HttpServletRequest request) {
        return RequestHeaderUtil.acceptsGzipEncoding(request);
    }

    protected boolean acceptsEncoding(final HttpServletRequest request, final String name) {
        return RequestHeaderUtil.acceptsEncoding(request, name);
    }

    protected boolean headerContains(final HttpServletRequest request, final String header, final String value) {
        return RequestHeaderUtil.headerContains(request, header, value);
    }

    protected void addAndVerifyHeader(HttpServletResponse response, String name, String value) {
        RequestHeaderUtil.addAndVerifyHeader(response, name, value);
    }



}
