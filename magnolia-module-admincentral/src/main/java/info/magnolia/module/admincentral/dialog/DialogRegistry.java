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
package info.magnolia.module.admincentral.dialog;

import java.util.HashMap;
import java.util.Map;
import javax.jcr.RepositoryException;

import info.magnolia.objectfactory.Components;

/**
 * Maintains a registry of dialog providers registered by name.
 */
public class DialogRegistry {

    private final Map<String, DialogProvider> providers = new HashMap<String, DialogProvider>();

    public void registerDialog(String dialogName, DialogProvider provider) {
        synchronized (providers) {
            if (providers.containsKey(dialogName))
                throw new IllegalStateException("Dialog already registered for name [" + dialogName + "]");
            providers.put(dialogName, provider);
        }
    }

    public void unregisterDialog(String dialogName) {
        synchronized (providers) {
            providers.remove(dialogName);
        }
    }

    /**
     * Gets dialog definition for dialog of provided name or null when such dialog is not registered.
     * @param dialogName name of the dialog to retrieve. Case sensitive. Null is not allowed.
     * @return dialog definition or null when dialog of requested name doesn't exist.
     */
    public DialogDefinition getDialog(String dialogName) throws RepositoryException {

        DialogProvider dialogProvider;
        synchronized (providers) {
            dialogProvider = providers.get(dialogName);
        }
        if (dialogProvider == null) {
            return null;
        }
        return dialogProvider.getDialogDefinition();
    }
/*
    private DialogDefinition mockDialog(String dialogName) {
        DialogDefinition def = new DialogDefinition();
        def.setName(dialogName);

        DialogTab tab = new DialogTab();
        tab.setLabel("Settings");
        def.addTab(tab);

        DialogField field1 = new DialogField();
        field1.setName("title");
        field1.setLabel("Title");
        field1.setControlType("edit");
        tab.addField(field1);

        DialogField field2 = new DialogField();
        field2.setName("text");
        field2.setLabel("Text");
        field2.setControlType("richText");
        tab.addField(field2);

        DialogField field3 = new DialogField();
        field3.setName("date");
        field3.setLabel("Date");
        field3.setControlType("date");
        tab.addField(field3);

        return def;
    }
*/
    public static DialogRegistry getInstance() {
        return Components.getSingleton(DialogRegistry.class);
    }
}
