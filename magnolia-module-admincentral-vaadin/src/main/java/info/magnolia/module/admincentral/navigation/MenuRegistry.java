/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.module.admincentral.navigation;

import info.magnolia.objectfactory.Components;

import javax.jcr.RepositoryException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Maintains a registry of menu providers registered by name.
 */
public class MenuRegistry {

    private final Map<String, MenuProvider> providers = new LinkedHashMap<String, MenuProvider>();

    public void registerMenu(String menuItemName, MenuProvider provider) {
        synchronized (providers) {
            if (providers.containsKey(menuItemName))
                throw new IllegalStateException("Menu item already registered for name [" + menuItemName + "]");
            providers.put(menuItemName, provider);
        }
    }

    public void unregisterMenu(String menuName) {
        synchronized (providers) {
            providers.remove(menuName);
        }
    }

    /**
     * Gets menu definition for menu of provided name or null when such dialog is not registered.
     * @param menuName name of the menu item to retrieve. Case sensitive. Null is not allowed.
     * @return menu definition or null when menu of requested name doesn't exist.
     */
    public MenuDefinition getMenu(String menuName) throws RepositoryException {
        MenuProvider menuProvider;
        synchronized (providers) {
            menuProvider = providers.get(menuName);
        }
        if (menuProvider == null) {
            return null;
        }
        return menuProvider.getMenuDefinition();
    }

    public Set<String> getMenuNames(){
        return Collections.unmodifiableSet(providers.keySet());
    }

    public static MenuRegistry getInstance() {
        return Components.getSingleton(MenuRegistry.class);
    }
}
