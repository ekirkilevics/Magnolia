/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.module.admininterface.lists;

import info.magnolia.cms.gui.control.FunctionBar;
import info.magnolia.cms.gui.controlx.list.ListControl;
import info.magnolia.cms.gui.controlx.search.SearchConfig;
import info.magnolia.cms.gui.controlx.search.SearchableListModel;
import info.magnolia.cms.gui.controlx.search.SimpleSearchUtil;
import info.magnolia.cms.gui.query.SearchQuery;
import info.magnolia.freemarker.FreemarkerUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
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
        str += FreemarkerUtil.process(AbstractSimpleSearchList.class, this);
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
        ((SearchableListModel) list.getModel()).setQuery(this.getQuery());
    }

    /**
     * @return
     */
    public SearchQuery getQuery() {
        if(this.getSearchConfig() == null){
            return SimpleSearchUtil.getSimpleSearchQuery(this.getSearchStr());
        }
        return SimpleSearchUtil.getSimpleSearchQuery(this.getSearchStr(), this.getSearchConfig());
    }

    protected void configureFunctionBar(FunctionBar bar) {
        super.configureFunctionBar(bar);
        bar.setSearchable(true);
        bar.setSearchStr(this.getSearchStr());
        bar.setOnSearchFunction(this.getList().getName() + ".search");
    }

    /**
     * @return
     */
    public abstract SearchConfig getSearchConfig();

}
