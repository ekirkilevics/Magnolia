/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.security;

import java.io.Serializable;

import info.magnolia.cms.util.UrlPattern;


/**
 * Date: Jan 5, 2005 Time: 11:32:36 AM
 * @author Sameer Charles
 */
public interface Permission extends Serializable {

    /**
     * All possible permissions
     */
    long NONE = 0;

    long ADD = 1;

    long SET = 2;

    long REMOVE = 4;

    long READ = 8;

    long EXECUTE = 16;

    long SYNDICATE = 32;

    // permission names are not localized, they are never displayed in the GUI but only used for exception messages

    String PERMISSION_NAME_ADD = "Add"; //$NON-NLS-1$

    String PERMISSION_NAME_SET = "Set"; //$NON-NLS-1$

    String PERMISSION_NAME_REMOVE = "Remove"; //$NON-NLS-1$

    String PERMISSION_NAME_READ = "Read"; //$NON-NLS-1$

    String PERMISSION_NAME_EXECUTE = "Execute"; //$NON-NLS-1$

    String PERMISSION_NAME_SYNDICATE = "Syndicate"; //$NON-NLS-1$

    String PERMISSION_NAME_ALL = "(Add, Set, Read, Execute, Syndicate)"; //$NON-NLS-1$

    String PERMISSION_NAME_WRITE = "(Add, Set, Read)"; //$NON-NLS-1$

    /**
     * permissions used via admin central module
     */
    long ALL = ADD | REMOVE | SET | READ | EXECUTE | SYNDICATE;

    long WRITE = ADD | SET | READ;

    void setPattern(UrlPattern value);

    UrlPattern getPattern();

    void setPermissions(long value);

    long getPermissions();

    boolean match(String path);
}
