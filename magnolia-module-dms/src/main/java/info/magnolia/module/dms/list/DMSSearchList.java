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
package info.magnolia.module.dms.list;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.control.ContextMenu;
import info.magnolia.cms.gui.control.ContextMenuItem;
import info.magnolia.cms.gui.control.FunctionBar;
import info.magnolia.cms.gui.control.FunctionBarItem;
import info.magnolia.cms.gui.controlx.list.ListColumn;
import info.magnolia.cms.gui.controlx.list.ListControl;
import info.magnolia.cms.gui.controlx.list.ListModel;
import info.magnolia.cms.gui.controlx.search.SearchConfig;
import info.magnolia.cms.gui.query.SearchQuery;
import info.magnolia.cms.gui.query.SearchQueryExpression;
import info.magnolia.cms.gui.query.StringSearchQueryParameter;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.lists.AbstractSimpleSearchList;
import info.magnolia.module.admininterface.lists.AdminListControlRenderer;
import info.magnolia.module.dms.DMSModule;
import info.magnolia.module.dms.beans.Document;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;


/**
 * DMS list view (used to render the search result)
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class DMSSearchList extends AbstractSimpleSearchList {

    protected Messages msgs = MessagesUtil.chainWithDefault("info.magnolia.module.dms.messages");

    /**
     * @param name
     * @param request
     * @param response
     */
    public DMSSearchList(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    /**
     * Set the js searchView varibale to true --> used for reloading after editing a document.
     */
    public String onRender() {
        String str = super.onRender();
        str += "<script type=\"text/javascript\">mgnl.dms.DMS.searchView = true;</script>";
        return str;
    }

    /**
     * @see info.magnolia.module.admininterface.lists.AbstractList#getModel()
     */
    public ListModel getModel() {
        String repository = DMSModule.getInstance().getRepository();
        return new DMSSearchListModel(repository);
    }

    /**
     * @see com.obinary.magnolia.professional.lists.AbstractAdvancedSearchList#configureList(info.magnolia.cms.gui.controlx.list.ListControl)
     */
    public void configureList(ListControl list) {

        // all this should be read from the dialog
        list.addColumn(new ListColumn() {

            {
                setName("name");
                setColumnName("name");
                setLabel(msgs.get("dms.list.url"));
                setWidth("200");
            }

            public String getIcon() {
                Document doc = new Document((Content) this.getListControl().getIteratorValueObject());
                return doc.getMimeTypeIcon();
            }

            public String render() {
                return "<span style=\"vertical-align: middle\"><img src=\""
                    + MgnlContext.getContextPath()
                    + this.getIcon()
                    + "\"/></span>"
                    + this.getValue();
            }

        });

        list.addColumn(new ListColumn("type", msgs.get("dms.list.type"), "200", true));
        list.addColumn(new ListColumn("title", msgs.get("dms.list.title"), "200", true));
        list.addColumn(new ListColumn("modificationDate", msgs.get("dms.list.date"), "200", true));

        list.addSortableField("name");
        list.addSortableField("modificationDate");

        list.addGroupableField("type");

        list.setRenderer(new AdminListControlRenderer() {

            public String onSelect(ListControl list, Integer index) {
                String str = "mgnl.dms.DMS.selectedPath = '"
                    + ((Content) list.getIteratorValueObject()).getHandle()
                    + "';";
                str += "mgnl.dms.DMS.selectedIsFolder = false;";
                str += super.onSelect(list, index);
                return str;
            }
        });
    }

    /**
     * Simple search only
     */
    public SearchQuery getQuery() {
        SearchQuery query = new SearchQuery();
        if (StringUtils.isNotEmpty(this.getSearchStr())) {
            SearchQueryExpression exp = new StringSearchQueryParameter(
                "*",
                this.getSearchStr(),
                StringSearchQueryParameter.CONTAINS);
            query.setRootExpression(exp);
        }
        return query;
    }

    /**
     * Used for the advanced search only
     */
    public SearchConfig getSearchConfig() {
        return null;
    }

    /**
     * The very basic context menu used in the list.
     */
    public void configureContextMenu(ContextMenu menu) {

        ContextMenuItem menuEditDocument = new ContextMenuItem("edit");
        menuEditDocument.setLabel(msgs.get("dms.menu.edit"));
        menuEditDocument.setIcon(MgnlContext.getContextPath() + "/.resources/icons/16/document_edit.gif");
        menuEditDocument.setOnclick("mgnl.dms.DMS.show(mgnl.dms.DMS.selectedPath);");
        menuEditDocument.addJavascriptCondition("mgnl.dms.DMS.selectedIsNotFolderCondition");
        menuEditDocument.addJavascriptCondition("{test: function(){return mgnl.dms.DMS.selectedPath != ''}}");

        ContextMenuItem showInTree = new ContextMenuItem("navigate");
        showInTree.setLabel(msgs.get("dms.menu.navigate"));
        showInTree.setIcon(MgnlContext.getContextPath() + "/.resources/icons/16/compass.gif");
        showInTree.setOnclick("mgnl.dms.DMS.showInTree(mgnl.dms.DMS.selectedPath);");
        showInTree.addJavascriptCondition("{test: function(){return mgnl.dms.DMS.selectedPath != ''}}");

        ContextMenuItem menuVersions = new ContextMenuItem("versions");
        menuVersions.setLabel(msgs.get("dms.menu.versions"));
        menuVersions.setIcon(MgnlContext.getContextPath() + "/.resources/icons/16/elements1.gif");
        menuVersions.setOnclick("mgnl.dms.DMS.showVersions(mgnl.dms.DMS.selectedPath);");
        menuVersions.addJavascriptCondition("mgnl.dms.DMS.selectedIsNotFolderCondition");
        menuVersions.addJavascriptCondition("{test: function(){return mgnl.dms.DMS.selectedPath != ''}}");

        menu.addMenuItem(menuEditDocument);
        menu.addMenuItem(showInTree);
        menu.addMenuItem(null); // line
        menu.addMenuItem(menuVersions);
    }

    /**
     * @see info.magnolia.module.admininterface.lists.AbstractList#configureFunctionBar(info.magnolia.cms.gui.control.FunctionBar)
     */
    protected void configureFunctionBar(FunctionBar bar) {
        ContextMenu menu = this.getContextMenu();
        bar.setSearchable(true);
        bar.setSearchStr(this.getSearchStr());
        bar.setOnSearchFunction("mgnl.dms.DMS.simpleSearch");

        bar.addMenuItem(new FunctionBarItem(menu.getMenuItemByName("edit")));
        bar.addMenuItem(new FunctionBarItem(menu.getMenuItemByName("navigate")));
        bar.addMenuItem(null);
        bar.addMenuItem(new FunctionBarItem(menu.getMenuItemByName("versions")));

    }

}