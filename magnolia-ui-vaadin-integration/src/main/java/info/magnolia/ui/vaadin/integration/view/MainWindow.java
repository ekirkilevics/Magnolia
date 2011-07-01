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
package info.magnolia.ui.vaadin.integration.view;

import java.util.ArrayList;
import java.util.Collection;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.vaadin.Application;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Window;


/**
 * Creates the main window. Classes that depend on having a window created can depend on this class.
 *
 * @version $Id$
 */
@Singleton
public class MainWindow {

    // TODO this class needs a better name, MainWindow.getMainWindow() isn't so nice

    private Application application;
    private Collection<Component> permanentComponents = new ArrayList<Component>();

    @Inject
    public MainWindow(Application application) {
        this.application = application;
        this.application.setMainWindow(new Window());
    }

    public Window getMainWindow() {
        return application.getMainWindow();
    }

    public void setContent(ComponentContainer newContent) {
        getMainWindow().setContent(newContent);
        for (Component component : permanentComponents) {
            newContent.addComponent(component);
        }
    }

    public void addPermanentComponent(Component component) {
        permanentComponents.add(component);
        getMainWindow().addComponent(component);
    }

    public void setCaption(String caption) {
        getMainWindow().setCaption(caption);
    }

    public Application getApplication() {
        return application;
    }

    public void setTheme(String theme) {
        application.setTheme(theme);
    }
}
