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
package info.magnolia.module.admincentral.control;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import info.magnolia.cms.core.Content;

import javax.jcr.RepositoryException;

/**
 * Control for selecting a page to link to.
 *
 * TODO: needs to open subwindow with a treetable where the editor can browse to the desired page.
 */
public abstract class AbstractLinkControl extends AbstractDialogControl {

    private boolean required;
    private String requiredErrorMessage;

    private TextField field;

    @Override
    protected Component getFieldComponent() {
        return field;
    }

    @Override
    public Component createFieldComponent(Content storageNode, final Window mainWindow) {

        if (field != null) {
            throw new UnsupportedOperationException("Multiple calls to component creation are not supported.");
        }

        field = new TextField();
        field.setRequired(required);
        field.setRequiredError(requiredErrorMessage);

        Button button = new Button();
        button.setCaption("Browse..");
        button.addListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {

                Window window = new Window("Choose page");
                window.setModal(true);
                window.setClosable(true);

                mainWindow.addWindow(window);
            }
        });

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addComponent(field);
        horizontalLayout.addComponent(button);

        if (isFocus()) {
            field.focus();
        }

        return horizontalLayout;
    }

    public void validate() {
        field.validate();
    }

    public void save(Content storageNode) throws RepositoryException {
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getRequiredErrorMessage() {
        return requiredErrorMessage;
    }

    public void setRequiredErrorMessage(String requiredErrorMessage) {
        this.requiredErrorMessage = requiredErrorMessage;
    }
}
