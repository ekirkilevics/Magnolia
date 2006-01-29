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

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class ContentTypeFilter implements Filter {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(ContentTypeFilter.class);

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        // unused
    }

    /**
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
        // unused
    }

    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
     * javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) throws IOException,
        ServletException {
        this.setContentType(req, resp);
        filterChain.doFilter(req, resp);
    }

    private void setContentType(ServletRequest req, ServletResponse resp) {
        resp.setContentType(MIMEMapping.getMIMEType((HttpServletRequest) req));
        String characterEncoding = MIMEMapping.getContentEncoding((HttpServletRequest) req);

        if (StringUtils.isEmpty(characterEncoding)) {
            characterEncoding = "UTF-8"; //$NON-NLS-1$
        }

        resp.setCharacterEncoding(characterEncoding);

        try {
            req.setCharacterEncoding(characterEncoding);
        }
        catch (IllegalStateException e) {
            log.debug("can't set character encoding for the request", e); //$NON-NLS-1$
        }
        catch (UnsupportedEncodingException e) {
            log.error("can't set character encoding for the request", e); //$NON-NLS-1$
        }
    }

}