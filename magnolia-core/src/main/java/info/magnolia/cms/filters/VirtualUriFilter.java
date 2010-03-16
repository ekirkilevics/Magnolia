/**
 * This file Copyright (c) 2003-2010 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.filters;

import info.magnolia.cms.beans.config.VirtualURIManager;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.MgnlContext;
import info.magnolia.voting.voters.DontDispatchOnForwardAttributeVoter;

import java.io.IOException;
import java.net.URL;

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
public class VirtualUriFilter extends AbstractMgnlFilter {

    private static final Logger log = LoggerFactory.getLogger(VirtualUriFilter.class);

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        final AggregationState aggregationState = MgnlContext.getAggregationState();
        String targetUri = getURIMapping(aggregationState.getCurrentURI());

        if (StringUtils.isNotEmpty(targetUri)) {
            if (!response.isCommitted()) {

                if (targetUri.startsWith("redirect:")) {
                    try {
                        String redirectUrl = StringUtils.substringAfter(targetUri, "redirect:");

                        if (isInternal(redirectUrl)) {
                            redirectUrl = request.getContextPath() + redirectUrl;
                        }

                        response.sendRedirect(redirectUrl);
                        return;
                    }
                    catch (IOException e) {
                        log.error("Failed to redirect to {}:{}", targetUri, e.getMessage());
                    }
                } else if (targetUri.startsWith("permanent:")) {
                    String permanentUrl = StringUtils.substringAfter(targetUri, "permanent:");

                    if (isInternal(permanentUrl)) {
                        permanentUrl = new URL(
                            request.getScheme(),
                            request.getServerName(),
                            request.getServerPort(),
                            request.getContextPath() + permanentUrl).toExternalForm();
                    }

                    response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                    response.setHeader("Location", permanentUrl);
                    return;
                } else if (targetUri.startsWith("forward:")) {
                    targetUri = StringUtils.substringAfter(targetUri, "forward:");
                    try {
                        // TODO: solves MAGNOLIA-2015 but should be solved by implementing MAGNOLIA-2027
                        if(targetUri.endsWith(".jsp")){
                            request.setAttribute(DontDispatchOnForwardAttributeVoter.DONT_DISPATCH_ON_FORWARD_ATTRIBUTE, Boolean.TRUE);
                        }
                        request.getRequestDispatcher(targetUri).forward(request, response);
                        return;
                    }
                    catch (Exception e) {
                        log.error("Failed to forward to {} - {}:{}", new Object[]{
                            targetUri,
                            ClassUtils.getShortClassName(e.getClass()),
                            e.getMessage()});
                    }
                } else {
                    aggregationState.setCurrentURI(targetUri);
                }
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

    protected boolean isInternal(String url) {
        return !url.startsWith("http://") && !url.startsWith("https://");
    }

    /**
     * @return URI mapping as in ServerInfo
     */
    protected String getURIMapping(String currentURI) {
        return VirtualURIManager.getInstance().getURIMapping(currentURI);
    }

}
