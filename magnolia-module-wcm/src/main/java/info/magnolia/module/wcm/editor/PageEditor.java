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

import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.module.wcm.PageEditorPresenter;
import info.magnolia.module.wcm.editor.client.VPageEditor;

import java.util.Map;

import javax.jcr.RepositoryException;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;

/**
 * Server side vaadin component for the page editor.
 *
 * @version $Id$
 */
@ClientWidget(VPageEditor.class)
public class PageEditor extends AbstractComponent {

    private String url;
    private PageEditorPresenter pageEditorPresenter;

    public PageEditor(PageEditorPresenter pageEditorPresenter, String url) {
        this.pageEditorPresenter = pageEditorPresenter;
        this.url = url;
        setSizeFull();
    }

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        super.changeVariables(source, variables);

        if (variables.containsKey(VPageEditor.OPEN_DIALOG)) {
            String workspace = (String) variables.get(VPageEditor.SELECTED_WORKSPACE);
            String path = (String) variables.get(VPageEditor.SELECTED_PATH);
            String collectionName = (String) variables.get(VPageEditor.SELECTED_COLLECTION_NAME);
            String nodeName = (String) variables.get(VPageEditor.SELECTED_NODE_NAME);
            String dialog = (String) variables.get(VPageEditor.DIALOG);
            pageEditorPresenter.openDialog(dialog, workspace, path, collectionName, nodeName);
        }
        if (variables.containsKey(VPageEditor.UPDATE_SELECTION)) {
            String type = (String) variables.get(VPageEditor.UPDATE_SELECTION);
            String workspace = (String) variables.get(VPageEditor.SELECTED_WORKSPACE);
            String path = (String) variables.get(VPageEditor.SELECTED_PATH);
            String collectionName = (String) variables.get(VPageEditor.SELECTED_COLLECTION_NAME);
            String nodeName = (String) variables.get(VPageEditor.SELECTED_NODE_NAME);
            String paragraphs = (String) variables.get(VPageEditor.PARAGRAPHS);
            String dialog = (String) variables.get(VPageEditor.DIALOG);
            pageEditorPresenter.selectionChanged(SelectionType.valueOf(type.toUpperCase()), workspace, path, collectionName, nodeName, paragraphs, dialog);
        }
        if (variables.containsKey(VPageEditor.ADD_PARAGRAPH)) {
            String workspace = (String) variables.get(VPageEditor.SELECTED_WORKSPACE);
            String path = (String) variables.get(VPageEditor.SELECTED_PATH);
            String collectionName = (String) variables.get(VPageEditor.SELECTED_COLLECTION_NAME);
            String nodeName = (String) variables.get(VPageEditor.SELECTED_NODE_NAME);
            String paragraphs = (String) variables.get(VPageEditor.PARAGRAPHS);
            String dialog = (String) variables.get(VPageEditor.DIALOG);
            pageEditorPresenter.addParagraph(workspace, path, collectionName, nodeName, paragraphs, dialog);
        }
        if (variables.containsKey(VPageEditor.MOVE)) {
            String workspace = (String) variables.get(VPageEditor.SELECTED_WORKSPACE);
            String sourcePath = (String) variables.get(VPageEditor.SOURCE_PATH);
            String destinationPath = (String) variables.get(VPageEditor.DESTINATION_PATH);
            try {
                pageEditorPresenter.moveParagraph(workspace, sourcePath, destinationPath);
            } catch (RepositoryException e) {
                throw new RuntimeRepositoryException(e);
            }
        }
        if (variables.containsKey(VPageEditor.MOVE_BEFORE)) {
            String workspace = (String) variables.get(VPageEditor.SELECTED_WORKSPACE);
            String sourcePath = (String) variables.get(VPageEditor.SOURCE_PATH);
            String destinationPath = (String) variables.get(VPageEditor.DESTINATION_PATH);
            try {
                pageEditorPresenter.moveParagraphBefore(workspace, sourcePath, destinationPath);
            } catch (RepositoryException e) {
                throw new RuntimeRepositoryException(e);
            }
        }
        if (variables.containsKey(VPageEditor.MOVE_AFTER)) {
            String workspace = (String) variables.get(VPageEditor.SELECTED_WORKSPACE);
            String sourcePath = (String) variables.get(VPageEditor.SOURCE_PATH);
            String destinationPath = (String) variables.get(VPageEditor.DESTINATION_PATH);
            try {
                pageEditorPresenter.moveParagraphAfter(workspace, sourcePath, destinationPath);
            } catch (RepositoryException e) {
                throw new RuntimeRepositoryException(e);
            }
        }
    }

    public void reload() {
        // this.requestRepaint();
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        target.addAttribute("url", url);
    }
}
