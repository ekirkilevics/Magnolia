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
package info.magnolia.module.templatingcomponents.componentsx;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.inline.BarEdit;
import info.magnolia.cms.gui.inline.ButtonEdit;

import java.io.IOException;
import java.io.Writer;

import javax.jcr.RepositoryException;

/**
 * This describes an "edit bar" for paragraphs. It typically displays edit, move and delete buttons. The dialog to use
 * is normally deduced from the given content node, but can be overridden by a specific one.
 * 
 * @version $Id$
 */
public class EditBar extends AbstractAuthoringUiComponent {
    private static final String DEFAULT_MOVE_LABEL = "buttons.move";
    private static final String DEFAULT_DELETE_LABEL = "buttons.delete";
    private static final String DEFAULT_EDIT_LABEL = "buttons.edit";

    /**
     * @param serverCfg
     * @param aggState
     * @param specificContent if null, will deduce a default
     * @param specificDialogName if null, deduced from paragraph definition of current node
     * @param editButtonLabel if null, will use default
     * @param enableMoveButton true should be the default
     * @param enableDeleteButton true should be the default
     */
    public static EditBar make(ServerConfiguration serverCfg, AggregationState aggState, Content specificContent, String specificDialogName, String editButtonLabel, boolean enableMoveButton, boolean enableDeleteButton) {
        // TODO - it would be nicer if this was available from some RenderingContext
        final boolean isInSingleton = SingletonParagraphBar.isInSingleton();

        final EditBar bar = new EditBar(serverCfg, aggState);
        if (specificContent != null) {
            bar.setContent(specificContent);
        } else {
            bar.setContent(bar.currentContent());
        }

        if (specificDialogName != null) {
            bar.setSpecificDialogName(specificDialogName);
        }

        if (editButtonLabel != null) {
            bar.setEditButtonLabel(editButtonLabel);
        }

        if (!isInSingleton) {
            bar.setEnableMoveButton(enableMoveButton);
        } else {
            // force move to false for singletons
            bar.setEnableMoveButton(false);
        }

        bar.setEnableDeleteButton(enableDeleteButton);

        return bar;
    }

    private Content content;
    private String specificDialogName;
    private String editButtonLabel = DEFAULT_EDIT_LABEL;
    private boolean enableMoveButton = true;
    private boolean enableDeleteButton = true;

    public EditBar(ServerConfiguration serverConfiguration, AggregationState aggregationState) {
        super(serverConfiguration, aggregationState);
    }

    public void setContent(Content content) {
        this.content = content;
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
    protected void doRender(Appendable out) throws IOException, RepositoryException {
        final BarEdit bar = new BarEdit();
        final String paragraphTemplateName = content.getMetaData().getTemplate();
        // it's called "setParagraph", but have a look further, once you reach ButtonEdit, this is called "dialog".
        if (specificDialogName != null) {
            bar.setParagraph(specificDialogName);
        } else {
            bar.setParagraph(paragraphTemplateName);
        }

        // needed for move ...
        final Content parent = content.getParent();
        final Content parentParent = parent.getParent();

        bar.setPath(parentParent.getHandle());
        bar.setNodeCollectionName(parent.getName());
        bar.setNodeName(content.getName());
        // bar.setPath(target.getHandle()); // would work for edit but not for move

        // bar.setDefaultButtons();
        if (enableMoveButton) {
            // doing the below instead of setButtonMove() for clarity, but see comment above for the bar.setPath method
            bar.setButtonMove(parent.getName(), content.getName());
            bar.getButtonMove().setLabel(getMessage(content, DEFAULT_MOVE_LABEL));
        } else {
            // buttonMove is initially set to new Button, so if we don't do this, we end up with a label-less, action-less, button
            bar.setButtonMove(null);
        }
        if (enableDeleteButton) {
            // simplified delete function -
            bar.setButtonDelete(content.getHandle());
            // these paths would otherwise get concatenated in inline.js#mgnlDeleteNode:
            // parentParent.getHandle(), targetParent.getName(), target.getName());
            bar.getButtonDelete().setLabel(getMessage(content, DEFAULT_DELETE_LABEL));
        } else {
            // buttonDelete is initially set to new Button, so if we don't do this, we end up with a label-less, action-less, button
            bar.setButtonDelete((Button) null);
        }

        bar.setButtonEdit();
        final ButtonEdit edit = bar.getButtonEdit();
        // TODO - fix circular dependency edit.setDialogPath(ParagraphSelectDialog.EDITPARAGRAPH_DIALOG_URL);
        edit.setDialogPath(".magnolia/dialogs/editParagraph.html");

        // TODO - yes this is a bit ugly - 1) we should in fact open the correct dialog immediately instead of faking the paragraph parameter - 2) the gui elements should not have defaults nor know anything about urls and onclick functions
        // TODO - test with specific dialog
        //  if (this.dialog == null) {
        edit.setDefaultOnclick(); // re-set the onclick after having set the dialog path.
        edit.setLabel(getMessage(content, editButtonLabel));


        // TODO : display useful paragraph info (if we had the ParagraphDefinition instance, maybe ?)

        bar.placeDefaultButtons();

        bar.drawHtml((Writer) out);
    }

}
