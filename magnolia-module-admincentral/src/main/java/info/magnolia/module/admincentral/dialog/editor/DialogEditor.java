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
package info.magnolia.module.admincentral.dialog.editor;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.SplitPanel;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.VerticalLayout;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.module.admincentral.control.AbstractDialogControl;
import info.magnolia.module.admincentral.control.DateControl;
import info.magnolia.module.admincentral.control.EditControl;
import info.magnolia.module.admincentral.control.FileControl;
import info.magnolia.module.admincentral.control.LinkControl;
import info.magnolia.module.admincentral.control.RadioControl;
import info.magnolia.module.admincentral.control.RichTextControl;
import info.magnolia.module.admincentral.control.SelectControl;
import info.magnolia.module.admincentral.control.SliderControl;
import info.magnolia.module.admincentral.control.UuidLinkControl;
import info.magnolia.module.admincentral.dialog.DialogControl;
import info.magnolia.module.admincentral.dialog.DialogTab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog editor. Provides layout of the whole dialog editor and binds together different parts of the editor (dialog preview + field editing + trash + element selection).
 * @author had
 * @version $Id: $
 */
public class DialogEditor extends VerticalLayout implements FieldEditingHandler {

    private static final long serialVersionUID = -4840593673990482462L;
    private static final Logger log = LoggerFactory.getLogger(DialogEditor.class);
    private FormLayout elementEditingForm;

    public DialogEditor(Content dialog) throws RepositoryException {

        // Add a horizontal SplitPanel to the lower area
        final SplitPanel topLayout = new SplitPanel();
        topLayout.setOrientation(SplitPanel.ORIENTATION_HORIZONTAL);
        topLayout.setSplitPosition(55); // percent

        elementEditingForm = new FormLayout();
        elementEditingForm.setMargin(false, false, false, true);

        // we need some default component to make sure container is big enough to be able to drag something into it
        Label dropFieldsLabel = new Label("Drop fields here");
        dropFieldsLabel.addStyleName("drop-field-label");
        // TODO: doesn't work with the FormLayout why?
        final DragAndDropContainer dialogPreview = new DragAndDropContainer(new VerticalLayout(), dropFieldsLabel, this);
        dialogPreview.addStyleName("no-horizontal-drag-hints");
        dialogPreview.addComponent(dropFieldsLabel);

        addDialogElements(dialog, dialogPreview , elementEditingForm);

        // left
        Panel dialogPreviewPanel = new Panel(getMessage("dialog.edit.preview.title", dialog.getName()), dialogPreview);
        VerticalLayout tmp = new VerticalLayout();
        tmp.setMargin(false, true, false, false);
        tmp.addComponent(dialogPreviewPanel);
        //trash
        Label trash = new Label();
        trash.addStyleName("dialog-editor-trash");
        DragAndDropContainer trashContainer = new DragAndDropContainer(new VerticalLayout(), null);
        trashContainer.addComponent(trash);
        trashContainer.addStyleName("no-horizontal-drag-hints");
        trashContainer.addStyleName("no-vertical-drag-hints");
        tmp.addComponent(trashContainer);
        tmp.setComponentAlignment(trashContainer, Alignment.BOTTOM_CENTER);
        topLayout.addComponent(tmp);

        // right
        Accordion toolBar = new Accordion();
        VerticalLayout innerFieldChooserContainer = new VerticalLayout();
        DragAndDropContainer fieldChooser = new DragAndDropContainer(innerFieldChooserContainer, null);
        fieldChooser.addStyleName("no-horizontal-drag-hints");
        for (Component component : createElementPrototypes()) {
            fieldChooser.addComponent(component);
        }

        // reset field selection on tab change and clear field form
        toolBar.addListener(new SelectedTabChangeListener() {
            public void selectedTabChange(SelectedTabChangeEvent event) {
                dialogPreview.setSelected(null);
                elementEditingForm.removeAllComponents();
            }});

        toolBar.addTab(fieldChooser, getMessage("dialog.edit.elements.title"), null);

        toolBar.addTab(elementEditingForm, getMessage("dialog.edit.elements.properties"), null);
        topLayout.addComponent(toolBar);

        addComponent(topLayout);
        dialogPreview.addStyleName("edit-dialog");
    }

    /**
     * Creates dialog elements from an existing dialog configuration.
     */
    private void addDialogElements(Content dialog, DragAndDropContainer parentLayout, FormLayout fieldEditForm) throws RepositoryException {
        boolean reset = false;
        for (Content tab : dialog.getContent("tabs").getChildren(ItemType.CONTENTNODE)) {

            try {
                for (DialogControl entry : ((DialogTab) Content2BeanUtil.toBean(tab, true, DialogTab.class)).getFields()) {
                    if (!reset) {
                        parentLayout.removeAllComponents();
                        reset = true;
                    }
                    final AbstractDialogControl element = (AbstractDialogControl) entry;
                    final DialogEditorField controlComponent = createElementInstanceFromTemplate(new TemplateControl(element.getName(), element), parentLayout);
                    parentLayout.addComponent(controlComponent);
                }
            } catch (Content2BeanException e) {
                log.error("failed to read configuration of " + dialog.getName() + " dialog.", e);
            }
        }
    }

    private List<Component> createElementPrototypes() {
        List<Component> components = new ArrayList<Component>();

        //TODO: use control manager for this
        components.add(new TemplateControl("edit", new EditControl()));
        components.add(new TemplateControl("upload", new FileControl()));
        components.add(new TemplateControl("date", new DateControl()));
        components.add(new TemplateControl("select", new SelectControl()));
        components.add(new TemplateControl("rich text", new RichTextControl()));
        components.add(new TemplateControl("radio", new RadioControl()));
        components.add(new TemplateControl("slider", new SliderControl()));
        components.add(new TemplateControl("link", new LinkControl()));
        components.add(new TemplateControl("uuidLink", new UuidLinkControl()));

        // TODO: fix layout
        //components.add(new TemplateControl("checkbox", new CheckBoxControl()));

        return components;
    }

    // ------------- handler implementation ---------
    public FormLayout getEditForm() {
        ((Accordion) elementEditingForm.getParent()).setSelectedTab(elementEditingForm);
        return elementEditingForm;
    }

    public DialogEditorField getElementInstanceFromTemplate(TemplateControl template, final DragAndDropContainer sortableLayout) {
        return createElementInstanceFromTemplate(template, sortableLayout);
    }

    public DialogEditorField createElementInstanceFromTemplate(TemplateControl template, final DragAndDropContainer sortableLayout) {
        final DialogEditorField controlComponent = template.createControlComponent(sortableLayout.getWindow());

        controlComponent.addListener(new LayoutClickListener() {

            public void layoutClick(LayoutClickEvent event) {
                FormLayout form = getEditForm();
                ((Accordion) form.getParent()).setSelectedTab(form);
                controlComponent.setSelected(true);
                TemplateControl.configureIn(controlComponent, form);
                // FYI: this doesn't repaint ALL children, due to caching of pre-rendered content. Changing css underneath doesn't invalidate the parent
                //sortableLayout.requestRepaint();
                sortableLayout.setSelected(controlComponent);
            }
        });
        return controlComponent;
    }

    private String getMessage(String key, Object... args) {
        final Messages messMan = MessagesManager.getMessages("info.magnolia.module.admincentral.messages");
        if (args != null && args.length > 0 ) {
            return messMan.get(key, args);
        }
        return messMan.getWithDefault(key, key);
    }
}