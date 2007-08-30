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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ContentUtil;
import org.apache.commons.lang.StringUtils;

import javax.jcr.RepositoryException;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class HierarchicalUserManager extends MgnlUserManager {

    protected Content createUserNode(String name) throws RepositoryException {
        final String parentPath = getParentPath(name);
        final Content parent = ContentUtil.createPath(getHierarchyManager(), parentPath, ItemType.FOLDER, false);
        return parent.createContent(name, ItemType.USER);
    }

    protected String getParentPath(String name) {
        final String lcName = name.toLowerCase();
        if (lcName.length() < 3) {
            return "/" + getRealmName();
        }
        return "/" + getRealmName() + "/" + lcName.charAt(0) + "/" + StringUtils.left(lcName, 2);
    }
}
