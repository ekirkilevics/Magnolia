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
package info.magnolia.cms.util;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.security.AccessDeniedException;

import java.util.Iterator;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;


/**
 * Some easy to use methods to handle with Content objects.
 * @author philipp
 */
public class ContentUtil {

    /**
     * If the node doesn't exist just create it.
     * @param node
     * @param name
     * @param contentType
     * @return
     * @throws AccessDeniedException
     * @throws RepositoryException
     */
    public static Content getOrCreateContent(Content node, String name, ItemType contentType)
        throws AccessDeniedException, RepositoryException {
        Content res = null;
        try {
            res = node.getContent(name);
        }
        catch (PathNotFoundException e) {
            res = node.createContent(name, contentType);
        }
        return res;
    }

    /**
     * Get a subnode case insensitive. It ignores the type of the subnode.
     * @param node
     * @param name
     * @return the node or null if not found.
     */
    public static Content getCaseInsensitive(Content node, String name) {
        Content res = null;
        res = getCaseInsensitive(node, name, ItemType.CONTENT);
        if (res == null) {
            res = getCaseInsensitive(node, name, ItemType.CONTENTNODE);
        }
        return res;
    }

    /**
     * Get a subnode case insensitive.
     * @param node
     * @param name
     * @param type
     * @return
     */
    public static Content getCaseInsensitive(Content node, String name, ItemType type) {
        name = name.toLowerCase();
        for (Iterator iter = node.getChildren(type).iterator(); iter.hasNext();) {
            Content child = (Content) iter.next();
            if (child.getName().toLowerCase().equals(name)) {
                return child;
            }
        }
        return null;
    }
}