/**
 * This file Copyright (c) 2007-2010 Magnolia International
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

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.importexport.DataTransporter;
import info.magnolia.module.InstallContext;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This task is used to bootstrap a part of a file.
 * @author tmiyar
 *
 */
public class PartialBootstrapTask extends AbstractTask {

    private static Logger log = LoggerFactory.getLogger(PartialBootstrapTask.class);

    private final String resource;
    private final String itemName;
    private final String itemPath;
    private final int importUUIDBehavior;

    private String targetResource;
    static private final int defaultImportUUIDBehavior = ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW;

    /**
     * Bootstraps fragment of file.
     * @param resource - resource file i.e. /mgnl-bootstrap/standard-templating-kit/dialogs/pages/config.modules.standard-templating-kit.dialogs.pages.article.stkArticleProperties.xml
     * @param itemPath - path in the file of the node you want to bootstrap i.e. /stkArticleProperties/tabCategorization
     */
    public PartialBootstrapTask(String name, String description, String resource, String itemPath) {
        this(name, description, resource, itemPath, defaultImportUUIDBehavior);
    }

    /**
     * Bootstraps newly created file.
     * @param resource - resource file i.e. /mgnl-bootstrap/standard-templating-kit/dialogs/pages/config.modules.standard-templating-kit.dialogs.pages.article.stkArticleProperties.xml
     * @param itemPath - path in the file of the node you want to bootstrap i.e. /stkArticleProperties/tabCategorization
     * @param targetResource - A target bootstrap file name in case you want to bootstrap the file in a different node /mgnl-bootstrap/standard-templating-kit/dialogs/pages/config.modules.standard-templating-kit.dialogs.pages.target.xml
     */
    public PartialBootstrapTask(String name, String description, String resource, String itemPath, String targetResource) {
        this(name, description, resource, itemPath, targetResource, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
    }

    public PartialBootstrapTask(String name, String description, String resource, String itemPath, String targetResource, int importUUIDBehavior) {
        this(name, description, resource, itemPath, importUUIDBehavior);
        this.targetResource = targetResource;
    }

    public PartialBootstrapTask(String name, String description, String resource, String itemPath, int importUUIDBehavior) {
        super(name, description);

        this.importUUIDBehavior = importUUIDBehavior;
        this.resource = resource;
        this.itemPath = StringUtils.chomp(itemPath, "/");
        this.itemName = StringUtils.substringAfterLast(itemPath , "/");
    }

    public void execute(InstallContext ctx) throws TaskExecutionException {

        try {

            // get name as config.modules.xxx
            String inputResourceName = StringUtils.removeEnd(StringUtils.substringAfterLast(resource, "/"), ".xml");
            //replacing all "/" with "."; getting string after first node
            String tmpitemPath = itemPath.replace("/", ".");

            tmpitemPath = StringUtils.removeStart(tmpitemPath, ".");
            tmpitemPath = StringUtils.substringAfter(tmpitemPath, ".");
            String outputResourceName = inputResourceName + "." + tmpitemPath;
            if(StringUtils.isNotEmpty(targetResource)) {
                outputResourceName = targetResource;
            }

            //bootstrap
            bootstrap(outputResourceName, itemName, importUUIDBehavior, getNodeStream(resource, itemPath));

        } catch (IOException e) {
            throw new TaskExecutionException("Cant find resource file");
        } catch (RepositoryException e) {
            throw new TaskExecutionException("Cant bootstrap resource file");
        }

    }

    protected InputStream getNodeStream(String fileName, String nodePath) {

        return BootstrapFileUtil.getElementAsStream(fileName, nodePath);

    }

    protected void bootstrap(String resourceName, String itemName, int importUUIDBehavior, InputStream stream) throws IOException, RepositoryException {
        //TODO: Code partially copied from BootstrapUtil class, need to refactor the core class to accept Inputstreams
        String name = StringUtils.removeEnd(resourceName, ".xml");

        String repository = StringUtils.substringBefore(name, ".");
        String pathName = StringUtils.substringAfter(StringUtils.substringBeforeLast(name, "."), "."); //$NON-NLS-1$
        String nodeName = StringUtils.substringAfterLast(name, ".");
        String fullPath;
        if (StringUtils.isEmpty(pathName)) {
            pathName = "/";
            fullPath = "/" + nodeName;
        }
        else {
            pathName = "/" + StringUtils.replace(pathName, ".", "/");
            fullPath = pathName + "/" + nodeName;
        }

        log.debug("Will bootstrap {}", resourceName);
        if (stream == null) {
            throw new IOException("Can't find resource to bootstrap at " + resourceName);
        }

        // if the path already exists --> delete it
        try {
            final HierarchyManager hm = MgnlContext.getHierarchyManager(repository);

            // hm can be null if module is not properly registered and the repository has not been created
            if (hm != null && hm.isExist(fullPath)) {
                hm.delete(fullPath);
                log.warn("Deleted already existing node for bootstrapping: {}", fullPath);
            }
        } catch (RepositoryException e) {
            throw new RepositoryException("Can't check existence of node for bootstrap file: [" + name + "]", e);
        }

        DataTransporter.importXmlStream(stream, repository, pathName, name, false, importUUIDBehavior, false, true);
    }

}
