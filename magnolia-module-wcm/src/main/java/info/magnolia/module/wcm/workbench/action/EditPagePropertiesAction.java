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
package info.magnolia.module.wcm.workbench.action;

import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MetaData;
import info.magnolia.jcr.util.MetaDataUtil;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.templating.Template;
import info.magnolia.module.templating.TemplateManager;
import info.magnolia.ui.admincentral.dialog.DialogPresenterFactory;
import info.magnolia.ui.admincentral.dialog.view.DialogPresenter;
import info.magnolia.ui.admincentral.tree.action.TreeAction;
import info.magnolia.ui.model.action.ActionBase;
import info.magnolia.ui.model.action.ActionExecutionException;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;


/**
 * Opens a dialog for editing the page properties of a page.
 *
 * @version $Id$
 */
public class EditPagePropertiesAction extends ActionBase<EditPagePropertiesActionDefinition> implements TreeAction {

    private DialogPresenterFactory dialogPresenterFactory;
    private TemplateManager templateManager;

    private Node nodeToEdit;

    public EditPagePropertiesAction(EditPagePropertiesActionDefinition definition, DialogPresenterFactory dialogPresenterFactory, TemplateManager templateManager, Node nodeToEdit) {
        super(definition);
        this.dialogPresenterFactory = dialogPresenterFactory;
        this.templateManager = templateManager;
        this.nodeToEdit = nodeToEdit;
    }

    @Override
    public boolean isAvailable(Item item) throws RepositoryException {
        return item.isNode() && NodeUtil.isNodeType((Node) item, ItemType.CONTENT.getSystemName()) && getDialogName() != null;
    }

    @Override
    public void execute() throws ActionExecutionException {
        try {

            String dialogName = getDialogName();

            DialogPresenter dialogPresenter = dialogPresenterFactory.createDialog(dialogName);
            dialogPresenter.setNode(nodeToEdit);
            dialogPresenter.showDialog();

        } catch (RepositoryException e) {
            throw new ActionExecutionException("Can't open dialog.", e);
        }
    }

    private String getDialogName() {
        MetaData metaData = MetaDataUtil.getMetaData(nodeToEdit);
        String template = metaData.getTemplate();
        Template templateDefinition = templateManager.getTemplateDefinition(template);
        return templateDefinition != null ? templateDefinition.getDialog() : null;
    }
}
