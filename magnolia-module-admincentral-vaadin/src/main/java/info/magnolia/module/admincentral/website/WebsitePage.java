package info.magnolia.module.admincentral.website;

import com.vaadin.addon.treetable.TreeTable;
import com.vaadin.data.Container;
import com.vaadin.ui.VerticalLayout;

public class WebsitePage extends VerticalLayout {

    private Container.Hierarchical websiteData = WebsiteTreeTableFactory.getInstance().getWebsiteData();
    private TreeTable websites = WebsiteTreeTableFactory.getInstance().createWebsiteTreeTable();

    public WebsitePage() {

        websites.setContainerDataSource(websiteData);
        websites.setVisibleColumns(WebsiteTreeTable.WEBSITE_FIELDS);

        WebsiteTreeTable  website = WebsiteTreeTableFactory.getInstance().createWebsiteTreeTable();
        Container.Hierarchical websiteData = WebsiteTreeTableFactory.getInstance().getWebsiteData();
        website.setContainerDataSource(websiteData);
        website.setVisibleColumns(WebsiteTreeTable.WEBSITE_FIELDS);
        addComponent(website);
    }
}
