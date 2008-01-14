/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.workflow;

import info.magnolia.context.Context;
import info.magnolia.context.ContextDecorator;

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
        if (scope == Context.LOCAL_SCOPE) {
            Attribute attr = this.workItem.getAttribute(name);
            if (attr != null) {
                Object obj = AttributeUtils.owfe2java(attr);
                if (obj != null) {
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
        if (scope == Context.LOCAL_SCOPE) {
            return AttributeUtils.map2java(this.workItem.getAttributes());
        }
        return super.getAttributes(scope);
    }

    /**
     * Use work item if request scope
     */
    public void setAttribute(String name, Object value, int scope) {
        if (scope == Context.LOCAL_SCOPE) {
            Attribute attr = AttributeUtils.java2owfe(value);
            try {
                if (this.workItem.containsAttribute(name)) {
                    log.debug("Attribute {} already set in WorkItem, will overwrite it with value {}", name, attr.toString());
                    this.workItem.setAttribute(name, attr);
                } else {
                    this.workItem.addAttribute(name, attr);
                }
            }
            catch (AttributeException e) {
                log.error("can't set value {}", name, e);
            }
        } else {
            super.setAttribute(name, value, scope);
        }
    }

    /**
     * Use work item if request scope
     */
    public void removeAttribute(String name, int scope) {
        if (scope == Context.LOCAL_SCOPE) {
            this.workItem.removeAttribute(name);
        } else {
            super.removeAttribute(name, scope);
        }
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
