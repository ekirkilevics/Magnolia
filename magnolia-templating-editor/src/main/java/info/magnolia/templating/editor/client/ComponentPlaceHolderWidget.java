/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
import info.magnolia.templating.editor.client.dom.MgnlElement;

import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.ui.Label;

/**
 * A Widget for adding components to area.
 *
 * @version $Id$
 */
public class ComponentPlaceHolderWidget extends AbstractPlaceHolder {

    private boolean optional = false;
    private boolean created = false;
    private boolean showAddButton = false;
    private String availableComponents = "";
    private String type = "";
    private String areaWorkspace = "";
    private String areaPath = "";
    private String name = "";

    public ComponentPlaceHolderWidget(PageEditor pageEditor, MgnlElement mgnlElement) {

        this.showAddButton = Boolean.parseBoolean(mgnlElement.getComment().getAttribute("showAddButton"));
        this.optional = Boolean.parseBoolean(mgnlElement.getComment().getAttribute("optional"));
        this.created = Boolean.parseBoolean(mgnlElement.getComment().getAttribute("created"));
        this.type = mgnlElement.getComment().getAttribute("type");
        this.name = mgnlElement.getComment().getAttribute("name");


        String areaContent = mgnlElement.getComment().getAttribute("content");
        int i = areaContent.indexOf(':');
        this.areaWorkspace = areaContent.substring(0, i);
        this.areaPath = areaContent.substring(i + 1);

        if(AreaDefinition.TYPE_NO_COMPONENT.equals(this.type)) {
            this.availableComponents = "";
        } else {
            this.availableComponents = mgnlElement.getComment().getAttribute("availableComponents");
        }

        this.addStyleName("component");
        Label areaName = new Label("Add Component");

        setVisible(false);

        add(areaName);

        createMouseEventsHandlers(pageEditor);
    }
    private void createMouseEventsHandlers(final PageEditor pageEditor) {

        if (this.optional) {
            if(!this.created) {
                addDomHandler(new MouseDownHandler() {

                    @Override
                    public void onMouseDown(MouseDownEvent event) {
                        pageEditor.createComponent(areaWorkspace, areaPath, name, "mgnl:area");

                    }
                }, MouseDownEvent.getType());
            }
            else {
                // move content away from edit
                //pageEditor.deleteComponent(path);
            }
        }

        else if (this.showAddButton){
            addDomHandler(new MouseDownHandler() {

                @Override
                public void onMouseDown(MouseDownEvent event) {
                    pageEditor.addComponent(areaWorkspace, areaPath, name, null, availableComponents);

                }
            }, MouseDownEvent.getType());
        }
    }
}
