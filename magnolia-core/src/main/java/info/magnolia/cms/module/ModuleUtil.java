/**
 * This file Copyright (c) 2003-2007 Magnolia International
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
package info.magnolia.cms.module;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.core.ie.DataTransporter;
import info.magnolia.cms.exchange.ActivationManager;
import info.magnolia.cms.exchange.ActivationManagerFactory;
import info.magnolia.cms.exchange.Subscriber;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.Role;
import info.magnolia.cms.security.Security;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.WebXmlUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.files.BasicFileExtractor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is a util providing some methods for the registration process of a module.
 * @author philipp
 * @version $Revision$ ($Author$)
 * @deprecated most methods here should be replaced by implementations of info.magnolia.module.delta.Task
 */
public final class ModuleUtil {

    /**
     * Logger
     */

    private static Logger log = LoggerFactory.getLogger(ModuleUtil.class);

    /**
     * Util has no public constructor
     */
    private ModuleUtil() {
    }

    /**
     * registers the properties in the repository
     * @param hm
     * @param name
     * @throws IOException
     * @throws RepositoryException
     * @throws PathNotFoundException
     * @throws AccessDeniedException
     */
    public static void registerProperties(HierarchyManager hm, String name) throws IOException, AccessDeniedException,
        PathNotFoundException, RepositoryException {
        Map map = new ListOrderedMap();

        // not using properties since they are not ordered
        // Properties props = new Properties();
        // props.load(ModuleUtil.class.getResourceAsStream("/" + name.replace('.', '/') + ".properties"));
        InputStream stream = ModuleUtil.class.getResourceAsStream("/" + name.replace('.', '/') + ".properties"); //$NON-NLS-1$ //$NON-NLS-2$
        LineNumberReader lines = new LineNumberReader(new InputStreamReader(stream));

        String line = lines.readLine();
        while (line != null) {
            line = line.trim();
            if (line.length() > 0 && !line.startsWith("#")) { //$NON-NLS-1$
                String key = StringUtils.substringBefore(line, "=").trim(); //$NON-NLS-1$
                String value = StringUtils.substringAfter(line, "=").trim(); //$NON-NLS-1$
                map.put(key, value);
            }
            line = lines.readLine();
        }
        IOUtils.closeQuietly(lines);
        IOUtils.closeQuietly(stream);
        registerProperties(hm, map);
    }

    public static void registerProperties(HierarchyManager hm, Map map) throws AccessDeniedException,
        PathNotFoundException, RepositoryException {
        for (Iterator iter = map.keySet().iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            String value = (String) map.get(key);

            String name = StringUtils.substringAfterLast(key, "."); //$NON-NLS-1$
            String path = StringUtils.substringBeforeLast(key, ".").replace('.', '/'); //$NON-NLS-1$
            Content node = ContentUtil.createPath(hm, path, ItemType.CONTENT);
            node.createNodeData(name).setValue(value);
        }
    }

    /**
     * Bootstraps the given resources and save.
     * @deprecated use bootstrap(String[] resourceNames, boolean saveAfterImport), saving explicitely.
     */
    public static void bootstrap(String[] resourceNames) throws IOException, RegisterException {
        bootstrap(resourceNames, true);
    }

    public static void bootstrap(String[] resourceNames, boolean saveAfterImport) throws IOException, RegisterException {
        // sort by length --> import parent node first
        List list = new ArrayList(Arrays.asList(resourceNames));

        Collections.sort(list, new Comparator() {

            public int compare(Object name1, Object name2) {
                return ((String) name1).length() - ((String) name2).length();
            }
        });

        for (Iterator iter = list.iterator(); iter.hasNext();) {
            String resourceName = (String) iter.next();

            // windows again
            resourceName = StringUtils.replace(resourceName, "\\", "/");

            String name = StringUtils.removeEnd(StringUtils.substringAfterLast(resourceName, "/"), ".xml");

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
            final InputStream stream = ModuleUtil.class.getResourceAsStream(resourceName);
            if (stream == null) {
                throw new IOException("Can't find resource to bootstrap at " + resourceName);
            }

            // if the path already exists --> delete it
            try {
                final HierarchyManager hm = MgnlContext.getHierarchyManager(repository);

                // hm can be null if module is not properly registered and the repository has not been created
                if (hm != null && hm.isExist(fullPath)) {
                    hm.delete(fullPath);
                    log.warn("already existing node [{}] deleted", fullPath);
                }
            }
            catch (Exception e) {
                throw new RegisterException("can't register bootstrap file: [" + name + "]", e);
            }

            DataTransporter.importXmlStream(stream, repository, pathName, name, false,
                ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW,
                saveAfterImport,
                true);
        }
    }

    /**
     * Extracts files of a jar and stores them in the magnolia file structure
     * @param names a list of resource names
     * @param prefix prefix which is not part of the magolia path (in common 'mgnl-files')
     * @throws Exception io exception
     * @deprecated
     * @see info.magnolia.module.files.FileExtractor
     */
    public static void installFiles(String[] names, String prefix) throws Exception {
        final BasicFileExtractor fileExtractor = new BasicFileExtractor();
        fileExtractor.installFiles(names, prefix);
    }

    /**
     * Create a minimal module configuration
     * @param node the module node
     * @param name module name
     * @param className the class used
     * @param version version number of the module
     * @return the modified node (not yet stored)
     * @throws AccessDeniedException exception
     * @throws PathNotFoundException exception
     * @throws RepositoryException exception
     * @deprecated
     */
    public static Content createMinimalConfiguration(Content node, String name, String displayName, String className,
        String version) throws AccessDeniedException, PathNotFoundException, RepositoryException {
        node.createNodeData("version").setValue(version); //$NON-NLS-1$
        node.createNodeData("name").setValue(name); //$NON-NLS-1$
        node.createNodeData("displayName").setValue(displayName); //$NON-NLS-1$
        node.createNodeData("class").setValue(className); //$NON-NLS-1$
        node.createContent("config"); //$NON-NLS-1$
        Content license = node.createContent("license", ItemType.CONTENTNODE); //$NON-NLS-1$
        license.createNodeData("key"); //$NON-NLS-1$
        license.createNodeData("owner"); //$NON-NLS-1$

        return node;
    }

    /**
     * Register a servlet based on the definition of the modules xml descriptor
     * @deprecated since 3.5, servlets are wrapped and executed through ServletDispatchingFilter
     * @see info.magnolia.cms.filters.ServletDispatchingFilter
     */
    public static boolean registerServlet(ServletDefinition servlet) throws JDOMException, IOException {
        String[] urlPatterns = (String[]) servlet.getMappings().toArray(new String[servlet.getMappings().size()]);
        Hashtable params = new Hashtable();
        for (Iterator iter = servlet.getParams().iterator(); iter.hasNext();) {
            ServletParameterDefinition param = (ServletParameterDefinition) iter.next();
            params.put(param.getName(), param.getValue());
        }
        return registerServlet(servlet.getName(), servlet.getClassName(), urlPatterns, servlet.getComment(), params);
    }

    /**
     * Register a servlet in the web.xml. The code checks if the servlet already exists
     * @deprecated Use WebXmlUtil
     */
    public static boolean registerServlet(String name, String className, String[] urlPatterns, String comment)
        throws JDOMException, IOException {
        return registerServlet(name, className, urlPatterns, comment, null);
    }

    /**
     * Register a servlet in the web.xml including init parameters. The code checks if the servlet already exists
     * @deprecated since 3.5, servlets are wrapped and executed through ServletDispatchingFilter
     * @see WebXmlUtil
     */
    public static boolean registerServlet(String name, String className, String[] urlPatterns, String comment,
            Hashtable initParams) throws JDOMException, IOException {
        return new WebXmlUtil().registerServlet(name, className, urlPatterns, comment, initParams);
    }

    /**
     * @deprecated Use WebXmlUtil
     */
    public static boolean registerServletMapping(Document doc, String name, String urlPattern, String comment)
        throws JDOMException {

        throw new IllegalStateException("This method should never have been public. Use one of the registerServlet methods if needed.");
    }

    public static boolean registerRepository(String name) throws RegisterException {
        return registerRepository(name, null);
    }

    /**
     * Register a repository
     * @param repositoryName
     * @param nodeTypeFile
     * @return <code>true</code> if a repository is registered or <code>false</code> if it was already existing
     * @throws RegisterException
     * @deprecated repositories in modules are automatically loaded by ModuleManager
     */
    public static boolean registerRepository(final String repositoryName, final String nodeTypeFile)
        throws RegisterException {

        log.warn("ModuleUtil.registerRepository will not perform any action: "
            + "repositories in modules are automatically loaded by ModuleManager");
        return false;
    }

    /**
     * @deprecated repositories in modules are automatically loaded by ModuleManager
     */
    public static boolean registerNodetypes(String repositoryName, String customNodetypes) throws RegisterException {

        log.warn("ModuleUtil.registerNodetypes will not perform any action: "
            + "repositories in modules are automatically loaded by ModuleManager");
        return false;

    }

    /**
     * Read the dom for the repositories.xml
     */
    public static Document getRepositoryDefinitionDocument() throws JDOMException, IOException {
        File source = getRepositoryDefinitionFile();
        SAXBuilder builder = new SAXBuilder();
        return builder.build(source);
    }

    /**
     * @return
     * @throws FileNotFoundException
     */
    public static File getRepositoryDefinitionFile() throws FileNotFoundException {
        File source = Path.getRepositoriesConfigFile();
        if (!source.exists()) {
            throw new FileNotFoundException("Failed to locate magnolia repositories config file at " //$NON-NLS-1$
                + source.getAbsolutePath());
        }
        return source;
    }

    /**
     * @param repositoryName
     * @param workspaceName
     * @throws RegisterException if the workspace could not be register
     * @deprecated repositories in modules are automatically loaded by ModuleManager
     */
    public static boolean registerWorkspace(final String repositoryName, final String workspaceName)
        throws RegisterException {

        log.warn("ModuleUtil.registerWorkspace will not perform any action: "
            + "repositories in modules are automatically loaded by ModuleManager");

        return false;
    }

    /**
     * Grant the superuser role by default
     */
    public static void grantRepositoryToSuperuser(String repository) {
        Role superuser = Security.getRoleManager().getRole("superuser");
        superuser.addPermission(repository, "/*", Permission.ALL);
    }

    /**
     * Register the repository to get used for activation
     * @param repository
     */
    public static void subscribeRepository(String repository) {
        ActivationManager sManager = ActivationManagerFactory.getActivationManager();
        Iterator subscribers = sManager.getSubscribers().iterator();
        while (subscribers.hasNext()) {
            Subscriber subscriber = (Subscriber) subscribers.next();
            if (!subscriber.isSubscribed("/", repository)) {
                Content subscriptionsNode = ContentUtil.getContent(ContentRepository.CONFIG, sManager.getConfigPath()
                    + "/"
                    + subscriber.getName()
                    + "/subscriptions");
                try {
                    Content newSubscription = subscriptionsNode.createContent(repository, ItemType.CONTENTNODE);
                    newSubscription.createNodeData("toURI").setValue("/");
                    newSubscription.createNodeData("repository").setValue(repository);
                    newSubscription.createNodeData("fromURI").setValue("/");
                    subscriptionsNode.save();
                }
                catch (RepositoryException re) {
                    log.error("wasn't able to subscribe repository [" + repository + "]", re);
                }
            }
        }
    }
}
