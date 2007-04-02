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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.context.MgnlContext;
import info.magnolia.api.HierarchyManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles $Id$
 */
public class MgnlGroupManager implements GroupManager {

    public static Logger log = LoggerFactory.getLogger(MgnlRoleManager.class);

    /**
     * Create a group
     * @param name
     * @return newly created group
     * @throws UnsupportedOperationException if the implementation does not support writing
     * @throws AccessDeniedException if logged in repository user does not sufficient rights
     */
    public Group createGroup(String name) throws UnsupportedOperationException, AccessDeniedException {
        try {
            Content node = getHierarchyManager().createContent("/", name, ItemType.GROUP.getSystemName());
            getHierarchyManager().save();
            return new MgnlGroup(node);
        }
        catch (Exception e) {
            log.error("can't create role [" + name + "]", e);
            return null;
        }
    }

    /**
     * Get group by the given name
     * @param name
     * @return group
     * @throws UnsupportedOperationException if the implementation does not support writing
     * @throws AccessDeniedException if logged in repository user does not sufficient rights
     */
    public Group getGroup(String name) throws UnsupportedOperationException, AccessDeniedException {
        try {
            return new MgnlGroup(getHierarchyManager().getContent(name));
        }
        catch (Exception e) {
            log.info("can't find group [" + name + "]", e);
            return null;
        }
    }

    /**
     * return the role HierarchyManager
     */
    protected HierarchyManager getHierarchyManager() {
        return MgnlContext.getHierarchyManager(ContentRepository.USER_GROUPS);
    }
}
