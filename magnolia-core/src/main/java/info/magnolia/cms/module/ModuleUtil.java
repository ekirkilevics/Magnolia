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
package info.magnolia.cms.module;

import info.magnolia.cms.beans.config.Bootstrapper;
import info.magnolia.cms.beans.config.ConfigurationException;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.Subscriber;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.core.ie.DataTransporter;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.Role;
import info.magnolia.cms.security.Security;
import info.magnolia.cms.util.ClasspathResourcesUtil;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.repository.Provider;
import info.magnolia.repository.RepositoryMapping;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
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
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is a util providing some methods for the registration process of a module.
 * @author philipp
 * @version $Revision$ ($Author$)
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

    public static void bootstrap(String[] resourceNames) throws IOException, RegisterException {

        HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.CONFIG);

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

            // if the path already exists --> delete it
            try {
                if (hm.isExist(fullPath)) {
                    hm.delete(fullPath);
                    if (log.isDebugEnabled()) {
                        log.debug("already existing node [{}] deleted", fullPath);
                    }
                }

                // if the parent path not exists just create it
                if (!pathName.equals("/")) {
                    ContentUtil.createPath(hm, pathName, ItemType.CONTENT);
                }
            }
            catch (Exception e) {
                throw new RegisterException("can't register bootstrap file: [" + name + "]", e);
            }
            InputStream stream = ModuleUtil.class.getResourceAsStream(resourceName);
            DataTransporter.importXmlStream(
                stream,
                repository,
                pathName,
                name,
                false,
                ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING,
                true,
                true);
        }
    }

    /**
     * Extracts files of a jar and stores them in the magnolia file structure
     * @param names a list of resource names
     * @param prefix prefix which is not part of the magolia path (in common 'mgnl-files')
     * @throws Exception io exception
     */
    public static void installFiles(String[] names, String prefix) throws Exception {

        String root = null;
        // Try to get root
        try {
            File f = new File(SystemProperty.getProperty(SystemProperty.MAGNOLIA_APP_ROOTDIR));
            if (f.isDirectory()) {
                root = f.getAbsolutePath();
            }
        }
        catch (Exception e) {
            // nothing
        }

        if (root == null) {
            throw new Exception("Invalid magnolia " + SystemProperty.MAGNOLIA_APP_ROOTDIR + " path"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // Loop throgh files and check writeable
        String error = StringUtils.EMPTY;
        for (int j = 0; j < names.length; j++) {
            String name = names[j];

            InputStream resourceStream = ClasspathResourcesUtil.getStream(name, false);

            File targetFile = new File(Path.getAbsoluteFileSystemPath(StringUtils.removeStart(name, prefix)));

            String s = StringUtils.EMPTY;
            if (!targetFile.getParentFile().exists() && !targetFile.getParentFile().mkdirs()) {
                s = "Can't create directories for " + targetFile.getAbsolutePath(); //$NON-NLS-1$
            }
            else if (!targetFile.getParentFile().canWrite()) {
                s = "Can't write to " + targetFile.getAbsolutePath(); //$NON-NLS-1$
            }
            if (s.length() > 0) {
                if (error.length() > 0) {
                    error += "\r\n"; //$NON-NLS-1$
                }
                error += s;
            }

            OutputStream out = new FileOutputStream(targetFile);
            IOUtils.copy(resourceStream, out);

            IOUtils.closeQuietly(resourceStream);
            IOUtils.closeQuietly(out);
        }

        if (error.length() > 0) {
            throw new Exception("Errors while installing files: " + error); //$NON-NLS-1$
        }

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
     * @param servlet
     * @throws JDOMException
     * @throws IOException
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
     * @param name
     * @param className
     * @param urlPatterns
     * @param comment
     * @throws JDOMException
     * @throws IOException
     */
    public static boolean registerServlet(String name, String className, String[] urlPatterns, String comment)
        throws JDOMException, IOException {
        return registerServlet(name, className, urlPatterns, comment, null);
    }

    /**
     * Register a servlet in the web.xml including init parameters. The code checks if the servlet already exists
     * @param name
     * @param className
     * @param urlPatterns
     * @param comment
     * @param initParams
     * @throws JDOMException
     * @throws IOException
     */
    public static boolean registerServlet(String name, String className, String[] urlPatterns, String comment,
        Hashtable initParams) throws JDOMException, IOException {

        boolean changed = false;

        // get the web.xml
        File source = new File(Path.getAppRootDir() + "/WEB-INF/web.xml");
        if (!source.exists()) {
            throw new FileNotFoundException("Failed to locate web.xml " //$NON-NLS-1$
                + source.getAbsolutePath());
        }
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(source);

        // check if there already registered
        XPath xpath = XPath.newInstance("/webxml:web-app/webxml:servlet[webxml:servlet-name='" + name + "']");
        // must add the namespace and use it: there is no default namespace elsewise
        xpath.addNamespace("webxml", doc.getRootElement().getNamespace().getURI());
        Element node = (Element) xpath.selectSingleNode(doc);

        if (node == null) {
            log.info("register servlet " + name);

            // make a nice comment
            doc.getRootElement().addContent(new Comment(comment));

            // the same name space must be used
            Namespace ns = doc.getRootElement().getNamespace();

            node = new Element("servlet", ns);
            node.addContent(new Element("servlet-name", ns).addContent(name));
            node.addContent(new Element("servlet-class", ns).addContent(className));

            if (initParams != null && !(initParams.isEmpty())) {
                Enumeration params = initParams.keys();
                while (params.hasMoreElements()) {
                    String paramName = params.nextElement().toString();
                    String paramValue = (String) initParams.get(paramName);
                    Element initParam = new Element("init-param", ns);
                    initParam.addContent(new Element("param-name", ns).addContent(paramName));
                    initParam.addContent(new Element("param-value", ns).addContent(paramValue));
                    node.addContent(initParam);
                }
            }

            doc.getRootElement().addContent(node);
            changed = true;
        }
        else {
            log.info("servlet {} already registered", name);
        }
        for (int i = 0; i < urlPatterns.length; i++) {
            String urlPattern = urlPatterns[i];
            changed = changed | registerServletMapping(doc, name, urlPattern, comment);
        }

        if (changed) {
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            outputter.output(doc, new FileWriter(source));
        }
        return changed;
    }

    public static boolean registerServletMapping(Document doc, String name, String urlPattern, String comment)
        throws JDOMException {
        XPath xpath = XPath.newInstance("/webxml:web-app/webxml:servlet-mapping[webxml:servlet-name='"
            + name
            + "' and webxml:url-pattern='"
            + urlPattern
            + "']");
        // must add the namespace and use it: there is no default namespace elsewise
        xpath.addNamespace("webxml", doc.getRootElement().getNamespace().getURI());
        Element node = (Element) xpath.selectSingleNode(doc);

        if (node == null) {
            log.info("register servlet mapping [{}] for servlet [{}]", urlPattern, name);

            // make a nice comment
            doc.getRootElement().addContent(new Comment(comment));

            // the same name space must be used
            Namespace ns = doc.getRootElement().getNamespace();

            // create the mapping
            node = new Element("servlet-mapping", ns);
            node.addContent(new Element("servlet-name", ns).addContent(name));
            node.addContent(new Element("url-pattern", ns).addContent(urlPattern));
            doc.getRootElement().addContent(node);
            return true;

        }

        log.info("servlet mapping [{}] for servlet [{}] already registered", urlPattern, name);
        return false;
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
     */
    public static boolean registerRepository(final String repositoryName, final String nodeTypeFile) throws RegisterException {

        boolean registered = false;
        
        Document doc;
        try {
            doc = getRepositoryDefinitionDocument();
        }
        catch (JDOMException e) {
            throw new RegisterException("Failed to read magnolia repositories config file", e);
        }
        catch (IOException e) {
            throw new RegisterException("Failed to read magnolia repositories config file", e);
        }

        // check if there
        try {
            Element node = (Element) XPath.selectSingleNode(doc, "/JCR/Repository[@name='" + repositoryName + "']");
            if (node == null) {
                // create
                node = new Element("Repository");

                String provider = ((Element) XPath.selectSingleNode(doc, "/JCR/Repository[@name='magnolia']"))
                    .getAttributeValue("provider");
                String configFile = ((Element) XPath.selectSingleNode(
                    doc,
                    "/JCR/Repository[@name='magnolia']/param[@name='configFile']")).getAttributeValue("value");
                String repositoryHome = ((Element) XPath.selectSingleNode(
                    doc,
                    "/JCR/Repository[@name='magnolia']/param[@name='repositoryHome']")).getAttributeValue("value");
                repositoryHome = StringUtils.substringBeforeLast(repositoryHome, "/") + "/" + repositoryName;
                String contextFactoryClass = ((Element) XPath.selectSingleNode(
                    doc,
                    "/JCR/Repository[@name='magnolia']/param[@name='contextFactoryClass']")).getAttributeValue("value");
                String providerURL = ((Element) XPath.selectSingleNode(
                    doc,
                    "/JCR/Repository[@name='magnolia']/param[@name='providerURL']")).getAttributeValue("value");
                String bindName = ((Element) XPath.selectSingleNode(
                    doc,
                    "/JCR/Repository[@name='magnolia']/param[@name='bindName']")).getAttributeValue("value");
                bindName = StringUtils.replace(bindName, "magnolia", "");
                bindName = repositoryName + bindName;

                node.setAttribute("name", repositoryName);
                node.setAttribute("id", repositoryName);
                node.setAttribute("loadOnStartup", "true");
                node.setAttribute("provider", provider);

                node.addContent(new Element("param").setAttribute("name", "configFile").setAttribute(
                    "value",
                    configFile));
                node.addContent(new Element("param").setAttribute("name", "repositoryHome").setAttribute(
                    "value",
                    repositoryHome));
                node.addContent(new Element("param").setAttribute("name", "contextFactoryClass").setAttribute(
                    "value",
                    contextFactoryClass));
                node.addContent(new Element("param").setAttribute("name", "providerURL").setAttribute(
                    "value",
                    providerURL));
                node.addContent(new Element("param").setAttribute("name", "bindName").setAttribute("value", bindName));

                if (StringUtils.isNotEmpty(nodeTypeFile)) {
                    node.addContent(new Element("param").setAttribute("name", "customNodeTypes").setAttribute(
                        "value",
                        nodeTypeFile));
                }

                // add a workspace
                node.addContent(new Element("workspace").setAttribute("name", repositoryName));

                doc.getRootElement().addContent(node);

                // make the mapping
                node = new Element("Map");
                node.setAttribute("name", repositoryName).setAttribute("repositoryName", repositoryName);
                // add it
                doc.getRootElement().getChild("RepositoryMapping").addContent(node);

                // save it
                XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
                outputter.output(doc, new FileWriter(getRepositoryDefinitionFile()));
                
                RepositoryMapping mapping = new RepositoryMapping();
                mapping.setName(repositoryName);
                mapping.addWorkspace(repositoryName);
                mapping.setProvider(providerURL);
                mapping.setLoadOnStartup(true);
                Map parameters = new HashMap();
                parameters.put("configFile", configFile);
                parameters.put("repositoryHome", repositoryHome);
                parameters.put("contextFactoryClass", contextFactoryClass);
                parameters.put("providerURL", providerURL);
                parameters.put("bindName", bindName);
                parameters.put("customNodeTypes", nodeTypeFile);
                
                
                mapping.setParameters(parameters);
                
                // bootstrap the new workspace if empty
                try {
                    // load new workspace
                    ContentRepository.loadRepository(mapping);

                    if(!ContentRepository.checkIfInitialized(repositoryName)){
                        Bootstrapper.bootstrapRepository(repositoryName, new Bootstrapper.BootstrapFilter(){
                            public boolean accept(String filename) {
                                return filename.startsWith(repositoryName + ".");
                            }
                        });
                    }
                }
                catch (Exception e) {
                    log.error("can't load or bootstrap newly registered repository " + repositoryName, e);
                }

                registered = true;
            }
            else if (nodeTypeFile != null) {
                // this never requires a restart
                ModuleUtil.registerNodetypes(repositoryName, nodeTypeFile);
            }

        }
        catch (IOException e) {
            log.error("Can't register repository. Unable to write modified file.", e);
            throw new RegisterException("Can't register repository. Unable to write modified file.", e);
        }
        catch (JDOMException e) {
            log.error("can't register repository", e);
            throw new RegisterException("can't register repository", e);
        }
        
        return registered;
    }

    public static boolean registerNodetypes(String repositoryName, String customNodetypes) throws RegisterException {
        Provider provider = ContentRepository.getRepositoryProvider(repositoryName);

        if (provider == null) {
            throw new RegisterException("before registering nodetypes you need to register the repository ["
                + repositoryName
                + "]");
        }

        try {
            provider.registerNodeTypes(customNodetypes);
        }
        catch (RepositoryException e) {
            throw new RegisterException("Error registering nodetypes for repository ["
                + repositoryName
                + "]: "
                + e.getMessage(), e);
        }

        // this never requires a restart
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
     */
    public static boolean registerWorkspace(final String repositoryName, final String workspaceName) throws RegisterException {

        boolean changed = false;

        try {
            Document doc = getRepositoryDefinitionDocument();
            // check if there
            Element repositoryNode = (Element) XPath.selectSingleNode(doc, "/JCR/Repository[@name='"
                + repositoryName
                + "']");
            if (repositoryNode == null) {
                throw new ConfigurationException("before registering a workspace ["
                    + workspaceName
                    + "] you need to register the repository ["
                    + repositoryName
                    + "]");
            }

            // make the mapping
            Element mappingNode = (Element) XPath.selectSingleNode(doc, "/JCR/RepositoryMapping/Map[@name='"
                + workspaceName
                + "']");
            if (mappingNode == null) {
                mappingNode = new Element("Map");
                mappingNode.setAttribute("name", workspaceName).setAttribute("repositoryName", repositoryName);
                // add it
                doc.getRootElement().getChild("RepositoryMapping").addContent(mappingNode);

                // check only if mapping not existing
                Element workspaceNode = (Element) XPath.selectSingleNode(doc, "/JCR/Repository[@name='"
                    + repositoryName
                    + "']/workspace[@name='"
                    + workspaceName
                    + "']");

                if (workspaceNode == null) {
                    workspaceNode = new Element("workspace");
                    workspaceNode.setAttribute("name", workspaceName);
                    repositoryNode.addContent(workspaceNode);
                }


                changed = true;
            }

            // save it
            if (changed) {
                XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
                outputter.output(doc, new FileWriter(getRepositoryDefinitionFile()));
                
                // load new workspace
                ContentRepository.loadWorkspace(repositoryName, workspaceName);
            }
            
            // bootstrap the new workspace if empty
            if(!ContentRepository.checkIfInitialized(workspaceName)){
                Bootstrapper.bootstrapRepository(workspaceName, new Bootstrapper.BootstrapFilter(){
                    public boolean accept(String filename) {
                        return filename.startsWith(workspaceName + ".");
                    }
                });
            }
            
        }
        catch (Exception e) {
            log.error("Can't register workspace [" + workspaceName + "]", e);
            throw new RegisterException("can't register workspace [" + workspaceName + "]", e);
        }

        return changed;
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
        List collectedNodes = new ArrayList();
        
        // singel subscriber schema
        Content subscriberNode = ContentUtil.getContent(ContentRepository.CONFIG, "/" + Subscriber.SUBSCRIBER_NODE_NAME);
        if(subscriberNode != null){
            collectedNodes.add(subscriberNode);
        }
        
        // multiple subscriber schema
        Content subscribersNode = ContentUtil.getContent(ContentRepository.CONFIG, "/" + Subscriber.SUBSCRIBERS_NODE_NAME);
        if (subscribersNode != null) {
            collectedNodes.addAll(subscribersNode.getChildren(ItemType.CONTENTNODE));
        }
        
        for (Iterator iter = collectedNodes.iterator(); iter.hasNext();) {
            Content node = (Content) iter.next();
            Content context = ContentUtil.getCaseInsensitive(node, Subscriber.CONTEXT_NODE_NAME);
            try {
                if (context != null && !context.hasContent(repository)) {
                    Content rep = context.createContent(repository, ItemType.CONTENTNODE);
                    Content entry = rep.createContent("0001", ItemType.CONTENTNODE);
                    entry.createNodeData("subscribedURI").setValue("/");
                    node.save();
                }
            }
            catch (RepositoryException e) {
                log.error("wasn't able to subscribe repository [" + repository + "]", e);
            }

        }
    }
}