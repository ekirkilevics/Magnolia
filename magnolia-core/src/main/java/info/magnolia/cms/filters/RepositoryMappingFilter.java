/**
 * This file Copyright (c) 2003-2012 Magnolia International
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

import info.magnolia.cms.beans.config.URI2RepositoryManager;
import info.magnolia.cms.beans.config.URI2RepositoryMapping;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Path;
import info.magnolia.context.MgnlContext;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maps a URI to content stored in the repository. Determine the workspace name
 * and content path.
 *
 * @see URI2RepositoryMapping, {@link URI2RepositoryManager}
 * @version $Id$
 */
public class RepositoryMappingFilter extends AbstractMgnlFilter {
    private static final Logger log = LoggerFactory.getLogger(RepositoryMappingFilter.class);

    private URI2RepositoryManager uri2RepositoryManager;

    @Inject
    public RepositoryMappingFilter(URI2RepositoryManager uri2RepositoryManager) {
        this.uri2RepositoryManager = uri2RepositoryManager;
    }

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String uri = MgnlContext.getAggregationState().getCurrentURI();
        int firstSelectorDelimiterPos = StringUtils.indexOf(uri, Path.SELECTOR_DELIMITER, StringUtils.lastIndexOf(uri, '/'));
        String path;
        // TODO Warning - this might change in the future - see MAGNOLIA-2343 for details.
        String selector;
        if (firstSelectorDelimiterPos > -1) {
            int lastSelectorDelimiterPos = StringUtils.lastIndexOf(uri, Path.SELECTOR_DELIMITER);
            path = StringUtils.substring(uri, 0, firstSelectorDelimiterPos);
            selector = StringUtils.substring(uri, firstSelectorDelimiterPos + 1, lastSelectorDelimiterPos);
        }
        else {
            // no tilde (and no extension)
            path = uri;
            selector = "";
        }

        URI2RepositoryMapping mapping = uri2RepositoryManager.getMapping(uri);

        // remove prefix if any
        path = mapping.getHandle(path);

        final AggregationState aggregationState = MgnlContext.getAggregationState();
        aggregationState.setRepository(mapping.getRepository());
        aggregationState.setHandle(path);

        // selector could potentially be set from some other place, but we have no better idea for now :)
        aggregationState.setSelector(selector);

        chain.doFilter(request, response);
    }
}
