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
package info.magnolia.module.wcm.action;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

import info.magnolia.context.MgnlContext;
import info.magnolia.module.wcm.ContentSelection;
import info.magnolia.module.wcm.PageChangedEvent;
import info.magnolia.ui.admincentral.dialog.DialogPresenterFactory;
import info.magnolia.ui.admincentral.dialog.DialogSaveCallback;
import info.magnolia.ui.admincentral.dialog.view.DialogPresenter;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.model.action.ActionBase;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;

/**
 * Abstract base class for actions that open dialogs for editing content.
 *
 * @param <D> the definition type
 * @version $Id$
 */
public abstract class AbstractEditAction<D extends ActionDefinition> extends ActionBase<D> {

    private DialogPresenterFactory dialogPresenterFactory;
    private ContentSelection selection;
    private EventBus eventBus;

    public AbstractEditAction(D definition, DialogPresenterFactory dialogPresenterFactory, ContentSelection selection, EventBus eventBus) {
        super(definition);
        this.dialogPresenterFactory = dialogPresenterFactory;
        this.selection = selection;
        this.eventBus = eventBus;
    }

    @Override
    public void execute() throws ActionExecutionException {

        String dialogName;
        try {
            dialogName = getDialog();
        } catch (RepositoryException e) {
            throw new ActionExecutionException(e);
        }

        DialogPresenter dialogPresenter = dialogPresenterFactory.createDialog(dialogName);
        dialogPresenter.setWorkspace(selection.getWorkspace());
        dialogPresenter.setPath(selection.getPath());
        dialogPresenter.setCollectionName(selection.getCollectionName());
        dialogPresenter.setNodeName(selection.getNodeName());
        dialogPresenter.setDialogSaveCallback(new DialogSaveCallback() {
            @Override
            public void onSave(Node node) {
                eventBus.fireEvent(new PageChangedEvent());
            }
        });
        dialogPresenter.showDialog();
    }

    protected abstract String getDialog() throws RepositoryException;

    // TODO This is duplicated in DialogEditorPresenter
    protected Node getNode() throws RepositoryException {
        Node node;
        try {
            node = MgnlContext.getJCRSession(selection.getWorkspace()).getNode(selection.getPath());
        } catch (PathNotFoundException e) {
            return null;
        }
        if (StringUtils.isNotEmpty(selection.getCollectionName())) {
            if (node.hasNode(selection.getCollectionName())) {
                node = node.getNode(selection.getCollectionName());
            } else {
                return null;
            }
        }
        if (StringUtils.isNotEmpty(selection.getNodeName())) {
            if (node.hasNode(selection.getNodeName())) {
                node = node.getNode(selection.getNodeName());
            } else {
                return null;
            }
        }
        return node;
    }
}
