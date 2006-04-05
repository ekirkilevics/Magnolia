package info.magnolia.cms.gui.controlx.search;

import info.magnolia.cms.gui.control.ContextMenu;
import info.magnolia.cms.gui.controlx.impl.AbstractControl;
import info.magnolia.cms.gui.controlx.list.ListColumn;
import info.magnolia.cms.gui.controlx.list.ListControl;
import info.magnolia.cms.gui.controlx.list.ListModel;
import info.magnolia.cms.gui.query.SearchQuery;

import java.util.Collection;
import java.util.List;


/**
 * Decorates a list
 * @author philipp
 *
 */
public class SearchListControl extends AbstractControl  {
    
    /**
     * 
     */
    private static final String RENDER_TYPE = "searchList";

    /**
     * The list showed
     */
    private ListControl list;

    /**
     * The advanced serach
     */
    private SearchForm searchForm;

    /**
     *  
     */
    public SearchListControl() {
        this.setRenderType(RENDER_TYPE);
        this.setName("searchList");
    }

    /**
     * @see info.magnolia.cms.gui.controlx.list.ListControl#addColumn(info.magnolia.cms.gui.controlx.list.ListColumn)
     */
    public void addColumn(ListColumn column) {
        this.getList().addChild(column);
    }

    /**
     * @see info.magnolia.cms.gui.controlx.list.ListControl#addGroupableField(java.lang.String)
     */
    public void addGroupableField(String name) {
        this.getList().addGroupableField(name);
    }

    /**
     * @see info.magnolia.cms.gui.controlx.list.ListControl#addSortableField(java.lang.String)
     */
    public void addSortableField(String name) {
        this.getList().addSortableField(name);
    }

    /**
     * @see info.magnolia.cms.gui.controlx.list.ListControl#getColumns()
     */
    public Collection getColumns() {
        return this.getList().getChildren();
    }

    /**
     * @see info.magnolia.cms.gui.controlx.list.ListControl#getContextMenu()
     */
    public ContextMenu getContextMenu() {
        return this.getList().getContextMenu();
    }

    /**
     * @see info.magnolia.cms.gui.controlx.list.ListControl#getGroupableFields()
     */
    public List getGroupableFields() {
        return this.getList().getGroupableFields();
    }

    /**
     * @see info.magnolia.cms.gui.controlx.list.ListControl#getMaxRowsPerGroup()
     */
    public int getMaxRowsPerGroup() {
        return this.getList().getMaxRowsPerGroup();
    }

    /**
     * @see info.magnolia.cms.gui.controlx.list.ListControl#getModel()
     */
    public ListModel getModel() {
        return this.getList().getModel();
    }

    /**
     * @see info.magnolia.cms.gui.controlx.list.ListControl#getSortableFields()
     */
    public List getSortableFields() {
        return this.getList().getSortableFields();
    }

    /**
     * @see info.magnolia.cms.gui.controlx.list.ListControl#setContextMenu(info.magnolia.cms.gui.control.ContextMenu)
     */
    public void setContextMenu(ContextMenu contextMenu) {
        this.getList().setContextMenu(contextMenu);
    }

    /**
     * @see info.magnolia.cms.gui.controlx.list.ListControl#setMaxRowsPerGroup(int)
     */
    public void setMaxRowsPerGroup(int maxRowsPerGroup) {
        this.getList().setMaxRowsPerGroup(maxRowsPerGroup);
    }

    /**
     * @see info.magnolia.cms.gui.controlx.list.ListControl#setModel(info.magnolia.cms.gui.controlx.list.ListModel)
     */
    public void setModel(ListModel model) {
        this.getList().setModel(model);
    }

    /**
     * @see info.magnolia.cms.gui.controlx.search.SearchView#getConfig()
     */
    public SearchConfig getSearchConfig() {
        return this.getSearchForm().getConfig();
    }

    /**
     * @see info.magnolia.cms.gui.controlx.search.SearchView#getGroupDirection()
     */
    public String getGroupDirection() {
        return this.getList().getGroupDirection();
    }

    /**
     * @see info.magnolia.cms.gui.controlx.search.SearchView#getGroupField()
     */
    public String getGroupField() {
        return this.getList().getGroupField();
    }

    /**
     * @see info.magnolia.cms.gui.controlx.search.SearchView#getSearchStr()
     */
    public String getSearchStr() {
        return this.getSearchStr();
    }

    /**
     * @see info.magnolia.cms.gui.controlx.search.SearchView#getSortDirection()
     */
    public String getSortDirection() {
        return this.getList().getSortDirection();
    }

    /**
     * @see info.magnolia.cms.gui.controlx.search.SearchView#getSortField()
     */
    public String getSortField() {
        return this.getList().getSortField();
    }

    /**
     * @see info.magnolia.cms.gui.controlx.search.SearchView#isSearchAdvanced()
     */
    public boolean isSearchAdvanced() {
        return this.getSearchForm().isSearchAdvanced();
    }

    /**
     * @see info.magnolia.cms.gui.controlx.search.SearchView#setConfig(info.magnolia.cms.gui.controlx.search.SearchConfig)
     */
    public void setSearchConfig(SearchConfig config) {
        this.getSearchForm().setConfig(config);
    }

    /**
     * @see info.magnolia.cms.gui.controlx.search.SearchView#setGroupDirection(java.lang.String)
     */
    public void setGroupDirection(String groupDirection) {
        this.getList().setGroupDirection(groupDirection);
    }

    /**
     * @see info.magnolia.cms.gui.controlx.search.SearchView#setGroupField(java.lang.String)
     */
    public void setGroupField(String groupField) {
        this.getList().setGroupField(groupField);
    }

    /**
     * @see info.magnolia.cms.gui.controlx.search.SearchView#setSearchAdvanced(boolean)
     */
    public void setSearchAdvanced(boolean searchAdvanced) {
        this.getSearchForm().setSearchAdvanced(searchAdvanced);
    }

    /**
     * @see info.magnolia.cms.gui.controlx.search.SearchView#setSearchStr(java.lang.String)
     */
    public void setSearchStr(String searchStr) {
        this.getSearchForm().setSearchStr(searchStr);
    }

    /**
     * @see info.magnolia.cms.gui.controlx.search.SearchView#setSortDirection(java.lang.String)
     */
    public void setSortDirection(String sortDirection) {
        this.getList().setSortDirection(sortDirection);
    }

    /**
     * @see info.magnolia.cms.gui.controlx.search.SearchView#setSortField(java.lang.String)
     */
    public void setSortField(String sortField) {
        this.getList().setSortField(sortField);
    }

    /**
     * @param list The list to set.
     */
    public void setList(ListControl list) {
        this.list = list;
        this.list.setName(this.getName() + "_list");
        this.addChild(list);
    }

    /**
     * @return Returns the list.
     */
    public ListControl getList() {
        if(list == null){
            setList(new ListControl());
        }
        return list;
    }

    /**
     * @param searchForm The searchForm to set.
     */
    public void setSearchForm(SearchForm searchForm) {
        this.searchForm = searchForm;
        this.searchForm.setName(this.getName() + "_searchForm");
        this.addChild(searchForm);
    }

    /**
     * @return Returns the searchForm.
     */
    public SearchForm getSearchForm() {
        if(searchForm == null){
            setSearchForm(new SearchForm());
        }
        return searchForm;
    }
    
    public void setQuery(SearchQuery query){
        ((SearchableListModel) this.getList().getModel()).setQuery(query);
    }
 
}
