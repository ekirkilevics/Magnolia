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
package info.magnolia.cms.gui.dialog;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.context.MgnlContext;


/**
 * @author Philipp Bracher
 * @version $Id$
 */
public class DialogUUIDLink extends DialogLink implements UUIDDialogControl {

    /**
     * The lazy bound uuid
     */
    private String path;

    /**
     * Convert the stored uuid to a path
     */
    public String getValue() {
        if (path == null) {
            String value = super.getValue();
            path = getPath(value);
        }
        return path;
    }

    /**
     * Convert the uuid to a path or return the uuid if the path can't get found.
     * @param uuid
     * @return the path
     */
    private String getPath(String uuid) {
        if(StringUtils.isNotEmpty(uuid)){
            HierarchyManager hm = MgnlContext.getHierarchyManager(this.getRepository());
            try {
                Content node = hm.getContentByUUID(uuid);
                return node.getHandle();
            }
            catch (Exception e) {
                // return the uuid
            }
            
        }
        return uuid;
    }

    /**
     * Get the configured repository
     */
    public String getRepository() {
        return getConfigValue("repository", ContentRepository.WEBSITE);
    }

}
