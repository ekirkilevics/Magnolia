/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.module.workflow.inbox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.control.ContextMenu;
import info.magnolia.cms.gui.control.ContextMenuItem;
import info.magnolia.cms.gui.control.Tree;
import info.magnolia.cms.gui.controlx.list.AbstractListModel;
import info.magnolia.cms.gui.controlx.list.IconListColumnRenderer;
import info.magnolia.cms.gui.controlx.list.ListColumn;
import info.magnolia.cms.gui.controlx.list.ListControl;
import info.magnolia.cms.gui.controlx.list.ListModel;
import info.magnolia.cms.util.DateUtil;
import info.magnolia.cms.util.MetaDataUtil;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.lists.AbstractList;
import info.magnolia.module.admininterface.lists.AdminListControlRenderer;
import info.magnolia.module.workflow.WorkflowConstants;
import info.magnolia.module.workflow.WorkflowUtil;

import openwfe.org.engine.workitem.InFlowWorkItem;
import openwfe.org.engine.workitem.ListAttribute;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 */
public class SubPagesList extends AbstractList {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(SubPagesList.class);

    private String workItemId;

    private InFlowWorkItem item;

    private String rootPath;

    private String repository;

    private HierarchyManager hm;

    private List subPages = new ArrayList();

    public SubPagesList(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    public void init() {
        super.init();
        item = WorkflowUtil.getWorkItem(getWorkItemId());

        if (item == null) {
            throw new RuntimeException("can't read workitem");
        }

        rootPath = item.getAttribute("path").toString();
        repository = item.getAttribute("repository").toString();
        hm = MgnlContext.getHierarchyManager(repository);

        ListAttribute versions = (ListAttribute) item.getAttribute(Context.ATTRIBUTE_VERSION_MAP);
        for (Iterator iter = versions.iterator(); iter.hasNext();) {
            Map versionMap = (Map) iter.next();
            String version = versionMap.get("version").toString();
            String uuid = versionMap.get("uuid").toString();
            try {
                Content node = hm.getContentByUUID(uuid);
                if (!node.getHandle().equals(rootPath)) {
                    this.subPages.add(new SubPage(node, version));
                }
            }
            catch (RepositoryException e) {
                log.error("can't read subpage for [" + rootPath + "]", e);
            }
        }
    }

    public void configureList(ListControl list) {
        ListColumn iconColumn = new ListColumn("icon", "", "23px", true);
        iconColumn.setRenderer(new IconListColumnRenderer());
        list.addColumn(iconColumn);

        list.addColumn(new ListColumn("label", "", "200", true));
        list.addColumn(new ListColumn("version", this.getMsgs().get("subpages.version"), "100px", true));
        list.addColumn(new ListColumn("modDate", this.getMsgs().get("subpages.date"), "150px", true));

        ListColumn statusColumn = new ListColumn("activationStatusIcon", this.getMsgs().get("subpages.status"), "50px", true);
        statusColumn.setRenderer(new IconListColumnRenderer());
        list.addColumn(statusColumn);

        list.setRenderer(new AdminListControlRenderer() {

            public String onDblClick(ListControl list, Integer index) {
                return "mgnl.workflow.Inbox.show();";
            }

            public String onSelect(ListControl list, Integer index) {

                String path = ObjectUtils.toString(list.getIteratorValue("path"));

                StringBuffer js = new StringBuffer();
                js.append("mgnl.workflow.Inbox.current = ");
                js.append("{");
                js.append("id : '").append(list.getIteratorValue("id")).append("',");
                js.append("path : '").append(path).append("',");
                js.append("version : '").append(list.getIteratorValue("version")).append("',");
                js.append("repository : '").append(repository).append("'");
                js.append("};");
                js.append("mgnl.workflow.Inbox.show = ").append(InboxHelper.getShowJSFunction(repository, path)).append(";");
                js.append(super.onSelect(list, index));

                return js.toString();
            }
        });
    }

    protected void configureContextMenu(ContextMenu menu) {
        ContextMenuItem show = new ContextMenuItem("show");
        show.setLabel(this.getMsgs().get("subpages.show"));
        show.setOnclick("mgnl.workflow.Inbox.show();");
        show.setIcon(MgnlContext.getContextPath() + "/.resources/icons/16/note_view.gif");
        show.addJavascriptCondition("{test: function(){return mgnl.workflow.Inbox.current.id!=null}}");
        menu.addMenuItem(show);
    }

    public ListModel getModel() {
        return new AbstractListModel() {

            protected Collection getResult() throws Exception {
                return subPages;
            }
        };
    }

    public String getWorkItemId() {
        return this.workItemId;
    }

    public void setWorkItemId(String workItemId) {
        this.workItemId = workItemId;
    }

    public class SubPage {

        private String version;

        private Content node;

        private Content versionedNode;

        protected SubPage(Content node, String version) throws RepositoryException {
            this.version = version;
            this.node = node;
            versionedNode = node.getVersionedContent(version);
        }

        public String getPath(){
            return node.getHandle();
        }

        public String getIcon() {
            return "/" + InboxHelper.getIcon(repository, node.getHandle());
        }

        public String getLabel() {
            return StringUtils.removeStart(node.getHandle(), rootPath + "/");
        }

        public String getActivationStatusIcon() {
            return Tree.ICONDOCROOT + MetaDataUtil.getActivationStatusIcon(versionedNode);
        }

        public String getModDate() {
            return DateUtil.formatDateTime(versionedNode.getMetaData().getModificationDate());
        }

        public String getVersion() {
            return this.version;
        }

    }

}
