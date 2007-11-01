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

import info.magnolia.cms.beans.config.VirtualURIManager;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.MgnlContext;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handle redirects configured using VirtualURIMappings.
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class VirtualUriFilter extends OncePerRequestAbstractMgnlFilter {

    private static final Logger log = LoggerFactory.getLogger(VirtualUriFilter.class);

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        final AggregationState aggregationState = MgnlContext.getAggregationState();
        String targetUri = getURIMapping(aggregationState.getCurrentURI());

        if (StringUtils.isNotEmpty(targetUri)) {
            if (!response.isCommitted()) {

                if (targetUri.startsWith("redirect:")) {
                    try {
                        response.sendRedirect(request.getContextPath()
                            + StringUtils.substringAfter(targetUri, "redirect:"));
                        return;
                    }
                    catch (IOException e) {
                        log.error("Failed to redirect to {}:{}", targetUri, e.getMessage());
                    }

                }
                // else if (targetUri.startsWith("forward:"))
                else {
                    targetUri = StringUtils.substringAfter(targetUri, "forward:");
                    try {
                        request.getRequestDispatcher(targetUri).forward(request, response);
                        return;
                    }
                    catch (Exception e) {
                        log.error("Failed to forward to {} - {}:{}", new Object[]{
                            targetUri,
                            ClassUtils.getShortClassName(e.getClass()),
                            e.getMessage()});
                    }

                }
                // else
                // {
                // aggregationState.setCurrentURI(targetUri);
                // ((MgnlFilterChain) chain).reset();
                // }
            }
            else {
                log.warn(
                    "Response is already committed, cannot forward to {} (original URI was {})",
                    targetUri,
                    request.getRequestURI());
            }

        }

        chain.doFilter(request, response);
    }

    /**
     * @return URI mapping as in ServerInfo
     */
    protected String getURIMapping(String currentURI) {
        return VirtualURIManager.getInstance().getURIMapping(currentURI);
    }

}