/**
 * This file Copyright (c) 2003-2009 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.i18n;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.filters.AbstractMgnlFilter;
import info.magnolia.context.MgnlContext;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.jstl.core.Config;
import java.io.IOException;
import java.util.Locale;

/**
 * Rewrites the i18n uris and sets the current locale.
 *
 * @author philipp
 * @version $Id$
 */
public class I18nContentSupportFilter extends AbstractMgnlFilter {

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        I18nContentSupport i18nSupport = I18nContentSupportFactory.getI18nSupport();
        if(i18nSupport != null){

            final Locale locale = i18nSupport.determineLocale();
            i18nSupport.setLocale(locale);

            AggregationState aggregationState = MgnlContext.getAggregationState();
            String currentUri = aggregationState.getCurrentURI();
            String newUri = i18nSupport.toRawURI(currentUri);
            // MAGNOLIA-2064 ... do not set current uri if it hasn't changed to prevent double removal of context path.
            if (!currentUri.equals(newUri)) {
                aggregationState.setCurrentURI(newUri);
            }

            // make the locale available to jstl
            Config.set(request, Config.FMT_LOCALE, locale);
        }

        chain.doFilter(request, response);
    }
}
