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
package info.magnolia.ui.admincentral.dialog.field;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.vaadin.Application;
import com.vaadin.event.FieldEvents;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Field;
import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.ui.admincentral.dialog.view.DialogView.Presenter;
import info.magnolia.ui.admincentral.jcr.view.builder.JcrViewBuilderProvider;
import info.magnolia.ui.model.dialog.definition.DialogDefinition;
import info.magnolia.ui.model.dialog.definition.FieldDefinition;
import info.magnolia.ui.model.dialog.definition.LinkFieldDefinition;
import info.magnolia.ui.model.dialog.definition.TabDefinition;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.model.workbench.registry.WorkbenchRegistry;

/**
 * The link field allows you to create a link to content stored in Magnolia. You can browse any specified workspace and
 * select a content node to link to such as a page (website), file (dms) or data item (data).
 *
 * @version $Id$
 */
public class DialogLinkField extends AbstractVaadinFieldDialogField implements LinkSelectWindow.Presenter {

    // TODO do we have to required a WorkbenchDefinition, can we take workspace as a string only?

    private Application application;
    private WorkbenchRegistry workbenchRegistry;
    private JcrViewBuilderProvider jcrViewBuilderProvider;
    private LinkSelectWindow linkSelectWindow;
    private Node selectedNode;
    private TextAndButtonField linkField;

    public DialogLinkField(DialogDefinition dialogDefinition, TabDefinition tabDefinition, FieldDefinition fieldDefinition, Presenter presenter, Application application, WorkbenchRegistry workbenchRegistry, JcrViewBuilderProvider jcrViewBuilderProvider) {
        super(dialogDefinition, tabDefinition, fieldDefinition, presenter);
        this.application = application;
        this.workbenchRegistry = workbenchRegistry;
        this.jcrViewBuilderProvider = jcrViewBuilderProvider;
    }

    @Override
    public Class<?> getDefaultFieldType(FieldDefinition fieldDefinition) {
        return String.class;
    }

    @Override
    public Field getField() {
        final LinkFieldDefinition definition = (LinkFieldDefinition) getFieldDefinition();
        linkField = new TextAndButtonField();

        linkField.getTextField().addListener(new FieldEvents.FocusListener() {
            @Override
            public void focus(FieldEvents.FocusEvent event) {
                getDialogPresenter().onFocus(DialogLinkField.this);
            }
        });

        linkField.getButton().setCaption(MessagesUtil.getWithDefault(definition.getButtonLabel(), definition.getButtonLabel()));
        linkField.getButton().addListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                onButtonClick();

            }
        });
        return linkField;
    }

    private void onButtonClick() {
        WorkbenchDefinition workbenchDefinition = workbenchRegistry.getWorkbench("website");
        linkSelectWindow = new LinkSelectWindow(this, application, jcrViewBuilderProvider, workbenchDefinition);
        linkSelectWindow.select((String) linkField.getValue());
    }

    @Override
    public void onCancel() {
        linkSelectWindow.close();
        linkSelectWindow = null;
    }

    @Override
    public void onClose() {
        selectedNode = linkSelectWindow.getSelectedNode();
        try {
            linkField.setValue(selectedNode.getPath());
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
        linkSelectWindow.close();
        linkSelectWindow = null;
    }
}
