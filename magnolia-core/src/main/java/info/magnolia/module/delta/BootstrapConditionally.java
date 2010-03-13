/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.module.delta;

import info.magnolia.importexport.DataTransporter;
import org.apache.commons.lang.StringUtils;

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
