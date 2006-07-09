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

import info.magnolia.cms.gui.control.ContextMenu;
import info.magnolia.cms.gui.control.FunctionBar;
import info.magnolia.cms.gui.controlx.RenderKit;
import info.magnolia.cms.gui.controlx.RenderKitFactory;
import info.magnolia.cms.gui.controlx.list.ListControl;
import info.magnolia.cms.gui.controlx.list.ListModel;
import info.magnolia.cms.util.FreeMarkerUtil;
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
        return FreeMarkerUtil.createTemplateName(AbstractList.class, "html");
    }

    /**
     * @see com.obinary.magnolia.professional.PageMVCHandler#show()
     */
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

}
