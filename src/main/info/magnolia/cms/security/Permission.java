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


package info.magnolia.cms.security;

import java.util.regex.Pattern;


/**
 * Date: Jan 5, 2005
 * Time: 11:32:36 AM
 *
 * @author Sameer Charles
 */


public interface Permission {


    /**
     * All possible permissions
     *
     * */
    public static final long ADD = 1;
    public static final long SET = 2;
    public static final long REMOVE = 4;
    public static final long READ = 8;
    public static final long EXECUTE = 16;
    public static final long SYNDICATE = 32;



    public static final String PERMISSION_NAME_ADD = "Add";
    public static final String PERMISSION_NAME_SET = "Set";
    public static final String PERMISSION_NAME_REMOVE = "Remove";
    public static final String PERMISSION_NAME_READ = "Read";
    public static final String PERMISSION_NAME_EXECUTE = "Execute";
    public static final String PERMISSION_NAME_SYNDICATE = "Syndicate";
    public static final String PERMISSION_NAME_ALL = "(Add, Set, Read, Execute, Syndicate)";
    public static final String PERMISSION_NAME_WRITE = "(Add, Set, Read)";


    /**
     * permissions used via admin central module
     * */
    public static final long ALL = ADD
            | REMOVE
            | SET
            | READ
            | EXECUTE
            | SYNDICATE;


    public static final long WRITE = ADD
            | SET
            | READ;




    public void setPattern(Pattern value);


    public Pattern getPattern();


    public void setPermissions(long value);


    public long getPermissions();


    public boolean match(String path);


}
