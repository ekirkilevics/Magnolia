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
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.User;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
public class WorkItemContext implements Context {
    
    private static Logger log = LoggerFactory.getLogger(WorkItemContext.class);

    /**
     * The inner context
     */
    private Context ctx;
    
    /**
     * The wrapped workitem
     */
    private WorkItem workItem;

    /**
     * @param ctx
     * @param workItem
     */
    public WorkItemContext(Context ctx, WorkItem workItem) {
        this.ctx = ctx;
        this.workItem = workItem;
    }
    
    /**
     * Check first on the workitem then on the wrapped context
     */
    public Object get(Object key) {
        Attribute attr = this.workItem.getAttribute(key.toString());
        if(attr != null){
            Object obj = AttributeUtils.owfe2java(attr);
            if(obj != null){
                return obj;
            }
        }
        return this.ctx.get(key);
    }
    
    /**
     * Store it in the workitem
     */
    public Object put(Object key, Object value) {
        Attribute attr = AttributeUtils.java2owfe(value);
        try {
            this.workItem.addAttribute(key.toString(), attr);
        }
        catch (AttributeException e) {
            log.error("can't set value {}", key, e);
        }
        return value;
    }
    
    /**
     * @see java.util.Map#clear()
     */
    public void clear() {
        this.ctx.clear();
    }

    /**
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object arg0) {
        return this.ctx.containsKey(arg0);
    }

    /**
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object arg0) {
        return this.ctx.containsValue(arg0);
    }

    /**
     * @see java.util.Map#entrySet()
     */
    public Set entrySet() {
        return this.ctx.entrySet();
    }

    /**
     * @see java.util.Map#equals(java.lang.Object)
     */
    public boolean equals(Object arg0) {
        return this.ctx.equals(arg0);
    }

    /**
     * @see info.magnolia.cms.beans.runtime.Context#getAccessManager(java.lang.String, java.lang.String)
     */
    public AccessManager getAccessManager(String repositoryId, String workspaceId) {
        return this.ctx.getAccessManager(repositoryId, workspaceId);
    }

    /**
     * @see info.magnolia.cms.beans.runtime.Context#getAccessManager(java.lang.String)
     */
    public AccessManager getAccessManager(String repositoryId) {
        return this.ctx.getAccessManager(repositoryId);
    }

    /**
     * @see info.magnolia.cms.beans.runtime.Context#getAttribute(java.lang.String, int)
     */
    public Object getAttribute(String name, int scope) {
        return this.ctx.getAttribute(name, scope);
    }

    /**
     * @see info.magnolia.cms.beans.runtime.Context#getHierarchyManager(java.lang.String, java.lang.String)
     */
    public HierarchyManager getHierarchyManager(String repositoryId, String workspaceId) {
        return this.ctx.getHierarchyManager(repositoryId, workspaceId);
    }

    /**
     * @see info.magnolia.cms.beans.runtime.Context#getHierarchyManager(java.lang.String)
     */
    public HierarchyManager getHierarchyManager(String repositoryId) {
        return this.ctx.getHierarchyManager(repositoryId);
    }

    /**
     * @see info.magnolia.cms.beans.runtime.Context#getLocale()
     */
    public Locale getLocale() {
        return this.ctx.getLocale();
    }

    /**
     * @see info.magnolia.cms.beans.runtime.Context#getMessages()
     */
    public Messages getMessages() {
        return this.ctx.getMessages();
    }

    /**
     * @see info.magnolia.cms.beans.runtime.Context#getMessages(java.lang.String)
     */
    public Messages getMessages(String basename) {
        return this.ctx.getMessages(basename);
    }

    /**
     * @see info.magnolia.cms.beans.runtime.Context#getQueryManager(java.lang.String, java.lang.String)
     */
    public QueryManager getQueryManager(String repositoryId, String workspaceId) {
        return this.ctx.getQueryManager(repositoryId, workspaceId);
    }

    /**
     * @see info.magnolia.cms.beans.runtime.Context#getQueryManager(java.lang.String)
     */
    public QueryManager getQueryManager(String repositoryId) {
        return this.ctx.getQueryManager(repositoryId);
    }

    /**
     * @see info.magnolia.cms.beans.runtime.Context#getUser()
     */
    public User getUser() {
        return this.ctx.getUser();
    }

    /**
     * @see java.util.Map#hashCode()
     */
    public int hashCode() {
        return this.ctx.hashCode();
    }

    /**
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        return this.ctx.isEmpty();
    }

    /**
     * @see java.util.Map#keySet()
     */
    public Set keySet() {
        return this.ctx.keySet();
    }

    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map arg0) {
        this.ctx.putAll(arg0);
    }

    /**
     * @see java.util.Map#remove(java.lang.Object)
     */
    public Object remove(Object arg0) {
        return this.ctx.remove(arg0);
    }

    /**
     * @see info.magnolia.cms.beans.runtime.Context#setAttribute(java.lang.String, java.lang.Object, int)
     */
    public void setAttribute(String name, Object value, int scope) {
        this.ctx.setAttribute(name, value, scope);
    }

    /**
     * @see info.magnolia.cms.beans.runtime.Context#setLocale(java.util.Locale)
     */
    public void setLocale(Locale locale) {
        this.ctx.setLocale(locale);
    }

    /**
     * @see info.magnolia.cms.beans.runtime.Context#setUser(info.magnolia.cms.security.User)
     */
    public void setUser(User user) {
        this.ctx.setUser(user);
    }

    /**
     * @see java.util.Map#size()
     */
    public int size() {
        return this.ctx.size();
    }

    /**
     * @see java.util.Map#values()
     */
    public Collection values() {
        return this.ctx.values();
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
