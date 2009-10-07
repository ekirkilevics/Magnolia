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
package info.magnolia.module;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.module.model.ModuleDefinition;
import org.apache.commons.lang.exception.ExceptionUtils;

import javax.jcr.RepositoryException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public interface InstallContext {

    void info(String message);

    void warn(String message);

    void error(String message, Throwable th);

    void restartNeeded(String message);

    int getExecutedTaskCount();

    int getTotalTaskCount();

    InstallStatus getStatus();

    /**
     * A Map of messages where the key is the module name.
     */
    Map<String, List<Message>> getMessages();

    HierarchyManager getHierarchyManager(String workspace);

    HierarchyManager getConfigHierarchyManager();

    boolean hasModulesNode();

    /**
     * @return the root node for all modules in the config workspace.
     */
    Content getModulesNode() throws RepositoryException;

    Content getOrCreateCurrentModuleNode() throws RepositoryException;

    Content getOrCreateCurrentModuleConfigNode() throws RepositoryException;

    /**
     * Allows generic tasks to know what's being installed/updated.
     */
    ModuleDefinition getCurrentModuleDefinition();

    boolean isModuleRegistered(String moduleName);

    public static final class Message {
        private final Date timestamp;
        private final String message;
        private final String details;
        private final MessagePriority priority;

        public Message(MessagePriority priority, String message) {
            this(priority, message, (String) null);
        }

        public Message(MessagePriority priority, String message, Throwable th) {
            this(priority, message, th != null ? ExceptionUtils.getStackTrace(th) : null);
        }

        public Message(MessagePriority priority, String message, String details) {
            this.timestamp = new Date();
            this.priority = priority;
            this.message = message;
            this.details = details;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public String getMessage() {
            return message;
        }

        public String getDetails() {
            return details;
        }

        public MessagePriority getPriority() {
            return priority;
        }
    }

    public enum MessagePriority {
        info, warning, error, restartNeeded
    }
}
