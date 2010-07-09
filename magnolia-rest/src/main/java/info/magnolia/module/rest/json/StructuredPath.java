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
 * null and empty string is treated as root, missing leading slash is added, trailing slash is removed, multiple adjacent slashes are combined
 */
public final class StructuredPath {

    public static final StructuredPath ROOT = new StructuredPath("/");

    private final String string;

    public static StructuredPath valueOf(String path) {
        String s = cleanup(path);
        if (s.length() == 1)
            return ROOT;
        return new StructuredPath(s);
    }

    private StructuredPath(String string) {
        this.string = string;
    }

    public boolean isRoot() {
        return string.length() == 1;
    }

    public String toString() {
        return string;
    }

    public int length() {
        return string.length();
    }

    public int depth() {
        if (isRoot())
            return 0;
        return StringUtils.countMatches(string, "/");
    }

    public String path() {
        return string;
    }

    public StructuredPath parent() {
        if (isRoot())
            throw new IllegalStateException("Cannot return parent of root node");
        if (depth() == 1)
            return ROOT;
        return new StructuredPath(StringUtils.substringBeforeLast(string, "/"));
    }

    public String parentPath() {
        if (isRoot())
            throw new IllegalStateException("Cannot return parent path of root node");
        if (depth() == 1)
            return "/";
        return StringUtils.substringBeforeLast(string, "/");
    }

    public StructuredPath appendSegment(String name) {
        if (name == null)
            throw new IllegalArgumentException("Segment to append must not be null");
        if (name.length() == 0)
            throw new IllegalArgumentException("Segment to append must not be empty");
        if (name.indexOf('/') != -1)
            throw new IllegalArgumentException("Segment to append must not contain a '/' character");
        if (isRoot())
            return new StructuredPath("/" + name);
        return new StructuredPath(string + "/" + name);
    }

    public StructuredPath appendPath(String path) {
        if (isRoot())
            return valueOf(path);
        String relative = cleanup(path);
        if (relative.length() == 1)
            return this;
        return new StructuredPath(string + relative);
    }

    public StructuredPath append(StructuredPath path) {
        if (path.isRoot())
            return this;
        if (isRoot())
            return path;
        return new StructuredPath(string + path.string);
    }

    public StructuredPath relativeTo(StructuredPath base) {

        if (base.isRoot())
            return this;

        // The other instance must be shorter or equal
        if (base.length() > length())
            throw new IllegalArgumentException("");

        if (!string.startsWith(base.string))
            throw new IllegalArgumentException("");

        if (base.length() == length())
            return ROOT;

        return new StructuredPath(string.substring(base.length()));
    }

    /**
     * @return the name of the last segment
     */
    public String name() {
        return isRoot() ? "/" : StringUtils.substringAfterLast(string, "/");
    }

    public static String cleanup(String path) {
        if (path == null || path.length() == 0)
            return "/";
        if (path.indexOf('/') == -1)
            return "/" + path;

        // this can be optimized down to one char array creation and a subsequent array copy in String

        return "/" + StringUtils.join(StringUtils.split(path, '/'), '/');
    }
}
