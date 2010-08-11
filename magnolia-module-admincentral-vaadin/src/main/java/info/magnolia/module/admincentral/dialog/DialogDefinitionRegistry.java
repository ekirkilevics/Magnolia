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
package info.magnolia.module.admincentral.dialog;

/**
 * Registry of configured dialogs.
 */
public class DialogDefinitionRegistry {

    public DialogDefinition getDialog(String name) {

        DialogDefinition dialog = new DialogDefinition();

        dialog.setName(name);

        DialogTab tab1 = new DialogTab();
        tab1.setLabel("Content");
        tab1.addSub(createEdit("title", "Title", "Title of the paragraph"));
        tab1.addSub(createEdit("description", "Short description", null));
        tab1.addSub(createDate("date", "Date", "Date of publication"));
        dialog.addTab(tab1);

        DialogTab tab2 = new DialogTab();
        tab2.setLabel("Margins");
        tab2.addSub(createEdit("leftMargin", "Left margin", "Margin on the left"));
        tab2.addSub(createEdit("rightMargin", "Right margin", "Margin on the right"));
        dialog.addTab(tab2);

        return dialog;
    }

    private DialogItem createDate(String name, String label, String description) {
        DialogDate date = new DialogDate();
        date.setName(name);
        date.setLabel(label);
        date.setDescription(description);
        return date;
    }

    private DialogEdit createEdit(String name, String label, String description) {
        DialogEdit edit = new DialogEdit();
        edit.setName(name);
        edit.setLabel(label);
        edit.setDescription(description);
        return edit;
    }
}