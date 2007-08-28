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
import info.magnolia.cms.filters.MagnoliaFilterChain;
import info.magnolia.cms.filters.OncePerRequestAbstractMagnoliaFilter;
import info.magnolia.context.AnonymousContext;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.context.WebContext;

import java.io.IOException;
import java.util.Iterator;

import javax.jcr.Session;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles
 * @author Fabrizio Giustina $Id$
 */
public class LogoutFilter extends OncePerRequestAbstractMagnoliaFilter {

    public static final String PARAMETER_LOGOUT = "mgnlLogout";

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(LogoutFilter.class);

    /**
     * Check if a request parameter PARAMETER_LOGOUT is set. If so logout user,  
     * unset the context and restart the filter chain.
     */
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        if (null != request.getParameter(PARAMETER_LOGOUT)) {
            Context ctx = MgnlContext.getInstance();
            if (ctx instanceof WebContext) {
                ((WebContext) ctx).logout();
            }
            MgnlContext.setInstance(null);

            if (chain instanceof MagnoliaFilterChain) {
                ((MagnoliaFilterChain) chain).reset();
            }
        }
        
        chain.doFilter(request, response);
    }
}
