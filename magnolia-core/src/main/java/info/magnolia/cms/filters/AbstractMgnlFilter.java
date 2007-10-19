/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.cms.filters;

import info.magnolia.context.MgnlContext;
import info.magnolia.voting.Voter;
import info.magnolia.voting.Voting;

import java.io.IOException;

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

}