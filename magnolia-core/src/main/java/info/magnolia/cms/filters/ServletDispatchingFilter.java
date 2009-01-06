/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
import javax.servlet.ServletRequest;
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

    private static final Logger log = LoggerFactory.getLogger(ServletDispatchingFilter.class);

    private static final String METACHARACTERS = "([\\^\\(\\)\\{\\}\\[\\]*$+])";

    private String servletName;

    private String servletClass;

    private Collection mappings;

    private Map parameters;

    private String comment;

    private Servlet servlet;

    public ServletDispatchingFilter() {
        mappings = new LinkedList();
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
        return determineMatchingEnd(request) < 0 || super.bypasses(request);
    }

    /**
     * Determines the index of the first pathInfo character. If the uri does not match any mapping this method returns
     * -1.
     */
    protected int determineMatchingEnd(HttpServletRequest request) {
        final Matcher matcher = findMatcher(request);
        if (matcher == null) {
            return -1;
        } else {
            if (matcher.groupCount() > 0) {
                return matcher.end(1);
            } else {
                return matcher.end();
            }
        }
    }

    protected Matcher findMatcher(HttpServletRequest request) {
        final String uri = StringUtils.substringAfter(request.getRequestURI(), request.getContextPath());
        return findMatcher(uri);
    }

    protected Matcher findMatcher(String uri) {
        for (Iterator iter = mappings.iterator(); iter.hasNext();) {
            final Matcher matcher = ((Pattern) iter.next()).matcher(uri);

            if (matcher.find()) {
                return matcher;
            }
        }

        return null;
    }

    /**
     * Dispatches the request to the servlet if not already bypassed. The request is wrapped for properly setting the
     * pathInfo.
     */
    public void doFilter(final HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.debug("Dispatching to servlet {}", getServletClass());
        final Matcher matcher = findMatcher(request);
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

    public Collection getMappings() {
        return mappings;
    }

    public void setMappings(Collection mappings) {
        this.mappings = mappings;
    }

    /**
     * See SRV.11.2 Specification of Mappings in the Servlet Specification
     * for the syntax of mappings. Additionally, you can also use plain regular
     * expressions to map your servlets, by prefix the mapping by "regex:". (in
     * which case anything in the request url following the expression's match
     * will be the pathInfo - if your pattern ends with a $, extra pathInfo won't
     * match)
     */
    public void addMapping(final String mapping) {
        final String pattern;

        // we're building a Pattern with 3 groups: (1) servletPath (2) ignored (3) pathInfo

        if (isDefaultMapping(mapping)) {
            // the mapping is exactly '/*', the servlet path should be
            // an empty string and everything else should be the path info
            pattern = "^()(/)(" + SimpleUrlPattern.MULTIPLE_CHAR_PATTERN + ")";
        } else if (isPathMapping(mapping)) {
            // the pattern ends with /*, escape out metacharacters for
            // use in a regex, and replace the ending * with MULTIPLE_CHAR_PATTERN
            final String mappingWithoutSuffix = StringUtils.removeEnd(mapping, "/*");
            pattern = "^(" + escapeMetaCharacters(mappingWithoutSuffix) + ")(/)(" + SimpleUrlPattern.MULTIPLE_CHAR_PATTERN + ")";
        } else if (isExtensionMapping(mapping)) {
            // something like '*.jsp', everything should be the servlet path
            // and the path info should be null
            final String regexedMapping = StringUtils.replace(mapping, "*.", SimpleUrlPattern.MULTIPLE_CHAR_PATTERN + "\\.");
            pattern = "^(" + regexedMapping + ")$";
        } else if (isRegexpMapping(mapping)) {
            final String mappingWithoutPrefix = StringUtils.removeStart(mapping, "regex:");
            pattern = "^(" + mappingWithoutPrefix + ")($|/)(" + SimpleUrlPattern.MULTIPLE_CHAR_PATTERN + ")";
        } else {
            // just literal text, ensure metacharacters are escaped, and that only
            // the exact string is matched.
            pattern = "^(" + escapeMetaCharacters(mapping) + ")$";
        }
        log.debug("Adding new mapping for {}", mapping);

        mappings.add(Pattern.compile(pattern));
    }

    static String escapeMetaCharacters(String str) {
        return str.replaceAll(METACHARACTERS, "\\\\$1");
    }

    /**
     * This is order specific, this method should not be called until
     * after the isDefaultMapping() method else it will return true
     * for a default mapping.
     */
    private boolean isPathMapping(String mapping) {
        return mapping.startsWith("/") && mapping.endsWith("/*");
    }

    private boolean isExtensionMapping(String mapping) {
        return mapping.startsWith("*.");
    }

    private boolean isDefaultMapping(String mapping) {
        // TODO : default mapping per spec is "/" - do we really want to support this? is there a point ?
        return mapping.equals("/");
    }

    private boolean isRegexpMapping(String mapping) {
        return mapping.startsWith("regex:");
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

    private class WrappedRequest extends HttpServletRequestWrapper {

        private Matcher matcher;

        /**
         * This is set to true when the original request object passed
         * in is changed by the setRequest() method.  This can indicate
         * that a forward occurred and the values pulled from the
         * matcher should no longer be used.
         */
        private boolean requestReplaced = false;

        /**
         * The given Matcher should be built from a Pattern containing two groups:
         * (1) servletPath (2) ignored (3) pathInfo
         */
        public WrappedRequest(HttpServletRequest request, Matcher matcher) {
            super(request);
            this.matcher = matcher;
        }

        public String getPathInfo() {
            if (requestReplaced) {
                return super.getPathInfo();
            }
            if (matcher.groupCount() > 2) {
                String pathInfo = matcher.group(3);
                if (pathInfo.equals("")) {
                    return null;
                }
                // according to the servlet spec the pathInfo should contain a leading slash
                return (pathInfo.startsWith("/") ? pathInfo : "/" + pathInfo);
            }
            return null;
        }

        public String getServletPath() {
            if (requestReplaced) {
                return super.getServletPath();
            }
            return matcher.group(1);
        }

        public void setRequest(ServletRequest request) {
            requestReplaced = true;
            super.setRequest(request);
        }

    }
}
