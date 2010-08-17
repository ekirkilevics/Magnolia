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


import com.vaadin.Application;
import com.vaadin.terminal.ClassResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.SplitPanel;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.UriFragmentUtility;
import com.vaadin.ui.UriFragmentUtility.FragmentChangedEvent;
import com.vaadin.ui.UriFragmentUtility.FragmentChangedListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import info.magnolia.module.admincentral.navigation.Menu;
import info.magnolia.module.admincentral.website.WebsitePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.Iterator;


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

    private HorizontalLayout bottomLeftCorner = new HorizontalLayout();

    //This is needed to make application bookmarkable. See http://vaadin.com/book/-/page/advanced.urifu.html
    private UriFragmentUtility uriFragmentUtility = new UriFragmentUtility();

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
        restoreApplicationStatus();
    }

    //TODO can this arise concurrency issues? Use ThreadLocal? Anyway, it should not be an issue, because as stated at
    //http://vaadin.com/book/-/page/architecture.server-side.html "... [Vaadin] associates an Application instance with each session."
    public ComponentContainer getMainContainer(){
        return mainContainer;
    }

    public UriFragmentUtility getUriFragmentUtility(){
        return uriFragmentUtility;
    }

    /**
     * package-private modifier is used for better testing possibilities...
     */
    void initLayout() {
        SplitPanel splitPanel = new SplitPanel(SplitPanel.ORIENTATION_HORIZONTAL);
        splitPanel.setSplitPosition(20);
        setMainWindow(new Window(WINDOW_TITLE, splitPanel));

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
        leftPaneLayout.addComponent(uriFragmentUtility);

        splitPanel.addComponent(leftPaneLayout);
        splitPanel.addComponent(mainContainer);

        // Set the startup page
        // TODO this should be the decision of navigation/menu
        mainContainer.addComponent(new WebsitePage());
    }

    void restoreApplicationStatus() {
        uriFragmentUtility.addListener(new FragmentChangedListener() {

            public void fragmentChanged(FragmentChangedEvent source) {
                String fragment = source.getUriFragmentUtility().getFragment();
                log.info("fragment is {}", fragment);
                if (fragment != null) {
                    restoreSelectedMenuItemTabFromURIFragment(fragment);
                }
            }
        });
    }

    /**
     * Tries to restore the menu status as it was saved i.e. by bookmarking the application URL.
     * @param fragment - String
     */
    void restoreSelectedMenuItemTabFromURIFragment(final String fragment) {
        for (Iterator<Component> iterator = menu.getComponentIterator(); iterator.hasNext();) {
            Component tabContent = iterator.next();
            Tab tab = menu.getTab(tabContent);
            if (fragment.equalsIgnoreCase(tab.getCaption())) {
                log.info("restoring app status: opening menu tab with caption {}", fragment);
                menu.setSelectedTab(tabContent);
                return;
            }
        }
    }
}
