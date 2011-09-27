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


import info.magnolia.templating.editor.client.jsni.LegacyJavascript;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;

/**
 * Page bar.
 */
public class PageBarWidget extends AbstractBarWidget {

    private PageEditor pageEditor;

    private String workspace;
    private String path;
    private String dialog;
    private boolean previewMode = false;

    public PageBarWidget(final PageEditor pageEditor, Element element) {
        super(null);
        this.pageEditor = pageEditor;

        String content = element.getAttribute("content");
        int i = content.indexOf(':');
        this.workspace = content.substring(0, i);
        this.path = content.substring(i + 1);
        this.dialog = element.getAttribute("dialog");

        if(LegacyJavascript.isPreviewMode()){
            createPreviewModeBar();
            previewMode = true;
        } else {
            createAuthoringModeBar();
        }
    }

    private void createAuthoringModeBar() {

        Button properties = new Button(getDictionary().get("buttons.properties.js"));
        properties.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                pageEditor.openDialog(dialog, workspace, path, null, null);
            }
        });
        addButton(properties, Float.RIGHT);

        Button preview = new Button(getDictionary().get("buttons.preview.js"));
        preview.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                pageEditor.preview(true);
            }
        });
        addButton(preview, Float.LEFT);

        Button adminCentral = new Button(getDictionary().get("buttons.admincentral.js"));
        adminCentral.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                pageEditor.showTree(workspace, path);
            }
        });
        addButton(adminCentral, Float.LEFT);

        setClassName("mgnlMainbar mgnlControlBar");
    }

    private void createPreviewModeBar() {
        Button preview = new Button(getDictionary().get("buttons.preview.hidden.js"));
        preview.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                pageEditor.preview(false);
            }
        });
        preview.getElement().getStyle().setTop(4.0, Unit.PX);
        preview.getElement().getStyle().setLeft(4.0, Unit.PX);
        preview.getElement().getStyle().setBackgroundColor("#9DB517");
        addButton(preview, Float.LEFT);
        //bar has to show up on the left hand side
        getStyle().setTop(0.0, Unit.PX);
        getStyle().setLeft(0.0, Unit.PX);
        setClassName("mgnlMainbarPreview");
    }

    public final boolean isPreviewMode() {
        return previewMode;
    }

    @Override
    protected void onSelect() {
        super.onSelect();
        pageEditor.updateSelection(this, PageEditor.SELECTION_TYPE_PAGE, workspace, path, null, null, "", dialog);
    }

    @Override
    public void attach(Element element) {
        element.appendChild(getElement());
        onAttach();
    }
}
