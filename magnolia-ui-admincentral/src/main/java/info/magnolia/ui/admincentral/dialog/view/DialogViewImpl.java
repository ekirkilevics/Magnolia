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
package info.magnolia.ui.admincentral.dialog.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;

import com.vaadin.ui.Window;

import info.magnolia.ui.framework.editor.Editor;
import info.magnolia.ui.framework.editor.EditorError;
import info.magnolia.ui.framework.editor.HasEditorErrors;
import info.magnolia.ui.framework.editor.HasEditors;
import info.magnolia.ui.model.dialog.definition.TabDefinition;

/**
 * Vaadin specific dialog implementation.
 *
 * @author tmattsson
 */
public class DialogViewImpl extends Window implements DialogView {

    private static final Logger log = LoggerFactory.getLogger(DialogViewImpl.class);

    private static class Tab implements HasEditors, HasEditorErrors {

        private String name;
        private List<Editor> editors = new ArrayList<Editor>();

        public Tab(String name) {
            this.name = name;
        }

        @Override
        public void showErrors(List<EditorError> errors) {
            for (EditorError error : errors) {
                if (editors.indexOf(error.getEditor()) != -1) {
                    log.error("Error in tab {}", name);
                    // TODO should highlight the tab to indicate the error
                }
            }
            // TODO if no errors are found we must remove any previous highlighting
        }

        @Override
        public Collection<Editor> getEditors() {
            return editors;
        }

        public void addEditor(Editor editor) {
            this.editors.add(editor);
        }
    }

    private Presenter presenter;
    private VerticalTabSheet tabSheet;
    private HorizontalLayout description;

    private Map<String, Tab> tabs = new HashMap<String, Tab>();

    public DialogViewImpl() {

        setModal(true);
        setResizable(true);
//        setScrollable(false); // TODO this is commented out because it doesnt work with dialogs in page editor
        setClosable(false);
        setWidth("800px");

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);

        Button save = new Button("Save", new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                presenter.onSave();
            }
        });
        save.addStyleName("primary");
        save.setClickShortcut(ShortcutAction.KeyCode.ENTER, ShortcutAction.ModifierKey.CTRL);
        buttons.addComponent(save);
        buttons.setComponentAlignment(save, Alignment.MIDDLE_RIGHT);

        Button cancel = new Button("Cancel", new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                presenter.onCancel();
            }
        });
        cancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
        buttons.addComponent(cancel);
        buttons.setComponentAlignment(cancel, Alignment.MIDDLE_RIGHT);

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
        layout.setComponentAlignment(buttons, Alignment.MIDDLE_RIGHT);

        super.getContent().addComponent(layout);
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    public void addTab(String name, String label) {
        VerticalLayout grid = new VerticalLayout();
        grid.setSizeFull();
        grid.setSpacing(true);
        grid.setMargin(false);
        tabSheet.addTab(name, label, grid);
        tabs.put(name, new Tab(name));
    }

    public void addField(String tabName, Component component) {
        ((Layout) tabSheet.getTab(tabName)).addComponent(component);
    }

    public void addField(String tabName, String label) {
        ((Layout) tabSheet.getTab(tabName)).addComponent(new Label(label));
    }

    public Component asView() {
        return this;
    }

    @Override
    public Collection<? extends Editor> getEditors() {
        return tabs.values();
    }

    public void addEditor(TabDefinition tabDefinition, Editor editor) {
        tabs.get(tabDefinition.getName()).addEditor(editor);
    }

    @Override
    public void setDescriptionText(String description) {
        Label descriptionLabel = (Label) this.description.getComponent(0);
        descriptionLabel.setValue(description);
    }

    @Override
    public void close() {
        super.close();
    }
}
