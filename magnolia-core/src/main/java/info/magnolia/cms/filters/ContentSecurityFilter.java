/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.cms.filters;

import info.magnolia.cms.core.Access;
import info.magnolia.cms.core.Aggregator;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.context.MgnlContext;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class ContentSecurityFilter extends AbstractMagnoliaFilter{

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(ContentSecurityFilter.class);

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException{
        try {
            authorize();
        }
        catch (AccessDeniedException e) {
            // don't throw further, simply return error and break filter chain
            log.debug(e.getMessage(), e);
            if (!response.isCommitted())
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            // stop the chain
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * Uses access manager to authorize this request.
     * @param request HttpServletRequest as received by the service method
     * @throws AccessDeniedException if the given request is not authorized
     */
    protected void authorize() throws AccessDeniedException {
        AccessManager accessManager = MgnlContext.getAccessManager(Aggregator.getRepository());
        if (null != accessManager) {
            Access.isGranted(accessManager, Aggregator.getHandle(), Permission.READ);
        }
    }

}
