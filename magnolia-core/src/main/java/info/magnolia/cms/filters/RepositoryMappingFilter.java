/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
        // TODO Warning - this might change in the future - see MAGNOLIA-2343 for details.
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
