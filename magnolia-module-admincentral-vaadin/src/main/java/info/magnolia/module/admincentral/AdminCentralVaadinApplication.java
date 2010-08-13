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
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admincentral.dialog.DialogRegistry;
import info.magnolia.module.admincentral.website.WebsiteTreeTable;
import info.magnolia.module.admincentral.website.WebsiteTreeTableFactory;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.Application;
import com.vaadin.addon.treetable.TreeTable;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.MouseEvents;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.SplitPanel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Window.Notification;


/**
 * Demo Application - simple AdressBook.
 * <p/>
 * Added Tree Table Add on to WEB-Inf/lib...
 * <p/>
 * TODO show website tree instead of dummy address book data. Started to work on that but it's
 * clearly a mess as you can see.
 *
 * @author dan
 * @author fgrilli
 */
public class AdminCentralVaadinApplication extends Application {

    private static final Logger log = LoggerFactory.getLogger(AdminCentralVaadinApplication.class);

    private static String[] fields = {"First Name", "Last Name", "Company",
            "Mobile Phone", "Work Phone", "Home Phone", "Work Email",
            "Home Email", "Street", "Zip", "City", "State", "Country"};

    private static final long serialVersionUID = 5773744599513735815L;

    public static final String WINDOW_TITLE = "Magnolia AdminCentral";

    private Navigation menu = createMenu();

    private VerticalLayout mainContainer;

    private IndexedContainer websiteData = WebsiteTreeTableFactory.getInstance().getWebsiteData();

    private HorizontalLayout bottomLeftCorner = new HorizontalLayout();

    private Form contactEditor = new Form();

    private TreeTable websites = WebsiteTreeTableFactory.getInstance().createWebsiteTreeTable();

    private Button contactRemovalButton;

    private Navigation createMenu() {
        Navigation navigation = null;
        try {
            navigation = new Navigation("/modules/adminInterface/config/menu");
            navigation.addListener(new Accordion.SelectedTabChangeListener() {

                private static final long serialVersionUID = 1L;

                public void selectedTabChange(SelectedTabChangeEvent event) {
                    TabSheet tabsheet = event.getTabSheet();
                    Tab tab = tabsheet.getTab(tabsheet.getSelectedTab());
                    if (tab != null) {
                        getMainWindow().showNotification(
                                "Selected tab: " + tab.getCaption());

                        if (tab.getCaption().equals("Dialogs")) {
                            mainContainer.removeAllComponents();
                            Button open = new Button("Edit paragraph",
                                    new Button.ClickListener() {

                                        public void buttonClick(Button.ClickEvent event) {
                                            try {
                                                openDialog();
                                            }
                                            catch (RepositoryException e) {
                                                e.printStackTrace(); // To change body of catch
                                                                     // statement use File |
                                                                     // Settings | File Templates.
                                            }
                                        }
                                    });
                            mainContainer.addComponent(open);
                        }
                    }
                }
            });
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
            getMainWindow().showNotification("Application menu could not be created.", re.getMessage(), Notification.TYPE_ERROR_MESSAGE);
        }
        return navigation;
    }

    private void openDialog() throws RepositoryException {

        HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.CONFIG);
        Content storageNode = ContentUtil.createPath(hm, "/modules/genuine-vaadin-central/foobar", true);

        DialogRegistry dialogRegistry = DialogRegistry.getInstance();

        getMainWindow().addWindow(dialogRegistry.createDialog("mock", storageNode));
    }

    /**
     * @return a container with 1000 First/Lastname combinations (randomly generated)
     */
    private IndexedContainer createDummyData() {

        String[] fnames = {"Peter", "Alice", "Joshua", "Mike", "Olivia",
                "Nina", "Alex", "Rita", "Dan", "Umberto", "Henrik", "Rene",
                "Lisa", "Marge"};
        String[] lnames = {"Smith", "Gordon", "Simpson", "Brown", "Clavel",
                "Simons", "Verne", "Scott", "Allison", "Gates", "Rowling",
                "Barks", "Ross", "Schneider", "Tate"};

        IndexedContainer ic = new IndexedContainer();

        for (String p : fields) {
            ic.addContainerProperty(p, String.class, "");
        }

        for (int i = 0; i < 1000; i++) {
            Object id = ic.addItem();
            ic.getContainerProperty(id, "First Name").setValue(
                    fnames[(int) (fnames.length * Math.random())]);
            ic.getContainerProperty(id, "Last Name").setValue(
                    lnames[(int) (lnames.length * Math.random())]);
        }

        return ic;
    }

    @Override
    public void init() {
        /**
         * dan: simply remove next in order to get the default theme ("reindeer")
         */
        setTheme("runo");
        initLayout();
        initContactAddRemoveButtons();
        initContactDetailsView();
        initContactList();
        initFilteringControls();
    }

    private void initContactAddRemoveButtons() {
        // New item button
        bottomLeftCorner.addComponent(new Button("+",
                new Button.ClickListener() {

                    /**
                     *
                     */
                    private static final long serialVersionUID = 1L;

                    public void buttonClick(ClickEvent event) {
                        Object id = websites.addItem();
                        websites.setValue(id);
                    }
                }));

        // Remove item button
        contactRemovalButton = new Button("-", new Button.ClickListener() {

            /**
             *
             */
            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {
                websites.removeItem(websites.getValue());
                websites.select(null);
            }
        });
        contactRemovalButton.setVisible(false);
        bottomLeftCorner.addComponent(contactRemovalButton);
    }

    /**
     * Demonstrate Dialog-Handling.
     */
    private void initContactDetailsView() {
        final Window dialog = new Window("Address Details");
        dialog.addComponent(contactEditor);
        websites.addListener(new ItemClickEvent.ItemClickListener() {

            /**
             *
             */
            private static final long serialVersionUID = 1L;

            public void itemClick(ItemClickEvent e) {
                if (e.getButton() == MouseEvents.ClickEvent.BUTTON_RIGHT) {
                    getMainWindow().addWindow(dialog);
                }
            }
        });
    }


    private void initContactList() {
        websites.setContainerDataSource(websiteData);
        websites.setVisibleColumns(WebsiteTreeTable.WEBSITE_FIELDS);
    }

    /**
     * TODO dlipp: decided whether needed or not - remove or fix then.
     */
    private void initFilteringControls() {
        for (final String pn : WebsiteTreeTable.WEBSITE_FIELDS) {
            final TextField sf = new TextField();
            bottomLeftCorner.addComponent(sf);
            sf.setWidth("100%");
            sf.setInputPrompt(pn);
            sf.setImmediate(true);
            bottomLeftCorner.setExpandRatio(sf, 1);
            sf.addListener(new Property.ValueChangeListener() {
                public void valueChange(ValueChangeEvent event) {
                    websiteData.removeContainerFilters(pn);
                    if (sf.toString().length() > 0 && !pn.equals(sf.toString())) {
                        websiteData.addContainerFilter(pn, sf.toString(),
                                true, false);
                    }
                    getMainWindow().showNotification(
                            "" + websiteData.size() + " matches found");
                }
            });
        }
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
        //We need this to fetch it from within the MenuItem's onclick handler and add the proper component
        //TODO is there a better way to achieve this?
        mainContainer.setCaption("mainContainer");

        menu.setSizeFull();

        contactEditor.setSizeFull();
        contactEditor.getLayout().setMargin(true);
        // contactEditor.setImmediate(true);

        bottomLeftCorner.setWidth("100%");

        splitPanel.addComponent(menu);
        splitPanel.addComponent(mainContainer);
        mainContainer.addComponent(websites);
        mainContainer.addComponent(bottomLeftCorner);

        mainContainer.setExpandRatio(websites, 10);
        mainContainer.setExpandRatio(bottomLeftCorner, 1);
    }
}
