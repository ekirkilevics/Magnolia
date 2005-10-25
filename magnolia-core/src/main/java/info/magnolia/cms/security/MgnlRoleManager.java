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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.HierarchyManager;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;


/**
 * Manages the users stored in magnolia itself.
 * @author philipp
 * @version $Revision$ ($Author$)
 */
public class MgnlRoleManager implements RoleManager {

    public static Logger log = Logger.getLogger(MgnlRoleManager.class);

    /**
     * Do not instantiate it!
     */
    protected MgnlRoleManager() {
    }

    public Role getRole(String name, HttpServletRequest request) throws UnsupportedOperationException {
        HierarchyManager hm = SessionAccessControl.getHierarchyManager(request, ContentRepository.USER_ROLES);
        return getRole(name, hm);
    }

    public Role getRole(String name) throws UnsupportedOperationException {
        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.USER_ROLES);
        return getRole(name, hm);
    }
    
    /**
     * @param name
     * @param hm
     * @return
     */
    private Role getRole(String name, HierarchyManager hm) {
        try {
            return new MgnlRole(hm.getContent(name));
        }
        catch (Exception e) {
            log.info("can't find role [" + name + "]", e);
            return null;
        }
    }
}
