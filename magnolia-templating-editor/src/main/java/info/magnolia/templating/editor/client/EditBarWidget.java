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
package info.magnolia.templating.editor.client;

import info.magnolia.rendering.template.AreaDefinition;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Button;

import static info.magnolia.templating.editor.client.PageEditor.getDictionary;

/**
 * Edit bar.
 */
public class EditBarWidget extends AbstractBarWidget {

    private String workspace;
    private String path;
    private String dialog;
    private String id;
    private String parentAreaType;

    public EditBarWidget(final AreaBarWidget parentBar, final PageEditor pageEditor, final Element element) {

        super(parentBar);

        String content = element.getAttribute("content");
        int i = content.indexOf(':');
        this.workspace = content.substring(0, i);
        this.path = content.substring(i + 1);

        this.id = path.substring(path.lastIndexOf("/") + 1);

        setId("__"+id);

        this.dialog = element.getAttribute("dialog");

        if(parentBar != null) {
            this.parentAreaType = parentBar.getType();
        }

        createButtons(pageEditor);

        createMouseEventsHandlers(pageEditor);

        setClassName("mgnlEditBar");

    }

    private void createMouseEventsHandlers(final PageEditor pageEditor) {

        addDomHandler(new MouseDownHandler() {

            @Override
            public void onMouseDown(MouseDownEvent event) {
                String parentPath = path.substring(0, path.lastIndexOf("/"));
                pageEditor.moveComponentEnd((EditBarWidget)event.getSource(), parentPath);

            }
        }, MouseDownEvent.getType());

        addDomHandler(new MouseOverHandler() {

            @Override
            public void onMouseOver(MouseOverEvent event) {
                pageEditor.moveComponentOver((EditBarWidget)event.getSource());
            }
        }, MouseOverEvent.getType());

        addDomHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                pageEditor.moveComponentOut((EditBarWidget)event.getSource());
            }
        }, MouseOutEvent.getType());
    }

    private void createButtons(final PageEditor pageEditor) {
        final Button edit = new Button(getDictionary().get("buttons.edit.js"));
        edit.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                pageEditor.openDialog(dialog, workspace, path, null, null);

            }
        });
        addButton(edit, Float.RIGHT);

        //single area component obviously cannot be moved
        if(AreaDefinition.TYPE_LIST.equals(parentAreaType)) {
            final Button move = new Button(getDictionary().get("buttons.move.js"));
            move.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    pageEditor.moveComponentStart(id);
                }
            });
            addButton(move, Float.RIGHT);
        }

        final Button delete = new Button(getDictionary().get("buttons.delete.js"));
        delete.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                pageEditor.deleteComponent(path);
            }
        });
        addButton(delete, Float.LEFT);
    }

}
