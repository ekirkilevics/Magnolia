/**
 * This file Copyright (c) 2003-2009 Magnolia International
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

import info.magnolia.cms.gui.controlx.list.DefaultValueProvider;
import info.magnolia.context.Context;
import info.magnolia.module.workflow.WorkflowUtil;
import openwfe.org.engine.workitem.Attribute;
import openwfe.org.engine.workitem.InFlowItem;
import openwfe.org.engine.workitem.StringAttribute;

import org.apache.commons.lang.StringUtils;


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
