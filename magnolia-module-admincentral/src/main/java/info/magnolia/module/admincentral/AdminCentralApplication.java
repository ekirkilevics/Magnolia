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
package info.magnolia.module.admincentral;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.MgnlUser;
import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admincentral.dialog.DialogWindow;
import info.magnolia.module.admincentral.navigation.Menu;
import info.magnolia.module.admincentral.views.TestDetailView;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.Application;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.AbstractSplitPanel.SplitterClickEvent;
import com.vaadin.ui.AbstractSplitPanel.SplitterClickListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
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
public class AdminCentralApplication extends Application {

    private static final Logger log = LoggerFactory.getLogger(AdminCentralApplication.class);

    private static final long serialVersionUID = 5773744599513735815L;

    private Messages messages;

    private VerticalLayout mainContainer;

    private TestDetailView detailView;

    public VerticalLayout getMainContainer() {
        return mainContainer;
    }

    public TestDetailView getDetailView() {
        return detailView;
    }

    @Override
    public void init() {
        setTheme("runo");
        //TODO: don't be lazy and make your own message bundle!
        messages = MessagesManager.getMessages("info.magnolia.module.admininterface.messages");
        initLayout();
        setLogoutURL(MgnlContext.getContextPath() + "/?mgnlLogout=true");
    }

    /*@Override
    public Window getWindow(String name) {
       // If we already have the requested window, use it
        Window w = super.getWindow(name);
        if (w == null) {
            // If no window found, create it. This happens e.g. when opening the app on a new browser tab (multitabs).
            w = new Window(name);
            // set windows name to the one requested
            w.setName(name);
            // add it to this application
            addWindow(w);
            // add some content
           /* Window modal = new Window();
            modal.setModal(true);
            Label  label = new Label("Sorry, multitabs for Magnolia AdminCentral are not yet supported. Please close this browser's tab.");
            modal.setHeight("200px");
            modal.setWidth("300px");
            modal.setCaption("Info message");
            modal.setClosable(false);
            modal.setDraggable(false);
            modal.setResizable(false);
            modal.center();
            modal.addComponent(label);
            // ensure use of window specific url
            w.open(new ExternalResource(w.getURL().toString()));
            w.addWindow(modal);
        }
        return w;

    }*/

    /**
     * Creates the application layout and UI elements.
     */
    private void initLayout() {
        mainContainer = new VerticalLayout();
        mainContainer.setSizeFull();

        detailView = new TestDetailView();
        detailView.setSizeFull();

        final VerticalLayout outerContainer = new VerticalLayout();
        outerContainer.setSizeFull();

        final HorizontalLayout innerContainer = new HorizontalLayout();
        innerContainer.setSizeFull();

        final Window mainWindow = new Window(messages.get("central.title"), outerContainer);
        setMainWindow(mainWindow);
        // TODO: this layout is wrong!!! breaks completely on long user name or with different languages (eg spanish). It needs to be floating instead
        final AbsoluteLayout headerLayout = new AbsoluteLayout();
        headerLayout.setHeight("50px");
        headerLayout.setWidth("100%");

        final Embedded magnoliaLogo = new Embedded();
        magnoliaLogo.setType(Embedded.TYPE_IMAGE);
        magnoliaLogo.setSource(new ExternalResource(MgnlContext.getContextPath() + "/.resources/admin-images/magnoliaLogo.gif"));
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
            private static final long serialVersionUID = 7477646576639532112L;

            public void buttonClick(ClickEvent event) {
                try {
                    if (user instanceof MgnlUser) {
                        Content userNode = ((MgnlUser) user).getUserNode();
                        getMainWindow().addWindow(new DialogWindow("userpreferences", userNode));
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

        final Button logout = new Button(messages.get("central.logout"));
        logout.setStyleName(BaseTheme.BUTTON_LINK);
        logout.addListener(new Button.ClickListener () {
            private static final long serialVersionUID = 6067826137675410483L;

            public void buttonClick(ClickEvent event) {
                ((WebApplicationContext)getContext()).getHttpSession().invalidate();
                getMainWindow().getApplication().close();

            }
        });
        headerLayout.addComponent(logout, "right: 10px; top: 10px;");

        final VerticalLayout leftPaneLayout = new VerticalLayout();
        leftPaneLayout.setHeight("100%");
        Menu menu = null;
        try {
            menu = new Menu();
            menu.setSizeFull();
        } catch (RepositoryException re) {
            re.printStackTrace();
            getMainWindow().showNotification("Application menu could not be created. Please contact site administrator.<br/>", re.getMessage(), Notification.TYPE_ERROR_MESSAGE);
            //don't go any further.
            return;
        }
        leftPaneLayout.addComponent(menu);


        final HorizontalSplitPanel mainSplitPanel = new HorizontalSplitPanel();
        mainSplitPanel.setSplitPosition(15);
        mainSplitPanel.setSizeFull();
        mainSplitPanel.addListener(new SplitterClickListener() {
            private static final long serialVersionUID = 4837023553542505515L;

            public void splitterClick(SplitterClickEvent event) {
                if(event.isDoubleClick()){
                    HorizontalSplitPanel panel = (HorizontalSplitPanel)event.getSource();
                    if(panel.getSplitPosition() > 0){
                        panel.setSplitPosition(0);
                    }else {
                        panel.setSplitPosition(15);
                    }
                }
            }
        });

        mainSplitPanel.addComponent(leftPaneLayout);
        mainSplitPanel.addComponent(mainContainer);
        innerContainer.addComponent(mainSplitPanel);
        innerContainer.addComponent(detailView);
        innerContainer.setExpandRatio(mainSplitPanel, 1.0f);
        innerContainer.setExpandRatio(detailView, .15f);
        outerContainer.addComponent(headerLayout);
        outerContainer.addComponent(innerContainer);
        outerContainer.setExpandRatio(headerLayout, 1.0f);
        outerContainer.setExpandRatio(innerContainer, 90.0f);
    }
}
