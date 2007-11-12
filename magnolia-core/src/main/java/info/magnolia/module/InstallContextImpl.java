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
package info.magnolia.module;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.module.model.ModuleDefinition;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class InstallContextImpl implements InstallContext {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InstallContextImpl.class);

    private static final String DEFAULT_KEY = "General messages";

    private ModuleDefinition currentModule;
    private InstallStatus status;
    private boolean restartNeeded;
    private int executedTaskCount;
    private int totalTaskCount;
    // ensure we'll keep messages in the order they were added 
    private final Map messages = new LinkedHashMap();

    public void setCurrentModule(ModuleDefinition module) {
        this.currentModule = module;
    }

    public void info(String message) {
        log.info("> " + message);
        log(new Message(MessagePriority.info, message));
    }

    public void warn(String message) {
        log.warn("> " + message);
        log(new Message(MessagePriority.warning, message));
    }

    public void error(String message, Throwable th) {
        log.error("> " + message, th);
        log(new Message(MessagePriority.error, message, th));
    }

    public void restartNeeded(String message) {
        this.restartNeeded = true;
        log.warn("> restartNeeded > " + message);
        log(new Message(MessagePriority.restartNeeded, message));
    }

    boolean isRestartNeeded() {
        return restartNeeded;
    }

    void incExecutedTaskCount() {
        executedTaskCount++;
    }

    public int getExecutedTaskCount() {
        return executedTaskCount;
    }

    public int getTotalTaskCount() {
        return totalTaskCount;
    }

    void setTotalTaskCount(int totalTaskCount) {
        this.totalTaskCount = totalTaskCount;
    }

    public InstallStatus getStatus() {
        return status;
    }

    void setStatus(InstallStatus status) {
        this.status = status;
    }

    public Map getMessages() {
        return messages;
    }

    public ModuleDefinition getCurrentModuleDefinition() {
        return currentModule;
    }

    public boolean isModuleRegistered(String moduleName) {
        return ModuleRegistry.Factory.getInstance().getDefinition(moduleName) != null;
    }

    public HierarchyManager getHierarchyManager(String workspace) {
        // TODO : use MgnlContext ??
        return ContentRepository.getHierarchyManager(workspace);
    }

    public HierarchyManager getConfigHierarchyManager() {
        return getHierarchyManager(ContentRepository.CONFIG);
    }

    public boolean hasModulesNode() {
        final HierarchyManager hm = getConfigHierarchyManager();
        return hm.isExist("/" + ModuleManagerImpl.MODULES_NODE);
    }

    public Content getModulesNode() throws RepositoryException {
        final HierarchyManager hm = getConfigHierarchyManager();
        return hm.getContent(ModuleManagerImpl.MODULES_NODE);
    }

    public Content getOrCreateCurrentModuleNode() throws RepositoryException {
        final Content allModulesNode = getModulesNode();
        return ContentUtil.getOrCreateContent(allModulesNode, currentModule.getName(), ItemType.CONTENT);
    }

    public Content getOrCreateCurrentModuleConfigNode() throws RepositoryException {
        final Content moduleNode = getOrCreateCurrentModuleNode();
        return ContentUtil.getOrCreateContent(moduleNode, "config", ItemType.CONTENT);
    }

    protected void log(final Message message) {
        final String k = getModuleKey();
        List messagesForKey = (List) messages.get(k);
        if (messagesForKey == null) {
            messagesForKey = new ArrayList();
            messages.put(k, messagesForKey);
        }
        messagesForKey.add(message);
    }

    /**
     * The key used in the map of messages. This is mostly because Maps keys in freemarker can only be Strings...
     * We just need to make sure this is consistent accross templates...
     */
    protected String getModuleKey() {
        return currentModule != null ? currentModule.toString() : DEFAULT_KEY;
    }
}
