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
package info.magnolia.module.admincentral.editor.vaadin;

import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;
import info.magnolia.module.admincentral.dialog.VerticalTabSheet;

/**
 * Vaadin specific dialog implementation.
 *
 * @author tmattsson
 */
public class VaadinDialog extends CustomComponent {

    /**
     * Presenter for VaadinDialog.
     */
    public static interface Presenter {

        void onSave();

        void onCancel();
    }

    private Presenter presenter;
    private VerticalTabSheet tabSheet;
    private HorizontalLayout description;

    public VaadinDialog() {

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);

        Button save = new Button("Save", new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                presenter.onSave();
            }
        });
        save.addStyleName("primary");
        save.setClickShortcut(ShortcutAction.KeyCode.ENTER, ShortcutAction.ModifierKey.CTRL);
        buttons.addComponent(save);
        buttons.setComponentAlignment(save, "right");

        Button cancel = new Button("Cancel", new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                presenter.onCancel();
            }
        });
        cancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
        buttons.addComponent(cancel);
        buttons.setComponentAlignment(cancel, "right");

        tabSheet = new VerticalTabSheet();
        tabSheet.setSizeFull();

        description = new HorizontalLayout();
        description.setSizeFull();
        description.addComponent(new Label("This is the description of the currently focused field"));

        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.setSizeFull();
        layout.addComponent(tabSheet);
        layout.addComponent(description);
        layout.addComponent(buttons);
        layout.setComponentAlignment(buttons, "right");
        super.setCompositionRoot(layout);
        super.setSizeFull();
    }

    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    public void addTab(String name, String label) {
        VerticalLayout grid = new VerticalLayout();
        grid.setSizeFull();
        grid.setSpacing(true);
        grid.setMargin(false);
        tabSheet.addTab(name, label, grid);
    }

    public VaadinDialogField addField(String tabName, String name, String label, String description, Component field) {
        VaadinDialogField q = new VaadinDialogField(name, label, description, field);
        ((Layout) tabSheet.getTab(tabName)).addComponent(q);
        return q;
    }

    public void addField(String tabName, String label) {
        ((Layout) tabSheet.getTab(tabName)).addComponent(new Label(label));
    }
}
