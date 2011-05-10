/**
 * This file Copyright (c) 2007-2011 Magnolia International
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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.ContentWrapper;
import info.magnolia.cms.util.DeprecationUtil;
import info.magnolia.context.MgnlContext;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Used to support the deprecated ${actpage} expression in jsps.
 * @deprecated since 4.3
 * @author philipp
 * @version $Id$
 */
public class BackwardCompatibilityFilter extends OncePerRequestAbstractMgnlFilter {

    private static Content ACTPAGE = new ContentWrapper(){

        @Override
        public Content getWrappedContent() {
            DeprecationUtil.isDeprecated("The request attribute ${" + ACTPAGE_ATTRIBUTE + "} is deprecated please use the new ${" + ACTPAGE_FUNCTION + "} function instead");

            return MgnlContext.getAggregationState().getCurrentContent();
        }
    };

    private static final String ACTPAGE_ATTRIBUTE = "actpage";

    private static final String ACTPAGE_FUNCTION = "cmsfn:mainPage()";

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        request.setAttribute(ACTPAGE_ATTRIBUTE, ACTPAGE);
        chain.doFilter(request, response);
    }

}
