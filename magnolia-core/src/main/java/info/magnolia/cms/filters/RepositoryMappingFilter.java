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

import info.magnolia.cms.beans.config.URI2RepositoryManager;
import info.magnolia.cms.beans.config.URI2RepositoryMapping;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.MgnlContext;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class RepositoryMappingFilter extends AbstractMgnlFilter {
    private static final Logger log = LoggerFactory.getLogger(RepositoryMappingFilter.class);

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String uri = MgnlContext.getAggregationState().getCurrentURI();
        int firstDotPos = StringUtils.indexOf(uri, '.', StringUtils.lastIndexOf(uri, '/'));
        String handle;
        String selector;
        if (firstDotPos > -1) {
            int lastDotPos = StringUtils.lastIndexOf(uri, '.');
            handle = StringUtils.substring(uri, 0, firstDotPos);
            selector = StringUtils.substring(uri, firstDotPos + 1, lastDotPos);
        }
        else {
            // no dots (and no extension)
            handle = uri;
            selector = "";
        }

        URI2RepositoryMapping mapping = URI2RepositoryManager.getInstance().getMapping(uri);

        // remove prefix if any
        handle = mapping.getHandle(handle);

        final AggregationState aggregationState = MgnlContext.getAggregationState();
        aggregationState.setRepository(mapping.getRepository());
        aggregationState.setHandle(handle);

        // selector could potentially be set from some other place, but we have no better idea for now :)
        aggregationState.setSelector(selector);

        chain.doFilter(request, response);
    }
}
