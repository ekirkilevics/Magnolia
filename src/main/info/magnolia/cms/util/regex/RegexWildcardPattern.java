/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.util.regex;

/**
 * @author Sameer Charles
 */
public final class RegexWildcardPattern {

    private static final String MULTIPLE_CHAR_PATTERN = "[a-z[A-Z[0-9[!\"#$%&'()*+,-./:;<=>?@\\^_`{|}~]]]]*";

    private static final String SINGLE_CHAR_PATTERN = "[a-z[A-Z[0-9[!\"#$%&'()*+,-./:;<=>?@\\^_`{|}~]]]]?";

    /**
     * Utility class, don't instantiate.
     */
    private RegexWildcardPattern() {
        // unused
    }

    /**
     * <p>
     * wild card pattern including all printable characters
     * </p>
     */
    public static String getSingleCharPattern() {
        return SINGLE_CHAR_PATTERN;
    }

    /**
     * <p>
     * wild card pattern including all printable characters
     * </p>
     */
    public static String getMultipleCharPattern() {
        return MULTIPLE_CHAR_PATTERN;
    }

    /**
     * <p>
     * replace all "*" with <code>RegexWildcardPattern.MULTIPLE_CHAR_PATTERN</code>
     * </p>
     */
    public static String getEncodedString(String str) {
        StringBuffer stringBuffer = new StringBuffer();
        char[] chars = str.toCharArray();
        int i = 0, last = 0;
        while (i < chars.length) {
            char c = chars[i];
            if (c == '*') {
                stringBuffer.append(chars, last, i - last);
                stringBuffer.append(RegexWildcardPattern.getMultipleCharPattern());
                last = i + 1;
            }
            i++;
        }
        stringBuffer.append(chars, last, i - last);
        return stringBuffer.toString();
    }
}
