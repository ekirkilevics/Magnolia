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
package info.magnolia.module.wcm.editor;

import java.util.Map;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;
import info.magnolia.context.MgnlContext;
import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.module.wcm.editor.client.VPageEditor;
import info.magnolia.ui.admincentral.dialog.view.DialogPresenter;
import info.magnolia.ui.framework.event.EventBus;

/**
 * Server side vaadin component for the page editor.
 */
@ClientWidget(VPageEditor.class)
public class PageEditor extends AbstractComponent {

    private EventBus eventBus;
    private DialogPresenter dialogPresenter;
    private String url;

    public PageEditor(EventBus eventBus, DialogPresenter dialogPresenter, String url) {
        this.eventBus = eventBus;
        this.dialogPresenter = dialogPresenter;
        this.url = url;
        setSizeFull();
    }

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        super.changeVariables(source, variables);
        if (variables.containsKey(VPageEditor.OPEN_DIALOG)) {
            String dialog = (String) variables.get(VPageEditor.OPEN_DIALOG);
            String workspace = (String) variables.get(VPageEditor.SELECTED_WORKSPACE);
            String path = (String) variables.get(VPageEditor.SELECTED_PATH);
            openDialog(dialog, workspace, path);
        }
        if (variables.containsKey(VPageEditor.UPDATE_SELECTION)) {
            String type = (String) variables.get(VPageEditor.UPDATE_SELECTION);
            String workspace = (String) variables.get(VPageEditor.SELECTED_WORKSPACE);
            String path = (String) variables.get(VPageEditor.SELECTED_PATH);
            String collectionName = (String) variables.get(VPageEditor.SELECTED_COLLECTION_NAME);
            String nodeName = (String) variables.get(VPageEditor.SELECTED_NODE_NAME);
            eventBus.fireEvent(new SelectionChangedEvent(type));
        }
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        target.addAttribute("url", url);
    }

    private void openDialog(String dialog, String workspace, String path) {
        try {
            Node node = MgnlContext.getJCRSession(workspace).getNode(path);
            dialogPresenter.showDialog(node, dialog);
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    private void addParagraph(String workspace, String path, String paragraphs) {

    }
}
