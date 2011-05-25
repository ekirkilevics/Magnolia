/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.ui.model.dialog.registry;

import java.util.HashMap;
import java.util.Map;
import javax.jcr.RepositoryException;

import info.magnolia.ui.model.dialog.definition.DialogDefinition;

/**
 * Maintains a registry of dialog providers registered by name.
 *
 * @version $Id$
 */
public class DialogDefinitionRegistryImpl implements DialogDefinitionRegistry {

    private final Map<String, DialogProvider> providers = new HashMap<String, DialogProvider>();

    @Override
    public void registerDialog(String dialogName, DialogProvider provider) {
        synchronized (providers) {
            if (providers.containsKey(dialogName)) {
                throw new IllegalStateException("Dialog already registered for name [" + dialogName + "]");
            }
            providers.put(dialogName, provider);
        }
    }

    @Override
    public void unregisterDialog(String dialogName) {
        synchronized (providers) {
            providers.remove(dialogName);
        }
    }

    @Override
    public DialogDefinition getDialogDefinition(String dialogName) throws RepositoryException {

        DialogProvider dialogProvider;
        synchronized (providers) {
            dialogProvider = providers.get(dialogName);
        }
        if (dialogProvider == null) {
            return null;
        }
        return dialogProvider.getDialogDefinition();
    }
}
