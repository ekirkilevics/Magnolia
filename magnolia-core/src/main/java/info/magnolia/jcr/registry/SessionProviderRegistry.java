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
package info.magnolia.jcr.registry;

import info.magnolia.registry.RegistrationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

/**
 * Central registry of all SessionsProviders.
 *
 * @version $Id$
 */
@Singleton
public class SessionProviderRegistry {
    private final Map<String, SessionProvider> providers = new HashMap<String, SessionProvider>();

    protected Map<String, SessionProvider> getProviders() {
        return providers;
    }

    public void register(SessionProvider provider) {
        synchronized (providers) {
            providers.put(provider.getLogicalWorkspaceName(), provider);
        }
    }

    public void unregister(String id) {
        synchronized (providers) {
            providers.remove(id);
        }
    }

    public SessionProvider get(String id) throws RegistrationException {

        SessionProvider provider;
        synchronized (providers) {
            provider = providers.get(id);
            if (provider == null) {
                // TODO dlipp: weird - why do some action when throwning an Exception anyway?
                List<String> types = new ArrayList<String>(providers.keySet());
                Collections.sort(types);
                throw new RegistrationException("Can't find a registration for logical workspaceName [" + id + "]. Registered workspaces are " + types);
            }
        }
        return provider;
    }

}
