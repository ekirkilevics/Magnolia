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
import info.magnolia.templating.editor.client.PageEditor;
import info.magnolia.templating.editor.client.dom.MgnlElement;
import info.magnolia.templating.editor.client.widget.controlbar.ComponentBar;
import info.magnolia.templating.editor.client.widget.placeholder.ComponentPlaceHolder;

import com.google.gwt.event.dom.client.DragEndEvent;
import com.google.gwt.event.dom.client.DragEndHandler;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;

/**
 * DragAndDropImpl.
 */
public class DragAndDropImpl {

    public void dragAndDrop (final ComponentBar bar) {

        bar.setDraggable(true);
        bar.addDomHandler(new DragStartHandler() {
            @Override
            public void onDragStart(DragStartEvent event) {
                bar.toggleButtons(false);

                bar.setStyleName("moveSource", true);

                MgnlElement area = bar.getMgnlElement().getParentArea();
                if (area != null) {
                    for (MgnlElement component : area.getComponents()) {
                        ComponentBar componentBar = (ComponentBar) PageEditor.model.getEditBar(component);
                        if (componentBar != null && componentBar != bar) {
                            componentBar.setStyleName("moveTarget", true);
                        }
                    }
                    ComponentPlaceHolder placeholder = (ComponentPlaceHolder) PageEditor.model.getComponentPlaceHolder(area);
                    if (placeholder != null) {
                        placeholder.setStyleName("moveOngoing", true);
                    }
                }

                int x = bar.getAbsoluteLeft();
                int y = bar.getAbsoluteTop();
                event.setData("text", bar.getNodeName() + "," + x +","+y);
                event.getDataTransfer().setDragImage(bar.getElement(), 10, 10);

            }
        }, DragStartEvent.getType());

        bar.addDomHandler(new DragEndHandler() {
            @Override
            public void onDragEnd(DragEndEvent event) {
                bar.toggleButtons(true);

                bar.setStyleName("moveSource", false);

                MgnlElement area = bar.getMgnlElement().getParentArea();
                if (area != null) {
                    for (MgnlElement component : area.getComponents()) {
                        ComponentBar componentBar = (ComponentBar) PageEditor.model.getEditBar(component);
                        if (componentBar != null && componentBar != bar) {
                            componentBar.setStyleName("moveTarget", false);
                        }
                    }
                    ComponentPlaceHolder placeholder = (ComponentPlaceHolder) PageEditor.model.getComponentPlaceHolder(area);
                    if (placeholder != null) {
                        placeholder.setStyleName("moveOngoing", false);
                    }
                }
            }
        }, DragEndEvent.getType());

        bar.addDomHandler(new DragOverHandler() {
            @Override
            public void onDragOver(DragOverEvent event) {
                String data = event.getData("text");
                String[] tokens = data.split(",");
                String idSource = tokens[0];

                if (!bar.getNodeName().equals(idSource)){
                    bar.setStyleName("moveOver", true);
                }
                event.stopPropagation();
            }
        }, DragOverEvent.getType());

        bar.addDomHandler(new DragLeaveHandler() {

            @Override
            public void onDragLeave(DragLeaveEvent event) {
                bar.setStyleName("moveOver", false);
                event.stopPropagation();
            }
        }, DragLeaveEvent.getType());

        bar.addDomHandler(new DropHandler() {
            @Override
            public void onDrop(DropEvent event) {
                String data = event.getData("text");
                String[] tokens = data.split(",");
                String idSource = tokens[0];

                if (!bar.getNodeName().equals(idSource)) {
                    int xTarget = bar.getAbsoluteLeft();
                    int yTarget = bar.getAbsoluteTop();
                    int xOrigin = Integer.valueOf(tokens[1]);
                    int yOrigin = Integer.valueOf(tokens[2]);

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

                event.preventDefault();
            }
        }, DropEvent.getType());

    }
}
