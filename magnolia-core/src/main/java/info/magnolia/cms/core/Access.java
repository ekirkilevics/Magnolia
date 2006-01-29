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
package info.magnolia.cms.core;

import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.PermissionImpl;

import java.text.MessageFormat;


/**
 * @author Sameer Charles
 * @version $Revision $ ($Author $)
 */
public final class Access {

    /**
     * Utility class, don't instantiate.
     */
    private Access() {
        // unused
    }

    /**
     * Checks if the accessmanager allows specified permission on the given path
     * @param manager accessmanager
     * @param path which needs to be tested using given accessmanager
     * @param permissions
     */
    public static void isGranted(AccessManager manager, String path, long permissions) throws AccessDeniedException {
        if (manager != null && !manager.isGranted(path, permissions)) {
            throw new AccessDeniedException(MessageFormat.format("User not allowed to {0} path [{1}]", //$NON-NLS-1$
                new Object[]{PermissionImpl.getPermissionAsName(permissions), path}));
        }
    }
}
