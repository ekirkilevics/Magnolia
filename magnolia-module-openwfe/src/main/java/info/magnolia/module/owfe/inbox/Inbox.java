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
package info.magnolia.module.owfe.inbox;

import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.gui.control.ContextMenu;
import info.magnolia.cms.gui.control.ContextMenuItem;
import info.magnolia.cms.gui.control.FunctionBar;
import info.magnolia.cms.gui.control.FunctionBarItem;
import info.magnolia.cms.gui.controlx.list.ListColumn;
import info.magnolia.cms.gui.controlx.list.ListControl;
import info.magnolia.cms.gui.controlx.list.ListModel;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.cms.util.FreeMarkerUtil;
import info.magnolia.module.admininterface.lists.AbstractList;
import info.magnolia.module.admininterface.lists.AdminListControlRenderer;
import info.magnolia.module.owfe.OWFEBean;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import openwfe.org.engine.workitem.InFlowWorkItem;
import openwfe.org.engine.workitem.StringMapAttribute;


/**
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class Inbox extends AbstractList {

    /**
     * The id of the workitem on which we called the command
     */
    private String flowItemId;

    /**
     * The comment the user entered by proceeding or rejecting
     */
    private String comment;

    /**
     * Show all the values of the workitem if true
     */
    private boolean debug = false;

    /**
     * @param name
     * @param request
     * @param response
     */
    public Inbox(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    /**
     * @see info.magnolia.module.admininterface.lists.AbstractList#getModel()
     */
    public ListModel getModel() {
        return new InboxListModel(MgnlContext.getUser().getName());
    }

    /**
     * Sets the select js code and defines the columns
     */
    public void configureList(ListControl list) {

        // define the select action
        list.setRenderer(new AdminListControlRenderer() {

            public String onSelect(ListControl list, Integer index) {
                String js = super.onSelect(list, index);
                js += "mgnl.owfe.Inbox.currentId = '" + list.getIteratorValue("id") + "';";
                return js;
            }
        });

        list.addSortableField("lastModified");
        list.addColumn(new ListColumn("pathSelected", "Page", "150", true));
        list.addColumn(new ListColumn("lastModified", "Date", "150", true));
        list.addColumn(new ListColumn("comment", "Comment", "150", true));

        if (this.isDebug()) {

            list.addColumn(new ListColumn() {

                {
                    setName("attributes");
                    setLabel("Attributes");
                }

                public Object getValue() {
                    String str = "";
                    InFlowWorkItem item = (InFlowWorkItem) this.getListControl().getIteratorValueObject();
                    StringMapAttribute attributes = item.getAttributes();
                    for (Iterator iter = attributes.alphaStringIterator(); iter.hasNext();) {
                        String key = (String) iter.next();
                        str += key + "=" + attributes.sget(key) + "<br/>";
                    }
                    return str;
                }
            });
        }
    }

    /**
     * @see info.magnolia.module.admininterface.lists.AbstractList#getContextMenu()
     */
    public void configureContextMenu(ContextMenu menu) {
        ContextMenuItem proceed = new ContextMenuItem("proceed");
        proceed.setLabel(MessagesManager.get("inbox.proceed"));
        proceed.setOnclick("mgnl.owfe.Inbox.proceed();");
        proceed.setIcon(MgnlContext.getContextPath() + "/.resources/icons/16/navigate_right2_green.gif");

        ContextMenuItem reject = new ContextMenuItem("reject");
        reject.setLabel(MessagesManager.get("inbox.reject"));
        reject.setOnclick("mgnl.owfe.Inbox.reject();");
        reject.setIcon(MgnlContext.getContextPath() + "/.resources/icons/16/navigate_left2_red.gif");

        ContextMenuItem cancel = new ContextMenuItem("cancel");
        cancel.setLabel(MessagesManager.get("buttons.cancel"));
        cancel.setOnclick("mgnl.owfe.Inbox.cancel();");
        cancel.setIcon(MgnlContext.getContextPath() + "/.resources/icons/16/delete2.gif");

        menu.addMenuItem(proceed);
        menu.addMenuItem(reject);
        menu.addMenuItem(null);
        menu.addMenuItem(cancel);
    }

    /**
     * Same as the context menu
     */
    public void configureFunctionBar(FunctionBar bar) {
        ContextMenu menu = this.getContextMenu();
        bar.addMenuItem(new FunctionBarItem(menu.getMenuItemByName("reject")));
        bar.addMenuItem(new FunctionBarItem(menu.getMenuItemByName("proceed")));
        bar.addMenuItem(null);
        bar.addMenuItem(new FunctionBarItem(menu.getMenuItemByName("cancel")));
    }

    /**
     * Add some inbox specific stuff: mainly hidden fields.
     */
    public String onRender() {
        return FreeMarkerUtil.process(this);
    }

    /**
     * Proceed the item
     */
    public String proceed() {
        try {
            new OWFEBean().approveActivation(this.getFlowItemId());
        }
        catch (Exception e) {
            AlertUtil.setMessage("can't proceed:", e);
        }
        return this.show();
    }

    /**
     * Reject the item (adds a comment)
     */
    public String reject() {
        try {
            new OWFEBean().rejectActivation(this.getFlowItemId(), this.getComment());
        }
        catch (Exception e) {
            AlertUtil.setMessage("can't reject:", e);
        }
        return this.show();
    }

    /**
     * Stop the workflow.
     */
    public String cancel() {
        try {
            new OWFEBean().cancel(this.getFlowItemId());
        }
        catch (Exception e) {
            AlertUtil.setMessage("can't cancel:", e);
        }
        return this.show();
    }

    /**
     * @return Returns the flowItemId.
     */
    public String getFlowItemId() {
        return this.flowItemId;
    }

    /**
     * @param flowItemId The flowItemId to set.
     */
    public void setFlowItemId(String flowItemId) {
        this.flowItemId = flowItemId;
    }

    /**
     * @return Returns the comment.
     */
    public String getComment() {
        return this.comment;
    }

    /**
     * @param comment The comment to set.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * @return Returns the debug.
     */
    public boolean isDebug() {
        return this.debug;
    }

    /**
     * @param debug The debug to set.
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

}
