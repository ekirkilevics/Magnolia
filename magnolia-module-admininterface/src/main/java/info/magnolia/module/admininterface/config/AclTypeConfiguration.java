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


public class AclTypeConfiguration extends BaseConfiguration {
    public static int TYPE_THIS = 1; // 01

    public static int TYPE_SUBS = 2; // 10

    public static int TYPE_ALL = TYPE_THIS | TYPE_SUBS; // 11 : subs and this

    private int type;

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

}