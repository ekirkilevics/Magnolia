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
package info.magnolia.module.admininterface;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.dialog.Dialog;
import info.magnolia.objectfactory.Components;
import info.magnolia.registry.RegistryMap;

import java.util.List;
import java.util.Set;

import javax.inject.Singleton;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Manages all the dialog handlers.
 */
@Singleton
public class DialogHandlerManager {

    private final RegistryMap<String, DialogHandlerProvider> registry = new RegistryMap<String, DialogHandlerProvider>() {

        @Override
        protected String keyFromValue(DialogHandlerProvider value) {
            return value.getId();
        }
    };

    public void register(DialogHandlerProvider provider) {
        registry.put(provider.getId(), provider);
    }

    public void unregister(String id) {
        registry.remove(id);
    }

    public Set<String> unregisterAndRegister(Set<String> registeredIds, List<DialogHandlerProvider> providers) {
        return this.registry.removeAndPutAll(registeredIds, providers);
    }

    /**
     * @deprecated since 4.3.2, is obsolete since fix for MAGNOLIA-2907
     */
    @Deprecated
    public Content getDialogConfigNode(String id) {
        DialogHandlerProvider provider = registry.get(id);
        if (provider == null) {
            throw new InvalidDialogHandlerException(id, registry.keySet().toString());
        }
        return provider.getDialogConfigNode();
    }

    public DialogMVCHandler getDialogHandler(String id, HttpServletRequest request, HttpServletResponse response) {
        DialogHandlerProvider provider = registry.get(id);
        if (provider == null) {
            throw new InvalidDialogHandlerException(id, registry.keySet().toString());
        }
        return provider.getDialogHandler(request, response);
    }

    /**
     * Caution: use this method with care, as it creates an Dialog instance having ServletRequest
     * and -Response as well as StorageNode being null.
     */
    public Dialog getDialog(String id) throws RepositoryException {
        DialogHandlerProvider provider = registry.get(id);
        if (provider == null) {
            throw new InvalidDialogHandlerException(id, registry.keySet().toString());
        }
        return provider.getDialog();
    }

    /**
     * @return Returns the instance.
     */
    public static DialogHandlerManager getInstance() {
        return Components.getSingleton(DialogHandlerManager.class);
    }

}
