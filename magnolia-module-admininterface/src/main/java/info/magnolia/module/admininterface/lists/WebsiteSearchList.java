package info.magnolia.module.admininterface.lists;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.controlx.list.ListColumn;
import info.magnolia.cms.gui.controlx.list.ListModel;
import info.magnolia.cms.gui.controlx.search.RepositorySearchListModel;
import info.magnolia.cms.gui.controlx.search.SearchConfig;
import info.magnolia.cms.gui.controlx.search.SearchConfigImpl;
import info.magnolia.cms.gui.controlx.search.SearchControlDefinition;
import info.magnolia.cms.gui.controlx.search.SearchListControl;
import info.magnolia.cms.gui.query.SearchQuery;
import info.magnolia.cms.util.DumperUtil;


public class WebsiteSearchList extends AbstractSearchList {

    public WebsiteSearchList(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    /**
     * Configures the list and search.
     */
    protected void init() {
        ListModel model = new RepositorySearchListModel(ContentRepository.WEBSITE); 
        SearchListControl list = getSearchList();
        list.setModel(model);
        
        list.addColumn(new ListColumn("name", "Name", "200", true));
        list.addColumn(new ListColumn("type", "Type", "200", true));
        list.addColumn(new ListColumn("authorid", "User", "200", true));
        list.addColumn(new ListColumn("title", "Title", "200", true));
        
        list.addSortableField("name");
        list.addSortableField("title");
        
        list.addGroupableField("authorId");
        
        list.setSearchConfig(getSearchConfig());
        
        // set an empty query --> returns everything
        list.setQuery(new SearchQuery());
    }

    /**
     * 
     */
    protected SearchConfig getSearchConfig() {
        SearchConfig searchConfig = new SearchConfigImpl();
        searchConfig.addControlDefinition(new SearchControlDefinition("title", "Title"));
        return searchConfig;
    }

    /**
     * @return
     */
    private ListColumn getNameColumn() {
        ListColumn nameColumn = new ListColumn(){
            {
                setName("name");
                setColumnName("name");
                setLabel("Name");
                setWidth("200");
            }
            
            public Object getValue() {
                Content content = (Content) this.getListControl().getIteratorValueObject();
                return content.getName();
            }
        };
        return nameColumn;
    }

}
