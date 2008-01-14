/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
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
import info.magnolia.cms.gui.controlx.search.SimpleSearchUtil;
import info.magnolia.cms.gui.query.SearchQuery;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.context.MgnlContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
        super.configureFunctionBar(bar);
        bar.setOnSearchFunction("mgnl.admininterface.WebsiteSearchList.search");

        ContextMenu menu = this.getContextMenu();

        bar.addMenuItem(new FunctionBarItem(menu.getMenuItemByName("open")));
        bar.addMenuItem(new FunctionBarItem(menu.getMenuItemByName("navigate")));

    }

    /**
     * Here we create a all over query
     */
    public SearchQuery getQuery() {
        return SimpleSearchUtil.getSimpleSearchQuery(this.getSearchStr());

    }

    /**
     * Not used in this context
     */
    public SearchConfig getSearchConfig() {
        return null;
    }

}
