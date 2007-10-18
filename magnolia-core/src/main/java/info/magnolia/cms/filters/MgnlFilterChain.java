/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.cms.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * Handles the bypassing
 * @author philipp
 * @version $Id$
 *
 */
public class MgnlFilterChain implements FilterChain {

    private MgnlFilter[] filters;

    private int position;

    private FilterChain originalChain;

    public MgnlFilterChain(FilterChain originalChain, MgnlFilter[] filters) {
        this.filters = filters;
        this.originalChain = originalChain;
    }

    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        if (position == filters.length) {
            originalChain.doFilter(request, response);
        }
        else {
            position++;
            MgnlFilter filter = filters[position - 1];
            if (!filter.bypasses((HttpServletRequest)request)) {
                filter.doFilter(request, response, this);
            } else {
                doFilter(request, response);
            }
        }
    }

    public void reset() {
        position = 0;
    }
}