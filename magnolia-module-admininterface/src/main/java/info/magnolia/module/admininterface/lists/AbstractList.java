/**
 * This file Copyright (c) 2003-2010 Magnolia International
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

import info.magnolia.cms.gui.control.ContextMenu;
import info.magnolia.cms.gui.control.ContextMenuItem;
import info.magnolia.cms.gui.control.FunctionBar;
import info.magnolia.cms.gui.controlx.RenderKit;
import info.magnolia.cms.gui.controlx.RenderKitFactory;
import info.magnolia.cms.gui.controlx.list.ListControl;
import info.magnolia.cms.gui.controlx.list.ListModel;
import info.magnolia.context.MgnlContext;
import info.magnolia.freemarker.FreemarkerUtil;
import info.magnolia.module.admininterface.TemplatedMVCHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public abstract class AbstractList extends TemplatedMVCHandler {

    private String sortBy = "";

    private String sortByOrder = "asc";

    private String groupBy = "";

    private String groupByOrder = "asc";

    /**
     * Control used.
     */
    private ListControl list;

    /**
     * The function bar shown at the bottom
     */
    private FunctionBar functionBar;

    private ContextMenu contextMenu;

    /**
     * @param name
     * @param request
     * @param response
     */
    public AbstractList(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    /**
     * Don't use the class name.
     */
    protected String getTemplateName(String viewName) {
        return FreemarkerUtil.createTemplateName(AbstractList.class, "html");
    }

    public String show() {
        String view = super.show();
        ListControl list = this.getList();
        initList(list);
        configureList(list);
        return view;
    }

    /**
     * @param list
     */
    public abstract void configureList(ListControl list);

    /**
     * @param list
     */
    public void initList(ListControl list) {
        list.setName("list");
        list.setRenderKit(this.getRenderKit());
        list.setContextMenu(this.getContextMenu());
        list.setModel(this.getModel());
        list.setSortBy(this.getSortBy());
        list.setSortByOrder(this.getSortByOrder());
        list.setGroupBy(this.getGroupBy());
        list.setGroupByOrder(this.getGroupByOrder());
    }

    public String getLanguage(){
        return MgnlContext.getUser().getLanguage();
    }

    /**
     * Returns the model used by this list
     */
    public abstract ListModel getModel();

    /**
     * @param list The list to set.
     */
    public void setList(ListControl list) {
        this.list = list;
    }

    /**
     * @return Returns the list.
     */
    public ListControl getList() {
        if (list == null) {
            list = new ListControl();
        }
        return list;
    }

    public ContextMenu getContextMenu() {
        if (this.contextMenu == null) {
            this.contextMenu = new ContextMenu("contextMenu");
            configureContextMenu(this.contextMenu);
        }
        return this.contextMenu;
    }

    /**
     * Override to configure the menu
     */
    protected void configureContextMenu(ContextMenu menu) {
    }

    /**
     * Helper method to creat menu items for the list
     */
    protected void addContextMenuItem(ContextMenu menu, String name, String label, String iconName, String methodName, String isActiveMethodName) {
        final ContextMenuItem showInTree = new ContextMenuItem(name);
        showInTree.setLabel(getMsgs().get(label));
        showInTree.setIcon(MgnlContext.getContextPath() + "/.resources/icons/16/" + iconName+".gif");
        showInTree.setOnclick(this.getList().getName() + "." + methodName + "();");
        showInTree.addJavascriptCondition("function(){return " + this.getList().getName() + "." + isActiveMethodName + "()}");
        menu.addMenuItem(showInTree);
    }


    /**
     * Returns the default admin interface render kit.
     */
    protected RenderKit getRenderKit() {
        return RenderKitFactory.getRenderKit(RenderKitFactory.ADMIN_INTERFACE_RENDER_KIT);
    }

    /**
     * @return Returns the functionBar.
     */
    public FunctionBar getFunctionBar() {
        if (this.functionBar == null) {
            this.functionBar = new FunctionBar("functionBar");
            configureFunctionBar(this.functionBar);
        }
        return this.functionBar;
    }

    /**
     * Override to configure the bar
     */
    protected void configureFunctionBar(FunctionBar bar) {
    }

    /**
     * @param functionBar The functionBar to set.
     */
    public void setFunctionBar(FunctionBar functionBar) {
        this.functionBar = functionBar;
    }

    /**
     * @return Returns the groupBy.
     */
    public String getGroupBy() {
        return this.groupBy;
    }

    /**
     * @param groupBy The groupBy to set.
     */
    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    /**
     * @return Returns the groupByOrder.
     */
    public String getGroupByOrder() {
        return this.groupByOrder;
    }

    /**
     * @param groupByOrder The groupByOrder to set.
     */
    public void setGroupByOrder(String groupByOrder) {
        this.groupByOrder = groupByOrder;
    }

    /**
     * @return Returns the sortBy.
     */
    public String getSortBy() {
        return this.sortBy;
    }

    /**
     * @param sortBy The sortBy to set.
     */
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    /**
     * @return Returns the sortByOrder.
     */
    public String getSortByOrder() {
        return this.sortByOrder;
    }

    /**
     * @param sortByOrder The sortByOrder to set.
     */
    public void setSortByOrder(String sortByOrder) {
        this.sortByOrder = sortByOrder;
    }

    /**
     * Do some additional rendering in the subclass
     */
    public String onRender() {
        return "";
    }

    /**
     * Do some additional rendering in the subclass
     */
    public String onRenderHeader() {
        return "";
    }

    public String getURI() {
        return MgnlContext.getAggregationState().getCurrentURI();
    }

}
