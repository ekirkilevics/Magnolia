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

/**
 * @author philipp
 * @version $Revision:2558 $ ($Author:scharles $)
 */
public interface Role extends Serializable {

    public abstract String getName();

    public abstract void addPermission(String repository, String path, long permission);

    public abstract void removePermission(String repository, String path);

    public abstract void removePermission(String repository, String path, long permission);

}