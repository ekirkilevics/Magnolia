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
package info.magnolia.module.delta;

import org.apache.commons.lang.StringUtils;
import info.magnolia.cms.core.ie.DataTransporter;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class BootstrapConditionally extends NodeExistsDelegateTask {

    /**
     * Bootstraps the given resource if the corresponding node does not exist yet, does nothing otherwise.
     */
    public BootstrapConditionally(String taskName, String taskDescription, String resourceToBootstrap) {
        super(taskName, taskDescription, determineRepository(resourceToBootstrap), determinePath(resourceToBootstrap), null, new BootstrapSingleResource(taskName, taskDescription, resourceToBootstrap));
    }

    /**
     * Bootstraps the given resource if the corresponding node does not exist yet, executes the given task otherwise.
     */
    public BootstrapConditionally(String taskName, String taskDescription, String resourceToBootstrap, Task ifNodeExists) {
        super(taskName, taskDescription, determineRepository(resourceToBootstrap), determinePath(resourceToBootstrap), ifNodeExists, new BootstrapSingleResource(taskName, taskDescription, resourceToBootstrap));
    }

    /**
     * Executes the given task if the specified node exists in the specific repository, bootstraps the given resource otherwise.
     */
    public BootstrapConditionally(String taskName, String taskDescription, String repositoryName, String pathToCheck, String resourceToBootstrap, Task ifNodeExists) {
        super(taskName, taskDescription, repositoryName, pathToCheck, ifNodeExists, new BootstrapSingleResource(taskName, taskDescription, resourceToBootstrap));        
    }

    // TODO --- these are copied from BootstrapUtil which has been removed with r12086
    private static String determineRepository(String filename) {
        return StringUtils.substringBefore(cleanupFilename(filename), ".");
    }

    private static String determinePath(String filename) {
        String withoutExtensionAndRepository = StringUtils.substringAfter(cleanupFilename(filename), ".");
        String path = StringUtils.replace(withoutExtensionAndRepository, ".", "/");
        return (StringUtils.isEmpty(path) ? "/" : "/" + path);
    }

    private static String cleanupFilename(String filename) {
        filename = StringUtils.replace(filename, "\\", "/");
        filename = StringUtils.substringAfterLast(filename, "/");
        filename = StringUtils.substringBeforeLast(filename, ".");

        return StringUtils.removeEnd(filename, DataTransporter.XML);
    }
}
