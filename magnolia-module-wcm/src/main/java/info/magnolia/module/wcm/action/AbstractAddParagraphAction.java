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

import org.apache.commons.lang.StringUtils;

import com.vaadin.Application;
import info.magnolia.cms.core.MetaData;
import info.magnolia.jcr.util.JCRMetadataUtil;
import info.magnolia.module.templating.Paragraph;
import info.magnolia.module.wcm.ContentSelection;
import info.magnolia.module.wcm.PageEditorHacks;
import info.magnolia.module.wcm.ParagraphSelectionDialog;
import info.magnolia.ui.admincentral.dialog.DialogPresenterFactory;
import info.magnolia.ui.admincentral.dialog.DialogSaveCallback;
import info.magnolia.ui.admincentral.dialog.view.DialogPresenter;
import info.magnolia.ui.model.action.ActionBase;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;

/**
 * Abstract base class for actions that open the paragraph selection dialog followed by the dialog for the selected
 * paragraph and finally add the paragraph to the page. Subclasses can do last minute changes to the node before it is
 * saved.
 *
 * @param <D> the definition type
 * @version $Id$
 */
public class AbstractAddParagraphAction<D extends ActionDefinition> extends ActionBase<D> {

    private Application application;
    private DialogPresenterFactory dialogPresenterFactory;
    private ContentSelection selection;

    public AbstractAddParagraphAction(D definition, Application application, DialogPresenterFactory dialogPresenterFactory, ContentSelection selection, Node node) {
        super(definition);
        this.application = application;
        this.dialogPresenterFactory = dialogPresenterFactory;
        this.selection = selection;
    }

    public void execute() throws ActionExecutionException {

        // TODO find the area and get available paragraphs from it

        // TODO if its not been created yet then we need another strategy for finding it, like name of the area in combination with the parent template/paragraph

        String paragraphs = "samplesHowToFTL,samplesFreemarkerParagraph";

        String[] paragraphsArray = StringUtils.split(paragraphs, ", \t\n");

        ParagraphSelectionDialog paragraphSelectionDialog = new ParagraphSelectionDialog(paragraphsArray) {
            @Override
            protected void onClosed(final Paragraph paragraph) {
                String dialogName = PageEditorHacks.getDialogUsedByParagraph(paragraph);
                if (dialogName != null) {

                    DialogPresenter dialogPresenter = dialogPresenterFactory.createDialog(dialogName);
                    dialogPresenter.setWorkspace(selection.getWorkspace());
                    dialogPresenter.setPath(selection.getPath());
                    dialogPresenter.setCollectionName(selection.getCollectionName());
                    dialogPresenter.setDialogSaveCallback(new DialogSaveCallback() {
                        @Override
                        public void onSave(Node node) {
                            try {
                                onPreSave(node, paragraph);
                                node.getSession().save();
                            } catch (RepositoryException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }
                        }
                    });
                    dialogPresenter.showDialog();
                }
            }
        };

        application.getMainWindow().addWindow(paragraphSelectionDialog);
    }

    public void setSelection(ContentSelection selection) {
        this.selection = selection;
    }

    protected void onPreSave(Node node, Paragraph paragraph) throws RepositoryException {
        MetaData metaData = JCRMetadataUtil.getMetaData(node);
        metaData.setTemplate(paragraph.getName());
    }
}
