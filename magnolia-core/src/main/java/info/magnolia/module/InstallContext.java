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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.module.model.ModuleDefinition;
import org.apache.commons.lang.exception.ExceptionUtils;

import javax.jcr.RepositoryException;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public interface InstallContext { // implements Context ?

    void debug(String message);

    void info(String message);

    void warn(String message);

    void error(String message, Throwable th);

    void restartNeeded(String message);

    boolean isRestartNeeded();

    /**
     * &lt;String (module), List&lt;Message&gt;&gt;
     */
    Map getMessages();

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

    public class MessagePriority {
        public static final MessagePriority debug = new MessagePriority("debug");
        public static final MessagePriority info = new MessagePriority("info");
        public static final MessagePriority warning = new MessagePriority("warning");
        public static final MessagePriority error = new MessagePriority("error");
        public static final MessagePriority restartNeeded = new MessagePriority("restartNeeded");

        private final String name;

        private MessagePriority(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }
    }
}
