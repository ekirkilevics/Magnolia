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

import info.magnolia.cms.gui.controlx.list.DefaultValueProvider;
import info.magnolia.cms.gui.controlx.list.ListModelIteratorImpl;
import info.magnolia.context.Context;
import info.magnolia.module.workflow.WorkflowUtil;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import openwfe.org.engine.workitem.Attribute;
import openwfe.org.engine.workitem.InFlowItem;
import openwfe.org.engine.workitem.StringAttribute;


/**
 * @author Philipp Bracher
 * @version $Revision:3416 $ ($Author:philipp $)
 */
public class InboxValueProvider extends DefaultValueProvider {

    /**
     * @see info.magnolia.cms.gui.controlx.list.ListModelIteratorImpl#getValue(java.lang.String, java.lang.Object)
     */
    public Object getValue(String name, Object obj) {
        InFlowItem item = (InFlowItem) obj;
        if (name.equalsIgnoreCase("name")){
            String path = (String) getValue("path", obj);
            if(path != null){
                return StringUtils.substringAfterLast(path.toString(), "/");
            }
            return StringUtils.EMPTY;
        }
        else if (name.equalsIgnoreCase("id")) {
            return WorkflowUtil.getId(item);
        }
        else if (name.equalsIgnoreCase("comment")) {
            if(item.containsAttribute(Context.ATTRIBUTE_EXCEPTION)){
                return item.getAttribute(Context.ATTRIBUTE_MESSAGE);
            }
            else if (item.containsAttribute("comment")) {
                return item.getAttribute("comment");                
            }
            else{
                return StringUtils.EMPTY;
            }
        }        
        else if (name.equalsIgnoreCase("workflow")) {
            return item.getId().getWorkflowDefinitionName();
        }        
        else if (name.equalsIgnoreCase("workItemPath")) {
            return WorkflowUtil.getPath(WorkflowUtil.getId(item));
        }
        if (item.containsAttribute(name)) {
            Attribute attribute = item.getAttribute(name);
            if(attribute instanceof StringAttribute){
                return ((StringAttribute)attribute).toString();
            }
            return attribute;
        }
        else {
            return super.getValue(name, obj);
        }
    }
}
