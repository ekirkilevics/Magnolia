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

import info.magnolia.cms.util.CustomServletConfig;
import info.magnolia.cms.util.SimpleUrlPattern;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.objectfactory.Classes;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        String uri = null;
        WebContext ctx = MgnlContext.getWebContextOrNull();
        if (ctx != null) {
            uri = ctx.getAggregationState().getCurrentURI();
        }
        if (uri == null) {
            // the web context is not available during installation
            uri = StringUtils.substringAfter(request.getRequestURI(), request.getContextPath());
        }
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
        if (matcher != null)
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
