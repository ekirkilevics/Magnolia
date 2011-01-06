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
package info.magnolia.module.workflow.inbox;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
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
import info.magnolia.module.workflow.WorkflowUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import openwfe.org.engine.workitem.InFlowWorkItem;
import openwfe.org.engine.workitem.ListAttribute;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The {@link info.magnolia.module.admininterface.lists.AbstractList} responsible for displaying sub pages included in an activation workitem.
 *
 * @author philipp
 * @version $Id$
 */
public class SubPagesList extends AbstractList {
    private static final Logger log = LoggerFactory.getLogger(SubPagesList.class);

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
        ListColumn iconColumn = new ListColumn("icon", "", "30px", true);
        iconColumn.setRenderer(new IconListColumnRenderer());
        list.addColumn(iconColumn);

        list.addColumn(new ListColumn("label", "", "200", true));
        list.addColumn(new ListColumn("version", this.getMsgs().get("subpages.version"), "70px", true));
        list.addColumn(new ListColumn("modDate", this.getMsgs().get("subpages.date"), "150px", true));

        ListColumn statusColumn = new ListColumn("activationStatusIcon", this.getMsgs().get("subpages.status"), "50px", true);
        statusColumn.setRenderer(new IconListColumnRenderer());
        list.addColumn(statusColumn);

        list.setRenderer(new AdminListControlRenderer() {

            {
                setBorder(true);
            }

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

    /**
     * Simple model bean for displayed subpages included in an activation workitem.
     */
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
