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
import org.apache.commons.collections.map.MultiValueMap;

import javax.jcr.RepositoryException;
import java.util.Map;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class InstallContextImpl implements InstallContext {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InstallContextImpl.class);

    private ModuleDefinition currentModule;
    private boolean installDone;
    private boolean restartNeeded;
    private final Map messages = new MultiValueMap();

    public void setCurrentModule(ModuleDefinition module) {
        this.currentModule = module;
    }

    public void debug(String message) {
        log.debug("> " + message);
        messages.put(getModuleKey(), new Message(MessagePriority.debug, message));
    }

    public void info(String message) {
        log.info("> " + message);
        messages.put(getModuleKey(), new Message(MessagePriority.info, message));
    }

    public void warn(String message) {
        log.warn("> " + message);
        messages.put(getModuleKey(), new Message(MessagePriority.warning, message));
    }

    public void error(String message, Throwable th) {
        log.error("> " + message, th);
        messages.put(getModuleKey(), new Message(MessagePriority.error, message, th));
    }

    public void installDone() {
        installDone = true;
    }

    public boolean isInstallDone() {
        return installDone;
    }

    public void restartNeeded(String message) {
        this.restartNeeded = true;
        log.warn("> restartNeeded > " + message);
        messages.put(getModuleKey(), new Message(MessagePriority.restartNeeded, message));
    }

    public boolean isRestartNeeded() {
        return restartNeeded;
    }

    public Map getMessages() {
        return messages;
    }

    public ModuleDefinition getCurrentModuleDefinition() {
        return currentModule;
    }

    public boolean isModuleRegistered(String moduleName)  {
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

    /**
     * The key used in the map of messages. This is mostly because Maps keys in freemarker can only be Strings...
     * We just need to make sure this is consistent accross templates...
     */
    protected String getModuleKey() {
        return currentModule.toString();
    }

}
