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

import java.util.List;

import openwfe.org.engine.workitem.InFlowItem;
import info.magnolia.cms.gui.controlx.list.ListModelIteratorImpl;


/**
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class InboxListModelIterator extends ListModelIteratorImpl {

    /**
     * @param list
     * @param groupKey
     */
    public InboxListModelIterator(List list, String groupKey) {
        super(list, groupKey);
    }

    /**
     * @see info.magnolia.cms.gui.controlx.list.ListModelIteratorImpl#getValue(java.lang.String, java.lang.Object)
     */
    protected Object getValue(String name, Object obj) {
        InFlowItem item = (InFlowItem) obj;
        if (item.containsAttribute(name)) {
            return item.getAttribute(name);
        }
        else if(name.equalsIgnoreCase("id")){
            return item.getId().toParseableString();
        }
        else {
            return super.getValue(name, obj);
        }
    }
}
