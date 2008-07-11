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
package info.magnolia.module;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;
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
        return MgnlContext.getSystemContext().getHierarchyManager(workspace);
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
