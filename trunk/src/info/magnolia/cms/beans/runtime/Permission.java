/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 * */




package info.magnolia.cms.beans.runtime;


import java.util.regex.Pattern;


/**
 * User: sameercharles
 * Date: Aug 14, 2003
 * Time: 2:27:08 PM
 * @author Sameer Charles
 * @version 1.1
 */



public class Permission {


    private Pattern pattern;
    private long permissions;



    public static final long ALL_PERMISSIONS = javax.jcr.access.Permission.ADD_NODE 
            | javax.jcr.access.Permission.REMOVE_ITEM
            | javax.jcr.access.Permission.SET_PROPERTY
            | javax.jcr.access.Permission.READ_ITEM;

    public static final long READ_PERMISSION = javax.jcr.access.Permission.READ_ITEM;



    public void setPattern(Pattern value) {
        this.pattern = value;
    }


    public Pattern getPattern() {
        return this.pattern;
    }


    public void setPermissions(long value) {
        this.permissions = value;
    }


    public long getPermissions() {
        return this.permissions;
    }


    public boolean match(String path) {
        return this.pattern.matcher(path).matches();
    }

}
