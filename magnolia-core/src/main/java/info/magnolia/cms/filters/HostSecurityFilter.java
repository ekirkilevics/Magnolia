/**
 * This file Copyright (c) 2003-2010 Magnolia International
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;


/**
 * <p>
 * A filter that hides urls dependending on the request host name. This filter can be useful if you want to serve
 * multiple public websites with a single magnolia instance, filtering out only the content that belong to the correct
 * host. For example this filter may be configured to only show the "/de/" website tree only on the acme.de website and
 * the "/en/" site tree only on the acme.com website.
 * </p>
 * <p>
 * The filter configuration should be added to server/filters (an appropriate location is just after the contentType
 * filter)
 * </p>
 *
 * <pre>
 * [] hostsecurity
 *    [] default
 *     - class            info.magnolia.cms.filters.HostSecurityFilter
 *       [] mappings
 *        - 1             /en/=acme.com
 *        - 2             /en/=acme.de
 *
 * </pre>
 * @author fgiust
 * @version $Id$
 */
public class HostSecurityFilter extends OncePerRequestAbstractMgnlFilter {

    private ArrayList<String[]> uriToHost;

    public HostSecurityFilter() {
        uriToHost = new ArrayList<String[]>();
    }

    // required by content2bean in order to make addMapping work, do not remove!
    public List<String> getMappings() {
        return null;
    }

    /**
     * Adds a mapping (used by content2bean).
     * @param mapping in the form /path=host
     */
    public void addMapping(String mapping) {
        String[] pathToHost = StringUtils.split(mapping, "=");
        if (pathToHost != null && pathToHost.length == 2) {
            synchronized (uriToHost) {
                uriToHost.add(pathToHost);
            }
        }
    }

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        String uri = request.getRequestURI();
        String host = request.getServerName();
        Boolean isHostValid = null;

        for (String[] mapping : uriToHost) {
            if (uri.startsWith(mapping[0])) {
                // set to false only if exist at least one matching pattern
                if (isHostValid == null) {
                    isHostValid = false;
                }
                // url allowed on this host
                if (host.endsWith(mapping[1])) {
                    isHostValid = true;
                    break;
                }

            }
        }
        if (isHostValid != null && !isHostValid.booleanValue()) {
            response.sendError(404);
            return;
        }

        chain.doFilter(request, response);

    }

}
