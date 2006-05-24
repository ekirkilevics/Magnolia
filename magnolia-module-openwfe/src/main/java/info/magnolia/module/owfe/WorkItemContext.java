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
package info.magnolia.module.owfe;

import info.magnolia.cms.beans.runtime.Context;
import info.magnolia.cms.beans.runtime.ContextDecorator;
import info.magnolia.cms.beans.runtime.MgnlContext;
import java.util.Map;

import openwfe.org.engine.workitem.Attribute;
import openwfe.org.engine.workitem.AttributeException;
import openwfe.org.engine.workitem.AttributeUtils;
import openwfe.org.engine.workitem.WorkItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This context wrapps a workitem and delegates the most of the methods to the inner context.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 *
 */
public class WorkItemContext extends ContextDecorator {
    
    private static Logger log = LoggerFactory.getLogger(WorkItemContext.class);

    /**
     * The wrapped workitem
     */
    private WorkItem workItem;
    
    /**
     * 
     */
    public WorkItemContext(Context ctx, WorkItem workItem) {
        super(ctx);
        this.workItem = workItem;
    }
    
    /**
     * Use work item if request scope
     */
    public Object getAttribute(String name, int scope) {
        if(scope == MgnlContext.REQUEST_SCOPE){
            Attribute attr = this.workItem.getAttribute(name);
            if(attr != null){
                Object obj = AttributeUtils.owfe2java(attr);
                if(obj != null){
                    return obj;
                }
            }
        }
        return super.getAttribute(name, scope);
    }
    
    /**
     * Use work item if request scope
     */
    public Map getAttributes(int scope) {
        if(scope == MgnlContext.REQUEST_SCOPE){
            return AttributeUtils.map2java(this.workItem.getAttributes());
        }
        return super.getAttributes(scope);
    }
    
    /**
     * Use work item if request scope
     */
    public void setAttribute(String name, Object value, int scope) {
        if(scope == MgnlContext.REQUEST_SCOPE){
            Attribute attr = AttributeUtils.java2owfe(value);
            try {
                this.workItem.addAttribute(name, attr);
            }
            catch (AttributeException e) {
                log.error("can't set value {}", name, e);
            }
        }
        super.setAttribute(name, value, scope);
    }
    
    /**
     * Use work item if request scope
     */
    public void removeAttribute(String name, int scope) {
        if(scope == MgnlContext.REQUEST_SCOPE){
            this.workItem.removeAttribute(name);
        }
        this.removeAttribute(name, scope);
    }
    
    /**
     * @return Returns the workItem.
     */
    public WorkItem getWorkItem() {
        return this.workItem;
    }
    
    /**
     * @param workItem The workItem to set.
     */
    public void setWorkItem(WorkItem workItem) {
        this.workItem = workItem;
    }

}
