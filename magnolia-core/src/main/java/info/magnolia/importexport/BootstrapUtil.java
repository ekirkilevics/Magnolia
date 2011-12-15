/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.importexport;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.util.SiblingsHelper;
import info.magnolia.cms.util.StringLengthComparator;
import info.magnolia.context.MgnlContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utilities to bootstrap set of files and/or to export content into a specified directory.
 * @version $Revision$ ($Author$)
 */
public class BootstrapUtil {
    private static final Logger log = LoggerFactory.getLogger(BootstrapUtil.class);

    public static void bootstrap(String[] resourceNames, int importUUIDBehavior) throws IOException, RepositoryException {
        // sort by length --> import parent node first
        List<String> list = new ArrayList<String>(Arrays.asList(resourceNames));

        Collections.sort(list, new StringLengthComparator());

        for (Iterator<String> iter = list.iterator(); iter.hasNext();) {
            String resourceName = iter.next();

            String name = getFilenameFromResource(resourceName, ".xml");
            String repository = getWorkspaceNameFromResource(resourceName);
            String pathName = getPathnameFromResource(resourceName);
            String fullPath = getFullpathFromResource(resourceName);
            String nodeName = StringUtils.substringAfterLast(fullPath, "/");

            log.debug("Will bootstrap {}", resourceName);

            final InputStream stream = BootstrapUtil.class.getResourceAsStream(resourceName);
            if (stream == null) {
                throw new IOException("Can't find resource to bootstrap at " + resourceName);
            }

            // if the node already exists we will keep the order
            String nameOfNodeAfterTheImportedNode = null;

            final HierarchyManager hm = MgnlContext.getHierarchyManager(repository);

            // if the path already exists --> delete it
            try {

                // hm can be null if module is not properly registered and the repository has not been created
                if (hm != null && hm.isExist(fullPath)) {
                    // but keep the order
                    Content node = hm.getContent(fullPath);
                    SiblingsHelper siblings = SiblingsHelper.of(node);
                    if(!siblings.isLast()){
                        nameOfNodeAfterTheImportedNode = siblings.next().getName();
                    }

                    hm.delete(fullPath);
                    log.warn("Deleted already existing node for bootstrapping: {}", fullPath);
                }
            } catch (RepositoryException e) {
                throw new RepositoryException("Can't check existence of node for bootstrap file: [" + name + "]", e);
            }

            DataTransporter.importXmlStream(stream, repository, pathName, name, false, importUUIDBehavior, false, true);

            if(nameOfNodeAfterTheImportedNode != null){
                Content newNode = hm.getContent(fullPath);
                newNode.getParent().orderBefore(nodeName, nameOfNodeAfterTheImportedNode);
            }

        }
    }

    public static void export(Content content, File directory) throws IOException, RepositoryException{
        String fileName = content.getWorkspace().getName() + DataTransporter.createExportPath(content.getHandle()) + ".xml";
        File file = new File(directory, fileName);
        FileOutputStream out = new FileOutputStream(file);
        try{
            DataTransporter.executeExport(out,false, true, content.getWorkspace().getSession(), content.getHandle(), content.getWorkspace().getName(), DataTransporter.XML);
        }
        finally{
            IOUtils.closeQuietly(out);
        }
    }

    /**
     * I.e. given a resource path like <code>/mgnl-bootstrap/foo/config.server.i18n.xml</code> it will return <code>config</code>.
     */
    public static String getWorkspaceNameFromResource(final String resourcePath) {
        String resourceName = StringUtils.replace(resourcePath, "\\", "/");

        String name = getFilenameFromResource(resourceName, ".xml");
        String fullPath = DataTransporter.revertExportPath(name);
        return StringUtils.substringBefore(fullPath, "/");
    }

    /**
     * I.e. given a resource path like <code>/mgnl-bootstrap/foo/config.server.i18n.xml</code> it will return <code>/server/i18n</code>.
     */
    public static String getFullpathFromResource(final String resourcePath) {
        String resourceName = StringUtils.replace(resourcePath, "\\", "/");

        String name = getFilenameFromResource(resourceName, ".xml");
        String fullPath = DataTransporter.revertExportPath(name);
        String repository = StringUtils.substringBefore(fullPath, "/");

        return StringUtils.removeStart(fullPath, repository);
    }

    /**
     * I.e. given a resource path like <code>/mgnl-bootstrap/foo/config.server.i18n.xml</code> it will return <code>/server</code>.
     */
    public static String getPathnameFromResource(final String resourcePath) {
        String resourceName = StringUtils.replace(resourcePath, "\\", "/");

        String name = getFilenameFromResource(resourceName, ".xml");
        String fullPath = DataTransporter.revertExportPath(name);
        String pathName = StringUtils.substringAfter(StringUtils.substringBeforeLast(fullPath, "/"), "/");

        if(!pathName.startsWith("/")) {
            pathName = "/" + pathName;
        }
        return pathName;
    }

    /**
     * I.e. given a resource path like <code>/mgnl-bootstrap/foo/config.server.i18n.xml</code> and <code>.xml</code> extension it will return <code>config.server.i18n</code> (no trailing dot).
     * If extension is <code>null</code>, it defaults to <code>.xml</code>.
     */
    public static String getFilenameFromResource(final String resourcePath, final String extension) {
        String ext = StringUtils.defaultIfEmpty(extension, ".xml");
        String tmpResourcePath = resourcePath;
        if(resourcePath.contains("/")) {
            tmpResourcePath = StringUtils.substringAfterLast(resourcePath, "/");
        }
        return StringUtils.removeEnd(tmpResourcePath, ext.startsWith(".") ? ext : "." + ext);
    }

}
