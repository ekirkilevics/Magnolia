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


/**
 * Virtual uri mapping implementation that uses regular expressions in fromURI/toURI. When using regular expression in
 * <code>fromURI</code>, <code>toURI</code> can contain references to the regexp matches. For example:
 *
 * <pre>
 * fromURI=/products/([0-9A-Z]+)\.html
 * toURI=/product/detail.html?productId=$1
 * </pre>
 *
 * @author Fabrizio Giustina
 * @author philipp
 * @version $Id: DefaultVirtualURIMapping.java 10295 2007-08-02 21:33:58Z fgiust $
 */
public class RegexpVirtualURIMapping implements VirtualURIMapping {

    private String fromURI;

    private String toURI;

    private Pattern regexp;

    public MappingResult mapURI(String uri) {
        MappingResult r = new MappingResult();

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

        return r;
    }

    public String getFromURI() {
        return this.fromURI;
    }

    public void setFromURI(String fromURI) {
        this.fromURI = fromURI;

        this.regexp = Pattern.compile(fromURI);
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
