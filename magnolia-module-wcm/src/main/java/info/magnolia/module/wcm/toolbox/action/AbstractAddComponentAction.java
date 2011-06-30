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
package info.magnolia.module.wcm.toolbox.action;

import info.magnolia.cms.core.MetaData;
import info.magnolia.jcr.util.MetaDataUtil;
import info.magnolia.module.wcm.editor.ComponentSelectionDialog;
import info.magnolia.module.wcm.editor.ContentSelection;
import info.magnolia.module.wcm.editor.PageChangedEvent;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.ui.admincentral.dialog.DialogPresenterFactory;
import info.magnolia.ui.admincentral.dialog.DialogSaveCallback;
import info.magnolia.ui.admincentral.dialog.view.DialogPresenter;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.model.action.ActionBase;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.vaadin.Application;

/**
 * Abstract base class for actions that open the component selection dialog followed by the dialog for the selected
 * component and finally add the component to the page. Subclasses can do last minute changes to the node before it is
 * saved.
 *
 * @param <D> the definition type
 * @version $Id$
 */
public class AbstractAddComponentAction<D extends ActionDefinition> extends ActionBase<D> {

    private Application application;
    private DialogPresenterFactory dialogPresenterFactory;
    private ContentSelection selection;
    private EventBus eventBus;

    public AbstractAddComponentAction(D definition, Application application, DialogPresenterFactory dialogPresenterFactory, ContentSelection selection, EventBus eventBus) {
        super(definition);
        this.application = application;
        this.dialogPresenterFactory = dialogPresenterFactory;
        this.selection = selection;
        this.eventBus = eventBus;
    }

    @Override
    public void execute() throws ActionExecutionException {

        ComponentSelectionDialog componentSelectionDialog = new ComponentSelectionDialog(selection.getAvailableComponents()) {
            @Override
            protected void onClosed(final TemplateDefinition definition) {
                String dialogName = definition.getDialog();
                if (dialogName != null) {

                    DialogPresenter dialogPresenter = dialogPresenterFactory.createDialog(dialogName);
                    dialogPresenter.setWorkspace(selection.getWorkspace());
                    dialogPresenter.setPath(selection.getPath());
                    dialogPresenter.setCollectionName(selection.getCollectionName());
                    dialogPresenter.setNodeName(selection.getNodeName());
                    dialogPresenter.setDialogSaveCallback(new DialogSaveCallback() {
                        @Override
                        public void onSave(Node node) {
                            try {
                                onPreSave(node, definition);
                                node.getSession().save();
                                eventBus.fireEvent(new PageChangedEvent());
                            } catch (RepositoryException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }
                        }
                    });
                    dialogPresenter.showDialog();
                }
            }
        };

        application.getMainWindow().addWindow(componentSelectionDialog);
    }

    public void setSelection(ContentSelection selection) {
        this.selection = selection;
    }

    protected void onPreSave(Node node, TemplateDefinition templateDefinition) throws RepositoryException {
        MetaData metaData = MetaDataUtil.getMetaData(node);
        metaData.setTemplate(templateDefinition.getId());
    }
}