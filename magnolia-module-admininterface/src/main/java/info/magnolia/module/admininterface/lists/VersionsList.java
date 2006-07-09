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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.gui.control.ContextMenu;
import info.magnolia.cms.gui.control.ContextMenuItem;
import info.magnolia.cms.gui.control.FunctionBar;
import info.magnolia.cms.gui.control.FunctionBarItem;
import info.magnolia.cms.gui.controlx.list.ListColumn;
import info.magnolia.cms.gui.controlx.list.ListControl;
import info.magnolia.cms.gui.controlx.list.ListModel;
import info.magnolia.cms.gui.controlx.version.VersionListModel;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.cms.util.FreeMarkerUtil;
import info.magnolia.context.MgnlContext;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public abstract class VersionsList extends AbstractList {

    private static Logger log = LoggerFactory.getLogger(VersionsList.class);

    /**
     * The repository
     */
    private String repository;

    /**
     * The path of the node
     */
    protected String path;

    /**
     * If the command is restore, this defines the label of the version to restore.
     */
    private String versionLabel;

    /**
     * @param name
     * @param request
     * @param response
     * @throws Exception
     */
    public VersionsList(String name, HttpServletRequest request, HttpServletResponse response) throws Exception {
        super(name, request, response);
    }

    /**
     * @see info.magnolia.module.admininterface.lists.AbstractList#getModel()
     */
    public ListModel getModel() {
        try {
            Content node = getNode();
            return new VersionListModel(node);
        }
        catch (Exception e) {
            log.error("can't find node for version list {}", this.path);
        }
        return null;
    }

    public void configureList(ListControl list) {
        // set onselect
        list.setRenderer(new AdminListControlRenderer() {

            public String onSelect(ListControl list, Integer index) {
                String js = super.onSelect(list, index);
                js += "mgnlVersionsList.currentVersionLabel = '" + list.getIteratorValue("versionLabel") + "';";
                return js;
            }

            public String onDblClick(ListControl list, Integer index) {
                return "mgnlVersionsList.showItem()";
            }
        });

        list.addGroupableField("userName");
        list.addSortableField("created");
        list.addColumn(new ListColumn("name", "Name", "150", true));
        list.addColumn(new ListColumn("created", "Date", "100", true));
        list.addColumn(new ListColumn("userName", "User", "100", true));
    }

    /**
     * The script executed on a show link
     */
    public abstract String getOnShowFunction();

    protected void configureContextMenu(ContextMenu menu) {
        ContextMenuItem show = new ContextMenuItem("show");
        show.setLabel(MessagesManager.get("versions.show"));
        show.setOnclick("mgnlVersionsList.showItem()");
        show.setIcon(MgnlContext.getContextPath() + "/.resources/icons/16/note_view.gif");

        ContextMenuItem restore = new ContextMenuItem("restore");
        restore.setLabel(MessagesManager.get("versions.restore"));
        restore.setOnclick("mgnlVersionsList.restore()");
        restore.setIcon(MgnlContext.getContextPath() + "/.resources/icons/16/undo.gif");

        menu.addMenuItem(show);
        menu.addMenuItem(restore);
    }

    /**
     * @see info.magnolia.module.admininterface.lists.AbstractList#configureFunctionBar(info.magnolia.cms.gui.control.FunctionBar)
     */
    protected void configureFunctionBar(FunctionBar bar) {
        bar.addMenuItem(new FunctionBarItem(this.getContextMenu().getMenuItemByName("show")));
        bar.addMenuItem(new FunctionBarItem(this.getContextMenu().getMenuItemByName("restore")));
    }

    /**
     * @return
     * @throws PathNotFoundException
     * @throws RepositoryException
     * @throws AccessDeniedException
     */
    protected Content getNode() throws PathNotFoundException, RepositoryException, AccessDeniedException {
        HierarchyManager hm = MgnlContext.getHierarchyManager(this.getRepository());
        Content node = hm.getContent(this.getPath());
        return node;
    }

    public String restore() {
        try {
            Content node = this.getNode();
            node.addVersion();
            node.restore(this.getVersionLabel(), true);
            AlertUtil.setMessage(MessagesManager.get("versions.restore.latest.success"));
        }
        catch (Exception e) {
            log.error("can't restore", e);
            AlertUtil.setMessage(MessagesManager.get("versions.restore.exception", new String[]{e.getMessage()}));
        }
        return show();
    }

    /**
     * @return Returns the path.
     */
    public String getPath() {
        return this.path;
    }

    /**
     * @param path The path to set.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return Returns the repository.
     */
    public String getRepository() {
        return this.repository;
    }

    /**
     * @param repository The repository to set.
     */
    public void setRepository(String repository) {
        this.repository = repository;
    }

    /**
     * @see com.obinary.magnolia.professional.lists.AbstractAdvancedSearchList#onRender()
     */
    public String onRender() {
        return FreeMarkerUtil.process(VersionsList.class, this);
    }

    /**
     * @return Returns the versionLabel.
     */
    public String getVersionLabel() {
        return this.versionLabel;
    }

    /**
     * @param versionLabel The versionLabel to set.
     */
    public void setVersionLabel(String versionLabel) {
        this.versionLabel = versionLabel;
    }

}
