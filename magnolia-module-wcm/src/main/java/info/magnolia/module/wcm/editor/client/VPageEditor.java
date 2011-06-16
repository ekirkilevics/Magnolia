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

    public static final String MARKER_PAGE = "cms:page";
    public static final String MARKER_EDIT = "cms:edit";
    public static final String MARKER_AREA = "cms:area";

    public static final String AREA_TYPE_LIST = "list";
    public static final String AREA_TYPE_SINGLE = "single";

    public static final String SELECTION_TYPE_PAGE = "PAGE";
    public static final String SELECTION_TYPE_AREA_LIST = "AREA_LIST";
    public static final String SELECTION_TYPE_AREA_SINGLE = "AREA_SINGLE";
    public static final String SELECTION_TYPE_COMPONENT_IN_LIST = "COMPONENT_IN_LIST";
    public static final String SELECTION_TYPE_COMPONENT_IN_SINGLE = "COMPONENT_IN_SINGLE";

    public static final String ACTION_OPEN_DIALOG = "openDialog";
    public static final String ACTION_UPDATE_SELECTION = "updateSelection";
    public static final String ACTION_ADD_COMPONENT = "addComponent";
    public static final String ACTION_MOVE = "move";
    public static final String ACTION_MOVE_BEFORE = "moveBefore";
    public static final String ACTION_MOVE_AFTER = "moveAfter";

    public static final String PARAM_SELECTED_WORKSPACE = "selectedWorkspace";
    public static final String PARAM_SELECTED_PATH = "selectedPath";
    public static final String PARAM_SELECTED_COLLECTION_NAME = "selectedCollectionName";
    public static final String PARAM_SELECTED_NODE_NAME = "selectedNodeName";
    public static final String PARAM_AVAILABLE_COMPONENTS = "components";
    public static final String PARAM_DIALOG = "dialog";
    public static final String PARAM_SOURCE_PATH = "sourcePath";
    public static final String PARAM_DESTINATION_PATH = "destinationPath";

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

                if (child.getTagName().equalsIgnoreCase(MARKER_PAGE)) {
                    PageBarWidget pageBarWidget = new PageBarWidget(this, child);
                    pageBarWidget.attach(child);
                } else if (child.getTagName().equalsIgnoreCase(MARKER_EDIT)) {
                    if (parentBar != null && parentBar.getType().equals(AREA_TYPE_SINGLE)) {
                        parentBar.mutateIntoSingleBar(child);
                    } else {
                        EditBarWidget editBarWidget = new EditBarWidget(parentBar, this, child);
                        editBarWidget.attach(child);
                    }
                } else if (child.getTagName().equalsIgnoreCase(MARKER_AREA)) {
                    AreaBarWidget areaBarWidget = new AreaBarWidget(parentBar, this, child);
                    areaBarWidget.attach(child);
                    parentBar = areaBarWidget;
                }

                detectCmsTag(child, parentBar);
            }
        }
    }

    public void openDialog(String dialog, String workspace, String path, String collectionName, String nodeName) {
        updateVariable(ACTION_OPEN_DIALOG, "dummy");
        updateVariable(PARAM_SELECTED_WORKSPACE, workspace);
        updateVariable(PARAM_SELECTED_PATH, path);
        updateVariable(PARAM_SELECTED_COLLECTION_NAME, collectionName);
        updateVariable(PARAM_SELECTED_NODE_NAME, nodeName);
        updateVariable(PARAM_DIALOG, dialog);
        client.sendPendingVariableChanges();
    }

    public void updateSelection(AbstractBarWidget selectedBar, String type, String workspace, String path, String collectionName, String nodeName, String availableComponents, String dialog) {
        if (this.selectedBar != null && (this.selectedBar != selectedBar)) {
            this.selectedBar.deselect();
        }
        this.selectedBar = selectedBar;
        updateVariable(ACTION_UPDATE_SELECTION, type);
        updateVariable(PARAM_SELECTED_WORKSPACE, workspace);
        updateVariable(PARAM_SELECTED_PATH, path);
        updateVariable(PARAM_SELECTED_COLLECTION_NAME, collectionName);
        updateVariable(PARAM_SELECTED_NODE_NAME, nodeName);
        updateVariable(PARAM_AVAILABLE_COMPONENTS, availableComponents);
        updateVariable(PARAM_DIALOG, dialog);
        client.sendPendingVariableChanges();
    }

    public void addComponent(String workspace, String path, String collectionName, String nodeName, String availableComponents) {
        updateVariable(ACTION_ADD_COMPONENT, "dummy");
        updateVariable(PARAM_SELECTED_WORKSPACE, workspace);
        updateVariable(PARAM_SELECTED_PATH, path);
        updateVariable(PARAM_SELECTED_COLLECTION_NAME, collectionName);
        updateVariable(PARAM_SELECTED_NODE_NAME, nodeName);
        updateVariable(PARAM_AVAILABLE_COMPONENTS, availableComponents);
        client.sendPendingVariableChanges();
    }

    public void moveComponent(String workspaceName, String sourcePath, String destinationPath) {
        updateVariable(ACTION_MOVE, "dummy");
        updateVariable(PARAM_SELECTED_WORKSPACE, workspaceName);
        updateVariable(PARAM_SOURCE_PATH, sourcePath);
        updateVariable(PARAM_DESTINATION_PATH, destinationPath);
        client.sendPendingVariableChanges();
    }

    public void moveComponentBefore(String workspaceName, String sourcePath, String destinationPath) {
        updateVariable(ACTION_MOVE_BEFORE, "dummy");
        updateVariable(PARAM_SELECTED_WORKSPACE, workspaceName);
        updateVariable(PARAM_SOURCE_PATH, sourcePath);
        updateVariable(PARAM_DESTINATION_PATH, destinationPath);
        client.sendPendingVariableChanges();
    }

    public void moveComponentAfter(String workspaceName, String sourcePath, String destinationPath) {
        updateVariable(ACTION_MOVE_AFTER, "dummy");
        updateVariable(PARAM_SELECTED_WORKSPACE, workspaceName);
        updateVariable(PARAM_SOURCE_PATH, sourcePath);
        updateVariable(PARAM_DESTINATION_PATH, destinationPath);
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
