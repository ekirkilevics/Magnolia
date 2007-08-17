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
package info.magnolia.module.admininterface.config;

import info.magnolia.cms.security.Permission;

public class PermissionConfiguration extends BaseConfiguration {
    private static final long PERMISSION_ALL = Permission.ALL;

    private static final long PERMISSION_READ = Permission.READ;

    private static final long PERMISSION_NO = 0;

    private int value;

    public int getValue() {
        return this.value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}