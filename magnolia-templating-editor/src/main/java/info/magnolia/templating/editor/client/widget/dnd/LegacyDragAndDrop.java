/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.templating.editor.client.widget.dnd;

import static info.magnolia.templating.editor.client.jsni.JavascriptUtils.moveComponent;

import com.google.gwt.dom.client.Element;


import info.magnolia.templating.editor.client.PageEditor;
import info.magnolia.templating.editor.client.dom.MgnlElement;
import info.magnolia.templating.editor.client.widget.controlbar.ComponentBar;
import info.magnolia.templating.editor.client.widget.placeholder.ComponentPlaceHolder;

/**
 * DragAndDropLegacy. GWT port of legacy /magnolia-module-admininterface/src/main/resources/mgnl-resources/admin-js/inline.js
 */
public class LegacyDragAndDrop {

    public static ComponentBar sourceBar;
    private static MoveWidget moveDiv;

    public static void moveComponentStart(ComponentBar bar) {
            toggleStyles(bar, true);

            // reset native drag and drop
            bar.setDraggable(false);
            MgnlElement area = bar.getMgnlElement().getParentArea();
            if (area != null) {
                for (MgnlElement component : area.getComponents()) {
                    ComponentBar componentBar = (ComponentBar) PageEditor.model.getEditBar(component);
                    if (componentBar != null && componentBar != bar) {
                        componentBar.setDraggable(false);
                    }
                }
            }
            sourceBar = bar;
            int height =bar.getOffsetHeight();
            int width = bar.getOffsetWidth();
            moveDiv = new MoveWidget(height, width);
    }

    public static void moveComponentOver(ComponentBar bar) {
        if (isMoving()) {
            String idSource = sourceBar.getNodeName();

            if (!bar.getNodeName().equals(idSource)){
                bar.setStyleName("moveOver", true);
            }
        }
    }

    public static void moveComponentOut(ComponentBar bar) {
        if (isMoving()) {
            String idSource = sourceBar.getNodeName();

            if (!bar.getNodeName().equals(idSource)){
                bar.setStyleName("moveOver", false);
            }
        }
    }

    public static void moveComponentEnd(ComponentBar bar) {
        if (isMoving()) {

            String idSource = sourceBar.getNodeName();

            if (!bar.getNodeName().equals(idSource)) {
                int xTarget = bar.getAbsoluteLeft();
                int yTarget = bar.getAbsoluteTop();
                int xOrigin = sourceBar.getAbsoluteLeft();
                int yOrigin = sourceBar.getAbsoluteTop();

                boolean isDragUp = yOrigin > yTarget;
                boolean isDragDown = !isDragUp;
                boolean isDragLeft = xOrigin > xTarget;
                boolean isDragRight = !isDragLeft;

                String order = null;

                if(isDragUp || isDragLeft) {
                    order = "before";
                } else if(isDragDown || isDragRight) {
                    order = "after";
                }
                String parentPath = bar.getPath().substring(0, bar.getPath().lastIndexOf("/"));
                moveComponent(bar.getNodeName(), idSource, parentPath, order);
            }
        }
    }

    public static void moveComponentReset() {
        if (isMoving()) {
            toggleStyles(sourceBar, false);

            // reset native drag and drop
            sourceBar.setDraggable(true);
            MgnlElement area = sourceBar.getMgnlElement().getParentArea();
            if (area != null) {
                for (MgnlElement component : area.getComponents()) {
                    ComponentBar componentBar = (ComponentBar) PageEditor.model.getEditBar(component);
                    if (componentBar != null && componentBar != sourceBar) {
                        componentBar.setDraggable(false);
                    }
                }
            }

            sourceBar = null;
            moveDiv.detach();
        }
    }

    private static void toggleStyles(ComponentBar bar, boolean isMove) {
        bar.toggleButtons(!isMove);

        bar.setStyleName("moveSource", isMove);

        MgnlElement area = bar.getMgnlElement().getParentArea();
        if (area != null) {
            for (MgnlElement component : area.getComponents()) {
                ComponentBar componentBar = (ComponentBar) PageEditor.model.getEditBar(component);
                if (componentBar != null && componentBar != bar) {
                    componentBar.setStyleName("moveTarget", isMove);

                    componentBar.getElement().setDraggable(Element.DRAGGABLE_TRUE);

                }
            }
            ComponentPlaceHolder placeholder = (ComponentPlaceHolder) PageEditor.model.getComponentPlaceHolder(area);
            if (placeholder != null) {
                placeholder.setStyleName("moveOngoing", isMove);
            }
        }
    }

    public static boolean isMoving() {
        return (sourceBar != null) ? true : false;
    }
}
