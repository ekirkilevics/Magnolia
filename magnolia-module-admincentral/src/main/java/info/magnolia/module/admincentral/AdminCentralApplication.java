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
import info.magnolia.module.admincentral.activity.EditWorkspaceActivity;
import info.magnolia.module.admincentral.activity.ShowContentActivity;
import info.magnolia.module.admincentral.dialog.DialogWindow;
import info.magnolia.module.admincentral.model.UIModel;
import info.magnolia.module.admincentral.navigation.Menu;
import info.magnolia.module.admincentral.navigation.MenuItemConfiguration;
import info.magnolia.module.admincentral.place.AdminCentralPlaceHistoryMapper;
import info.magnolia.module.admincentral.place.EditWorkspacePlace;
import info.magnolia.module.admincentral.place.ShowContentPlace;
import info.magnolia.module.admincentral.place.SomePlace;
import info.magnolia.module.vaadin.activity.AbstractActivity;
import info.magnolia.module.vaadin.activity.Activity;
import info.magnolia.module.vaadin.activity.ActivityManager;
import info.magnolia.module.vaadin.activity.ActivityMapper;
import info.magnolia.module.vaadin.component.ComponentContainerBasedDisplay;
import info.magnolia.module.vaadin.component.HasComponent;
import info.magnolia.module.vaadin.event.EventBus;
import info.magnolia.module.vaadin.place.Place;
import info.magnolia.module.vaadin.place.PlaceChangeEvent;
import info.magnolia.module.vaadin.place.PlaceChangeListener;
import info.magnolia.module.vaadin.place.PlaceChangeRequestEvent;
import info.magnolia.module.vaadin.place.PlaceChangeRequestListener;
import info.magnolia.module.vaadin.place.PlaceController;
import info.magnolia.module.vaadin.place.PlaceHistoryHandler;
import info.magnolia.module.vaadin.place.PlaceHistoryMapper;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.Application;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;

/**
 * Magnolia's AdminCentral.
 *
 * @author dan
 * @author fgrilli
 */
public class AdminCentralApplication extends Application {

    private final class MenuActivity extends AbstractActivity implements Menu.Presenter {

        public void start(HasComponent display, EventBus eventBus) {
            Menu menu;
            try {
                menu = new Menu(this);
                display.setComponent(menu);
            }
            catch (RepositoryException e) {
                getMainWindow().showNotification(e.getMessage());
            }
        }

        public void onMenuSelection(MenuItemConfiguration menuConfig) {
            Place newPlace;
            if(StringUtils.isNotBlank(menuConfig.getRepo())){
                newPlace = new EditWorkspacePlace(menuConfig.getRepo());
            } else if(StringUtils.isNotBlank(menuConfig.getViewTarget())) {
                newPlace = new ShowContentPlace(menuConfig.getViewTarget(), menuConfig.getView());
            }else {
                newPlace = new SomePlace(menuConfig.getName());
            }

            placeController.goTo(newPlace);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(AdminCentralApplication.class);

    private static final long serialVersionUID = 5773744599513735815L;

    private Messages messages;

    private VerticalLayout outerContainer;

    private VerticalLayout mainContainer;

    private VerticalLayout menuDisplay;

    private EventBus eventBus;

    private PlaceController placeController;

    // FIXME should be a component
    private UIModel uiModel = new UIModel();

    public VerticalLayout getMainContainer() {
        return mainContainer;
    }

    @Override
    public void init() {
        setTheme("magnolia");
        //TODO: don't be lazy and make your own message bundle!
        messages = MessagesManager.getMessages("info.magnolia.module.admininterface.messages");

        setLogoutURL(MgnlContext.getContextPath() + "/?mgnlLogout=true");

        eventBus = new EventBus();
        initLayout();
        eventBus.register(PlaceChangeListener.class, PlaceChangeEvent.class);
        eventBus.register(PlaceChangeRequestListener.class, PlaceChangeRequestEvent.class);

        placeController = new PlaceController(eventBus, new PlaceController.DefaultDelegate(this));

        ActivityManager menuActivityManager = new ActivityManager(new ActivityMapper() {
            Activity menuActivity = new MenuActivity();
            public Activity getActivity(Place place) {
                return menuActivity;
            }
        }, eventBus);

        ActivityManager mainActivityManager = new ActivityManager(new ActivityMapper() {

            public Activity getActivity(final Place place) {
                if(place instanceof EditWorkspacePlace){
                    EditWorkspacePlace editWorkspacePlace = (EditWorkspacePlace)place;
                    return new EditWorkspaceActivity(editWorkspacePlace.getWorkspace(), placeController, AdminCentralApplication.this, uiModel);
                }
                else if(place instanceof ShowContentPlace){
                    ShowContentPlace showContentPlace = (ShowContentPlace)place;
                    return new ShowContentActivity(showContentPlace.getViewTarget(), showContentPlace.getViewName());
                }
                else if(place instanceof SomePlace){
                    return new AbstractActivity() {
                        public void start(HasComponent display, EventBus eventBus) {
                            display.setComponent(new Label(((SomePlace)place).getName()));
                        }
                    };
                }
                else{
                    return null;
                }
            }
        }, eventBus);


        mainActivityManager.setDisplay(new ComponentContainerBasedDisplay("main", mainContainer));
        menuActivityManager.setDisplay(new ComponentContainerBasedDisplay("navigation", menuDisplay));

        // Browser history integration
        PlaceHistoryMapper historyMapper = new AdminCentralPlaceHistoryMapper();
        PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
        final EditWorkspacePlace defaultPlace = new EditWorkspacePlace("website");
        historyHandler.register(placeController, eventBus, defaultPlace);
        outerContainer.addComponent(historyHandler);
        historyHandler.handleCurrentHistory();
    }

    /**
     * Creates the application layout and UI elements.
     */
    private void initLayout() {
        mainContainer = new VerticalLayout();
        mainContainer.setSizeFull();


        outerContainer = new VerticalLayout();
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

        menuDisplay = new VerticalLayout();
        menuDisplay.setHeight("100%");


        final HorizontalSplitPanel mainSplitPanel = new HorizontalSplitPanel();
        mainSplitPanel.setSplitPosition(15);
        mainSplitPanel.setSizeFull();
        mainSplitPanel.addComponent(menuDisplay);
        mainSplitPanel.addComponent(mainContainer);
        innerContainer.addComponent(mainSplitPanel);
        innerContainer.setExpandRatio(mainSplitPanel, 1.0f);
        outerContainer.addComponent(headerLayout);
        outerContainer.addComponent(innerContainer);
        outerContainer.setExpandRatio(headerLayout, 1.0f);
        outerContainer.setExpandRatio(innerContainer, 90.0f);
    }
}
