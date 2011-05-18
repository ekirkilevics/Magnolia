/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.wcm;

import info.magnolia.cms.util.PathUtil;
import info.magnolia.module.templating.Paragraph;
import info.magnolia.module.wcm.editor.ContentSelection;

/**
 * Utility methods for the page editor that should be reviewed and placed elsewhere.
 *
 * @version $Id$
 */
public class PageEditorHacks {

    /**
     * Returns the name of the dialog that the paragraph uses.
     * <p/>
     * TODO this is used in a few more places (might be a good candidate to add to ParagraphManager)
     * See also {@link info.magnolia.module.admininterface.dialogs.ParagraphEditDialog}
     */
    public static String getDialogUsedByParagraph(Paragraph paragraph) {
        String dialogToUse = paragraph.getDialog();
        if (dialogToUse == null) {
            return paragraph.getName();
        }
        return dialogToUse;
    }

    public static ContentSelection convertFromPointingToParagraphToCollection(ContentSelection selection) {
        String collectionPath = PathUtil.getFolder(selection.getPath());
        String collectionName = PathUtil.getFileName(collectionPath);
        String path = PathUtil.getFolder(collectionPath);

        ContentSelection contentSelection = new ContentSelection();
        contentSelection.setType(selection.getType());
        contentSelection.setWorkspace(selection.getWorkspace());
        contentSelection.setPath(path);
        contentSelection.setCollectionName(collectionName);
        contentSelection.setParagraphs(selection.getParagraphs());
        contentSelection.setDialog(selection.getDialog());
        return contentSelection;
    }
}
