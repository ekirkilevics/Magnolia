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
public class AbsolutePath {

    private static final String[] EMPTY_STRING_ARRAY = new String[]{};

    public static final AbsolutePath ROOT = new AbsolutePath(EMPTY_STRING_ARRAY);

    private final String[] segments;
    private final int length;

    public AbsolutePath(String path) {
        this(splitSegments(path));
    }

    public AbsolutePath(AbsolutePath base, String relative) {
        this(add(base.segments, base.length, splitSegments(relative)));
    }

    public AbsolutePath(String base, String relative) {
        this(add(splitSegments(base), splitSegments(relative)));
    }

    private AbsolutePath(String[] segments) {
        this(segments, segments.length);
    }

    private AbsolutePath(String[] segments, int length) {
        this.segments = segments;
        this.length = length;
    }

    public boolean isRoot() {
        return this.length == 0;
    }

    public String toString() {
        return path();
    }

    public String path() {
        return "/" + StringUtils.join(this.segments, '/', 0, this.length);
    }

    public AbsolutePath parent() {
        if (isRoot())
            throw new IllegalStateException("Cannot return parent of root node");
        return new AbsolutePath(this.segments, this.length - 1);
    }

    public String parentPath() {
        if (isRoot())
            throw new IllegalStateException("Cannot return parent path of root node");
        return "/" + StringUtils.join(this.segments, '/', 0, this.length - 1);
    }

    public AbsolutePath appendSegment(String name) {
        if (name.indexOf("/") != -1)
            throw new IllegalArgumentException("Name must not be contain a '/' character");
        return new AbsolutePath(add(this.segments, this.length, name));
    }

    public AbsolutePath appendPath(String path) {
        return new AbsolutePath(add(this.segments, this.length, splitSegments(path)));
    }

    public AbsolutePath relativeTo(AbsolutePath absolute) {
        if (absolute.length > length)
            throw new IllegalArgumentException("");
        for (int i = 0; i < absolute.length; i++) {
            if (!segments[i].equals(absolute.segments[i]))
                throw new IllegalArgumentException("");
        }
        if (absolute.length == length)
            return ROOT;

        // if we had a start index in this class we could avoid creating a new array here and just return a new instance using the same array with an offset

        String[] newArray = new String[this.length - absolute.length];
        System.arraycopy(this.segments, absolute.length, newArray, 0, this.length - absolute.length);
        return new AbsolutePath(newArray);
    }

    /**
     * @return the name of the last segment
     */
    public String name() {
        if (isRoot())
            return "/";
        return segments[length - 1];
    }

    private static String[] splitSegments(String path) {
        if (StringUtils.isEmpty(path))
            return EMPTY_STRING_ARRAY;
        return StringUtils.split(path, '/');
    }

    private static String[] add(String[] array, int length, String name) {
        String[] newArray = new String[length + 1];
        System.arraycopy(array, 0, newArray, 0, length);
        newArray[length] = name;
        return newArray;
    }

    private static String[] add(String[] first, String[] second) {
        return add(first, first.length, second);
    }

    private static String[] add(String[] first, int length, String[] second) {
        String[] newArray = new String[length + second.length];
        System.arraycopy(first, 0, newArray, 0, length);
        System.arraycopy(second, 0, newArray, length, second.length);
        return newArray;
    }
}
