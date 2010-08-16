package info.magnolia.module.admincentral.navigation;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admincentral.dialog.DialogSandboxPage;
import info.magnolia.module.admincentral.website.WebsiteTreeTable;
import info.magnolia.module.admincentral.website.WebsiteTreeTableFactory;

import java.util.Iterator;

import javax.jcr.RepositoryException;

import com.vaadin.data.Container.Hierarchical;
import com.vaadin.terminal.ClassResource;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;


/**
 * The Application accordion Menu.
 * @author fgrilli
 *
 */
public class Menu extends Accordion{

    private static final long serialVersionUID = 1L;
    private Content node = null;

    /**
     * @param path the path to the menu
     */
    public Menu(final String path) throws RepositoryException{
        node = MgnlContext.getSystemContext().getHierarchyManager(ContentRepository.CONFIG).getContent(path);
    }
    /**
     * See {@link AbstractComponent#getApplication()} javadoc as to why we need to do most of the initialization here and not in the constructor.
     */
    @Override
    public void attach() {
        super.attach();
        for (Iterator<Content> iter = node.getChildren(ItemType.CONTENTNODE).iterator(); iter.hasNext();) {
            Content menuItem = iter.next();
            GridLayout gridLayout = new GridLayout(1,1);
            gridLayout.setSpacing(true);
            gridLayout.setMargin(true);
            // check permission
            if (isMenuItemRenderable(menuItem)) {
                // sub menu items (2 levels only)
                for (Iterator<Content> iterator = menuItem.getChildren(ItemType.CONTENTNODE).iterator(); iterator.hasNext();) {
                    Content sub = iterator.next();
                    if (isMenuItemRenderable(sub)) {
                        gridLayout.addComponent(new MenuItem(sub));
                    }
                }
            }
            if(gridLayout.getComponentIterator().hasNext()){
                addTab(gridLayout, getLabel(menuItem), new ClassResource(getIconPath(menuItem), getApplication()));
            } else {
                addTab(new Label(), getLabel(menuItem), new ClassResource(getIconPath(menuItem), getApplication()));
            }
        }
        //TODO for testing only. To be removed.
        addTab(new Label("For testing dialogs"), "Dialogs", null);
        addListener(new SelectedMenuItemTabChangeListener());
    }

    /**
     * @param menuItem
     * @return
     */
    protected String getLabel(Content menuItem) {
        return NodeDataUtil.getI18NString(menuItem, "label");
    }

    protected String getIconPath(Content menuItem){
        String iconPath = NodeDataUtil.getString(menuItem, "icon").trim();
        return iconPath.replaceFirst(".resources/", "mgnl-resources/");
    }

    protected Component getComponentByCaption(String caption){
        for(Iterator<Component> iter = getApplication().getMainWindow().getComponentIterator(); iter.hasNext();){
            Component component = iter.next();
            if(caption.equalsIgnoreCase(component.getCaption())){
                return component;
            }
        }
        return null;
    }

    /**
     * @param menuItem
     * @return <code>true</code> if the the current user is granted access to this menu item, <code>false</code> otherwise
     */
    protected boolean isMenuItemRenderable(Content menuItem) {
        return MgnlContext.getAccessManager(ContentRepository.CONFIG).isGranted(menuItem.getHandle(), Permission.READ);
    }

    //TODO extract this as a top level class?
    public class MenuItem extends Button{
        private static final long serialVersionUID = 1L;
        private Content content;

        public MenuItem(final Content content) {
            this.content = content;
        }

        /**
         * See {@link AbstractComponent#getApplication()} javadoc as to why we need to do most of the initialization here and not in the constructor.
         */
        @Override
        public void attach() {
            super.attach();
            setCaption(getLabel(content));
            setStyleName(BaseTheme.BUTTON_LINK);
            setHeight(30f, Button.UNITS_PIXELS);
            setIcon(new ClassResource(getIconPath(content), getApplication()));
            final String onClickAction = NodeDataUtil.getString(content, "onclick").trim();
            addListener(new Button.ClickListener () {

                public void buttonClick(ClickEvent event) {
                    ComponentContainer mainContent = (ComponentContainer)getComponentByCaption("mainContainer");
                    //TODO add proper component here, for now just show onclick action
                    getApplication().getMainWindow().showNotification("OnClick", onClickAction, Notification.TYPE_HUMANIZED_MESSAGE);
                }

            });
        }
    }

    public class SelectedMenuItemTabChangeListener implements SelectedTabChangeListener {

        private static final long serialVersionUID = 1L;

        public void selectedTabChange(SelectedTabChangeEvent event) {
            TabSheet tabsheet = event.getTabSheet();
            Tab tab = tabsheet.getTab(tabsheet.getSelectedTab());
            if (tab != null) {
                getApplication().getMainWindow().showNotification("Selected tab: " + tab.getCaption());

            if("website".equalsIgnoreCase(tab.getCaption())) {
                ComponentContainer mainContainer = (ComponentContainer)getComponentByCaption("mainContainer");
                mainContainer.removeAllComponents();
                WebsiteTreeTable  website = WebsiteTreeTableFactory.getInstance().createWebsiteTreeTable();
                Hierarchical websiteData = WebsiteTreeTableFactory.getInstance().getWebsiteData();
                website.setContainerDataSource(websiteData);
                website.setVisibleColumns(WebsiteTreeTable.WEBSITE_FIELDS);
                mainContainer.addComponent(website);
            }
            //TODO remove this if block, it's here just for testing purposes
            if ("dialogs".equalsIgnoreCase(tab.getCaption())) {
                ComponentContainer mainContainer = (ComponentContainer)getComponentByCaption("mainContainer");
                mainContainer.removeAllComponents();
                mainContainer.addComponent(new DialogSandboxPage());
            }
          }
        }
    }
}


