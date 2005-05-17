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
package info.magnolia.cms.filters;

import info.magnolia.cms.beans.config.MIMEMapping;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 * @version 1.1
 */
public class ContentTypeFilter extends BaseFilter {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(ContentTypeFilter.class);

    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
     * javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) throws IOException,
        ServletException {
        this.setContentType(req, resp);
        filterChain.doFilter(req, resp);
    }

    private void setContentType(ServletRequest req, ServletResponse resp) throws UnsupportedEncodingException {
        resp.setContentType(MIMEMapping.getMIMEType((HttpServletRequest) req));
        String characterEncoding = MIMEMapping.getContentEncoding((HttpServletRequest) req);
        if (StringUtils.isNotEmpty(characterEncoding)) {
            resp.setCharacterEncoding(characterEncoding);
            req.setCharacterEncoding(characterEncoding);
        } else {
            resp.setCharacterEncoding("UTF-8");
        }
    }
}
