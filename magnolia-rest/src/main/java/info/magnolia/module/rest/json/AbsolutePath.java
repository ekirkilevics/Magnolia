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
 *
 * Treats the empty string as the root node.
 */
public class AbsolutePath {

    private final String[] segments;
    private final int length;

    public AbsolutePath(String path) {
        this(StringUtils.split(path, "/"));
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
        return "/" + StringUtils.join(this.segments, "/", 0, this.length);
    }

    public AbsolutePath parent() {
        if (isRoot())
            throw new IllegalStateException("Cannot return parent of root node");
        return new AbsolutePath(this.segments, this.length - 1);
    }

    public String parentPath() {
        if (isRoot())
            throw new IllegalStateException("Cannot return parent path of root node");
        return "/" + StringUtils.join(this.segments, "/", 0, this.length - 1);
    }

    public AbsolutePath append(String name) {
        if (name.indexOf("/") != -1)
            throw new IllegalArgumentException("Name must not be contain a '/' character");
        String[] newSegments = new String[this.length + 1];
        System.arraycopy(this.segments, 0, newSegments, 0, this.length);
        newSegments[this.length] = name;
        return new AbsolutePath(newSegments);
    }

    /**
     *
     * @return the name of the last segment
     */
    public String name() {
        if (isRoot())
            return "/";
        return segments[length - 1];
    }
}
