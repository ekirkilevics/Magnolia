/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.util;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;


/**
 * An implementation of URLPattern which matches strings using simple <code>*</code> or <code>?</code> wildcards.
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * @todo rewrite this class using ant-style path comparison and avoiding regexp. See
 * org.springframework.util.AntPathMatcher in spring 1.2 for a nice implementation
 * @version $Revision $ ($Author $)
 */
public final class SimpleUrlPattern implements UrlPattern {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Regexp pattern used for the simple keyword <code>*</code>
     */
    private static final String MULTIPLE_CHAR_PATTERN = "[\\p{L}[a-z[A-Z[0-9[!\"#$%&'()*+,-./:; <=>?@\\^_`{|}~\\[\\]]]]]]*"; //$NON-NLS-1$

    /**
     * Regexp pattern used for the simple keyword <code>?</code>
     */
    private static final String SINGLE_CHAR_PATTERN = "[\\p{L}[a-z[A-Z[0-9[!\"#$%&'()*+,-./:; <=>?@\\^_`{|}~\\[\\]]]]]]?"; //$NON-NLS-1$

    /**
     * Regexp pattern used in match().
     */
    private Pattern pattern;

    /**
     * Pattern length. Longer patterns have higher priority.
     */
    private int length;

    /**
     * internal pattern string.
     */
    private String patternString;

    /**
     * Compile a regexp pattern handling <code>*</code> and <code>?</code> chars.
     * @param string input string
     * @return a RegExp pattern
     */
    public SimpleUrlPattern(String string) {
        this.length = StringUtils.removeEnd(string, "*").length();
        this.pattern = Pattern.compile(getEncodedString(string));
        this.patternString = string;
    }

    /**
     * Replace all "*" with <code>RegexWildcardPattern.MULTIPLE_CHAR_PATTERN</code>.
     * @param str input string
     * @return string where all the occurrences of <code>*</code> and <code>?</code> are replaced with a regexp
     * pattern.
     */
    public static String getEncodedString(String str) {
        StringBuffer stringBuffer = new StringBuffer();
        char[] chars = str.toCharArray();
        int i = 0, last = 0;
        while (i < chars.length) {
            char c = chars[i];
            if (c == '*') {
                stringBuffer.append(chars, last, i - last);
                stringBuffer.append(MULTIPLE_CHAR_PATTERN);
                last = i + 1;
            }
            else if (c == '?') {
                stringBuffer.append(chars, last, i - last);
                stringBuffer.append(SINGLE_CHAR_PATTERN);
                last = i + 1;
            }
            i++;
        }
        stringBuffer.append(chars, last, i - last);
        return stringBuffer.toString();
    }

    /**
     * @see info.magnolia.cms.util.UrlPattern#match(java.lang.String)
     */
    public boolean match(String str) {
        return this.pattern.matcher(str).matches();
    }

    /**
     * @see info.magnolia.cms.util.UrlPattern#getLength()
     */
    public int getLength() {
        return this.length;
    }

    public String toString() {
        // don't use pattern.pattern(), but keep the original string.
        // The "compiled" pattern will display the ugly patterns like MULTIPLE_CHAR_PATTERN instead of simple *
        return "SimpleUrlPattern{" + patternString + '}';
    }
}
