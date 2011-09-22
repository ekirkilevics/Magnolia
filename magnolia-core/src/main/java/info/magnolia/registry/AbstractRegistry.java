/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Superclass for Registries storing providers to generate definitions from.
 *
 * @param <D> the definition that can be created by the provider
 * @param <P> the provider to be registered
 *
 * @version $Id$
 */
public abstract class AbstractRegistry<D, P extends Provider<D>> {

    private final Map<String, P> providers = new HashMap<String, P>();

    protected Map<String, P> getProviders() {
        return providers;
    }

    public void register(P provider) {
        synchronized (providers) {
            providers.put(provider.getId(), provider);
        }
    }

    public void unregister(String id) {
        synchronized (providers) {
            providers.remove(id);
        }
    }

    public Set<String> unregisterAndRegister(Collection<String> remove, Collection<P> providers2) {
        synchronized (providers) {
            final Set<String> ids = new HashSet<String>();
            for (String id : remove) {
                providers.remove(id);
            }
            for (P provider : providers2) {
                String id = provider.getId();
                providers.put(provider.getId(), provider);
                ids.add(id);
            }
            return ids;
        }
    }

    public D get(String id) throws RegistrationException {

        P provider;
        synchronized (providers) {
            provider = providers.get(id);
            if (provider == null) {
                List<String> types = new ArrayList<String>(providers.keySet());
                Collections.sort(types);
                throw new RegistrationException("Can't find a registration for type [" + id + "]. Registered types are " + types);
            }
        }
        return provider.getDefinition();
    }

}
