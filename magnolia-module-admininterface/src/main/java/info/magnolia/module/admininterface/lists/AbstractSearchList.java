/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.admininterface.lists;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import info.magnolia.cms.gui.controlx.RenderKitFactory;
import info.magnolia.cms.gui.controlx.search.SearchListControl;
import info.magnolia.module.admininterface.TemplatedMVCHandler;


/**
 * The search and list interface for the website.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 *
 */
public abstract class AbstractSearchList extends TemplatedMVCHandler {

    /**
     * Control used.
     */
    private SearchListControl searchList;
    
    /**
     * @see info.magnolia.module.admininterface.PageMVCHandler#show()
     */
    public String show() {
        return super.show();
    }

    /**
     * Configure the list control.
     */
    protected abstract void init();
    
    /**
     * @param name
     * @param request
     * @param response
     */
    public AbstractSearchList(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    /* (non-Javadoc)
     * @see info.magnolia.cms.gui.controlx.search.SearchListControl#getGroupDirection()
     */
    public String getGroupDirection() {
        return getSearchList().getGroupDirection();
    }

    /* (non-Javadoc)
     * @see info.magnolia.cms.gui.controlx.search.SearchListControl#getGroupField()
     */
    public String getGroupField() {
        return getSearchList().getGroupField();
    }

    /* (non-Javadoc)
     * @see info.magnolia.cms.gui.controlx.search.SearchListControl#getSearchStr()
     */
    public String getSearchStr() {
        return getSearchList().getSearchStr();
    }

    /* (non-Javadoc)
     * @see info.magnolia.cms.gui.controlx.search.SearchListControl#getSortDirection()
     */
    public String getSortDirection() {
        return getSearchList().getSortDirection();
    }

    /* (non-Javadoc)
     * @see info.magnolia.cms.gui.controlx.search.SearchListControl#getSortField()
     */
    public String getSortField() {
        return getSearchList().getSortField();
    }

    /* (non-Javadoc)
     * @see info.magnolia.cms.gui.controlx.search.SearchListControl#isSearchAdvanced()
     */
    public boolean isSearchAdvanced() {
        return getSearchList().isSearchAdvanced();
    }

    /* (non-Javadoc)
     * @see info.magnolia.cms.gui.controlx.search.SearchListControl#setGroupDirection(java.lang.String)
     */
    public void setGroupDirection(String groupDirection) {
        getSearchList().setGroupDirection(groupDirection);
    }

    /* (non-Javadoc)
     * @see info.magnolia.cms.gui.controlx.search.SearchListControl#setGroupField(java.lang.String)
     */
    public void setGroupField(String groupField) {
        getSearchList().setGroupField(groupField);
    }

    /* (non-Javadoc)
     * @see info.magnolia.cms.gui.controlx.search.SearchListControl#setSearchAdvanced(boolean)
     */
    public void setSearchAdvanced(boolean searchAdvanced) {
        getSearchList().setSearchAdvanced(searchAdvanced);
    }

    /* (non-Javadoc)
     * @see info.magnolia.cms.gui.controlx.search.SearchListControl#setSearchStr(java.lang.String)
     */
    public void setSearchStr(String searchStr) {
        getSearchList().setSearchStr(searchStr);
    }

    /* (non-Javadoc)
     * @see info.magnolia.cms.gui.controlx.search.SearchListControl#setSortDirection(java.lang.String)
     */
    public void setSortDirection(String sortDirection) {
        getSearchList().setSortDirection(sortDirection);
    }

    /* (non-Javadoc)
     * @see info.magnolia.cms.gui.controlx.search.SearchListControl#setSortField(java.lang.String)
     */
    public void setSortField(String sortField) {
        getSearchList().setSortField(sortField);
    }

    /**
     * @param searchList The searchList to set.
     */
    public void setSearchList(SearchListControl searchList) {
        this.searchList = searchList;
    }

    /**
     * @return Returns the searchList.
     */
    public SearchListControl getSearchList() {
        if(searchList == null){
            searchList = new SearchListControl();
            searchList.setName("searchList");
            searchList.setRenderKit(RenderKitFactory.getRenderKit(RenderKitFactory.ADMIN_INTERFACE_RENDER_KIT));
        }
        return searchList;
    }

}
