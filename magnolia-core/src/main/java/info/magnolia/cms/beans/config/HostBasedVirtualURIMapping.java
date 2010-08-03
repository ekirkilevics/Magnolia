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
package info.magnolia.cms.beans.config;

import info.magnolia.cms.util.SimpleUrlPattern;
import info.magnolia.cms.util.UrlPattern;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;


/**
 * Simple VirtualURI mapping that can forward to a different url depending on the request host name. The "host" property
 * must be a list of uri=destination strings. See below for a sample configuration:
 *
 * <pre>
 * [] virtualURIMapping
 *    [] default
 *     - class            info.magnolia.cms.beans.config.HostBasedVirtualURIMapping
 *     - fromURI          /
 *     - toURI            redirect:/.magnolia/pages/adminCentral.html
 *       [] hosts
 *        - 1             www.acme.com=forward:/acme/en/index.html
 *        - 2             www.acme.de=forward:/acme/de/index.html
 * </pre>
 * @author fgiust
 * @version $Id$
 */
public class HostBasedVirtualURIMapping implements VirtualURIMapping {

    private String fromURI;

    private UrlPattern pattern;

    private String toURI;

    private Map<String, String> hosts = new HashMap<String, String>();

    /**
     *
     */
    public HostBasedVirtualURIMapping() {
        hosts = new HashMap<String, String>();
    }

    // required by content2bean in order to make addHost work, do not remove!
    public List<String> getHosts() {
        return null;
    }

    /**
     * Adds a host mapping (used by content2bean).
     * @param mapping in the form host=path
     */
    public void addHost(String mapping) {
        String[] hostToPath = StringUtils.split(mapping, "=");
        if (hostToPath != null && hostToPath.length == 2) {
            synchronized (hosts) {
                hosts.put(hostToPath[0], hostToPath[1]);
            }
        }
    }

    public MappingResult mapURI(String uri) {

        String destination = toURI;

        if (pattern != null && pattern.match(uri)) {

            String requestHost = ((WebContext) MgnlContext.getInstance()).getRequest().getServerName();

            if (hosts != null) {

                Iterator<Map.Entry<String, String>> hostIt = hosts.entrySet().iterator();
                while (hostIt.hasNext()) {

                    Map.Entry<String, String> hk = hostIt.next();
                    if (requestHost.endsWith(hk.getKey())) {
                        destination = hk.getValue();
                        break;
                    }
                }
            }

            MappingResult r = new MappingResult();
            r.setLevel(pattern.getLength());
            r.setToURI(destination);
            return r;
        }
        return null;
    }

    public String getFromURI() {
        return this.fromURI;
    }

    public void setFromURI(String fromURI) {
        this.fromURI = fromURI;
        this.pattern = new SimpleUrlPattern(fromURI);
    }

    public String getToURI() {
        return this.toURI;
    }

    public void setToURI(String toURI) {
        this.toURI = toURI;
    }

    @Override
    public String toString() {
        return fromURI + " --> " + toURI;
    }

}
