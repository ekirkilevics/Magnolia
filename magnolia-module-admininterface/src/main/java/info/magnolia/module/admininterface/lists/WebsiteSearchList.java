package info.magnolia.module.admininterface.lists;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.gui.control.FunctionBar;
import info.magnolia.cms.gui.controlx.list.ListColumn;
import info.magnolia.cms.gui.controlx.list.ListControl;
import info.magnolia.cms.gui.controlx.list.ListModel;
import info.magnolia.cms.gui.controlx.search.RepositorySearchListModel;
import info.magnolia.cms.gui.controlx.search.SearchConfig;
import info.magnolia.cms.gui.controlx.search.SearchConfigImpl;
import info.magnolia.cms.gui.controlx.search.SearchControlDefinition;


public class WebsiteSearchList extends AbstractSimpleSearchList {

    public WebsiteSearchList(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }
    
    /**
     * @see com.obinary.magnolia.professional.lists.AbstractAdvancedSearchList#getFunctionBar()
     */
    public FunctionBar getFunctionBar() {
        if(super.getFunctionBar() == null){
            this.setFunctionBar(new FunctionBar("functionBar"));
        }
        return super.getFunctionBar();
    }

    /**
     * @see info.magnolia.module.admininterface.lists.AbstractList#getModel()
     */
    public ListModel getModel() {
        return new RepositorySearchListModel(ContentRepository.WEBSITE);
    }
    
    public void configureList(ListControl list) {
        
        list.addColumn(new ListColumn("name", "Name", "200", true));
        list.addColumn(new ListColumn("type", "Type", "200", true));
        list.addColumn(new ListColumn("mgnl:authorid", "User", "200", true));
        list.addColumn(new ListColumn("title", "Title", "200", true));
        
        list.addSortableField("name");
        list.addSortableField("title");
        
        list.addGroupableField("mgnl:authorid");
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
