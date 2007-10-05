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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.context.MgnlContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PathNotFoundException;


/**
 * @author Sameer Charles $Id$
 */
public class MgnlGroupManager implements GroupManager {
    private static final Logger log = LoggerFactory.getLogger(MgnlGroupManager.class);

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
            log.error("can't create group [" + name + "]", e);
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
        } catch (PathNotFoundException e) {
            // this is not an error, once we have MAGNOLIA-1757 implemented we can change this.
            log.warn("can't find group [" + name + "] in magnolia");
            log.debug("can't find group [" + name + "] in magnolia", e);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Throwable e) {
            log.error("Exception while retrieving group", e);
        }
        return null;
    }

    /**
     * All groups
     */
    public Collection getAllGroups() {
        Collection groups = new ArrayList();
        try {
            Collection nodes = getHierarchyManager().getRoot().getChildren(ItemType.GROUP);
            for (Iterator iter = nodes.iterator(); iter.hasNext();) {
                groups.add(new MgnlGroup((Content) iter.next()));
            }
        }
        catch (Exception e) {
            log.error("can't find user");
        }
        return groups;
    }

    /**
     * return the groups HierarchyManager (through the system context)
     */
    protected HierarchyManager getHierarchyManager() {
        return MgnlContext.getSystemContext().getHierarchyManager(ContentRepository.USER_GROUPS);
    }
}
