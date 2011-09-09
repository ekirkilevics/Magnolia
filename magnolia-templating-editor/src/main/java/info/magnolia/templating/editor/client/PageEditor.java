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
package info.magnolia.templating.editor.client;

import info.magnolia.templating.editor.client.jsni.GeneralJavascript;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.HTML;

/**
 * Client side implementation of the page editor. Outputs ui widgets inside document element (typically the <code>&lt;html&gt;</code> element).
 *
 * @version $Id$
 */
public class PageEditor extends HTML implements EventListener, EntryPoint {

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

    private boolean pageEditBarAlreadyProcessed = false;
    private AbstractBarWidget selectedBar;

    //private I18nContentSupport i18nSupport = I18nContentSupportFactory.getI18nSupport();

    public PageEditor() {
    }

    @Override
    public void onModuleLoad() {
        Element documentElement = Document.get().getDocumentElement();
        detectCmsTag(documentElement, null);
    }

    @Override
    public void onBrowserEvent(Event event) {
        super.onBrowserEvent(event);
    }

    private void detectCmsTag(Element element, AreaBarWidget parentBar) {
        final NodeList<Element> edits = element.getOwnerDocument().getElementsByTagName(MARKER_EDIT);
        final NodeList<Element> areas = element.getOwnerDocument().getElementsByTagName(MARKER_AREA);

        for (int i = 0; i < element.getChildCount(); i++) {
            Node childNode = element.getChild(i);
            if (childNode.getNodeType() == Element.ELEMENT_NODE) {
                Element child = (Element) childNode;
                if (child.getTagName().equalsIgnoreCase(MARKER_EDIT)) {
                    //We assume the first cms:edit we encounter in DOM is the page edit bar.
                    if(!pageEditBarAlreadyProcessed){
                        PageBarWidget pageBarWidget = new PageBarWidget(this, child);
                        pageBarWidget.attach(child);
                        pageEditBarAlreadyProcessed = true;
                    //avoid processing cms:edit marker twice if this is an area
                    } else if(!isAreaEditBar(child, areas)) {
                        EditBarWidget editBarWidget = new EditBarWidget(parentBar, this, child);
                        editBarWidget.attach(child);
                    }
                    if (parentBar != null && parentBar.getType().equals(AREA_TYPE_SINGLE)) {
                        parentBar.mutateIntoSingleBar(child);
                    }

                } else if (child.getTagName().equalsIgnoreCase(MARKER_AREA)) {
                    Element edit = findCmsEditMarkerForArea(child, edits);
                    if(edit != null) {
                        AreaBarWidget areaBarWidget = new AreaBarWidget(parentBar, this, child);
                        areaBarWidget.attach(edit);
                        parentBar = areaBarWidget;
                    }
                }

                detectCmsTag(child, parentBar);
            }
        }
    }

    private boolean isAreaEditBar(Element edit, NodeList<Element> areas) {

        String editContent = edit.getAttribute("content");
        int i = editContent.lastIndexOf("/");

        if(i == -1) {
            return false;
        }
        String match = editContent.substring(0, i);
        //GWT only shows these messages in dev mode.
        GWT.log("String to match area is " + match);

        for(int j=0; j < areas.getLength(); j++) {

            Element area = areas.getItem(j);
            String areaContent = area.getAttribute("content");

            if(match.equals(areaContent)) {
                GWT.log("found match with element " + area);
                return true;
            }
        }
        return false;
    }

    /**
     * Looks in DOM for an existing &lt;cms:edit ... &gt; marker associated with an area element. For an edit bar to be associated with the passed in element,
     * they must have the same content value.
     */
    private Element findCmsEditMarkerForArea(Element area, NodeList<Element> edits) {
        String content = area.getAttribute("content");
        String name = area.getAttribute("name");

        String match = content + (name != null ? ("/" + name) : "");
        //GWT shows these messages only in dev mode.
        GWT.log("String to match edit bar is " + match);

        for(int i=0; i < edits.getLength(); i++){
            Element edit = edits.getItem(i);
            String editContent = edit.getAttribute("content");

            if(match.equals(editContent)) {
                GWT.log("found match with element " + edit);
                return edit;
            }
        }
        return null;
    }

    /**
     * TODO: rename and/or remove arguments no longer needed (collectionName, nodeName).
     */
    public  void openDialog(String dialog, String workspace, String path, String collectionName, String nodeName){
        if (collectionName == null) {
            collectionName = "";
        }
        if (nodeName == null) {
            nodeName = "";
        }
        int i = dialog.indexOf(":");
        String dialogName = dialog.substring(i+1);
        //TODO how do we pass the locale (last arg)         here?
        GeneralJavascript.mgnlOpenDialog(path, collectionName, nodeName, dialogName, workspace, "", "", "", "");
    };

    public void moveComponentStart(String id) {
        GeneralJavascript.mgnlMoveNodeStart(id);
    }

    public void moveComponentEnd(EditBarWidget source, String path) {
        GeneralJavascript.mgnlMoveNodeEnd(source.getElement(), path);
    }

    public void moveComponentOver(EditBarWidget source) {
        GeneralJavascript.mgnlMoveNodeHigh(source.getElement());
    }

    public void moveComponentOut(EditBarWidget source) {
        GeneralJavascript.mgnlMoveNodeReset(source.getElement());
    }

    public void deleteComponent(String path) {
        GeneralJavascript.mgnlDeleteNode(path);
    }

    public void updateSelection(AbstractBarWidget selectedBar, String type, String workspace, String path, String collectionName, String nodeName, String availableComponents, String dialog) {
        if (this.selectedBar != null && (this.selectedBar != selectedBar)) {
            this.selectedBar.deselect();
        }
        this.selectedBar = selectedBar;
    }

    public void addComponent(String workspace, String path, String collectionName, String nodeName, String availableComponents) {
        //TODO for the time being does nothing
    }

}
