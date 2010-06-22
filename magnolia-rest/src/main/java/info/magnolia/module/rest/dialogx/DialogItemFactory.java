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
package info.magnolia.module.rest.dialogx;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.objectfactory.Components;

import java.util.Iterator;

public class DialogItemFactory {

    public Dialog createDialog(Content configNode) {

        Dialog dialog = new Dialog();
        dialog.setName(configNode.getName());
        dialog.setLabel(configNode.getNodeData("label").getString());

        getSubs(configNode, dialog);

        return dialog;
    }

    private void getSubs(Content parentConfigNode, DialogItemContainer dialogItemContainer) {
        Iterator it = parentConfigNode.getChildren(ItemType.CONTENTNODE).iterator();
        while (it.hasNext()) {
            Content configNode = (Content) it.next();

            String controlType = configNode.getNodeData("controlType").getString();

            DialogItem dialogItem = null;
            if (controlType.equals("tab")) {
                dialogItem = createTab(configNode);
            } else if (controlType.equals("edit")) {
                dialogItem = createEditControl(configNode);
            } else if (controlType.equals("date")) {
                dialogItem = createDateControl(configNode);
            } else {
                // Unknown controlType...
            }

            if (dialogItem != null) {
                dialogItemContainer.addSub(dialogItem);
                if (dialogItem instanceof DialogItemContainer) {
                    getSubs(configNode, (DialogItemContainer) dialogItem);
                }
            }
        }
    }

    private Tab createTab(Content configNode) {
        Tab tab = new Tab();
        tab.setType("tab");
        tab.setName(configNode.getName());
        tab.setLabel(configNode.getNodeData("label").getString());
        return tab;
    }

    private EditControl createEditControl(Content configNode) {
        EditControl editControl = new EditControl();
        editControl.setType("edit");
        editControl.setName(configNode.getName());
        editControl.setLabel(configNode.getNodeData("label").getString());
        return editControl;
    }

    private DateControl createDateControl(Content configNode) {
        DateControl dateControl = new DateControl();
        dateControl.setType("date");
        dateControl.setName(configNode.getName());
        dateControl.setLabel(configNode.getNodeData("label").getString());
        return dateControl;
    }

    public static DialogItemFactory getInstance() {
        return Components.getSingleton(DialogItemFactory.class);
    }
}
