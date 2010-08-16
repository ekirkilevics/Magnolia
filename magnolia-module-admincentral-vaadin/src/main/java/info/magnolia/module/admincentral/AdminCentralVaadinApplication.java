/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.module.admincentral;


import info.magnolia.module.admincentral.navigation.Menu;
import info.magnolia.module.admincentral.website.WebsiteTreeTable;
import info.magnolia.module.admincentral.website.WebsiteTreeTableFactory;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.Application;
import com.vaadin.addon.treetable.TreeTable;
import com.vaadin.data.Container.Hierarchical;
import com.vaadin.terminal.ClassResource;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.SplitPanel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;


/**
 * Magnolia's AdminCentral.
 *
 * @author dan
 * @author fgrilli
 */
public class AdminCentralVaadinApplication extends Application {

    private static final Logger log = LoggerFactory.getLogger(AdminCentralVaadinApplication.class);

    private static final long serialVersionUID = 5773744599513735815L;

    public static final String WINDOW_TITLE = "Magnolia AdminCentral";

    private Menu menu = createMenu();

    private VerticalLayout mainContainer;

    private Hierarchical websiteData = WebsiteTreeTableFactory.getInstance().getWebsiteData();

    private HorizontalLayout bottomLeftCorner = new HorizontalLayout();

    private TreeTable websites = WebsiteTreeTableFactory.getInstance().createWebsiteTreeTable();


    private Menu createMenu() {
        Menu menu = null;
        try {
            menu = new Menu("/modules/adminInterface/config/menu");
            menu.setSizeFull();
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
            getMainWindow().showNotification("Application menu could not be created.", re.getMessage(), Notification.TYPE_ERROR_MESSAGE);
        }
        return menu;
    }

    @Override
    public void init() {
        /**
         * dan: simply remove next in order to get the default theme ("reindeer")
         */
        setTheme("runo");
        initLayout();
        initWebsiteTreeTable();
    }

    private void initWebsiteTreeTable() {
        websites.setContainerDataSource(websiteData);
        websites.setVisibleColumns(WebsiteTreeTable.WEBSITE_FIELDS);
    }

    public ComponentContainer getMainContainer(){
        return mainContainer;
    }

    /**
     * package-private modifier is used for better testing possibilities...
     */
    void initLayout() {
        SplitPanel splitPanel = new SplitPanel(SplitPanel.ORIENTATION_HORIZONTAL);
        setMainWindow(new Window(WINDOW_TITLE, splitPanel));
        splitPanel.setSplitPosition(20);

        mainContainer = new VerticalLayout();
        mainContainer.setSizeFull();

        bottomLeftCorner.setWidth("100%");

        VerticalLayout leftPaneLayout = new VerticalLayout();
        leftPaneLayout.setMargin(true);
        Embedded embedded = new Embedded();
        embedded.setType(Embedded.TYPE_IMAGE);
        embedded.setSource(new ClassResource("/mgnl-resources/admin-images/magnoliaLogo.gif", this));
        embedded.setWidth("294px");
        embedded.setHeight("36px");
        leftPaneLayout.addComponent(embedded);
        leftPaneLayout.addComponent(menu);

        splitPanel.addComponent(leftPaneLayout);
        splitPanel.addComponent(mainContainer);
        mainContainer.addComponent(websites);
        mainContainer.addComponent(bottomLeftCorner);

        mainContainer.setExpandRatio(websites, 10);
        mainContainer.setExpandRatio(bottomLeftCorner, 1);
    }
}
