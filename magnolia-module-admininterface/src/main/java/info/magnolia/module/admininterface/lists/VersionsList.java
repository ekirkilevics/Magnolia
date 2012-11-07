/**
 * This file Copyright (c) 2003-2012 Magnolia International
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

import java.util.Collection;

import info.magnolia.cms.beans.config.VersionConfig;
import info.magnolia.cms.core.Content;
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
import info.magnolia.freemarker.FreemarkerUtil;
import info.magnolia.module.admininterface.VersionUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.cms.core.HierarchyManager;

import javax.jcr.InvalidItemStateException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.version.Version;
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

    private String jsExecutedAfterSaving;

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
    @Override
    public ListModel getModel() {
        try {
            Content node = getNode();
            return new VersionListModel(node, this.getMaxShowedVersions());
        }
        catch (Exception e) {
            log.error("can't find node for version list {}", this.path);
        }
        return null;
    }

    /**
     * Check the version rollover. The latest can not get restored therfore we don't show it.
     */
    protected int getMaxShowedVersions() {
        return Math.min((int)VersionConfig.getInstance().getMaxVersionAllowed(), 50);
    }

    @Override
    public void configureList(ListControl list) {
        // set onselect
        list.setRenderer(new AdminListControlRenderer() {
            @Override
            public String onDblClick(ListControl list, Integer index) {
                return list.getName() + ".showItem()";
            }

            @Override
            public String getJavaScriptClass() {
                return "mgnl.admininterface.VersionsList";
            }

            @Override
            protected String buildJavaScriptObject(ListControl list, Object value) {
                return super.buildJavaScriptObject(list, value) + ", versionLabel: '" + list.getIteratorValue("versionLabel")  + "'";
            }

            @Override
            public String getConstructorArguments(ListControl list) {
                return super.getConstructorArguments(list) + ", '"+repository+"', '"+path+"', " + getOnShowFunction() + ", " + getOnDiffFunction() ;
            }

        });

        list.addGroupableField("userName");
        list.addSortableField("created");
        list.addColumn(new ListColumn("name", "Name", "150", true));
        list.addColumn(new ListColumn("created", "Date", "100", true));
        list.addColumn(new ListColumn("userName", "User", "100", true));
        list.addColumn(new ListColumn("comment", "Comment", "150", true));
    }

    /**
     * The script executed on a show link
     */
    public abstract String getOnShowFunction();

    @Override
    protected void configureContextMenu(ContextMenu menu) {
        ContextMenuItem show = new ContextMenuItem("show");
        show.setLabel(MessagesManager.get("versions.show"));
        show.setOnclick(this.getList().getName() + ".showItem()");
        show.setIcon(MgnlContext.getContextPath() + "/.resources/icons/16/note_view.gif");
        show.addJavascriptCondition("function(){return " + getList().getName()+".isSelected()}");

        ContextMenuItem restore = new ContextMenuItem("restore");
        restore.setLabel(MessagesManager.get("versions.restore"));
        restore.setOnclick(this.getList().getName() + ".restore()");
        restore.setIcon(MgnlContext.getContextPath() + "/.resources/icons/16/undo.gif");
        restore.addJavascriptCondition("function(){return " + getList().getName()+".isSelected()}");

        menu.addMenuItem(show);
        menu.addMenuItem(restore);

        if(isSupportsDiff()){
            ContextMenuItem diffWithCurrent = new ContextMenuItem("diffWithCurrent");
            diffWithCurrent.setLabel(MessagesManager.get("versions.compareWithCurrent"));
            diffWithCurrent.setOnclick(this.getList().getName() + ".diffItemWithCurrent()");
            diffWithCurrent.setIcon(MgnlContext.getContextPath() + "/.resources/icons/16/compare_with_current.gif");
            diffWithCurrent.addJavascriptCondition("function(){return " + getList().getName()+".isSelected()}");

            ContextMenuItem diffWithPrevious = new ContextMenuItem("diffWithPrevious");
            diffWithPrevious.setLabel(MessagesManager.get("versions.compareWithPrevious"));
            diffWithPrevious.setOnclick(this.getList().getName() + ".diffItemWithPrevious()");
            diffWithPrevious.setIcon(MgnlContext.getContextPath() + "/.resources/icons/16/compare_with_previous.gif");
            diffWithPrevious.addJavascriptCondition("function(){return " + getList().getName()+".hasPreviousVersion()}");

            menu.addMenuItem(diffWithCurrent);
            menu.addMenuItem(diffWithPrevious);
        }
    }

    protected boolean isSupportsDiff() {
        return false;
    }

    /**
     * @see info.magnolia.module.admininterface.lists.AbstractList#configureFunctionBar(info.magnolia.cms.gui.control.FunctionBar)
     */
    @Override
    protected void configureFunctionBar(FunctionBar bar) {
        bar.addMenuItem(new FunctionBarItem(this.getContextMenu().getMenuItemByName("show")));
        bar.addMenuItem(new FunctionBarItem(this.getContextMenu().getMenuItemByName("restore")));

        if(isSupportsDiff()){
            bar.addMenuItem(new FunctionBarItem(this.getContextMenu().getMenuItemByName("diffWithCurrent")));
            bar.addMenuItem(new FunctionBarItem(this.getContextMenu().getMenuItemByName("diffWithPrevious")));
        }
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
            final Content node = this.getNode();
            final String version = this.getVersionLabel();
            node.addVersion();
            try {
                node.restore(version, true);
            } catch (InvalidItemStateException e) {
                node.refresh(false);
                node.restore(version, true);
            }
            AlertUtil.setMessage(MessagesManager.get("versions.restore.latest.success"));
        }
        catch (Exception e) {
            log.error("can't restore", e);
            AlertUtil.setMessage(MessagesManager.get("versions.restore.exception", new String[]{e.getMessage()}));
        }
        return show();
    }

    public String restoreRecursive() {
        try {
            restoreRecursive(this.getNode(), this.getVersionLabel());
            AlertUtil.setMessage(MessagesManager.get("versions.restore.latest.success"));
        } catch (Exception e) {
            log.error("can't restore", e);
            AlertUtil.setMessage(MessagesManager.get("versions.restore.exception", new String[]{e.getMessage()}));
        }
        return show();
    }

    private String restoreRecursive(Content node, String version) throws UnsupportedRepositoryOperationException, RepositoryException {
        node.addVersion();
        // get last non deleted version
        try {
            node.restore(version, true);
        } catch (InvalidItemStateException e) {
            node.refresh(false);
            node.restore(version, true);
        }
        //node.save();
        // all children of the same type
        for (Content child : node.getChildren()) {
            // figure out undelete version for the child
            Collection<Version> versions = VersionUtil.getSortedNotDeletedVersions(child);
            if (versions.size() > 0) {
                String childVersion = versions.iterator().next().getName();
                restoreRecursive(child, childVersion);
            }
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

    @Override
    public String onRender() {
        return FreemarkerUtil.process(VersionsList.class, this);
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

    public String getOnDiffFunction() {
        return "function(versionLabel){}";
    }

    public String getJsExecutedAfterSaving() {
        return this.jsExecutedAfterSaving;
    }

    public void setJsExecutedAfterSaving(String jsExecutedAfterSaving) {
        this.jsExecutedAfterSaving = jsExecutedAfterSaving;
    }
}
