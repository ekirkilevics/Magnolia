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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.control.ContextMenu;
import info.magnolia.cms.gui.control.ContextMenuItem;
import info.magnolia.cms.gui.controlx.list.ListColumn;
import info.magnolia.cms.gui.controlx.list.ListControl;
import info.magnolia.cms.gui.controlx.list.ListModel;
import info.magnolia.cms.gui.controlx.search.DialogBasedSearchConfig;
import info.magnolia.cms.gui.controlx.search.SearchConfig;
import info.magnolia.module.admininterface.lists.AbstractSimpleSearchList;
import info.magnolia.module.admininterface.lists.AdminListControlRenderer;
import info.magnolia.module.dms.DMSModule;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * DMS list view (used to render the search result)
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class DMSSearchList extends AbstractSimpleSearchList {
    
    private static Logger log = LoggerFactory.getLogger(DMSSearchList.class);

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
        str += "<script>mgnl.dms.DMS.searchView = true;</script>";
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
        list.addColumn(new IconListColumn());
        list.addColumn(new ListColumn("name", "Name", "200", true));
        list.addColumn(new ListColumn("type", "Type", "200", true));
        list.addColumn(new ListColumn("title", "Title", "200", true));
        list.addColumn(new ListColumn("modificationDate", "Date", "200", true));

        list.addSortableField("name");
        list.addSortableField("modificationDate");

        list.addGroupableField("type");

        list.setRenderer(new AdminListControlRenderer() {

            public String onSelect(ListControl list, Integer index) {
                String str = super.onSelect(list, index);
                str += "mgnl.dms.DMS.selectedPath = '" + ((Content)list.getIteratorValueObject()).getHandle() + "';";
                str += "mgnl.dms.DMS.selectedIsFolder = false;";
                return str;
            }
        });
    }

    /**
     * @see com.obinary.magnolia.professional.lists.AbstractAdvancedSearchList#getSearchConfig()
     */
    public SearchConfig getSearchConfig() {
        String dialogPath = DMSModule.getInstance().getBaseDialog();
        try{
            Content dialogNode = MgnlContext.getHierarchyManager(ContentRepository.CONFIG).getContent(dialogPath);
            return new DialogBasedSearchConfig(dialogNode);
        }
        catch(Exception e){
            log.error("can't configure the dms search", e);
            return null;
        }
    }
    
    /**
     * The very basic context menu used in the list.
     */
    public ContextMenu getContextMenu() {
        ContextMenu cm = new ContextMenu("contextMenu");

        ContextMenuItem menuEditDocument = new ContextMenuItem();
        menuEditDocument.setLabel("Edit document");
        menuEditDocument.setIcon(MgnlContext.getContextPath() + "/.resources/icons/16/document_edit.gif");
        menuEditDocument.setOnclick("mgnl.dms.DMS.show(mgnl.dms.DMS.selectedPath);");
        menuEditDocument.addJavascriptCondition("mgnl.dms.DMS.selectedIsNotFolderCondition");

        ContextMenuItem showInTree = new ContextMenuItem();
        showInTree.setLabel("Show in tree");
        showInTree.setIcon(MgnlContext.getContextPath() + "/.resources/icons/16/document_edit.gif");
        showInTree.setOnclick("mgnl.dms.DMS.showInTree(mgnl.dms.DMS.selectedPath);");

        ContextMenuItem menuVersions = new ContextMenuItem();
        menuVersions.setLabel("Versions");
        menuVersions.setIcon(MgnlContext.getContextPath() + "/.resources/icons/16/exchange.gif");
        menuVersions.setOnclick("mgnl.dms.DMS.showVersions(mgnl.dms.DMS.selectedPath);");
        menuVersions.addJavascriptCondition("mgnl.dms.DMS.selectedIsNotFolderCondition");

        cm.addMenuItem(menuEditDocument);
        cm.addMenuItem(showInTree);
        cm.addMenuItem(null); // line
        cm.addMenuItem(menuVersions);
        return cm;
    }

}
