/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
package info.magnolia.rendering.renderer.registry;

import info.magnolia.registry.RegistryMap;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.renderer.Renderer;

import javax.inject.Singleton;
import java.util.List;
import java.util.Set;

/**
 * Central registry of all renderers.
 *
 * @version $Id$
 */
@Singleton
public class RendererRegistry {

    private final RegistryMap<String, RendererProvider> registry = new RegistryMap<String, RendererProvider>() {

        @Override
        protected String keyFromValue(RendererProvider provider) {
            return provider.getType();
        }
    };

    public Renderer getRenderer(String type) throws RegistrationException {

        RendererProvider provider;
        try {
            provider = registry.getRequired(type);
        } catch (RegistrationException e) {
            throw new RegistrationException("No renderer registered for type: " + type, e);
        }

        return provider.getRenderer();
    }

    public void register(RendererProvider rendererProvider) {
        registry.put(rendererProvider.getType(), rendererProvider);
    }

    public void unregister(String type) {
        registry.remove(type);
    }

    public Set<String> unregisterAndRegister(Set<String> registeredTypes, List<RendererProvider> providers) {
        return registry.removeAndPutAll(registeredTypes, providers);
    }
}
