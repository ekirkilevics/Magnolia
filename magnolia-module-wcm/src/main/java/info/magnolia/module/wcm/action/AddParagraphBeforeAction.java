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

import com.vaadin.Application;
import info.magnolia.cms.util.PathUtil;
import info.magnolia.jcr.util.JCRUtil;
import info.magnolia.module.templating.Paragraph;
import info.magnolia.module.wcm.ContentSelection;
import info.magnolia.module.wcm.PageEditorHacks;
import info.magnolia.ui.admincentral.dialog.DialogPresenterFactory;
import info.magnolia.ui.framework.event.EventBus;

/**
 * Opens a dialog for adding a paragraph before another paragraph.
 *
 * @version $Id$
 */
public class AddParagraphBeforeAction extends AbstractAddParagraphAction<AddParagraphBeforeActionDefinition> {

    private ContentSelection selection;

    public AddParagraphBeforeAction(AddParagraphBeforeActionDefinition definition, Application application, DialogPresenterFactory dialogPresenterFactory, ContentSelection selection, EventBus eventBus) {
        super(definition, application, dialogPresenterFactory, selection, eventBus);
        this.selection = selection;
        super.setSelection(PageEditorHacks.convertFromPointingToParagraphToCollection(selection));
    }

    @Override
    protected void onPreSave(Node node, Paragraph paragraph) throws RepositoryException {
        JCRUtil.orderBefore(node, PathUtil.getFileName(selection.getPath()));
        super.onPreSave(node, paragraph);
    }
}
