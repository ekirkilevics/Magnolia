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
package info.magnolia.cms.filters;

import info.magnolia.context.AnonymousContext;
import info.magnolia.context.MgnlContext;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class initializes the current context.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class MgnlContextFilter extends AbstractMagnoliaFilter {

    private ServletContext servletContext;

    /**
     * Logger.
     */
    private Logger log = LoggerFactory.getLogger(MgnlContextFilter.class);

    public void init(FilterConfig filterConfig) throws ServletException {
        this.servletContext = filterConfig.getServletContext();
    }

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        // if the filter chain was reset, this filter could be called several time. Using this flag so that only the first call will unset the context (which should be the last post-filters operation)
        boolean contextSet = false;
        if (!MgnlContext.hasInstance()) {
            AnonymousContext ctx = new AnonymousContext();
            ctx.init(request, response, servletContext);
            MgnlContext.setInstance(ctx);
            contextSet = true;
        }
        try {
            chain.doFilter(request, response);
        }
        finally {
            if (contextSet) {
                MgnlContext.getInstance().release();
                MgnlContext.setInstance(null);
            }
        }
    }

}
