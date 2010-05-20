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

import info.magnolia.cms.beans.config.MIMEMapping;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.MgnlContext;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;


/**
 * TODO : rename this filter. What it really does is initialize and setup the basic,
 * non-content related attributes of the AggregationState. ContentType could become an
 * attribute of the AggregationState too and could be set later.
 *
 * FIXME: the original uri should not be reset, MAGNOLIA-3204
 *
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * @author gjoseph
 * @version $Id$
 */
public class ContentTypeFilter extends AbstractMgnlFilter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ContentTypeFilter.class);

    /**
     * If set we have to reset the aggregation state before setting the original URI/URL with new values
     */
    private static final String AGGREGATION_STATE_INITIALIZED = ContentTypeFilter.class.getName() + ".aggregationStateInitialized";

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        // we will set the original uri, to avoid conflicts we have to reset the aggregation state
        // this will mainly reset the original uri and keep all other information
        if(request.getAttribute(AGGREGATION_STATE_INITIALIZED) != null){
            MgnlContext.resetAggregationState();
        }
        else{
            request.setAttribute(AGGREGATION_STATE_INITIALIZED, Boolean.TRUE);
        }

        final String originalUri = request.getRequestURI();
        final String ext = getUriExtension(originalUri);
        StringBuffer url = request.getRequestURL();
        String query = request.getQueryString();
        if (!StringUtils.isEmpty(query))
        {
            url.append("?").append(query);
        }

        final String characterEncoding = setupContentTypeAndCharacterEncoding(ext, request, response);
        final AggregationState aggregationState = MgnlContext.getAggregationState();
        aggregationState.setCharacterEncoding(characterEncoding);

        aggregationState.setOriginalURI(URLDecoder.decode(originalUri, characterEncoding));
        aggregationState.setOriginalURL(URLDecoder.decode(url.toString(), characterEncoding));
        aggregationState.setOriginalBrowserURI(originalUri);
        aggregationState.setOriginalBrowserURL(url.toString());
        aggregationState.setExtension(ext);

        chain.doFilter(request, response);
    }

    // TODO : test + simplification (substringAfterLast(uri, ".") probably does the trick !?
    protected String getUriExtension(String originalUri) {
        final String fileName = StringUtils.substringAfterLast(originalUri, "/");
        return StringUtils.substringAfterLast(fileName, ".");
    }

    protected String setupContentTypeAndCharacterEncoding(String extension, HttpServletRequest req, HttpServletResponse resp) {
        final String mimeType = MIMEMapping.getMIMETypeOrDefault(extension);
        final String characterEncoding = MIMEMapping.getContentEncodingOrDefault(mimeType);

        try {
            // let's not override the request encoding if set by the app server
            if (req.getCharacterEncoding() == null) {
                req.setCharacterEncoding(characterEncoding);
            }
        } catch (UnsupportedEncodingException e) {
            log.error("Can't set character encoding for the request (extension=" + extension + ",mimetype=" + mimeType + ")", e);
        }

        resp.setContentType(mimeType);
        resp.setCharacterEncoding(characterEncoding);

        return characterEncoding;
    }

}
