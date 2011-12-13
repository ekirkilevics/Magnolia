/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.cms.beans.config;

/**
 * QueryAwareVirtualURIMapping implementations are used by {@link info.magnolia.cms.beans.config.VirtualURIManager} and
 * {@link info.magnolia.cms.filters.VirtualUriFilter} to transform incoming URI requests with query string.
 * @author milan
 */
public interface QueryAwareVirtualURIMapping extends VirtualURIMapping {
    
    /**
     * Maps an incoming URI and queryString to a new target URI. Returns a {@link VirtualURIMapping.MappingResult} describing the new
     * URI and the level of match. The new URI can be prefixed with "redirect:", "permanent:" or "forward:" to trigger
     * either a temporary redirect, a permanent redirect or a forward respectively. For redirects the URI can be
     * absolute or relative within the web application (the context path is added automatically). If the mapping does
     * not apply to the input URI this method returns null.
     *
     * @param uri the URI of the current request, decoded and without the context path
     * @param queryString the Query String of the current request
     * @return a {@link VirtualURIMapping.MappingResult} with the target URI and level or null if the mapping doesn't apply
     */
    MappingResult mapURI(String uri, String queryString);

}
