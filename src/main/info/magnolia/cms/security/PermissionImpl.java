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
package info.magnolia.cms.security;

import info.magnolia.cms.util.UrlPattern;

import java.util.Hashtable;
import java.util.Map;


/**
 * Date: Jan 4, 2005 Time: 11:35:22 AM
 * @author Sameer Charles
 */
public class PermissionImpl implements Permission {

    private static Map nameStrings = new Hashtable();
    static {
        nameStrings.put(new Long(Permission.ADD), Permission.PERMISSION_NAME_ADD);
        nameStrings.put(new Long(Permission.SET), Permission.PERMISSION_NAME_SET);
        nameStrings.put(new Long(Permission.REMOVE), Permission.PERMISSION_NAME_REMOVE);
        nameStrings.put(new Long(Permission.READ), Permission.PERMISSION_NAME_READ);
        nameStrings.put(new Long(Permission.EXECUTE), Permission.PERMISSION_NAME_EXECUTE);
        nameStrings.put(new Long(Permission.SYNDICATE), Permission.PERMISSION_NAME_SYNDICATE);
        nameStrings.put(new Long(Permission.ALL), Permission.PERMISSION_NAME_ALL);
        nameStrings.put(new Long(Permission.WRITE), Permission.PERMISSION_NAME_WRITE);
    }

    private UrlPattern pattern;

    private long permissions;

    public void setPattern(UrlPattern value) {
        this.pattern = value;
    }

    public UrlPattern getPattern() {
        return this.pattern;
    }

    public void setPermissions(long value) {
        this.permissions = value;
    }

    public long getPermissions() {
        return this.permissions;
    }

    public boolean match(String path) {
        return this.pattern.match(path);
    }

    public static String getPermissionAsName(long permission) {
        return (String) nameStrings.get(new Long(permission));
    }
}
