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
package info.magnolia.cms.i18n;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.filters.AbstractMgnlFilter;
import info.magnolia.context.MgnlContext;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Rewrites the i18n uris and sets the current language.
 *
 * @author philipp
 * @version $Id$
 */
public class I18nContentSupportFilter extends AbstractMgnlFilter {

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        I18nContentSupport i18nSupport = I18nContentSupportFactory.getI18nSupport();

        AggregationState aggregationState = MgnlContext.getAggregationState();
        i18nSupport.setLocale(i18nSupport.determineLocale());
        
        String newUri = i18nSupport.toRawURI(aggregationState.getCurrentURI());
        aggregationState.setCurrentURI(newUri);

        chain.doFilter(request, response);
    }
}
