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
import info.magnolia.templating.editor.client.widget.controlbar.ComponentBar;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.DragEndEvent;
import com.google.gwt.event.dom.client.DragEndHandler;
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
        bar.addDomHandler(new DragStartHandler() {
            @Override
            public void onDragStart(DragStartEvent event) {
                bar.toggleButtons(false);
                int x = bar.getAbsoluteLeft();
                int y = bar.getAbsoluteTop();
                event.setData("text", bar.id + "," + x +","+y);
                event.getDataTransfer().setDragImage(bar.getElement(), 10, 10);

            }
        }, DragStartEvent.getType());

        bar.addDomHandler(new DragEndHandler() {
            @Override
            public void onDragEnd(DragEndEvent event) {
                bar.toggleButtons(true);
            }
        }, DragEndEvent.getType());

        bar.addDomHandler(new DragOverHandler() {
            @Override
            public void onDragOver(DragOverEvent event) {
                event.stopPropagation();
            }
        }, DragOverEvent.getType());


        bar.addDomHandler(new DropHandler() {
            @Override
            public void onDrop(DropEvent event) {
                String data = event.getData("text");
                String[] tokens = data.split(",");
                String idSource = tokens[0];

                int xTarget = bar.getAbsoluteLeft();
                int yTarget = bar.getAbsoluteTop();
                int xOrigin = Integer.valueOf(tokens[1]);
                int yOrigin = Integer.valueOf(tokens[2]);

                boolean isDragUp = yOrigin > yTarget;
                boolean isDragDown = !isDragUp;
                boolean isDragLeft = xOrigin > xTarget;
                boolean isDragRight = !isDragLeft;

                String order = "before";

                if(isDragUp || isDragLeft) {
                    order = "before";
                } else if(isDragDown || isDragRight) {
                    order = "after";
                }
                String parentPath = bar.path.substring(0, bar.path.lastIndexOf("/"));
                moveComponent(bar.id, idSource, parentPath, order);
                event.preventDefault();
            }
        }, DropEvent.getType());

        bar.getElement().setDraggable(Element.DRAGGABLE_TRUE);
    }
}
