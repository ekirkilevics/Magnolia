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
package info.magnolia.module.workflow.inbox;

import info.magnolia.cms.gui.control.ContextMenu;
import info.magnolia.cms.gui.control.ContextMenuItem;
import info.magnolia.cms.gui.control.FunctionBar;
import info.magnolia.cms.gui.control.FunctionBarItem;
import info.magnolia.cms.gui.controlx.list.ListColumn;
import info.magnolia.cms.gui.controlx.list.ListControl;
import info.magnolia.cms.gui.controlx.list.ListModel;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.cms.util.DateUtil;
import info.magnolia.freemarker.FreemarkerUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.lists.AbstractList;
import info.magnolia.module.admininterface.lists.AdminListControlRenderer;
import info.magnolia.module.workflow.WorkflowConstants;
import info.magnolia.module.workflow.WorkflowUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;


/**
 * @author Philipp Bracher
 * @version $Revision:3416 $ ($Author:philipp $)
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

    protected Messages msgs = MessagesManager.getMessages("info.magnolia.module.workflow.messages");

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

                String customEditDialog = ObjectUtils.toString(list
                    .getIteratorValue(WorkflowConstants.ATTRIBUTE_EDIT_DIALOG));

                String editDialog = StringUtils.defaultIfEmpty(customEditDialog, WorkflowConstants.DEFAULT_EDIT_DIALOG);
                String repository = ObjectUtils.toString(list.getIteratorValue("repository"));
                String path = ObjectUtils.toString(list.getIteratorValue("path"));

                StringBuffer js = new StringBuffer();
                js.append("mgnl.workflow.Inbox.current = {");
                js.append("id : '").append(list.getIteratorValue("id")).append("',");
                js.append("path : '").append(path).append("',");
                js.append("version : '").append(list.getIteratorValue("version")).append("',");
                js.append("repository : '").append(repository).append("',");
                js.append("workItemPath : '").append(list.getIteratorValue("workItemPath")).append("',");
                js.append("editDialog : '").append(editDialog).append("'");
                js.append("};");
                js.append("mgnl.workflow.Inbox.show = ").append(getShowJSFunction(repository, path)).append(";");
                js.append(super.onSelect(list, index));
                return js.toString();
            }

            public String onDblClick(ListControl list, Integer index) {
                return "mgnl.workflow.Inbox.edit();";
            }
        });

        list.addSortableField("lastModified");
        list.addGroupableField("repository");
        list.addGroupableField("workflow");

        list.addColumn(new ListColumn() {

            {
                setName("icon");
                setLabel("");
                setWidth("30px");
                setSeparator(false);
            }

            public Object getValue() {
                String path = "" + this.getListControl().getIteratorValue("path");
                String repository = "" + this.getListControl().getIteratorValue("repository");
                return "<img src=\""
                    + MgnlContext.getContextPath()
                    + "/"
                    + InboxHelper.getIcon(repository, path)
                    + "\" alt=\"\" border=\"0\" />";
            }
        });
        list.addColumn(new ListColumn("name", msgs.get("inbox.item"), "100px", true));
        list.addColumn(new ListColumn("repository", msgs.get("inbox.repository"), "100", true));
        list.addColumn(new ListColumn("workflow", msgs.get("inbox.workflow"), "100", true));
        list.addColumn(new ListColumn("comment", msgs.get("inbox.comment"), "200", true));
        list.addColumn(new ListColumn() {

            {
                setName("lastModified");
                setLabel(msgs.get("inbox.date"));
                setWidth("100");
            }

            public Object getValue() {
                String str = (String) super.getValue();
                Date date = null;
                try {
                    date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ").parse(str);
                    return DateUtil.formatDateTime(date);

                }
                catch (ParseException e) {
                    return StringUtils.EMPTY;
                }
            }
        });

    }

    protected String getShowJSFunction(String repository, String path) {
        return "mgnl.workflow.Inbox.showFunctions." + repository;
    }

    /**
     * @see info.magnolia.module.admininterface.lists.AbstractList#getContextMenu()
     */
    public void configureContextMenu(ContextMenu menu) {
        ContextMenuItem edit = new ContextMenuItem("edit");
        edit.setLabel(msgs.get("inbox.edit"));
        edit.setOnclick("mgnl.workflow.Inbox.edit();");
        edit.setIcon(MgnlContext.getContextPath() + "/.resources/icons/16/mail_write.gif");
        edit.addJavascriptCondition("{test: function(){return mgnl.workflow.Inbox.current.id!=null}}");

        ContextMenuItem show = new ContextMenuItem("show");
        show.setLabel(msgs.get("inbox.show"));
        show.setOnclick("mgnl.workflow.Inbox.show();");
        show.setIcon(MgnlContext.getContextPath() + "/.resources/icons/16/note_view.gif");
        show.addJavascriptCondition("{test: function(){return mgnl.workflow.Inbox.current.id!=null}}");

        ContextMenuItem proceed = new ContextMenuItem("proceed");
        proceed.setLabel(msgs.get("inbox.proceed"));
        proceed.setOnclick("mgnl.workflow.Inbox.proceed();");
        proceed.setIcon(MgnlContext.getContextPath() + "/.resources/icons/16/navigate_right2_green.gif");
        proceed.addJavascriptCondition("{test: function(){return mgnl.workflow.Inbox.current.id!=null}}");

        ContextMenuItem reject = new ContextMenuItem("reject");
        reject.setLabel(msgs.get("inbox.reject"));
        reject.setOnclick("mgnl.workflow.Inbox.reject();");
        reject.setIcon(MgnlContext.getContextPath() + "/.resources/icons/16/navigate_left2_red.gif");
        reject.addJavascriptCondition("{test: function(){return mgnl.workflow.Inbox.current.id!=null}}");

        ContextMenuItem cancel = new ContextMenuItem("cancel");
        cancel.setLabel(msgs.get("inbox.cancel"));
        cancel.setOnclick("mgnl.workflow.Inbox.cancel();");
        cancel.setIcon(MgnlContext.getContextPath() + "/.resources/icons/16/delete2.gif");
        cancel.addJavascriptCondition("{test: function(){return mgnl.workflow.Inbox.current.id!=null}}");

        menu.addMenuItem(edit);
        menu.addMenuItem(null);
        menu.addMenuItem(show);
        menu.addMenuItem(null);
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
        bar.addMenuItem(new FunctionBarItem(menu.getMenuItemByName("edit")));
        bar.addMenuItem(null);
        bar.addMenuItem(new FunctionBarItem(menu.getMenuItemByName("show")));
        bar.addMenuItem(null);
        bar.addMenuItem(new FunctionBarItem(menu.getMenuItemByName("reject")));
        bar.addMenuItem(new FunctionBarItem(menu.getMenuItemByName("proceed")));
        bar.addMenuItem(null);
        bar.addMenuItem(new FunctionBarItem(menu.getMenuItemByName("cancel")));
    }

    /**
     * Add some inbox specific stuff: mainly hidden fields.
     */
    public String onRender() {
        return FreemarkerUtil.process(this);
    }

    /**
     * Proceed the item
     */
    public String proceed() {
        try {
            WorkflowUtil.proceed(this.getFlowItemId(), WorkflowConstants.ACTION_PROCEED);
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
            WorkflowUtil.proceed(this.getFlowItemId(), WorkflowConstants.ACTION_REJECT, this.getComment());
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
            WorkflowUtil.proceed(this.getFlowItemId(), WorkflowConstants.ACTION_CANCEL);
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
