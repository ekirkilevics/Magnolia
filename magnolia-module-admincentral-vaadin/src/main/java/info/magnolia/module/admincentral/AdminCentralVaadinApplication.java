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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.MgnlUser;
import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admincentral.dialog.EditParagraphWindow;
import info.magnolia.module.admincentral.navigation.Menu;
import info.magnolia.module.admincentral.tree.TreeController;

import java.util.Iterator;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.navigator.Navigator;
import org.vaadin.navigator.Navigator.NavigableApplication;

import com.vaadin.Application;
import com.vaadin.addon.treetable.TreeTable;
import com.vaadin.terminal.ClassResource;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.SplitPanel;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.UriFragmentUtility;
import com.vaadin.ui.UriFragmentUtility.FragmentChangedEvent;
import com.vaadin.ui.UriFragmentUtility.FragmentChangedListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;

/**
 * Magnolia's AdminCentral.
 *
 * @author dan
 * @author fgrilli
 */
public class AdminCentralVaadinApplication extends Application implements NavigableApplication{

    private static final Logger log = LoggerFactory.getLogger(AdminCentralVaadinApplication.class);

    private static final long serialVersionUID = 5773744599513735815L;

    //public static final String WINDOW_TITLE = "Magnolia AdminCentral";

    private VerticalLayout mainContainer = new VerticalLayout();

    private HorizontalLayout bottomLeftCorner = new HorizontalLayout();

    private String contextPath;

    /**
     * TODO this is a sad little hack to get hold of this instance in classes that create and return
     * vaadin components (controllers). They need it in order to load icons with ClassResource.
     */
    public static AdminCentralVaadinApplication application;

    //This is needed to make application bookmarkable. See http://vaadin.com/book/-/page/advanced.urifu.html
    private UriFragmentUtility uriFragmentUtility = new UriFragmentUtility();

    private Messages messages;

    private Menu createMenu(Navigator navigator) {
        Menu menu = null;
        try {
            menu = new Menu(navigator);
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
        application = this;
        contextPath = "/"+ StringUtils.substringBetween(getURL().getPath(), "/");
        setTheme("runo");
        //TODO: don't be lazy and make your own message bundle!
        messages = MessagesManager.getInstance().getMessages("info.magnolia.module.admininterface.messages");
        setMainWindow(createNewWindow());
        //restoreApplicationStatus();
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
    private Window initLayout() {

        final VerticalLayout outerContainer = new VerticalLayout();
        outerContainer.setSizeFull();
        final Window mainWindow = new Window(messages.get("central.title"), outerContainer);
        final Navigator navigator = new Navigator();
        mainWindow.addComponent(navigator);
        setMainWindow(mainWindow);
        setLogoutURL(contextPath + "/.magnolia/pages/adminCentral.html?logout=true&mgnlLogout=true");

        // TODO: this layout is wrong!!! breaks completely on long user name or with different languages (eg spanish). It needs to be floating instead
        final AbsoluteLayout headerLayout = new AbsoluteLayout();
        headerLayout.setHeight("50px");
        headerLayout.setWidth("100%");

        final Embedded magnoliaLogo = new Embedded();
        magnoliaLogo.setType(Embedded.TYPE_IMAGE);
        magnoliaLogo.setSource(new ClassResource("/mgnl-resources/admin-images/magnoliaLogo.gif", this));
        magnoliaLogo.setWidth("294px");
        magnoliaLogo.setHeight("36px");
        headerLayout.addComponent(magnoliaLogo, "left: 20px; top: 10px;");

        final Label loggedUser = new Label(messages.get("central.user"));
        loggedUser.setWidth("35px");
        headerLayout.addComponent(loggedUser, "right: 120px; top: 10px;");

        final User user = MgnlContext.getUser();
        final Button userPreferences = new Button(user.getName());
        userPreferences.setStyleName(BaseTheme.BUTTON_LINK);
        userPreferences.addListener(new Button.ClickListener () {
            public void buttonClick(ClickEvent event) {
                try {
                    if (user instanceof MgnlUser) {
                        Content userNode = ((MgnlUser) user).getUserNode();
                        String handle = userNode.getHandle();
                        String parent = StringUtils.substringBeforeLast(handle, "/");
                        String nodeCollection = null;
                        getMainWindow().addWindow(new EditParagraphWindow("userpreferences",
                                ContentRepository.USERS, parent, nodeCollection, userNode.getName()));
                    }
                } catch (RepositoryException e) {
                    e.printStackTrace();
                }
            }
        });
        headerLayout.addComponent(userPreferences, "right: 65px; top: 10px;");

        final Label divider = new Label(" |");
        divider.setWidth("10px");
        headerLayout.addComponent(divider, "right: 50px; top: 10px;");

        final Button logout = new Button("logout");
        logout.setStyleName(BaseTheme.BUTTON_LINK);
        logout.addListener(new Button.ClickListener () {
            public void buttonClick(ClickEvent event) {
                getMainWindow().getApplication().close();
            }
        });
        headerLayout.addComponent(logout, "right: 10px; top: 10px;");

        final VerticalLayout leftPaneLayout = new VerticalLayout();
        final Menu menu = createMenu(navigator);
        leftPaneLayout.addComponent(menu);
        leftPaneLayout.addComponent(uriFragmentUtility);

        // Set the startup page
        // TODO this should be the decision of navigation/menu;
        final TreeTable treeTable = new TreeController().createTreeTable("website");
        mainContainer.addComponent(treeTable);
        bottomLeftCorner.setWidth("100%");
        mainContainer.addComponent(bottomLeftCorner);
        mainContainer.setSizeFull();

        final SplitPanel splitPanel = new SplitPanel(SplitPanel.ORIENTATION_HORIZONTAL);
        splitPanel.setSplitPosition(15);
        splitPanel.addComponent(leftPaneLayout);
        splitPanel.addComponent(mainContainer);

        outerContainer.addComponent(headerLayout);
        outerContainer.addComponent(splitPanel);
        outerContainer.setExpandRatio(splitPanel, 1.0f);

        mainContainer.setExpandRatio(treeTable, 15.0f);
        mainContainer.setExpandRatio(bottomLeftCorner, 1.0f);

        return mainWindow;

    }

    /*void restoreApplicationStatus() {
        uriFragmentUtility.addListener(new FragmentChangedListener() {

            public void fragmentChanged(FragmentChangedEvent source) {
                String fragment = source.getUriFragmentUtility().getFragment();
                log.debug("fragment is {}", fragment);
                if (fragment != null) {
                    restoreSelectedMenuItemTabFromURIFragment(fragment);
                }
            }
        });
    }

    /**
     * Tries to restore the menu status as it was saved i.e. by bookmarking the application URL.
     * @param fragment
     */
    /*void restoreSelectedMenuItemTabFromURIFragment(final String fragment) {
        for (Iterator<Component> iterator = menu.getComponentIterator(); iterator.hasNext();) {
            Component tabContent = iterator.next();
            Tab tab = menu.getTab(tabContent);
            if (fragment.equalsIgnoreCase(tab.getCaption())) {
                log.debug("restoring app status: opening menu tab with caption {}", fragment);
                menu.setSelectedTab(tabContent);
                return;
            }
        }
    }*/

    @Override
    public Window getWindow(String name) {
        // Use navigator to manage multiple browser windows
        return Navigator.getWindow(this, name, super.getWindow(name));
    }

    public Window createNewWindow() {
        return initLayout();
    }
}
