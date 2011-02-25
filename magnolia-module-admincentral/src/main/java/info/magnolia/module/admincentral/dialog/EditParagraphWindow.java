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
package info.magnolia.module.admincentral.dialog;

import info.magnolia.module.templating.Paragraph;
import info.magnolia.module.templating.ParagraphManager;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

/**
 * Dialog for creating or editing a paragraph. Uses the paragraphs dialog to find a dialog definition from DialogRegistry.
 */
public class EditParagraphWindow extends DialogWindow {

    public EditParagraphWindow(String paragraphName, String repository, String path) throws RepositoryException {
        super(getDialogUsedByParagraph(paragraphName), repository, path);
    }

    public EditParagraphWindow(String paragraphName, Node node) throws RepositoryException {
        super(getDialogUsedByParagraph(paragraphName), node.getSession().getWorkspace().getName(), node.getPath());
    }

    private static String getDialogUsedByParagraph(String paragraphName) {
        if (StringUtils.isEmpty(paragraphName)) {
            throw new IllegalStateException("No paragraph selected.");
        }
        final Paragraph para = ParagraphManager.getInstance().getParagraphDefinition(paragraphName);
        if (para == null) {
            throw new IllegalStateException("No paragraph registered with name " + paragraphName);
        }
        final String dialogName;
        if (para.getDialog() != null) {
            dialogName = para.getDialog();
        } else {
            dialogName = para.getName();
        }
        return dialogName;
    }
}
