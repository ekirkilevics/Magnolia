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
package info.magnolia.module.wcm.editor.client;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.HTML;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;

/**
 * Client side implementation of the page editor. Outputs an iframe and injects ui widgets inside its content.
 *
 * @version $Id$
 */
public class VPageEditor extends HTML implements Paintable, EventListener {

    public static final String EDIT_MARKER = "cms:edit";
    public static final String AREA_MARKER = "cms:area";

    public static final String SELECTED_WORKSPACE = "selectedWorkspace";
    public static final String SELECTED_PATH = "selectedPath";
    public static final String SELECTED_COLLECTION_NAME = "selectedCollectionName";
    public static final String SELECTED_NODE_NAME = "selectedNodeName";
    public static final String OPEN_DIALOG = "open-dialog";
    public static final String UPDATE_SELECTION = "update-selection";
    public static final String ADD_PARAGRAPH = "addParagraph";
    public static final String MOVE_BEFORE = "moveBefore";
    public static final String SOURCE_PATH = "sourcePath";
    public static final String DESTINATION_PATH = "destinationPath";
    public static final String MOVE_AFTER = "moveAfter";
    public static final String MOVE = "move";
    public static final String PARAGRAPHS = "paragraphs";
    public static final String DIALOG = "dialog";

    private IFrameElement iFrameElement;
    private ApplicationConnection client;
    private String id;
    private AbstractBarWidget selectedBar;

    public VPageEditor() {
        iFrameElement = Document.get().createIFrameElement();
        iFrameElement.setAttribute("width", "100%");
        iFrameElement.setAttribute("height", "100%");
        iFrameElement.setFrameBorder(0);
        getElement().appendChild(iFrameElement);

        hookEvents(iFrameElement, this);
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {

        // Save details
        this.client = client;
        id = uidl.getId();

        if (this.client.updateComponent(this, uidl, true)) {
            return;
        }
        String url = uidl.getStringAttribute("url");

        iFrameElement.setSrc(url);

        this.selectedBar = null;
    }

    @Override
    public void onBrowserEvent(Event event) {
        super.onBrowserEvent(event);
    }

    /**
     * Inspired by {@link com.google.gwt.user.client.ui.impl.FormPanelImpl}.
     * <p/>
     * TODO probably doesn't work in IE6 as FormPanelImpl has a special impl for it.
     */
    public native void hookEvents(Element iframe, VPageEditor listener) /*-{
        if (iframe) {
            iframe.onload = $entry(function() {
                listener.@info.magnolia.module.wcm.editor.client.VPageEditor::onFrameLoad()();
            });
        }
    }-*/;

    public void onFrameLoad() {
        // TODO when the user navigates in the iframe we need to respond accordingly
        Element documentElement = iFrameElement.getContentDocument().getDocumentElement();
        detectCmsTag(documentElement, null);
        // TODO ideally we would do updateSelection() to select the page here but we don't have access to website+path
        // TODO so this is now done in PageEditorActivity
        // TODO this could maybe be solved with a special page marker, but that requires a new directive
    }

    private void detectCmsTag(Element element, AreaBarWidget parentBar) {

        for (int i = 0; i < element.getChildCount(); i++) {
            Node childNode = element.getChild(i);
            if (childNode.getNodeType() == Element.ELEMENT_NODE) {
                Element child = (Element) childNode;

                if (child.getTagName().equalsIgnoreCase(EDIT_MARKER)) {
                    if (parentBar != null && parentBar.getType().equals("slot")) {
                        parentBar.mutateIntoSlotBar(child);
                    } else {
                        EditBarWidget editBarWidget = new EditBarWidget(parentBar, this, child);
                        editBarWidget.attach(child);
                    }
                } else if (child.getTagName().equalsIgnoreCase(AREA_MARKER)) {
                    AreaBarWidget areaBarWidget = new AreaBarWidget(parentBar, this, child);
                    areaBarWidget.attach(child);
                    parentBar = areaBarWidget;
                }

                detectCmsTag(child, parentBar);
            }
        }
    }

    public void openDialog(String dialog, String workspace, String path, String collectionName, String nodeName) {
        updateVariable(OPEN_DIALOG, "dummy");
        updateVariable(SELECTED_WORKSPACE, workspace);
        updateVariable(SELECTED_PATH, path);
        updateVariable(SELECTED_COLLECTION_NAME, collectionName);
        updateVariable(SELECTED_NODE_NAME, nodeName);
        updateVariable(DIALOG, dialog);
        client.sendPendingVariableChanges();
    }

    public void updateSelection(AbstractBarWidget selectedBar, String type, String workspace, String path, String collectionName, String nodeName, String paragraphs, String dialog) {
        if (this.selectedBar != null && (this.selectedBar != selectedBar)) {
            this.selectedBar.deselect();
        }
        this.selectedBar = selectedBar;
        updateVariable(UPDATE_SELECTION, type);
        updateVariable(SELECTED_WORKSPACE, workspace);
        updateVariable(SELECTED_PATH, path);
        updateVariable(SELECTED_COLLECTION_NAME, collectionName);
        updateVariable(SELECTED_NODE_NAME, nodeName);
        updateVariable(PARAGRAPHS, paragraphs);
        updateVariable(DIALOG, dialog);
        client.sendPendingVariableChanges();
    }

    public void addParagraph(String workspace, String path, String collectionName, String nodeName, String paragraphs) {
        updateVariable(ADD_PARAGRAPH, "dummy");
        updateVariable(SELECTED_WORKSPACE, workspace);
        updateVariable(SELECTED_PATH, path);
        updateVariable(SELECTED_COLLECTION_NAME, collectionName);
        updateVariable(SELECTED_NODE_NAME, nodeName);
        updateVariable(PARAGRAPHS, paragraphs);
        client.sendPendingVariableChanges();
    }

    public void moveParagraph(String workspaceName, String sourcePath, String destinationPath) {
        updateVariable(MOVE, "dummy");
        updateVariable(SELECTED_WORKSPACE, workspaceName);
        updateVariable(SOURCE_PATH, sourcePath);
        updateVariable(DESTINATION_PATH, destinationPath);
        client.sendPendingVariableChanges();
    }

    public void moveParagraphBefore(String workspaceName, String sourcePath, String destinationPath) {
        updateVariable(MOVE_BEFORE, "dummy");
        updateVariable(SELECTED_WORKSPACE, workspaceName);
        updateVariable(SOURCE_PATH, sourcePath);
        updateVariable(DESTINATION_PATH, destinationPath);
        client.sendPendingVariableChanges();
    }

    public void moveParagraphAfter(String workspaceName, String sourcePath, String destinationPath) {
        updateVariable(MOVE_AFTER, "dummy");
        updateVariable(SELECTED_WORKSPACE, workspaceName);
        updateVariable(SOURCE_PATH, sourcePath);
        updateVariable(DESTINATION_PATH, destinationPath);
        client.sendPendingVariableChanges();
    }

    private void updateVariable(String variableName, String value) {
        // We don't add it if the value is null since it appears as the string "null" on the server side
        // See Vaadin ticket #6968
        if (value != null) {
            client.updateVariable(id, variableName, value, false);
        }
    }
}
