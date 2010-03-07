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
    private static final long serialVersionUID = 223L;

    public static final String URL_CHAR_PATTERN = "[\\p{L}\\p{Mn}\\p{S}\\w!\"#$%&'*+,-./:; <=>?@`|~\\^\\(\\)\\{\\}\\[\\]]"; //$NON-NLS-1$

    /**
     * Regexp pattern used for the simple keyword <code>*</code>. Matches 0 or more characters.
     */
    public static final String MULTIPLE_CHAR_PATTERN = URL_CHAR_PATTERN + "*"; //$NON-NLS-1$

    /**
     * Regexp pattern used for the simple keyword <code>?</code>. Matches 0 or 1 character.
     */
    public static final String SINGLE_CHAR_PATTERN = URL_CHAR_PATTERN + "?"; //$NON-NLS-1$

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
                stringBuffer.append('(');
                stringBuffer.append(chars, last, i - last);
                stringBuffer.append(')');
                stringBuffer.append(MULTIPLE_CHAR_PATTERN);
                last = i + 1;
            }
            else if (c == '?') {
                stringBuffer.append('(');
                stringBuffer.append(chars, last, i - last);
                stringBuffer.append(')');
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
