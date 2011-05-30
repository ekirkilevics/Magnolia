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
package info.magnolia.ui.admincentral.dialog;

import javax.jcr.RepositoryException;

import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.admincentral.dialog.builder.DialogBuilder;
import info.magnolia.ui.admincentral.dialog.view.DialogPresenter;
import info.magnolia.ui.model.dialog.definition.DialogDefinition;
import info.magnolia.ui.model.dialog.registry.DialogDefinitionRegistry;

/**
 * Implementation of {@link DialogPresenterFactory}.
 *
 * @version $Id$
 */
public class DialogPresenterFactoryImpl implements DialogPresenterFactory {

    private ComponentProvider componentProvider;
    private DialogDefinitionRegistry dialogDefinitionRegistry;

    public DialogPresenterFactoryImpl(ComponentProvider componentProvider, DialogDefinitionRegistry dialogDefinitionRegistry) {
        this.componentProvider = componentProvider;
        this.dialogDefinitionRegistry = dialogDefinitionRegistry;
    }

    @Override
    public DialogPresenter createDialog(String dialogName) {

        DialogDefinition dialogDefinition;
        try {
            dialogDefinition = dialogDefinitionRegistry.getDialogDefinition(dialogName);
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }

        if (dialogDefinition == null) {
            throw new IllegalArgumentException("No dialog definition registered for name [" + dialogName + "]");
        }

        DialogBuilder builder = componentProvider.newInstance(DialogBuilder.class);

        return new DialogPresenter(builder, dialogDefinition);
    }
}
