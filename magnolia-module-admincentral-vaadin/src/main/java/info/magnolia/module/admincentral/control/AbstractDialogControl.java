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
package info.magnolia.module.admincentral.control;

import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import info.magnolia.cms.core.Content;
import info.magnolia.module.admincentral.dialog.DialogControl;
import info.magnolia.module.admincentral.dialog.DialogTab;
import info.magnolia.module.admincentral.dialog.I18nAwareComponent;


import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

/**
 * Abstract base class for controls that have a label displayed to the left and a description placed below any
 * components that the implementing class wants to add.
 */
public abstract class AbstractDialogControl extends I18nAwareComponent implements DialogControl {

    private String name;
    private String label;
    private String description;
    private DialogTab parent;
    private boolean focus;

    private String width;
    private String height;

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        System.out.println("ADO: setWidth(" + width + ")");
        this.width = width;
        getFieldComponent().setWidth(width);
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DialogTab getParent() {
        return parent;
    }

    public I18nAwareComponent getI18nAwareParent() {
        return parent;
    }

    public void setParent(DialogTab parent) {
        this.parent = parent;
    }

    public final void create(Content storageNode, GridLayout grid) {

        grid.addComponent(new Label(getMessages().get(label)));

        VerticalLayout verticalLayout = new VerticalLayout();

        Window window = grid.getApplication() == null ? null : grid.getApplication().getMainWindow();
        verticalLayout.addComponent(createField(storageNode, window));

        if (description != null)
            verticalLayout.addComponent(new Label(description));

        grid.addComponent(verticalLayout);
    }

    /**
     * Creates new component representing element field. Implementing classes should ensure that call to this method returns always new component or fails when called multiple times and creating of multiple instances is not supported.
     */
    protected abstract Component createFieldComponent(Content storageNode, Window mainWindow);

    /**
     * Gets an existing component representing element field or null if such component was not yet created.
     */
    protected abstract Component getFieldComponent();

    public Component createField(Content storageNode, Window mainWindow) {
        Component field = createFieldComponent(storageNode, mainWindow);
        if (!StringUtils.isBlank(getWidth())) {
            field.setWidth(getWidth() +"px");
        }
        if (!StringUtils.isBlank(getHeight())) {
            field.setWidth(getHeight() +"px");
        }
        return field;
    }

    public void validate() {
    }

    public void save(Content storageNode) throws RepositoryException {
    }

    public void setFocus(boolean focus) {
        this.focus = focus;
    }

    public boolean isFocus() {
        return this.focus;
    }
}
