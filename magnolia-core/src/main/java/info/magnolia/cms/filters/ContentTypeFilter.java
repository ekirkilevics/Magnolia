/**
 * This file Copyright (c) 2003-2011 Magnolia International
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import info.magnolia.cms.beans.config.MIMEMapping;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.util.ServletUtils;
import info.magnolia.context.MgnlContext;


/**
 * Sets content type and encoding for requests based on the uri extension and prepares uri path information in the
 * aggregation state.
 *
 * TODO : rename this filter. What it really does is initialize and setup the basic,
 * non-content related attributes of the AggregationState. ContentType could become an
 * attribute of the AggregationState too and could be set later.
 *
 * FIXME: the original uri should not be reset, MAGNOLIA-3204
 *
 * @version $Id$
 * @see MIMEMapping
 * @see AggregationState
 */
public class ContentTypeFilter extends AbstractMgnlFilter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ContentTypeFilter.class);

    /**
     * If set we have to reset the aggregation state before setting the original URI/URL with new values.
     */
    private static final String AGGREGATION_STATE_INITIALIZED = ContentTypeFilter.class.getName() + ".aggregationStateInitialized";

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        // we will set the original uri, to avoid conflicts we have to reset the aggregation state
        // this will mainly reset the original uri and keep all other information
        if (request.getAttribute(AGGREGATION_STATE_INITIALIZED) != null) {
            MgnlContext.resetAggregationState();
        } else {
            request.setAttribute(AGGREGATION_STATE_INITIALIZED, Boolean.TRUE);
        }

        final String originalUri = ServletUtils.getOriginalRequestURI(request);
        final String originalUrl = ServletUtils.getOriginalRequestURLIncludingQueryString(request);
        final String extension = getUriExtension(originalUri);

        final String characterEncoding = setupContentTypeAndCharacterEncoding(extension, request, response);
        final AggregationState aggregationState = MgnlContext.getAggregationState();
        aggregationState.setCharacterEncoding(characterEncoding);

        aggregationState.setOriginalURI(URLDecoder.decode(originalUri, characterEncoding));
        aggregationState.setOriginalURL(URLDecoder.decode(originalUrl, characterEncoding));
        aggregationState.setOriginalBrowserURI(originalUri);
        aggregationState.setOriginalBrowserURL(originalUrl);
        aggregationState.setCurrentURI(URLDecoder.decode(ServletUtils.getRequestUri(request), characterEncoding));
        aggregationState.setExtension(extension);
        aggregationState.setQueryString(request.getQueryString());

        chain.doFilter(request, response);
    }

    protected String getUriExtension(String uri) {
        final String fileName = StringUtils.substringAfterLast(uri, "/");
        return StringUtils.substringAfterLast(fileName, ".");
    }

    protected String setupContentTypeAndCharacterEncoding(String extension, HttpServletRequest request, HttpServletResponse response) {
        final String mimeType = MIMEMapping.getMIMETypeOrDefault(extension);
        final String characterEncoding = MIMEMapping.getContentEncodingOrDefault(mimeType);
        final String defaultExtension = ServerConfiguration.getInstance().getDefaultExtension();

        try {
            // let's not override the request encoding if set by the servlet container or the requesting browser
            if (request.getCharacterEncoding() == null) {
                request.setCharacterEncoding(characterEncoding);
            }
        } catch (UnsupportedEncodingException e) {
            log.error("Can't set character encoding for the request (extension=" + extension + ",mimetype=" + mimeType + ")", e);
        }

        response.setCharacterEncoding(characterEncoding);

        // do not send empty ContentType
        if (StringUtils.isEmpty(defaultExtension)) {
            response.setContentType("text/html");
        } else {
            response.setContentType(mimeType);
        }

        return characterEncoding;
    }

}
