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

import java.util.regex.Pattern;


/**
 * Date: Jan 5, 2005 Time: 11:32:36 AM
 * @author Sameer Charles
 */
public interface Permission {

    /**
     * All possible permissions
     */
    long ADD = 1;

    long SET = 2;

    long REMOVE = 4;

    long READ = 8;

    long EXECUTE = 16;

    long SYNDICATE = 32;

    String PERMISSION_NAME_ADD = "Add";

    String PERMISSION_NAME_SET = "Set";

    String PERMISSION_NAME_REMOVE = "Remove";

    String PERMISSION_NAME_READ = "Read";

    String PERMISSION_NAME_EXECUTE = "Execute";

    String PERMISSION_NAME_SYNDICATE = "Syndicate";

    String PERMISSION_NAME_ALL = "(Add, Set, Read, Execute, Syndicate)";

    String PERMISSION_NAME_WRITE = "(Add, Set, Read)";

    /**
     * permissions used via admin central module
     */
    long ALL = ADD | REMOVE | SET | READ | EXECUTE | SYNDICATE;

    long WRITE = ADD | SET | READ;

    void setPattern(Pattern value);

    Pattern getPattern();

    void setPermissions(long value);

    long getPermissions();

    boolean match(String path);
}
