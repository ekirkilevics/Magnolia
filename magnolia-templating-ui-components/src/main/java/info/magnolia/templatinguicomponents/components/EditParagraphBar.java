/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.templatinguicomponents.components;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.gui.inline.BarEdit;

import java.io.IOException;
import java.io.Writer;

/**
 * This describes an "edit bar" for paragraphs. It typically displays edit, move and delete buttons.
 * The dialog to use is normally deduced from the given content node, but can be overridden by a specific one.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $) 
 */
public class EditParagraphBar extends AbstractAuthoringUiComponent {
    private String specificDialogName;
    private String editButtonLabel = "buttons.edit";
    private boolean enableMoveButton = true;
    private boolean enableDeleteButton = true;

    public EditParagraphBar(ServerConfiguration serverConfiguration, AggregationState aggregationState) {
        super(serverConfiguration, aggregationState);
    }

    public void setSpecificDialogName(String specificDialogName) {
        this.specificDialogName = specificDialogName;
    }

    public void setEditButtonLabel(String editButtonLabel) {
        this.editButtonLabel = editButtonLabel;
    }

    public void setEnableMoveButton(boolean enableMoveButton) {
        this.enableMoveButton = enableMoveButton;
    }

    public void setEnableDeleteButton(boolean enableDeleteButton) {
        this.enableDeleteButton = enableDeleteButton;
    }

    @Override
    protected void doRender(Appendable out) throws IOException {
        final BarEdit bar = new BarEdit();
        final String paragraphTemplateName = getTarget().getMetaData().getTemplate();
        // it's called "setParagraph", but have a look further, once you reach ButtonEdit, this is called "dialog".
        if (specificDialogName != null) {
            bar.setParagraph(specificDialogName);
        } else {
            bar.setParagraph(paragraphTemplateName);
        }

        final String targetPath = getTarget().getHandle();

        bar.setPath(targetPath);

        // needed for delete and/or move ? - todo : delete still doesn't work - might need to fix the path variable too
        final int lastSlash = targetPath.lastIndexOf('/');
        final int secondLastSlash = targetPath.lastIndexOf('/', lastSlash - 1);
        final String lastPortionPath = targetPath.substring(lastSlash + 1);
        final String secondLastPortionPath = targetPath.substring(secondLastSlash + 1, lastSlash);
        bar.setNodeCollectionName(secondLastPortionPath);
        bar.setNodeName(lastPortionPath);

        bar.setDefaultButtons();

        //bar.getButtonEdit().setDialogPath(ParagraphSelectDialog.EDITPARAGRAPH_DIALOG_URL);
        bar.getButtonEdit().setDialogPath(".magnolia/dialogs/editParagraph.html");
        bar.getButtonEdit().setLabel(editButtonLabel);

        if (!enableMoveButton) {
            bar.setButtonMove(null);
        }

        if (!enableDeleteButton) {
            bar.setButtonDelete(null);
        }

        // TODO : display useful paragraph info (if we had the ParagraphDefinition instance, maybe ?)

        bar.placeDefaultButtons();

        bar.drawHtml((Writer) out);
    }

}
