package info.magnolia.module.admininterface.lists;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.gui.control.ContextMenu;
import info.magnolia.cms.gui.control.ContextMenuItem;
import info.magnolia.cms.gui.control.FunctionBar;
import info.magnolia.cms.gui.control.FunctionBarItem;
import info.magnolia.cms.gui.controlx.list.ListColumn;
import info.magnolia.cms.gui.controlx.list.ListControl;
import info.magnolia.cms.gui.controlx.list.ListModel;
import info.magnolia.cms.gui.controlx.search.RepositorySearchListModel;
import info.magnolia.cms.gui.controlx.search.SearchConfig;
import info.magnolia.cms.gui.query.SearchQuery;
import info.magnolia.cms.gui.query.SearchQueryExpression;
import info.magnolia.cms.gui.query.StringSearchQueryParameter;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.context.MgnlContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;


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
        open.setOnclick("mgnl.admininterface.WebsiteSearchList.show();"); //$NON-NLS-1$ 
        open.addJavascriptCondition("{test: function(){return mgnl.admininterface.WebsiteSearchList.selected != null}}");

        ContextMenuItem navigate = new ContextMenuItem("navigate");
        navigate.setLabel(msgs.get("tree.menu.navigate")); //$NON-NLS-1$
        navigate.setIcon(request.getContextPath() + "/.resources/icons/16/compass.gif"); //$NON-NLS-1$
        navigate.setOnclick("mgnl.admininterface.WebsiteSearchList.navigate();"); //$NON-NLS-1$ 
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
     * Here we create a all over query
     */
    public SearchQuery getQuery() {
        SearchQuery query = new SearchQuery();
        if(StringUtils.isNotEmpty(this.getSearchStr())){
            SearchQueryExpression exp = new StringSearchQueryParameter("*", this.getSearchStr(),StringSearchQueryParameter.CONTAINS);
            query.setRootExpression(exp);
        }
        return query;
    }
    
    /**
     * Not used in this context
     */
    public SearchConfig getSearchConfig() {
        return null;
    }


}
