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
import info.magnolia.cms.gui.controlx.list.ListColumn;
import info.magnolia.cms.gui.controlx.list.ListControl;
import info.magnolia.cms.gui.controlx.list.ListModel;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.cms.util.FreeMarkerUtil;
import info.magnolia.module.admininterface.lists.AbstractList;
import info.magnolia.module.owfe.OWFEBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 *
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
     * @see com.obinary.magnolia.professional.lists.AbstractAdvancedSearchList#configureList(info.magnolia.cms.gui.controlx.list.ListControl)
     */
    public void configureList(ListControl list) {
        
        list.addSortableField("lastModified");
        list.addColumn(new ListColumn("pathSelected","Page","150", true));
        list.addColumn(new ListColumn("lastModified","Date","150", true));
        list.addColumn(new ListColumn("comment","Comment","150", true));
        // uncomment for debugging

        /*
        list.addColumn(new ListColumn(){
            {
                setName("attributes");
                setLabel("Attributes");
            }
            public Object getValue() {
                String str ="";
                InFlowWorkItem item = (InFlowWorkItem)this.getListControl().getIteratorValueObject();
                StringMapAttribute attributes = item.getAttributes();
                
                for (Iterator iter = attributes.alphaStringIterator(); iter.hasNext();) {
                    String key = (String) iter.next();
                    str += key + "=" + attributes.sget(key) + "<br/>";
                }
                return str;
            }
        });
        */
        
        ListColumn functionsColumn = new ListColumn(){
            {
                setName("id");
                setColumnName("id");
                setLabel(" ");
            }
            
            public String render() {
                String id = (String) this.getValue();
                
                return "<a href=\"javascript:mgnl.owfe.Inbox.proceed('" + id + "');\">proceed</a> " +
                "<a href=\"javascript:mgnl.owfe.Inbox.reject('" + id + "');\">reject</a> " +
                "<a href=\"javascript:mgnl.owfe.Inbox.cancel('" + id + "');\">cancel</a> ";
            }
        };
        
        list.addColumn(functionsColumn);
    }
    
    /**
     * @see com.obinary.magnolia.professional.lists.AbstractAdvancedSearchList#onRender()
     */
    public String onRender() {
        return FreeMarkerUtil.process(this);
    }
    
    public String proceed(){
        try{
            new OWFEBean().approveActivation(this.getFlowItemId());
        }
        catch(Exception e){
            AlertUtil.setMessage("can't proceed:", e);
        }
        return this.show();
    }

    public String reject(){
        try{
            new OWFEBean().rejectActivation(this.getFlowItemId(), this.getComment());
        }
        catch(Exception e){
            AlertUtil.setMessage("can't reject:", e);
        }
        return this.show();
    }

    public String cancel(){
        try{
            new OWFEBean().cancel(this.getFlowItemId());
        }
        catch(Exception e){
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
