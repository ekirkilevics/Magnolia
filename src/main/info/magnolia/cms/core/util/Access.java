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



package info.magnolia.cms.core.util;

import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.PermissionImpl;

/**
 * Date: Dec 30, 2004
 * Time: 10:16:57 AM
 *
 * @author Sameer Charles
 * @version 2.01
 */



public class Access {


    public static void isGranted(AccessManager manager, String path, long permissions)
            throws AccessDeniedException {
        if (manager!=null && !manager.isGranted(path,permissions)) {
            throw new AccessDeniedException("not allowed to "
                    +PermissionImpl.getPermissionAsName(permissions)+" - "+path);
        }
    }


}
