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

import info.magnolia.cms.gui.controlx.list.ListControl;
import info.magnolia.cms.gui.controlx.search.SearchConfig;
import info.magnolia.cms.gui.controlx.search.SearchableListModel;
import info.magnolia.cms.gui.controlx.search.SimpleSearchUtil;
import info.magnolia.cms.gui.query.SearchQuery;
import info.magnolia.cms.util.FreeMarkerUtil;


/**
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 *
 */
public abstract class AbstractSimpleSearchList extends AbstractList {
    
    /**
     * @param name
     * @param request
     * @param response
     */
    public AbstractSimpleSearchList(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    private String searchStr = "";
    
    /**
     * @see info.magnolia.module.admininterface.lists.AbstractList#onRender()
     */
    public String onRender() {
        String str = super.onRender();
        str += FreeMarkerUtil.process(AbstractSimpleSearchList.class, this);
        return str;
    }

    /**
     * @return Returns the searchStr.
     */
    public String getSearchStr() {
        return this.searchStr;
    }

    /**
     * @param searchStr The searchStr to set.
     */
    public void setSearchStr(String searchStr) {
        this.searchStr = searchStr;
    }
    
    /**
     * @see info.magnolia.module.admininterface.lists.AbstractList#configureList(info.magnolia.cms.gui.controlx.list.ListControl)
     */
    public void initList(ListControl list) {
        super.initList(list);
        ((SearchableListModel)list.getModel()).setQuery(this.getQuery());
    }
    
    /**
     * @return
     */
    public SearchQuery getQuery() {
        return SimpleSearchUtil.getSimpleSearchQuery(this.getSearchStr(), this.getSearchConfig());
    }

    /**
     * @return
     */
    public abstract SearchConfig getSearchConfig();

}
