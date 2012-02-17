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
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.DragEndEvent;
import com.google.gwt.event.dom.client.DragEndHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;

/**
 * Area bar.
 */
public class DragAndDropImpl {

    public void dragAndDrop (final ComponentBar abstractBar) {
        abstractBar.addDomHandler(new DragStartHandler() {
            @Override
            public void onDragStart(DragStartEvent event) {
                abstractBar.getElement().getStyle().setCursor(Cursor.MOVE);
                abstractBar.toggleButtons(false);
                event.getDataTransfer().setDragImage(abstractBar.getElement(), 10, 10);
                event.getDataTransfer().setData("text/plain", abstractBar.id);

            }
        }, DragStartEvent.getType());

        abstractBar.addDomHandler(new DragEndHandler() {
            @Override
            public void onDragEnd(DragEndEvent event) {
                abstractBar.toggleButtons(true);
            }
        }, DragEndEvent.getType());

        abstractBar.addDomHandler(new DragOverHandler() {
            @Override
            public void onDragOver(DragOverEvent event) {
                event.stopPropagation();
            }
        }, DragOverEvent.getType());


        abstractBar.addDomHandler(new DropHandler() {
            @Override
            public void onDrop(DropEvent event) {
                String idSource = event.getDataTransfer().getData("text/plain");
                String path = abstractBar.path;
                String parentPath = path.substring(0, path.lastIndexOf("/"));
                moveComponent(abstractBar.id, idSource, parentPath);
                event.preventDefault();
                //PageEditor.moveComponentEnd((ComponentBar)event.getSource(), parentPath);
            }
        }, DropEvent.getType());

        abstractBar.getElement().setDraggable(Element.DRAGGABLE_TRUE);
    }
}
