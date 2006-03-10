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

import info.magnolia.cms.beans.config.ModuleLoader;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.core.ie.DataTransporter;
import info.magnolia.cms.security.AccessDeniedException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
     * used by the installFiles() method
     * @author philipp
     */
    public interface IncludeMatcher {

        /**
         * @param name
         * @return true if this file should get installed
         */
        boolean match(String name);

        /**
         * trans from the path from the jar entry name into a real path
         * @param name
         * @return the path to which this file should get installed
         */
        String transform(String name);
    }

    /**
     * install files from a specifig directory
     * @author philipp
     */
    public static class DirectoryIncludeMatcher implements IncludeMatcher {

        private String path;

        public DirectoryIncludeMatcher(String path) {
            this.path = path;
        }

        public boolean match(String name) {
            return name.startsWith(path) && !name.endsWith("/");
        }

        public String transform(String name) {
            return StringUtils.removeStart(name, path);
        }
    }

    /**
     * Util has no public constructor
     */
    private ModuleUtil() {
    }

    /**
     * blocksize
     */
    public static final int DATA_BLOCK_SIZE = 1024 * 1024;

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
            Content node = createPath(hm, path);
            node.createNodeData(name).setValue(value);
        }
    }

    public static Content createPath(HierarchyManager hm, String path) throws AccessDeniedException,
        PathNotFoundException, RepositoryException {
        return createPath(hm, path, ItemType.CONTENTNODE);
    }

    public static Content createPath(HierarchyManager hm, String path, ItemType type) throws AccessDeniedException,
        PathNotFoundException, RepositoryException {
        // remove leading /
        path = StringUtils.removeStart(path, "/");

        String[] names = path.split("/"); //$NON-NLS-1$
        Content node = hm.getRoot();
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            if (node.hasContent(name)) {
                node = node.getContent(name);
            }
            else {
                node = node.createContent(name, type);
            }
        }
        return node;
    }

    /**
     * @param jar
     * @throws Exception
     * @deprecated use installFiles(jar, path) or installFiles(jar, matcher)
     */
    public static void installFiles(JarFile jar) throws Exception {
        IncludeMatcher matcher = new IncludeMatcher() {

            public boolean match(String name) {
                if (!name.toUpperCase().equals("/") //$NON-NLS-1$
                    && !name.endsWith("/") //$NON-NLS-1$
                    && !name.startsWith("CH") //$NON-NLS-1$
                    && !name.startsWith("META-INF") //$NON-NLS-1$
                    && !name.endsWith(".JAR")) { //$NON-NLS-1$
                    return true;
                }
                return false;
            }

            public String transform(String name) {
                return name;
            }
        };

        installFiles(jar, matcher);
    }

    public static void bootstrap(Collection resourceNames) throws IOException {
        for (Iterator iter = resourceNames.iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String repository = StringUtils.substringBefore(name, ".");
            String pathName = StringUtils.substringAfter(StringUtils.substringBeforeLast(StringUtils
                .substringBeforeLast(name, "."), "."), "."); //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$
            pathName = "/" + StringUtils.replace(pathName, ".", "/");
            InputStream stream = ModuleUtil.class.getResourceAsStream(name);
            DataTransporter.executeImport(
                pathName,
                repository,
                stream,
                name,
                false,
                ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING,
                true);
        }
    }

    public static void installFiles(JarFile jar, final String path) throws Exception {
        installFiles(jar, new DirectoryIncludeMatcher(path));
    }

    /**
     * Extracts files of a jar and stores them in the magnolia file structure
     * @param jar the jar containing the files (jsp, images)
     * @param matcher checks if the file must get installed
     * @param prefix the prefix to remove from the names
     * @throws Exception io exception
     */
    public static void installFiles(JarFile jar, IncludeMatcher matcher) throws Exception {

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

        Map files = new HashMap();
        Enumeration entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = (JarEntry) entries.nextElement();
            String name = entry.getName();

            if (matcher.match(name)) { //$NON-NLS-1$
                files.put(new File(root, matcher.transform(name)), entry);
            }
        }

        // Loop throgh files an check writeable
        String error = StringUtils.EMPTY;
        Iterator iter = files.keySet().iterator();
        while (iter.hasNext()) {
            File file = (File) iter.next();
            String s = StringUtils.EMPTY;
            if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                s = "Can't create directories for " + file.getAbsolutePath(); //$NON-NLS-1$
            }
            else if (!file.getParentFile().canWrite()) {
                s = "Can't write to " + file.getAbsolutePath(); //$NON-NLS-1$
            }
            if (s.length() > 0) {
                if (error.length() > 0) {
                    error += "\r\n"; //$NON-NLS-1$
                }
                error += s;
            }
        }

        if (error.length() > 0) {
            throw new Exception("Errors while installing files: " + error); //$NON-NLS-1$
        }

        // Copy files
        iter = files.keySet().iterator();
        while (iter.hasNext()) {
            File file = (File) iter.next();
            JarEntry entry = (JarEntry) files.get(file);

            int byteCount = 0;
            byte[] data = new byte[DATA_BLOCK_SIZE];

            InputStream in = null;
            BufferedOutputStream out = null;

            try {
                in = jar.getInputStream(entry);
                out = new BufferedOutputStream(new FileOutputStream(file), DATA_BLOCK_SIZE);

                while ((byteCount = in.read(data, 0, DATA_BLOCK_SIZE)) != -1) {
                    out.write(data, 0, byteCount);
                }
            }
            finally {
                IOUtils.closeQuietly(out);
            }
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
    public static Content createMinimalConfiguration(Content node, String name, String className, String version)
        throws AccessDeniedException, PathNotFoundException, RepositoryException {
        node.createNodeData("version").setValue(version); //$NON-NLS-1$
        node.createNodeData("license"); //$NON-NLS-1$
        node.createContent("Config"); //$NON-NLS-1$
        node.createContent("VirtualURIMapping", ItemType.CONTENTNODE); //$NON-NLS-1$

        Content register = node.createContent(ModuleLoader.CONFIG_NODE_REGISTER, ItemType.CONTENTNODE);
        register.createNodeData("moduleName"); //$NON-NLS-1$
        register.createNodeData("moduleDescription"); //$NON-NLS-1$
        register.createNodeData("class").setValue(className); //$NON-NLS-1$
        register.createNodeData("repository"); //$NON-NLS-1$
        register.createContent("sharedRepositories", ItemType.CONTENTNODE); //$NON-NLS-1$
        register.createContent("initParams", ItemType.CONTENTNODE); //$NON-NLS-1$
        return node;
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
    public static void registerServlet(String name, String className, String[] urlPatterns, String comment)
        throws JDOMException, IOException {
        registerServlet(name, className, urlPatterns, comment, null);
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
    public static void registerServlet(String name, String className, String[] urlPatterns, String comment,
        Hashtable initParams) throws JDOMException, IOException {
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
        }
        else {
            log.info("servlet " + name + " allready registered");
        }
        for (int i = 0; i < urlPatterns.length; i++) {
            String urlPattern = urlPatterns[i];
            registerServletMapping(doc, name, urlPattern, comment);
        }

        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        outputter.output(doc, new FileWriter(source));
    }

    public static void registerServletMapping(Document doc, String name, String urlPattern, String comment)
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
            log.info("register servlet " + name);

            // make a nice comment
            doc.getRootElement().addContent(new Comment(comment));

            // the same name space must be used
            Namespace ns = doc.getRootElement().getNamespace();

            // create the mapping
            node = new Element("servlet-mapping", ns);
            node.addContent(new Element("servlet-name", ns).addContent(name));
            node.addContent(new Element("url-pattern", ns).addContent(urlPattern));
            doc.getRootElement().addContent(node);
        }
        else {
            log.info("servlet " + name + " allready registered");
        }
    }

    public static void registerRepository(String name) {
        try {
            File source = Path.getRepositoriesConfigFile();
            if (!source.exists()) {
                throw new FileNotFoundException("Failed to locate magnolia repositories config file at " //$NON-NLS-1$
                    + source.getAbsolutePath());
            }
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(source);

            // check if there
            Element node = (Element) XPath.selectSingleNode(doc, "/JCR/Repository[@name='" + name + "']");
            if (node == null) {
                // create
                node = new Element("Repository");

                String provider = ((Element) XPath.selectSingleNode(doc, "/JCR/Repository[@name='website']"))
                    .getAttributeValue("provider");
                String configFile = ((Element) XPath.selectSingleNode(
                    doc,
                    "/JCR/Repository[@name='website']/param[@name='configFile']")).getAttributeValue("value");
                String repositoryHome = ((Element) XPath.selectSingleNode(
                    doc,
                    "/JCR/Repository[@name='website']/param[@name='repositoryHome']")).getAttributeValue("value");
                repositoryHome = StringUtils.substringBeforeLast(repositoryHome, "/") + "/" + name;
                String contextFactoryClass = ((Element) XPath.selectSingleNode(
                    doc,
                    "/JCR/Repository[@name='website']/param[@name='contextFactoryClass']")).getAttributeValue("value");
                String providerURL = ((Element) XPath.selectSingleNode(
                    doc,
                    "/JCR/Repository[@name='website']/param[@name='providerURL']")).getAttributeValue("value");
                String bindName = ((Element) XPath.selectSingleNode(
                    doc,
                    "/JCR/Repository[@name='website']/param[@name='bindName']")).getAttributeValue("value");
                bindName = StringUtils.replace(bindName, "website", name);

                node.setAttribute("name", name);
                node.setAttribute("id", name);
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

                doc.getRootElement().addContent(node);

                // make the mapping
                node = new Element("Map");
                node.setAttribute("name", name).setAttribute("repositoryName", name);
                // add it
                doc.getRootElement().getChild("RepositoryMapping").addContent(node);

                // save it
                XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
                outputter.output(doc, new FileWriter(source));
            }
        }
        catch (Exception e) {
            log.error("can't register repository", e);
        }
    }
}