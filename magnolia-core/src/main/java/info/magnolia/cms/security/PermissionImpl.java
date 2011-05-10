/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.cms.security;

import info.magnolia.cms.util.UrlPattern;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;


/**
 * Concrete implementation of {@link Permission} using {@link UrlPattern} to match pathes.
 * @author Sameer Charles
 */
public class PermissionImpl implements Permission, Serializable {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private static Map<Long, String> nameStrings = new Hashtable<Long, String>();

    static {
        nameStrings.put(Long.valueOf(0), "none");
        nameStrings.put(Long.valueOf(Permission.ADD), Permission.PERMISSION_NAME_ADD);
        nameStrings.put(Long.valueOf(Permission.SET), Permission.PERMISSION_NAME_SET);
        nameStrings.put(Long.valueOf(Permission.REMOVE), Permission.PERMISSION_NAME_REMOVE);
        nameStrings.put(Long.valueOf(Permission.READ), Permission.PERMISSION_NAME_READ);
        nameStrings.put(Long.valueOf(Permission.EXECUTE), Permission.PERMISSION_NAME_EXECUTE);
        nameStrings.put(Long.valueOf(Permission.SYNDICATE), Permission.PERMISSION_NAME_SYNDICATE);
        nameStrings.put(Long.valueOf(Permission.ALL), Permission.PERMISSION_NAME_ALL);
        nameStrings.put(Long.valueOf(Permission.WRITE), Permission.PERMISSION_NAME_WRITE);
    }

    private UrlPattern pattern;

    private long permissions;

    @Override
    public void setPattern(UrlPattern value) {
        this.pattern = value;
    }

    @Override
    public UrlPattern getPattern() {
        return this.pattern;
    }

    @Override
    public void setPermissions(long value) {
        this.permissions = value;
    }

    @Override
    public long getPermissions() {
        return this.permissions;
    }

    @Override
    public boolean match(String path) {
        return this.pattern.match(path);
    }

    @Override
    public String toString() {
        return getPermissionAsName(permissions) + " " + pattern;
    }

    public static String getPermissionAsName(long permission) {
        final String name = nameStrings.get(Long.valueOf(permission));
        if (name == null) {
            return "[unknown permission]";
        }
        return name;
    }
}
