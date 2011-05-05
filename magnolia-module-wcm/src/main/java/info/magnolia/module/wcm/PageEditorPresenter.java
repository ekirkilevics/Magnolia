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
package info.magnolia.module.wcm;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

import com.vaadin.Application;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.module.templating.Paragraph;
import info.magnolia.module.templating.ParagraphManager;
import info.magnolia.module.wcm.editor.SelectionChangedEvent;
import info.magnolia.module.wcm.editor.SelectionChangedHandler;
import info.magnolia.module.wcm.place.PageEditorPlace;
import info.magnolia.module.wcm.toolbox.ToolboxView;
import info.magnolia.ui.admincentral.MainActivityMapper;
import info.magnolia.ui.admincentral.dialog.view.DialogPresenter;
import info.magnolia.ui.framework.activity.ActivityManager;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.place.Place;
import info.magnolia.ui.framework.place.PlaceController;
import info.magnolia.ui.framework.place.PlaceHistoryHandler;
import info.magnolia.ui.framework.place.PlaceHistoryMapper;
import info.magnolia.ui.framework.place.PlaceHistoryMapperImpl;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.menu.definition.MenuItemDefinition;
import info.magnolia.ui.vaadin.integration.view.ComponentContainerBasedViewPort;

/**
 * Presenter logic for page editor.
 */
public class PageEditorPresenter implements ToolboxView.Presenter, SelectionChangedHandler {

    private Shell shell;
    private EventBus eventBus;
    private PlaceController placeController;
    private MainActivityMapper mainActivityMapper;
    private PageEditorView pageEditorView;
    private ToolboxView toolboxView;
    private WcmModule wcmModule;
    private Application application;
    private DialogPresenter dialogPresenter;

    // TODO should not depend on wcmModule but rather on a configuration provider

    public PageEditorPresenter(Shell shell, EventBus eventBus, PlaceController placeController, MainActivityMapper mainActivityMapper, PageEditorView pageEditorView, Application application, DialogPresenter dialogPresenter, WcmModule wcmModule) {
        this.shell = shell;
        this.eventBus = eventBus;
        this.placeController = placeController;
        this.mainActivityMapper = mainActivityMapper;
        this.pageEditorView = pageEditorView;
        this.wcmModule = wcmModule;
        this.application = application;
        this.dialogPresenter = dialogPresenter;
    }

    public void init() {

        PlaceHistoryMapper historyMapper = new PlaceHistoryMapperImpl(PageEditorPlace.class);
        PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper, shell);
        Place defaultPlace = Place.NOWHERE;
        historyHandler.register(placeController, eventBus, defaultPlace);

        ActivityManager activityManager = new ActivityManager(mainActivityMapper, eventBus);
        activityManager.setViewPort(new ComponentContainerBasedViewPort("main", pageEditorView.getEditorContainer()));

        historyHandler.handleCurrentHistory();

        toolboxView = pageEditorView.getToolboxView();
        toolboxView.setPresenter(this);

        eventBus.addHandler(SelectionChangedEvent.class, this);

        toolboxView.showRack(wcmModule.getToolboxConfiguration().getPage());
    }

    private String type;
    private String workspace;
    private String collectionName;
    private String nodeName;

    @Override
    public void onSelectionChanged(SelectionChangedEvent event) {
        this.type = event.getType();
        this.workspace = event.getWorkspace();
        this.collectionName = event.getCollectionName();
        this.nodeName = event.getNodeName();
        if ("page".equals(event.getType())) {
            toolboxView.showRack(wcmModule.getToolboxConfiguration().getPage());
        } else if ("area".equals(event.getType())) {
            toolboxView.showRack(wcmModule.getToolboxConfiguration().getArea());
        } else if ("paragraph".equals(event.getType())) {
            toolboxView.showRack(wcmModule.getToolboxConfiguration().getParagraph());
        }
    }

    @Override
    public void onMenuItemSelected(MenuItemDefinition menuItem) {
        System.out.println("Command clicked " + menuItem);
    }

    public void openDialog(String dialog, String workspace, String path, String collectionName) {
        try {
            dialogPresenter.showDialog(workspace, path, collectionName, dialog);
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public void addParagraph(String workspace, String path, String collectionName, String paragraphs) {
        application.getMainWindow().addWindow(new ParagraphSelectionDialog(StringUtils.split(paragraphs, ", \t\n"), workspace, path, collectionName));
    }

    public void selectionChanged(String type, String workspace, String path, String collectionName, String nodeName) {
        // TODO we fire the event from this class and receives it in this class, not really necessary
        eventBus.fireEvent(new SelectionChangedEvent(type, workspace, path, collectionName, nodeName));
    }

    /**
     * Dialog for selecting a paragraph to add.
     */
    private class ParagraphSelectionDialog extends Window {

        private OptionGroup optionGroup;

        private ParagraphSelectionDialog(String[] paragraphs, final String workspace, final String path, final String collectionName) {

            setCaption("Select paragraph");
            setModal(true);
            setResizable(true);
            setClosable(false);
            setWidth("800px");

            HorizontalLayout buttons = new HorizontalLayout();
            buttons.setSpacing(true);
            Button save = new Button("Save", new Button.ClickListener() {

                public void buttonClick(Button.ClickEvent event) {

                    String paragraphName = (String) optionGroup.getValue();

                    // TODO validate that something is selected

                    Paragraph paragraph = ParagraphManager.getInstance().getParagraphDefinition(paragraphName);

                    if (paragraph != null) {

                        close();

                        String dialog = resolveDialog(paragraph);

                        if (dialog != null) {
                            try {
                                dialogPresenter.showDialog(workspace, path, collectionName, dialog);
                            } catch (RepositoryException e) {
                                throw new RuntimeRepositoryException(e);
                            }
                        }
                    }
                }
            });
            save.addStyleName("primary");
            save.setClickShortcut(ShortcutAction.KeyCode.ENTER, ShortcutAction.ModifierKey.CTRL);
            buttons.addComponent(save);
            buttons.setComponentAlignment(save, "right");

            Button cancel = new Button("Cancel", new Button.ClickListener() {

                public void buttonClick(Button.ClickEvent event) {
                    close();
                }
            });
            cancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
            buttons.addComponent(cancel);
            buttons.setComponentAlignment(cancel, "right");

            VerticalLayout layout = new VerticalLayout();
            layout.setMargin(true);
            layout.setSpacing(true);
            layout.setSizeFull();

            // TODO use IoC
            ParagraphManager paragraphManager = ParagraphManager.getInstance();

            optionGroup = new OptionGroup();

            for (String paragraph : paragraphs) {
                Paragraph paragraphDefinition = paragraphManager.getParagraphDefinition(paragraph);
                if (paragraphDefinition == null) {
                    continue;
                }

                optionGroup.addItem(paragraph);
                // TODO i18n
                optionGroup.setItemCaption(paragraph, paragraphDefinition.getTitle());
            }

            HorizontalLayout horizontalLayout = new HorizontalLayout();
            horizontalLayout.addComponent(optionGroup);
            layout.addComponent(horizontalLayout);

            layout.addComponent(buttons);
            layout.setComponentAlignment(buttons, "right");

            super.getContent().addComponent(layout);
        }
    }

    // TODO this is duplicated in a few more places
    private String resolveDialog(Paragraph paragraph) {
        String dialogToUse = paragraph.getDialog();
        if (dialogToUse == null) {
            return paragraph.getName();
        }
        return dialogToUse;
    }
}
