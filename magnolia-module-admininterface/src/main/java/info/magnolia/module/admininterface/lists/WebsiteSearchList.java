package info.magnolia.module.admininterface.lists;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.gui.control.ContextMenu;
import info.magnolia.cms.gui.control.ContextMenuItem;
import info.magnolia.cms.gui.control.FunctionBar;
import info.magnolia.cms.gui.control.FunctionBarItem;
import info.magnolia.cms.gui.controlx.list.ListColumn;
import info.magnolia.cms.gui.controlx.list.ListControl;
import info.magnolia.cms.gui.controlx.list.ListModel;
import info.magnolia.cms.gui.controlx.search.RepositorySearchListModel;
import info.magnolia.cms.gui.controlx.search.SearchConfig;
import info.magnolia.cms.gui.controlx.search.SearchConfigImpl;
import info.magnolia.cms.gui.controlx.search.SearchControlDefinition;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;


public class WebsiteSearchList extends AbstractSimpleSearchList {
    
    protected Messages msgs = MessagesManager.getMessages();

    public WebsiteSearchList(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    public ListModel getModel() {
        return new RepositorySearchListModel(ContentRepository.WEBSITE);
    }

    public void configureList(ListControl list) {
        list.setRenderer(new AdminListControlRenderer() {
            public String onSelect(ListControl list, Integer index) {
                String js = "mgnl.admininterface.WebsiteSearchList.selected = '" + list.getIteratorValue("path") + "';";
                js += super.onSelect(list, index);
                return js;
            }
        });
        
        // show the icon by the name
        list.addColumn(new ListColumn() {

            {
                setName("name");
                setColumnName("name");
                setLabel("Name");
                setWidth("200");
            }

            public String render() {
                return "<span style=\"vertical-align: middle\"><img  src=\""
                    + MgnlContext.getContextPath()
                    + "/.resources/icons/16/document_plain_earth.gif\"/></span>"
                    + this.getValue();
            }

        });

        list.addColumn(new ListColumn("mgnl:authorid", "User", "200", true));
        list.addColumn(new ListColumn("title", "Title", "200", true));

        list.addSortableField("name");
        list.addSortableField("title");

        list.addGroupableField("mgnl:authorid");
    }
    
    protected void configureContextMenu(ContextMenu menu) {
        ContextMenuItem open = new ContextMenuItem("open");
        open.setLabel(msgs.get("tree.web.menu.open")); //$NON-NLS-1$
        open.setIcon(request.getContextPath() + "/.resources/icons/16/document_plain_earth.gif"); //$NON-NLS-1$
        open.setOnclick("mgnl.admininterface.WebsiteSearchList.show();"); //$NON-NLS-1$ //$NON-NLS-2$
        open.addJavascriptCondition("{test: function(){return mgnl.admininterface.WebsiteSearchList.selected != null}}");

        ContextMenuItem navigate = new ContextMenuItem("navigate");
        navigate.setLabel(msgs.get("tree.menu.navigate")); //$NON-NLS-1$
        navigate.setIcon(request.getContextPath() + "/.resources/icons/16/compass.gif"); //$NON-NLS-1$
        navigate.setOnclick("mgnl.admininterface.WebsiteSearchList.navigate();"); //$NON-NLS-1$ //$NON-NLS-2$
        navigate.addJavascriptCondition("{test: function(){return mgnl.admininterface.WebsiteSearchList.selected != null}}");
        
        menu.addMenuItem(open);
        menu.addMenuItem(null);
        menu.addMenuItem(navigate);
    }

    protected void configureFunctionBar(FunctionBar bar) {
        bar.setSearchable(true);
        bar.setSearchStr(this.getSearchStr());
        bar.setOnSearchFunction("mgnl.admininterface.WebsiteSearchList.search");
        
        ContextMenu menu = this.getContextMenu();

        bar.addMenuItem(new FunctionBarItem(menu.getMenuItemByName("open")));
        bar.addMenuItem(new FunctionBarItem(menu.getMenuItemByName("navigate")));

    }

    /**
     * The configuration used for the search form.
     */
    public SearchConfig getSearchConfig() {
        SearchConfig searchConfig = new SearchConfigImpl();
        searchConfig.addControlDefinition(new SearchControlDefinition("title", "Title"));
        searchConfig.addControlDefinition(new SearchControlDefinition("mgnl:authorid", "User"));
        return searchConfig;
    }

}
