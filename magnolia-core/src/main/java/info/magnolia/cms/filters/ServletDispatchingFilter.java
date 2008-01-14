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

import info.magnolia.cms.util.ClassUtil;
import info.magnolia.cms.util.SimpleUrlPattern;

import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author vsteller
 * @version $Id$
 */
public class ServletDispatchingFilter extends AbstractMgnlFilter {

    private static final Logger log = LoggerFactory.getLogger(ServletDispatchingFilter.class);

    private String servletName;

    private String servletClass;

    private Collection mappings;

    private Map parameters;

    private String comment;

    private Servlet servlet;

    public ServletDispatchingFilter() {
        mappings = new LinkedList();
    }

    /**
     * Initializes the servlet and its mappings. ServletConfig is wrapped to take init parameters into account.
     */
    public void init(final FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);

        if (servletClass != null) {
            try {
                servlet = (Servlet) ClassUtil.newInstance(servletClass);
                servlet.init(new WrappedServletConfig(servletName, filterConfig, parameters));
            }
            catch (Throwable e) {
                log.error("Unable to load servlet " + servletClass + " : " + e.getMessage(), e);
            }
        }
    }

    /**
     * Bypasses if the current request does not match any of the mappings of the servlet. Explicit bypasses defined in
     * the bypasses content node of this filter are taken into account as well.
     */
    public boolean bypasses(HttpServletRequest request) {
        final String uri = StringUtils.substringAfter(request.getRequestURI(), request.getContextPath());
        return determineMatchingEnd(uri) < 0 || super.bypasses(request);
    }

    /**
     * Determines the index of the first pathInfo character. If the uri does not match any mapping this method returns
     * -1.
     */
    protected int determineMatchingEnd(String uri) {
        for (Iterator iter = mappings.iterator(); iter.hasNext();) {
            final Matcher matcher = ((Pattern) iter.next()).matcher(uri);

            if (matcher.find()) {
                if (matcher.groupCount() > 0) {
                    return matcher.end(1);
                } else {
                    return matcher.end();
                }
            }
        }

        return -1;
    }

    /**
     * Dispatches the request to the servlet if not already bypassed. The request is wrapped for properly setting the
     * pathInfo.
     */
    public void doFilter(final HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        log.debug("Dispatching to servlet " + getServletClass());
        servlet.service(new HttpServletRequestWrapper(request) {

            public String getPathInfo() {
                final String uri = StringUtils.substringAfter(request.getRequestURI(), request.getContextPath());
                final String pathInfo = StringUtils.substring(uri, determineMatchingEnd(uri));

                // according to the servlet spec the pathInfo should contain a leading slash
                return (pathInfo.startsWith("/") ? pathInfo : "/" + pathInfo);
            }
        }, response);
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

    public Collection getMappings() {
        return mappings;
    }

    public void setMappings(Collection mappings) {
        this.mappings = mappings;
    }

    public void addMapping(String mapping) {
        if (isPathMapping(mapping)) {
            mapping = "^" + StringUtils.removeEnd(mapping, "*");
        } else if (isExtensionMapping(mapping)) {
            // TODO
            throw new UnsupportedOperationException("Extension mappings are currently not supported");
        } else if (isDefaultMapping(mapping)) {
            mapping = "^" + mapping + "*";
        } else {
            mapping = "^" + mapping + "$";
        }
        final String encodedString = SimpleUrlPattern.getEncodedString(mapping);

        mappings.add(Pattern.compile(encodedString));
    }

    private boolean isPathMapping(String mapping) {
        return mapping.startsWith("/") && mapping.endsWith("/*");
    }

    private boolean isExtensionMapping(String mapping) {
        return mapping.startsWith("*.");
    }
    
    private boolean isDefaultMapping(String mapping) {
        return mapping.equals("/");
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

    private final static class WrappedServletConfig implements ServletConfig {

        private final String servletName;

        private final FilterConfig filterConfig;

        private final Map parameters;

        public WrappedServletConfig(String servletName, FilterConfig filterConfig, Map parameters) {
            this.servletName = servletName;
            this.filterConfig = filterConfig;
            this.parameters = parameters;
        }

        public String getInitParameter(String name) {
            return (String) parameters.get(name);
        }

        public Enumeration getInitParameterNames() {
            return new Enumeration() {

                private Iterator iter = parameters.keySet().iterator();

                public boolean hasMoreElements() {
                    return iter.hasNext();
                }

                public Object nextElement() {
                    return iter.next();
                }
            };
        }

        public ServletContext getServletContext() {
            return filterConfig.getServletContext();
        }

        public String getServletName() {
            return servletName;
        }

    }
}
