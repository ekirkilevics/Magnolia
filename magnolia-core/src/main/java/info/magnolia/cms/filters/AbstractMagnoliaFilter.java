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

import info.magnolia.cms.core.Path;
import info.magnolia.context.MgnlContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;


/**
 * @author philipp
 * @version $Id$
 */
public abstract class AbstractMagnoliaFilter implements MagnoliaFilter {

    private String name;

    private Map bypasses = new HashMap();

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    public abstract void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException;

    public boolean bypasses(HttpServletRequest request) {
        String requestURI = getCurrentURI(request);
        return bypasses(requestURI);
    }

    /**
     * In case there was no context set the request is used directly
     */
    protected String getCurrentURI(HttpServletRequest request) {
        if (!MgnlContext.hasInstance()) {
            return StringUtils.removeStart(request.getRequestURI(), request.getContextPath());
        }
        return Path.getURI();
    }

    protected boolean bypasses(String uri) {
        for (Iterator iter = bypasses.values().iterator(); iter.hasNext();) {
            Bypass bypass = (Bypass) iter.next();
            if (bypass.bypass(uri)) {
                return true;
            }
        }
        return false;
    }

    public void destroy() {
        // nothing to do here
    }

    public Map getBypasses() {
        return this.bypasses;
    }

    public void setBypasses(Map bypasses) {
        this.bypasses = bypasses;
    }

    public void addBypass(String name, Bypass bypass) {
        this.bypasses.put(name, bypass);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

}