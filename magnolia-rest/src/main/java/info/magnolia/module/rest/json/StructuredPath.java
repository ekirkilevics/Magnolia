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
package info.magnolia.module.rest.json;

import org.apache.commons.lang.StringUtils;

/**
 * Immutable representation of an absolute path, avoids code duplication and error prone use of StringUtils.
 * <p/>
 * Treats the empty string as the root node.
 */
public class StructuredPath {

    private static final String[] EMPTY_STRING_ARRAY = new String[]{};

    public static final StructuredPath ROOT = new StructuredPath(EMPTY_STRING_ARRAY, 0, 0);

    private final String[] segments;
    private final int startIndex;
    private final int endIndex;

    public StructuredPath(String path) {
        this(split(path));
    }

    public StructuredPath(StructuredPath base, String relative) {
        this(add(base.segments, base.startIndex, base.endIndex, split(relative)));
    }

    public StructuredPath(String base, String relative) {
        this(add(split(base), split(relative)));
    }

    private StructuredPath(String[] segments) {
        this(segments, 0, segments.length);
    }

    private StructuredPath(String[] segments, int startIndex, int endIndex) {
        this.segments = segments;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public boolean isRoot() {
        return startIndex == endIndex;
    }

    public String toString() {
        return path();
    }

    public int length() {
        return length(segments, startIndex, endIndex);
    }

    public int depth() {
        return endIndex - startIndex;
    }

    public String path() {
        return join(segments, startIndex, endIndex);
    }

    public StructuredPath parent() {
        if (isRoot())
            throw new IllegalStateException("Cannot return parent of root node");
        return new StructuredPath(segments, startIndex, endIndex - 1);
    }

    public String parentPath() {
        if (isRoot())
            throw new IllegalStateException("Cannot return parent path of root node");
        return join(segments, startIndex, endIndex - 1);
    }

    public StructuredPath appendSegment(String name) {
        if (name == null)
            throw new IllegalArgumentException("Segment to append must not be null");
        if (name.length() == 0)
            throw new IllegalArgumentException("Segment to append must not be empty");
        if (name.indexOf('/') != -1)
            throw new IllegalArgumentException("Segment to append must not contain a '/' character");
        return new StructuredPath(add(segments, startIndex, endIndex, name));
    }

    public StructuredPath appendPath(String path) {
        return new StructuredPath(this, path);
    }

    public StructuredPath relativeTo(StructuredPath absolute) {

        // They must have the same depth
        if (absolute.depth() > depth())
            throw new IllegalArgumentException("");

        // They must be equal down to where absolute ends
        if (!equals(segments, startIndex, startIndex + absolute.depth(), absolute.segments, absolute.startIndex))
            throw new IllegalArgumentException("");

        if (absolute.depth() == depth())
            return ROOT;

        return new StructuredPath(segments, startIndex + absolute.depth(), endIndex);
    }

    /**
     * @return the name of the last segment
     */
    public String name() {
        if (isRoot())
            return "/";
        return segments[endIndex - 1];
    }

    private static String[] split(String path) {
        if (path == null || path.length() == 0)
            return EMPTY_STRING_ARRAY;
        if (path.indexOf('/') == -1)
            return new String[]{path};
        return StringUtils.split(path, '/');
    }

    private static String[] add(String[] array, int startIndex, int endIndex, String element) {
        String[] newArray = new String[(endIndex - startIndex) + 1];
        System.arraycopy(array, startIndex, newArray, 0, endIndex - startIndex);
        newArray[endIndex] = element;
        return newArray;
    }

    private static String[] add(String[] first, String[] second) {
        return add(first, 0, first.length, second);
    }

    private static String[] add(String[] first, int startIndex, int endIndex, String[] second) {
        String[] newArray = new String[(endIndex - startIndex) + second.length];
        System.arraycopy(first, startIndex, newArray, 0, (endIndex - startIndex));
        System.arraycopy(second, 0, newArray, (endIndex - startIndex), second.length);
        return newArray;
    }

    private static int length(String[] array, int startIndex, int endIndex) {
        if (startIndex == endIndex)
            return 1;
        int n = endIndex - startIndex;
        for (int index = startIndex; index < endIndex; index++)
            n += array[index].length();
        return n;
    }

    private static String join(String[] array, int startIndex, int endIndex) {
        if (startIndex == endIndex)
            return "/";
        int pos = 0;
        char[] chars = new char[length(array, startIndex, endIndex)];
        for (int index = startIndex; index < endIndex; index++) {
            chars[pos++] = '/';
            String str = array[index];
            str.getChars(0, str.length(), chars, pos);
            pos += str.length();
        }
        return new String(chars);
    }

    private static boolean equals(String[] array, int startIndex, int endIndex, String[] second, int secondStartIndex) {
        for (int index = startIndex; index < endIndex; index++) {
            if (!array[index].equals(second[secondStartIndex++]))
                return false;
        }
        return true;
    }
}
