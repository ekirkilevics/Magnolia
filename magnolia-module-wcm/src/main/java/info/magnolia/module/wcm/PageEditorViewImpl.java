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
package info.magnolia.module.wcm;

import com.vaadin.Application;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import info.magnolia.module.wcm.editor.PageEditor;
import info.magnolia.module.wcm.toolbox.ToolboxViewImpl;

/**
 * Main page editor view.
 */
public class PageEditorViewImpl implements PageEditorView {

    private Application application;
    private HorizontalLayout layout;
    private VerticalLayout pageLayout;
    private VerticalLayout toolboxLayout;
    private WcmModule wcmModule;

    public PageEditorViewImpl(Application application, WcmModule wcmModule) {
        this.application = application;
        this.wcmModule = wcmModule;
    }

    public void init() {

        pageLayout = new VerticalLayout();
        pageLayout.setHeight(100, Sizeable.UNITS_PERCENTAGE);

        toolboxLayout = new VerticalLayout();
        toolboxLayout.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        toolboxLayout.addComponent(new ToolboxViewImpl(wcmModule).asVaadinComponent());
        toolboxLayout.addComponent(new PageEditor());

        layout = new HorizontalLayout();
        layout.setSizeFull();
        layout.addComponent(pageLayout);
        layout.addComponent(toolboxLayout);
        layout.setExpandRatio(pageLayout, 5);
        layout.setExpandRatio(toolboxLayout, 1);

        Window window = new Window("Page Editor", layout);
        this.application.setMainWindow(window);
    }

    public ComponentContainer getMainContainer() {
        return pageLayout;
    }
}
