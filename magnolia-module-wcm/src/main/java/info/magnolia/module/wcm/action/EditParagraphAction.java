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
import javax.jcr.RepositoryException;

import info.magnolia.cms.core.MetaData;
import info.magnolia.jcr.util.JCRMetadataUtil;
import info.magnolia.module.templating.Paragraph;
import info.magnolia.module.templating.ParagraphManager;
import info.magnolia.module.wcm.ContentSelection;
import info.magnolia.module.wcm.PageEditorHacks;
import info.magnolia.ui.admincentral.dialog.DialogPresenterFactory;
import info.magnolia.ui.admincentral.dialog.DialogSaveCallback;
import info.magnolia.ui.admincentral.dialog.view.DialogPresenter;
import info.magnolia.ui.model.action.ActionBase;
import info.magnolia.ui.model.action.ActionExecutionException;

/**
 * Opens a dialog for editing a paragraph.
 *
 * @version $Id$
 */
public class EditParagraphAction extends ActionBase<EditParagraphActionDefinition> {

    private Node node;
    private DialogPresenterFactory dialogPresenterFactory;
    private ContentSelection selection;
    private ParagraphManager paragraphManager;

    public EditParagraphAction(EditParagraphActionDefinition definition, Node node, DialogPresenterFactory dialogPresenterFactory, ContentSelection selection, ParagraphManager paragraphManager) {
        super(definition);
        this.node = node;
        this.dialogPresenterFactory = dialogPresenterFactory;
        this.selection = selection;
        this.paragraphManager = paragraphManager;
    }

    public void execute() throws ActionExecutionException {

        String template = JCRMetadataUtil.getMetaData(node).getTemplate();
        final Paragraph paragraph = paragraphManager.getParagraphDefinition(template);
        String dialogName = PageEditorHacks.getDialogUsedByParagraph(paragraph);

        DialogPresenter dialogPresenter = dialogPresenterFactory.createDialog(dialogName);
        dialogPresenter.setWorkspace(selection.getWorkspace());
        dialogPresenter.setPath(selection.getPath());
        dialogPresenter.setCollectionName(selection.getCollectionName());
        dialogPresenter.setDialogSaveCallback(new DialogSaveCallback() {
            @Override
            public void onSave(Node node) {
                try {
                    MetaData metaData = JCRMetadataUtil.getMetaData(node);
                    metaData.setTemplate(paragraph.getName());
                    node.getSession().save();
                } catch (RepositoryException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        });
        dialogPresenter.showDialog();
    }
}
