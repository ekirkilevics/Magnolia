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
package info.magnolia.module.admincentral.dialog;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Validator;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admincentral.control.StaticControl;
import info.magnolia.module.admincentral.dialog.editor.EditDialogWindow;

/**
 * View for a dialog.
 *
 * @author tmattsson
 */
public class DialogView extends CustomComponent {

    private HorizontalLayout description;

    /**
     * Presenter for DialogView.
     */
    public interface Presenter {
        void onSave();
        void onCancel();
        void onClose();
    }

    /**
     * Called after saving and before closing the window.
     */
    public interface PostSaveListener {

        void postSave(Node orCreateContentNode);

    }

    private static final Logger log = LoggerFactory.getLogger(EditParagraphWindow.class);

    private Presenter presenter;

    private String repository;
    private String path;
    private String nodeCollection;
    private String nodeName;
    private DialogDefinition dialog;
    private PostSaveListener postSaveListener;

    public DialogView(Presenter presenter, String repository, String path, String nodeCollection, String nodeName, String dialogName, DialogDefinition dialog) throws RepositoryException {
        this.presenter = presenter;
        this.repository = repository;
        this.path = path;
        this.nodeCollection = nodeCollection;
        this.nodeName = nodeName;
        this.dialog = dialog;

        initLayout(dialogName);
    }

    private void initLayout(final String dialogName) throws RepositoryException {

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);

        Component mainViewArea;

        if (dialog == null) {
            GridLayout grid = new GridLayout(2, 1);
            grid.setSpacing(true);
            grid.setMargin(true);
            StaticControl warning = new StaticControl();
            warning.setLabel("Dialog \"" + dialogName + "\" for this paragraph cannot be found. Please contact system administrator.");
            warning.create((Node)null, grid);
            mainViewArea = grid;
        } else {

            Node storageNode = getContentNode();

            setCaption(storageNode != null ? "Edit paragraph" : "New paragraph");

            Button editDialogConfiguration = new Button(MessagesManager.getMessages("info.magnolia.module.admincentral.messages").getWithDefault("buttons.editDialogConfiguration", "buttons.editDialogConfiguration"), new Button.ClickListener() {
                public void buttonClick(Button.ClickEvent event) {
                    getApplication().getMainWindow().addWindow(new EditDialogWindow(dialogName));
                }
            });
            editDialogConfiguration.setClickShortcut(ShortcutAction.KeyCode.D, ShortcutAction.ModifierKey.CTRL);
            editDialogConfiguration.setStyleName(BaseTheme.BUTTON_LINK);
            buttons.addComponent(editDialogConfiguration);
            buttons.setComponentAlignment(editDialogConfiguration, "right");

            Button save = new Button(dialog.getMessages().get("buttons.save"), new Button.ClickListener() {
                public void buttonClick(Button.ClickEvent event) {
                    if (save()){
                        if(postSaveListener != null){
                            try {
                                postSaveListener.postSave(getOrCreateContentNode());
                            }
                            catch (RepositoryException e) {
                                throw new RuntimeException("Error after saving.", e);
                            }
                        }
                        presenter.onClose();
                    }
                }
            });
            save.addStyleName("primary");
            save.setClickShortcut(ShortcutAction.KeyCode.ENTER, ShortcutAction.ModifierKey.CTRL);
            buttons.addComponent(save);
            buttons.setComponentAlignment(save, "right");


            VerticalTabSheet tabSheet = new VerticalTabSheet();
            tabSheet.setSizeFull();
            for (DialogTab dialogTab : dialog.getTabs()) {

                GridLayout grid = new GridLayout(2, 1);
                grid.setSpacing(true);
                grid.setMargin(false);

                for (final DialogControl dialogItem : dialogTab.getFields()) {

                    dialogItem.create(storageNode, grid);
                    dialogItem.setPresenter(new DialogControl.Presenter() {
                        public void onFocus() {
                            Label label = (Label) description.getComponent(0);
                            label.setValue(dialogItem.getDescription() + dialogItem.toString());
                            System.out.println(dialogItem);
                        }
                    });

                    grid.newLine();
                }

                tabSheet.addTab(dialogTab.getLabel(), dialogTab.getLabel(), grid);
            }

            mainViewArea = tabSheet;
        }

        final String key = "buttons.cancel";
        final String label = dialog != null ? dialog.getMessages().get(key) : MessagesManager.getMessages().getWithDefault(key, key);
        Button cancel = new Button(label, new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                presenter.onCancel();
                presenter.onClose();
            }
        });
        cancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
        buttons.addComponent(cancel);
        buttons.setComponentAlignment(cancel, "right");

        description = new HorizontalLayout();
        description.setSizeFull();
        description.addComponent(new Label("This is the description of the currently focused field"));

        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.addComponent(mainViewArea);
        layout.addComponent(description);
        layout.addComponent(buttons);
        layout.setComponentAlignment(buttons, "right");
        super.setCompositionRoot(layout);
    }

    public void setPostSaveListener(PostSaveListener postSaveListener) {
        this.postSaveListener = postSaveListener;
    }

    private boolean save() {
        try {

            Node storageNode = getOrCreateContentNode();

            // Validate
            for (DialogTab dialogTab : dialog.getTabs()) {
                for (DialogControl control : dialogTab.getFields()) {
                    try {
                        control.validate();
                    } catch (Validator.InvalidValueException e) {
                        // TODO we want the Shell class here
//                        getParent().showNotification(e.getMessage(), Window.Notification.TYPE_WARNING_MESSAGE);
                        return false;
                    }
                }
            }

            // Save
            for (DialogTab dialogTab : dialog.getTabs()) {
                for (DialogControl control : dialogTab.getFields()) {
                    control.save(storageNode);
                }
            }

            storageNode.getSession().save();

        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return true;
    }

    private Node getContentNode() throws RepositoryException {

        HierarchyManager hm = MgnlContext.getHierarchyManager(repository);

        Node content = hm.getContent(path).getJCRNode();

        if (StringUtils.isNotEmpty(nodeCollection)) {
            if (!content.hasNode(nodeCollection))
                return null;
            content = content.getNode(nodeCollection);
        }

        if (StringUtils.isNotEmpty(nodeName)) {
            if (!content.hasNode(nodeName))
                return null;
            content = content.getNode(nodeName);
        }

        return content;
    }

    private Node getOrCreateContentNode() throws RepositoryException {

        HierarchyManager hm = MgnlContext.getHierarchyManager(repository);

        Node content = hm.getContent(path).getJCRNode();

        if (StringUtils.isNotEmpty(nodeCollection)) {
            if (!content.hasNode(nodeCollection))
                content = content.addNode(nodeCollection, ItemType.CONTENTNODE.getSystemName());
            else
                content = content.getNode(nodeCollection);
        }

        if (StringUtils.isNotEmpty(nodeName)) {
            if (!content.hasNode(nodeName))
                content = content.addNode(nodeName, ItemType.CONTENTNODE.getSystemName());
            else
                content = content.getNode(nodeName);
        }

        return content;
    }
}
