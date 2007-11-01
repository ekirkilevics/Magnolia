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

import info.magnolia.cms.beans.config.MIMEMapping;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.MgnlContext;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

/**
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * @author gjoseph
 * @version $Id$
 */
public class ContentTypeFilter extends AbstractMgnlFilter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ContentTypeFilter.class);

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        final String originalUri = request.getRequestURI();
        final String ext = getUriExtension(originalUri);

        final String characterEncoding = setupContentTypeAndCharacterEncoding(ext, request, response);

        // reset any leftover found in request
        MgnlContext.resetAggregationState();

        final AggregationState aggregationState = MgnlContext.getAggregationState();
        aggregationState.setCharacterEncoding(characterEncoding);
        aggregationState.setOriginalURI(originalUri);
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