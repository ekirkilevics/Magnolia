/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.owfe.inbox;

import info.magnolia.cms.gui.controlx.list.AbstractListModel;
import info.magnolia.cms.gui.controlx.list.ListModelIterator;
import info.magnolia.module.owfe.OWFEBean;

import java.util.Collection;
import java.util.List;


/**
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class InboxListModel extends AbstractListModel {

    private String userName;

    /**
     * @param userName
     */
    public InboxListModel(String userName) {
        super();
        this.userName = userName;
    }

    /**
     * @see info.magnolia.cms.gui.controlx.list.AbstractListModel#getResult()
     */
    protected Collection getResult() throws Exception {
        return (new OWFEBean()).getUserInbox(this.userName);
    }

    /**
     * @see info.magnolia.cms.gui.controlx.list.AbstractListModel#createIterator(java.util.Collection)
     */
    protected ListModelIterator createIterator(Collection items) {
        return new InboxListModelIterator((List) items, this.getGroupBy());
    }

    /**
     * @return Returns the userName.
     */
    public String getUserName() {
        return this.userName;
    }

    /**
     * @param userName The userName to set.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

}
