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
package info.magnolia.cms.beans.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import info.magnolia.cms.util.SimpleUrlPattern;
import info.magnolia.cms.util.UrlPattern;


/**
 * Default virtual uri mapping. Uris can be fixed or regular expressions. By default fromURI and toURI are interpreted
 * as fixed strings, you can use regular expression by prefixing fromURI with <code>regexp:</code>. When using regexp
 * toURI can contain references to the regexp matches. For example:
 *
 * <pre>
 * fromURI=regexp:/products/([0-9A-Z]+)\.html
 * toURI=/product/detail.html?productId=$1
 * </pre>
 *
 * @author Fabrizio Giustina
 * @author philipp
 * @version $Id$
 */
public class DefaultVirtualURIMapping implements VirtualURIMapping {

    /**
     * Prefix for regexp virtual URIs.
     */
    private static final String REGEXP_PREFIX = "regexp:";

    private String fromURI;

    private UrlPattern pattern;

    private String toURI;

    private Pattern regexp;

    public MappingResult mapURI(String uri) {
        MappingResult r = new MappingResult();

        if (regexp != null) {
            Matcher matcher = regexp.matcher(uri);
            if (matcher.find()) {
                String replaced = toURI;
                int matcherCount = matcher.groupCount();
                for (int j = 0; j <= matcherCount; j++) {
                    // @todo of course we should improve this using a stringbuffer
                    replaced = StringUtils.replace(replaced, "$" + j, matcher.group(j));
                }

                r.setLevel(matcherCount + 1);
                r.setToURI(replaced);
            }
        }
        else if (pattern.match(uri)) {
            r.setLevel(pattern.getLength());
            r.setToURI(toURI);
        }
        return r;
    }

    public String getFromURI() {
        return this.fromURI;
    }

    public void setFromURI(String fromURI) {
        this.fromURI = fromURI;

        if (fromURI.startsWith(REGEXP_PREFIX)) {
            this.regexp = Pattern.compile(StringUtils.removeStart(fromURI, REGEXP_PREFIX));
        }
        else {
            this.pattern = new SimpleUrlPattern(fromURI);
        }
    }

    public String getToURI() {
        return this.toURI;
    }

    public void setToURI(String toURI) {
        this.toURI = toURI;
    }

    public String toString() {
        return fromURI + " --> " + toURI;
    }

}
