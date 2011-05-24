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
package info.magnolia.ui.admincentral;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.vaadin.Application;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.MgnlUser;
import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.ui.admincentral.dialog.DialogPresenterFactory;
import info.magnolia.ui.admincentral.dialog.view.DialogPresenter;

/**
 * Main application view layout.
 */
public class AdminCentralViewImpl implements AdminCentralView {

    private Application application;
    private DialogPresenterFactory dialogPresenterFactory;

    private VerticalLayout mainContainer;
    private VerticalLayout menuDisplay;

    public AdminCentralViewImpl(Application application, DialogPresenterFactory dialogPresenterFactory) {
        this.application = application;
        this.dialogPresenterFactory = dialogPresenterFactory;
    }

    @Override
    public void init() {

        application.setTheme("magnolia");
        application.setLogoutURL(MgnlContext.getContextPath() + "/?mgnlLogout=true");

        Messages messages = MessagesManager.getMessages("info.magnolia.ui.admincentral.messages");

        menuDisplay = new VerticalLayout();
        menuDisplay.setHeight(100,Sizeable.UNITS_PERCENTAGE);
        menuDisplay.setMargin(false, true, false, true);

        mainContainer = new VerticalLayout();
        mainContainer.setSizeFull();

        final HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setSizeFull();
        mainLayout.addComponent(menuDisplay);
        mainLayout.addComponent(mainContainer);
        mainLayout.setExpandRatio(menuDisplay, 1);
        mainLayout.setExpandRatio(mainContainer, 5);

        final HorizontalLayout innerContainer = new HorizontalLayout();
        innerContainer.setSizeFull();
        innerContainer.addComponent(mainLayout);
        innerContainer.setExpandRatio(mainLayout, 1.0f);

        final AbsoluteLayout headerLayout = createHeaderLayout(messages);

        final VerticalLayout outerContainer = new VerticalLayout();
        outerContainer.setSizeFull();
        outerContainer.addComponent(headerLayout);

        outerContainer.addComponent(innerContainer);
        outerContainer.setExpandRatio(headerLayout, 1.0f);
        outerContainer.setExpandRatio(innerContainer, 90.0f);

        application.setMainWindow(new Window(messages.get("central.title"), outerContainer));
    }

    private AbsoluteLayout createHeaderLayout(Messages messages) {

        // FIXME: this layout breaks completely on long user name or with different languages (eg spanish). It needs to be floating instead

        final AbsoluteLayout headerLayout = new AbsoluteLayout();
        headerLayout.setHeight(45, Sizeable.UNITS_PIXELS);
        headerLayout.setWidth(100, Sizeable.UNITS_PERCENTAGE);

        final Embedded magnoliaLogo = new Embedded();
        magnoliaLogo.setType(Embedded.TYPE_IMAGE);
        magnoliaLogo.setSource(new ExternalResource(MgnlContext.getContextPath() + "/.resources/admin-images/magnoliaLogo.gif"));
        magnoliaLogo.setWidth(294, Sizeable.UNITS_PIXELS);
        magnoliaLogo.setHeight(36, Sizeable.UNITS_PIXELS);
        headerLayout.addComponent(magnoliaLogo, "left: 20px; top: 10px;");

        final Label loggedUser = new Label(messages.get("central.user"));
        loggedUser.setWidth(35, Sizeable.UNITS_PIXELS);
        headerLayout.addComponent(loggedUser, "right: 130px; top: 20px;");

        final User user = MgnlContext.getUser();
        final Button userPreferences = new Button(user.getName());
        userPreferences.setStyleName(BaseTheme.BUTTON_LINK);
        userPreferences.addListener(new Button.ClickListener () {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                try {
                    if (user instanceof MgnlUser) {
                        Node userNode = ((MgnlUser) user).getUserNode().getJCRNode();

                        DialogPresenter dialogPresenter = dialogPresenterFactory.createDialog("userpreferences");
                        dialogPresenter.setNode(userNode);
                        dialogPresenter.showDialog();
                    }
                } catch (RepositoryException e) {
                    e.printStackTrace();
                }
            }
        });
        headerLayout.addComponent(userPreferences, "right: 75px; top: 20px;");

        final Label divider = new Label(" |");
        divider.setWidth(10, Sizeable.UNITS_PIXELS);
        headerLayout.addComponent(divider, "right: 60px; top: 20px;");

        final Button logout = new Button(messages.get("central.logout"));
        logout.setStyleName(BaseTheme.BUTTON_LINK);
        logout.addListener(new Button.ClickListener () {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                ((WebApplicationContext)application.getContext()).getHttpSession().invalidate();
                application.getMainWindow().getApplication().close();
            }
        });
        headerLayout.addComponent(logout, "right: 20px; top: 20px;");

        return headerLayout;
    }

    @Override
    public VerticalLayout getMainContainer() {
        return mainContainer;
    }

    @Override
    public VerticalLayout getMenuDisplay() {
        return menuDisplay;
    }
}
