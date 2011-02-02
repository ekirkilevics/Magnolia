/**
 * This file Copyright (c) 2010-2011 Magnolia International
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

import info.magnolia.cms.util.SimpleUrlPattern;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A URI mapping as configured for filters and servlets.
 * @version $Id$
 *
 */
public class Mapping {

    private static Logger log = LoggerFactory.getLogger(Mapping.class);

    private static final String METACHARACTERS = "([\\^\\(\\)\\{\\}\\[\\]*$+])";

    protected static String escapeMetaCharacters(String str) {
        return str.replaceAll(METACHARACTERS, "\\\\$1");
    }

    protected Collection<Pattern> mappings = new ArrayList<Pattern>();

    public MatchingResult match(HttpServletRequest request) {
        Matcher matcher = findMatcher(request);
        boolean matches = matcher != null;
        int matchingEndPosition = determineMatchingEnd(matcher);
        return new MatchingResult(matcher, matches, matchingEndPosition);
    }

    /**
     * Determines the index of the first pathInfo character. If the uri does not match any mapping
     * this method returns -1.
     */
    private int determineMatchingEnd(Matcher matcher) {
        if (matcher == null) {
            return -1;
        }
        else {
            if (matcher.groupCount() > 0) {
                return matcher.end(1);
            }
            else {
                return matcher.end();
            }
        }
    }

    private Matcher findMatcher(HttpServletRequest request) {
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

    private Matcher findMatcher(String uri) {
        for (Iterator iter = mappings.iterator(); iter.hasNext();) {
            final Matcher matcher = ((Pattern) iter.next()).matcher(uri);

            if (matcher.find()) {
                return matcher;
            }
        }

        return null;
    }

    public Collection<Pattern> getMappings() {
        return mappings;
    }

    /**
     * See SRV.11.2 Specification of Mappings in the Servlet Specification for the syntax of
     * mappings. Additionally, you can also use plain regular expressions to map your servlets, by
     * prefix the mapping by "regex:". (in which case anything in the request url following the
     * expression's match will be the pathInfo - if your pattern ends with a $, extra pathInfo won't
     * match)
     */
    public void addMapping(final String mapping) {
        final String pattern;

        // we're building a Pattern with 3 groups: (1) servletPath (2) ignored (3) pathInfo

        if (isDefaultMapping(mapping)) {
            // the mapping is exactly '/*', the servlet path should be
            // an empty string and everything else should be the path info
            pattern = "^()(/)(" + SimpleUrlPattern.MULTIPLE_CHAR_PATTERN + ")";
        }
        else if (isPathMapping(mapping)) {
            // the pattern ends with /*, escape out metacharacters for
            // use in a regex, and replace the ending * with MULTIPLE_CHAR_PATTERN
            final String mappingWithoutSuffix = StringUtils.removeEnd(mapping, "/*");
            pattern = "^(" + escapeMetaCharacters(mappingWithoutSuffix) + ")(/)(" + SimpleUrlPattern.MULTIPLE_CHAR_PATTERN + ")";
        }
        else if (isExtensionMapping(mapping)) {
            // something like '*.jsp', everything should be the servlet path
            // and the path info should be null
            final String regexedMapping = StringUtils.replace(mapping, "*.", SimpleUrlPattern.MULTIPLE_CHAR_PATTERN + "\\.");
            pattern = "^(" + regexedMapping + ")$";
        }
        else if (isRegexpMapping(mapping)) {
            final String mappingWithoutPrefix = StringUtils.removeStart(mapping, "regex:");
            pattern = "^(" + mappingWithoutPrefix + ")($|/)(" + SimpleUrlPattern.MULTIPLE_CHAR_PATTERN + ")";
        }
        else {
            // just literal text, ensure metacharacters are escaped, and that only
            // the exact string is matched.
            pattern = "^(" + escapeMetaCharacters(mapping) + ")$";
        }
        log.debug("Adding new mapping for {}", mapping);

        mappings.add(Pattern.compile(pattern));
    }

    /**
     * This is order specific, this method should not be called until after the isDefaultMapping()
     * method else it will return true for a default mapping.
     */
    private boolean isPathMapping(String mapping) {
        return mapping.startsWith("/") && mapping.endsWith("/*");
    }

    private boolean isExtensionMapping(String mapping) {
        return mapping.startsWith("*.");
    }

    private boolean isDefaultMapping(String mapping) {
        // TODO : default mapping per spec is "/" - do we really want to support this? is there a
        // point ?
        return mapping.equals("/");
    }

    private boolean isRegexpMapping(String mapping) {
        return mapping.startsWith("regex:");
    }

    /**
     * Result of {@link ThemeReader} {@link Mapping#match(HttpServletRequest)} method.
     * @version $Id$
     */
    public static class MatchingResult {

        Matcher matcher;

        boolean matches;

        int matchingEndPosition;

        public MatchingResult(Matcher matcher, boolean matches, int matchingEndPosition) {
            this.matcher = matcher;
            this.matches = matches;
            this.matchingEndPosition = matchingEndPosition;
        }

        public Matcher getMatcher() {
            return matcher;
        }

        public boolean isMatching() {
            return matches;
        }

        public int getMatchingEndPosition() {
            return matchingEndPosition;
        }
    }

}
