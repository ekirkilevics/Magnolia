/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.cms.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides support for a simple syntax form of doing redirects and forwards. You pass it a url prefixed with either
 * "redirect:", "permanent:" or "forward:". The context path is added to the url.
 *
 * @author tmattsson
 */
public class RequestDispatchUtil {

    private static final Logger log = LoggerFactory.getLogger(RequestDispatchUtil.class);

    private static final String REDIRECT_PREFIX = "redirect:";
    private static final String PERMANENT_PREFIX = "permanent:";
    private static final String FORWARD_PREFIX = "forward:";

    /**
     * Returns true if processing took place, even if it fails.
     */
    public static boolean dispatch(String targetUri, HttpServletRequest request, HttpServletResponse response) {

        if (targetUri == null)
            return false;

        if (targetUri.startsWith(REDIRECT_PREFIX)) {
            String redirectUrl = StringUtils.substringAfter(targetUri, REDIRECT_PREFIX);
            try {

                if (isInternal(redirectUrl)) {
                    redirectUrl = request.getContextPath() + redirectUrl;
                }

                response.sendRedirect(redirectUrl);

            } catch (IOException e) {
                log.error("Failed to redirect to {}:{}", targetUri, e.getMessage());
            }
            return true;
        }

        if (targetUri.startsWith(PERMANENT_PREFIX)) {
            String permanentUrl = StringUtils.substringAfter(targetUri, PERMANENT_PREFIX);
            try {

                if (isInternal(permanentUrl)){
                    if (isUsingStandardPort(request)) {
                        permanentUrl = new URL(
                            request.getScheme(),
                            request.getServerName(),
                            request.getContextPath() + permanentUrl).toExternalForm();
                    } else {
                        permanentUrl = new URL(
                            request.getScheme(),
                            request.getServerName(),
                            request.getServerPort(),
                            request.getContextPath() + permanentUrl).toExternalForm();
                    }
                }

                response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                response.setHeader("Location", permanentUrl);

            } catch (MalformedURLException e) {
                log.error("Failed to create permanent url to redirect to {}:{}", targetUri, e.getMessage());
            }
            return true;
        }

        if (targetUri.startsWith(FORWARD_PREFIX)) {
            String forwardUrl = StringUtils.substringAfter(targetUri, FORWARD_PREFIX);
            try {
                request.getRequestDispatcher(forwardUrl).forward(request, response);
            } catch (Exception e) {
                log.error("Failed to forward to {} - {}:{}", new Object[]{
                        forwardUrl,
                        ClassUtils.getShortClassName(e.getClass()),
                        e.getMessage()});
            }
            return true;
        }

        return false;
    }

    private static boolean isUsingStandardPort(HttpServletRequest request) {
        String requestScheme = request.getScheme();
        int serverPort = request.getServerPort();
        return (serverPort == 80 && "http".equals(requestScheme)) || (serverPort == 443 && "https".equals(requestScheme));
    }

    private static boolean isInternal(String url) {
        return !url.startsWith("http://") && !url.startsWith("https://");
    }
}
