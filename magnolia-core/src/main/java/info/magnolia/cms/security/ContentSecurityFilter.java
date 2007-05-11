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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import info.magnolia.cms.core.Aggregator;
import info.magnolia.cms.core.Access;
import info.magnolia.context.MgnlContext;

/**
 * @author Sameer Charles
 * $Id$
 */
public class ContentSecurityFilter extends BaseSecurityFilter {

    private static final Logger log = LoggerFactory.getLogger(ContentSecurityFilter.class);

    public boolean isAllowed(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String repositoryName = Aggregator.getRepository();
        AccessManager accessManager = MgnlContext.getAccessManager(repositoryName);
        return isAuthorized(accessManager);
    }

    /**
     * check for read permissions of the aggregated handle
     * */
    protected boolean isAuthorized(AccessManager accessManager) {
        if (null == accessManager) return false;
        try {
            Access.isGranted(accessManager, Aggregator.getHandle(), Permission.READ);
            return true;
        } catch (AccessDeniedException ade) {
            log.debug(ade.getMessage(), ade);
        }
        return false;
    }


}
