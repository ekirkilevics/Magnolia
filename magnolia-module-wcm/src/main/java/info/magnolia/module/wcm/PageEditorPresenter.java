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

import java.util.ArrayList;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.context.MgnlContext;
import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.wcm.editor.ContentSelection;
import info.magnolia.module.wcm.editor.PageChangedEvent;
import info.magnolia.module.wcm.editor.PageChangedHandler;
import info.magnolia.module.wcm.editor.SelectionChangedEvent;
import info.magnolia.module.wcm.editor.SelectionChangedHandler;
import info.magnolia.module.wcm.editor.SelectionType;
import info.magnolia.module.wcm.place.PageEditorPlace;
import info.magnolia.module.wcm.toolbox.ToolboxActionFactory;
import info.magnolia.module.wcm.toolbox.ToolboxConfigurationProvider;
import info.magnolia.module.wcm.toolbox.ToolboxView;
import info.magnolia.module.wcm.toolbox.action.AddParagraphActionDefinition;
import info.magnolia.module.wcm.toolbox.action.ToolboxAction;
import info.magnolia.ui.admincentral.MainActivityMapper;
import info.magnolia.ui.admincentral.dialog.DialogPresenterFactory;
import info.magnolia.ui.admincentral.dialog.DialogSaveCallback;
import info.magnolia.ui.admincentral.dialog.view.DialogPresenter;
import info.magnolia.ui.framework.activity.ActivityManager;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.place.Place;
import info.magnolia.ui.framework.place.PlaceController;
import info.magnolia.ui.framework.place.PlaceHistoryHandler;
import info.magnolia.ui.framework.place.PlaceHistoryMapper;
import info.magnolia.ui.framework.place.PlaceHistoryMapperImpl;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.action.Action;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.menu.definition.MenuItemDefinition;
import info.magnolia.ui.vaadin.integration.view.ComponentContainerBasedViewPort;

/**
 * Presenter logic for page editor.
 *
 * @version $Id$
 */
public class PageEditorPresenter implements ToolboxView.Presenter, SelectionChangedHandler, PageChangedHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Shell shell;
    private EventBus eventBus;
    private PlaceController placeController;
    private MainActivityMapper mainActivityMapper;
    private PageEditorView pageEditorView;
    private ToolboxView toolboxView;
    private ToolboxConfigurationProvider toolboxConfigurationProvider;
    private DialogPresenterFactory dialogPresenterFactory;
    private ToolboxActionFactory toolboxActionFactory;

    public PageEditorPresenter(Shell shell, EventBus eventBus, PlaceController placeController, MainActivityMapper mainActivityMapper, PageEditorView pageEditorView, DialogPresenterFactory dialogPresenterFactory, ToolboxActionFactory toolboxActionFactory, ToolboxConfigurationProvider toolboxConfigurationProvider) {
        this.shell = shell;
        this.eventBus = eventBus;
        this.placeController = placeController;
        this.mainActivityMapper = mainActivityMapper;
        this.pageEditorView = pageEditorView;
        this.dialogPresenterFactory = dialogPresenterFactory;
        this.toolboxActionFactory = toolboxActionFactory;
        this.toolboxConfigurationProvider = toolboxConfigurationProvider;

        eventBus.addHandler(PageChangedEvent.class, this);
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

        toolboxView.showRack(toolboxConfigurationProvider.getToolboxConfiguration().getPage());
    }

    private ContentSelection contentSelection;

    @Override
    public void onSelectionChanged(SelectionChangedEvent event) {

        this.contentSelection = new ContentSelection();
        this.contentSelection.setType(event.getType());
        this.contentSelection.setWorkspace(event.getWorkspace());
        this.contentSelection.setPath(event.getPath());
        this.contentSelection.setCollectionName(event.getCollectionName());
        this.contentSelection.setNodeName(event.getNodeName());
        this.contentSelection.setParagraphs(event.getParagraphs());
        this.contentSelection.setDialog(event.getDialog());

        List<MenuItemDefinition> menuItemDefinitions = null;
        switch (this.contentSelection.getType()) {
            case PAGE:
                menuItemDefinitions = toolboxConfigurationProvider.getToolboxConfiguration().getPage();
                break;
            case AREA:
                menuItemDefinitions = toolboxConfigurationProvider.getToolboxConfiguration().getArea();
                break;
            case SINGLE:
                menuItemDefinitions = toolboxConfigurationProvider.getToolboxConfiguration().getSingle();
                break;
            case PARAGRAPH:
                menuItemDefinitions = toolboxConfigurationProvider.getToolboxConfiguration().getParagraph();
                break;
            case PARAGRAPH_IN_SINGLE:
                menuItemDefinitions = toolboxConfigurationProvider.getToolboxConfiguration().getParagraphInSingle();
                break;
        }

        if (menuItemDefinitions != null) {
            List<MenuItemDefinition> x = new ArrayList<MenuItemDefinition>();
            for (MenuItemDefinition mid : menuItemDefinitions) {
                Node node = getNodeForSelection(this.contentSelection);

                // TODO an optimization here would be to use reflection to test if the action implements TreeAction, instantiating it only to test this is a waste

                Action action = toolboxActionFactory.createAction(mid.getActionDefinition(), node, this.contentSelection);

                if (action instanceof ToolboxAction) {
                    try {
                        ToolboxAction toolboxAction = (ToolboxAction) action;
                        if (toolboxAction.isAvailable(this.contentSelection, node)) {
                            x.add(mid);
                        }
                    } catch (RepositoryException e) {
                        log.error("Error when determining if action [" + mid.getName() + "] is available", e);
                    }
                } else {
                    x.add(mid);
                }
            }
            toolboxView.showRack(x);
        }
    }

    @Override
    public void onMenuItemSelected(MenuItemDefinition menuItem) {
        System.out.println("Command clicked " + menuItem);

        ContentSelection selection = this.contentSelection;
        if (selection == null) {
            // TreeActivity selects the page when it loads so this should never be null after that
            return;
        }

        executeAction(menuItem.getActionDefinition(), selection);
    }

    private void executeAction(ActionDefinition actionDefinition, ContentSelection selection) {
        Node node = getNodeForSelection(selection);
        Action action = toolboxActionFactory.createAction(actionDefinition, node, selection);
        System.out.println("Executing " + action.getClass());
        try {
            action.execute();
        } catch (ActionExecutionException e) {
            // TODO present error to user
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private Node getNodeForSelection(ContentSelection selection) {
        try {
            return MgnlContext.getJCRSession(selection.getWorkspace()).getNode(selection.getPath());
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public void openDialog(String dialogName, String workspace, String path, String collectionName, String nodeName) {
        DialogPresenter dialogPresenter = dialogPresenterFactory.createDialog(dialogName);
        dialogPresenter.setWorkspace(workspace);
        dialogPresenter.setPath(path);
        dialogPresenter.setCollectionName(collectionName);
        dialogPresenter.setNodeName(nodeName);
        dialogPresenter.setDialogSaveCallback(new DialogSaveCallback() {
            @Override
            public void onSave(Node node) {
                eventBus.fireEvent(new PageChangedEvent());
            }
        });
        dialogPresenter.showDialog();
    }

    public void addParagraph(String workspace, String path, String collectionName, String nodeName, String paragraphs, String dialog) {

        ContentSelection selection = new ContentSelection();
        selection.setDialog(dialog);
        selection.setWorkspace(workspace);
        selection.setPath(path);
        selection.setCollectionName(collectionName);
        selection.setNodeName(nodeName);
        selection.setParagraphs(paragraphs);
        selection.setDialog(dialog);

        executeAction(new AddParagraphActionDefinition(), selection);
    }

    public void selectionChanged(SelectionType type, String workspace, String path, String collectionName, String nodeName, String paragraphs, String dialog) {
        // TODO we fire the event from this class and receives it in this class, not really necessary
        eventBus.fireEvent(new SelectionChangedEvent(type, workspace, path, collectionName, nodeName, paragraphs, dialog));
    }

    @Override
    public void onPageChanged() {
        this.contentSelection = null;
        this.toolboxView.showRack(toolboxConfigurationProvider.getToolboxConfiguration().getPage());
    }

    public void moveParagraph(String workspaceName, String sourcePath, String destinationPath) throws RepositoryException {
        Session session = MgnlContext.getJCRSession(workspaceName);
        Node source = session.getNode(sourcePath);
        Node destination = session.getNode(destinationPath);
        NodeUtil.moveNode(source, destination);
        session.save();
        eventBus.fireEvent(new PageChangedEvent());
    }

    public void moveParagraphBefore(String workspaceName, String sourcePath, String destinationPath) throws RepositoryException {
        Session session = MgnlContext.getJCRSession(workspaceName);
        Node source = session.getNode(sourcePath);
        Node destination = session.getNode(destinationPath);
        NodeUtil.moveNodeBefore(source, destination);
        session.save();
        eventBus.fireEvent(new PageChangedEvent());
    }

    public void moveParagraphAfter(String workspaceName, String sourcePath, String destinationPath) throws RepositoryException {
        Session session = MgnlContext.getJCRSession(workspaceName);
        Node source = session.getNode(sourcePath);
        Node destination = session.getNode(destinationPath);
        NodeUtil.moveNodeAfter(source, destination);
        session.save();
        eventBus.fireEvent(new PageChangedEvent());
    }
}
