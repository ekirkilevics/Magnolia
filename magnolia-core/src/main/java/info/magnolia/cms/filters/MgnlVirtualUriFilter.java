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
import info.magnolia.cms.core.Path;

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
public class MgnlVirtualUriFilter extends AbstractMagnoliaFilter  {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(MgnlVirtualUriFilter.class);

    private static final int REDIRECT = 1;

    private static final int INCLUDE = 2;

    private static final int NO_ACTION = 3;

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException{
        // set original uri if not yet set
        Path.setURI(request.getRequestURI());

        int result = redirect(request, response);
        if (result == REDIRECT) {
            return;
        } else if (result == INCLUDE) {
            ((MagnoliaFilterChain)chain).reset();
        }
        chain.doFilter(request, response);
    }

    /**
     * Redirect based on the mapping in config/server/.node.xml
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @return <code>true</code> if request has been redirected, <code>false</code> otherwise
     */
    private int redirect(HttpServletRequest request, HttpServletResponse response) {

        String uri = this.getURIMap(request);

        if (StringUtils.isNotEmpty(uri)) {
            if (!response.isCommitted()) {

                if (uri.startsWith("redirect:")) {
                    try {
                        response.sendRedirect(request.getContextPath() + StringUtils.substringAfter(uri, "redirect:"));
                        return REDIRECT;
                    }
                    catch (IOException e) {
                        log.error("Failed to redirect to {}:{}", //$NON-NLS-1$
                            new Object[]{uri, e.getMessage()});
                    }
                } else if (uri.startsWith("forward:")) {
                    uri = StringUtils.substringAfter(uri, "forward:");
                    try {
                        request.getRequestDispatcher(uri).forward(request, response);
                        return REDIRECT;
                    } catch (Exception e) {
                        log.error("Failed to forward to {} - {}:{}", //$NON-NLS-1$
                            new Object[]{uri, ClassUtils.getShortClassName(e.getClass()), e.getMessage()});
                    }
                } else {
                    Path.setURI(uri);
                    return INCLUDE;
                }
            }
            else {
                log.warn("Response is already committed, cannot forward to {} (original URI was {})",//$NON-NLS-1$
                    uri,
                    request.getRequestURI());
            }

            //return true;
        }

        return NO_ACTION;
    }

    /**
     * @return URI mapping as in ServerInfo
     * @param request HttpServletRequest
     */
    private String getURIMap(HttpServletRequest request) {
        return VirtualURIManager.getInstance().getURIMapping(Path.getURI());
    }


}