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
package info.magnolia.cms.filters;

import info.magnolia.cms.util.CustomServletConfig;
import info.magnolia.objectfactory.Classes;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A filter that dispatches requests to a wrapped servlet.
 *
 * TODO : cache matching URIs ?
 *
 * @author vsteller
 * @version $Id$
 */
public class ServletDispatchingFilter extends AbstractMgnlFilter {

    static final Logger log = LoggerFactory.getLogger(ServletDispatchingFilter.class);

    private String servletName;

    private String servletClass;

    private Map parameters;

    private String comment;

    private Servlet servlet;

    public ServletDispatchingFilter() {
    }

    public String getName() {
        return "Wrapper for " + servletName + " servlet";
    }

    /**
     * Initializes the servlet and its mappings. ServletConfig is wrapped to take init parameters into account.
     */
    public void init(final FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);

        if (servletClass != null) {
            try {
                servlet = Classes.newInstance(servletClass);
                servlet.init(new CustomServletConfig(servletName, filterConfig.getServletContext(), parameters));
            }
            catch (Throwable e) {
                log.error("Unable to load servlet " + servletClass + " : " + e.getMessage(), e);
            }
        }
    }

    /**
     * Delegates the destroy() call to the wrapper servlet, then to this filter itself.
     */
    public void destroy() {
        if (servlet != null) {
            servlet.destroy();
        }
        super.destroy();
    }

    /**
     * Dispatches the request to the servlet if not already bypassed. The request is wrapped for properly setting the
     * pathInfo.
     */
    public void doFilter(final HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.debug("Dispatching to servlet {}", getServletClass());
        final Matcher matcher = getMapping().match(request).getMatcher();
        servlet.service(new WrappedRequest(request, matcher), response);
    }

    public String getServletName() {
        return servletName;
    }

    public void setServletName(String servletName) {
        this.servletName = servletName;
    }

    public String getServletClass() {
        return servletClass;
    }

    public void setServletClass(String servletClass) {
        this.servletClass = servletClass;
    }

    public Map getParameters() {
        return parameters;
    }

    public void setParameters(Map parameters) {
        this.parameters = parameters;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Request wrapper that overrides servletPath and pathInfo with new values. If any of the path elements changes in
     * a wrapper behind it then it returns them instead of the overridden values. This happens on forwards. It's
     * necessary to check all four since they act as a group, if any of them changes we cannot override any of them.
     */
    private static class WrappedRequest extends HttpServletRequestWrapper {

        private String originalRequestUri;
        private String originalServletPath;
        private String originalPathInfo;
        private String originalQueryString;

        private String newServletPath;
        private String newPathInfo;

        /**
         * The given Matcher should be built from a Pattern containing two groups:
         * (1) servletPath (2) ignored (3) pathInfo.
         */
        public WrappedRequest(HttpServletRequest request, Matcher matcher) {
            super(request);

            this.originalRequestUri = request.getRequestURI();
            this.originalServletPath = request.getServletPath();
            this.originalPathInfo = request.getPathInfo();
            this.originalQueryString = request.getQueryString();

            this.newServletPath = matcher.group(1);
            if (matcher.groupCount() > 2) {
                String pathInfo = matcher.group(3);
                // pathInfo should be null when empty
                if (!pathInfo.equals("")) {
                    // according to the servlet spec the pathInfo should contain a leading slash
                    this.newPathInfo = (pathInfo.startsWith("/") ? pathInfo : "/" + pathInfo);
                }
            }
        }

        public String getPathInfo() {
            String current = super.getPathInfo();
            if (!StringUtils.equals(super.getRequestURI(), originalRequestUri))
                return current;
            if (!StringUtils.equals(super.getServletPath(), originalServletPath))
                return current;
            if (!StringUtils.equals(current, originalPathInfo))
                return current;
            if (!StringUtils.equals(super.getQueryString(), originalQueryString))
                return current;
            return newPathInfo;
        }

        public String getServletPath() {
            String current = super.getServletPath();
            if (!StringUtils.equals(super.getRequestURI(), originalRequestUri))
                return current;
            if (!StringUtils.equals(current, originalServletPath))
                return current;
            if (!StringUtils.equals(super.getPathInfo(), originalPathInfo))
                return current;
            if (!StringUtils.equals(super.getQueryString(), originalQueryString))
                return current;
            return newServletPath;
        }
    }
}
