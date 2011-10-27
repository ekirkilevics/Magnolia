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
package info.magnolia.jackrabbit;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.repository.Provider;
import info.magnolia.repository.RepositoryNotInitializedException;
import info.magnolia.repository.definition.RepositoryDefinition;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.NamespaceException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeTypeManager;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.jackrabbit.core.WorkspaceImpl;
import org.apache.jackrabbit.core.jndi.RegistryHelper;
import org.apache.jackrabbit.core.nodetype.NodeTypeManagerImpl;
import org.apache.jackrabbit.core.nodetype.NodeTypeRegistry;
import org.apache.jackrabbit.core.nodetype.xml.NodeTypeReader;
import org.apache.jackrabbit.spi.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Provider implementation for Apache JackRabbit JCR repository.
 *
 * @version $Id$
 */
public class ProviderImpl implements Provider {

    /**
     * Magnolia property (entry in magnolia.properties) with the cluster id, which will be passed to jackrabbit.
     */
    private static final String MAGNOLIA_CLUSTERID_PROPERTY = "magnolia.clusterid";

    /**
     * Jackrabbit system property for cluster node id.
     */
    private static final String JACKRABBIT_CLUSTER_ID_PROPERTY = "org.apache.jackrabbit.core.cluster.node_id";

    private static final Logger log = LoggerFactory.getLogger(ProviderImpl.class);

    private static final String CONFIG_FILENAME_KEY = "configFile";

    private static final String REPOSITORY_HOME_KEY = "repositoryHome";

    private static final String CONTEXT_FACTORY_CLASS_KEY = "contextFactoryClass";

    private static final String PROVIDER_URL_KEY = "providerURL";

    private static final String BIND_NAME_KEY = "bindName";

    private static final String MGNL_NODETYPES = "/mgnl-nodetypes/magnolia-nodetypes.xml";

    private static final String CUSTOM_NODETYPES = "customNodeTypes";

    private RepositoryDefinition repositoryMapping;

    private Repository repository;

    private String bindName;

    private Hashtable<String, Object> jndiEnv;

    private static final String REPO_HOME_PREFIX = "${repository.home}";

    private static final int REPO_HOME_SUFIX_LEN = REPO_HOME_PREFIX.length();

    private static final String sysRepositoryHome = System.getProperty("repository.home");

    private static final String sysRepositoryHomes = System.getProperty("repository.homes");

    /**
     * Finds the physical path to the repository folder.
     * @param repositoryHome the property set in the repository.xml file
     * @return the full path resolved to the repository dir
     */
    private String getRepositoryHome(final String repositoryHome) {
        boolean relocate = false;
        String tmp = repositoryHome;
        if (repositoryHome.startsWith(REPOSITORY_HOME_KEY)) {
            tmp = repositoryHome.substring(REPO_HOME_SUFIX_LEN);
            relocate = true;
        }
        /*
         * Resolve if the path started with the suffix
         */
        if (sysRepositoryHome != null && relocate) {
            return sysRepositoryHome + File.separator + tmp;
        }

        /*
         * This is apply to all repositories if the java property is set
         */
        if (sysRepositoryHomes != null) {
            return sysRepositoryHomes + File.separator + tmp;
        }

        /*
         * Return the same value as before if neither of the above applied
         */
        return Path.getAbsoluteFileSystemPath(tmp);
    }

    /**
     * @see info.magnolia.repository.Provider#init(info.magnolia.repository.RepositoryMapping)
     */
    @Override
    public void init(RepositoryDefinition repositoryMapping) throws RepositoryNotInitializedException {
        checkXmlSettings();

        this.repositoryMapping = repositoryMapping;
        /* connect to repository */
        Map params = this.repositoryMapping.getParameters();
        String configFile = (String) params.get(CONFIG_FILENAME_KEY);
        configFile = Path.getAbsoluteFileSystemPath(configFile);
        String repositoryHome = (String) params.get(REPOSITORY_HOME_KEY);
        repositoryHome = getRepositoryHome(repositoryHome);

        // cleanup the path, to remove eventual ../.. and make it absolute
        try {
            File repoHomeFile = new File(repositoryHome);
            repositoryHome = repoHomeFile.getCanonicalPath();
        }
        catch (IOException e1) {
            // should never happen and it's not a problem at this point, just pass it to jackrabbit and see
        }

        String clusterid = SystemProperty.getProperty(MAGNOLIA_CLUSTERID_PROPERTY);
        if (StringUtils.isNotBlank(clusterid)) {
            System.setProperty(JACKRABBIT_CLUSTER_ID_PROPERTY, clusterid);
        }

        // get it back from system properties, if it has been set elsewhere
        clusterid = System.getProperty(JACKRABBIT_CLUSTER_ID_PROPERTY);

        log.info("Loading repository at {} (config file: {}) - cluster id: \"{}\"", new Object[]{
            repositoryHome,
            configFile,
            StringUtils.defaultString(clusterid, "<unset>")});

        bindName = (String) params.get(BIND_NAME_KEY);
        jndiEnv = new Hashtable<String, Object>();
        jndiEnv.put(Context.INITIAL_CONTEXT_FACTORY, params.get(CONTEXT_FACTORY_CLASS_KEY));
        jndiEnv.put(Context.PROVIDER_URL, params.get(PROVIDER_URL_KEY));

        try {
            InitialContext ctx = new InitialContext(jndiEnv);
            // first try to find the existing object if any
            try {
                this.repository = (Repository) ctx.lookup(bindName);
            }
            catch (NameNotFoundException ne) {
                log.debug("No JNDI bound Repository found with name {}, trying to initialize a new Repository", bindName);
                RegistryHelper.registerRepository(ctx, bindName, configFile, repositoryHome, true);
                this.repository = (Repository) ctx.lookup(bindName);
            }
            this.validateWorkspaces();
        }
        catch (NamingException e) {
            log.error("Unable to initialize repository: " + e.getMessage(), e);
            throw new RepositoryNotInitializedException(e);
        }
        catch (RepositoryException e) {
            log.error("Unable to initialize repository: " + e.getMessage(), e);
            throw new RepositoryNotInitializedException(e);
        }
        catch (TransformerFactoryConfigurationError e) {
            log.error("Unable to initialize repository: " + e.getMessage(), e);
            throw new RepositoryNotInitializedException(e);
        }
    }

    @Override
    public void shutdownRepository() {
        log.info("Shutting down repository bound to '{}'", bindName);

        try {
            Context ctx = new InitialContext(jndiEnv);
            RegistryHelper.unregisterRepository(ctx, bindName);
        } catch (NamingException e) {
            log.warn("Unable to shutdown repository " + bindName + ": " + e.getMessage(), e);
        } catch (Throwable e) {
            log.error("Failed to shutdown repository " + bindName + ": " + e.getMessage(), e);
        }
    }

    /**
     * @deprecated typo - use get #getUnderlyingRepository() - since 4.0
     */
    @Override
    public Repository getUnderlineRepository() throws RepositoryNotInitializedException {
        return getUnderlyingRepository();
    }

    @Override
    public Repository getUnderlyingRepository() throws RepositoryNotInitializedException {
        if (this.repository == null) {
            throw new RepositoryNotInitializedException("Null repository");
        }
        return this.repository;
    }

    /**
     * @see info.magnolia.repository.Provider#registerNamespace(java.lang.String, java.lang.String, javax.jcr.Workspace)
     */
    @Override
    public void registerNamespace(String namespacePrefix, String uri, Workspace workspace) throws RepositoryException {
        try {
            workspace.getNamespaceRegistry().getURI(namespacePrefix);
        }
        catch (NamespaceException e) {
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage());
            }
            log.info("Registering prefix [{}] with URI {}", namespacePrefix, uri);
            workspace.getNamespaceRegistry().registerNamespace(namespacePrefix, uri);
        }
    }

    /**
     * @see info.magnolia.repository.Provider#unregisterNamespace(java.lang.String, javax.jcr.Workspace)
     */
    @Override
    public void unregisterNamespace(String prefix, Workspace workspace) throws RepositoryException {
        workspace.getNamespaceRegistry().unregisterNamespace(prefix);
    }

    /**
     * @see info.magnolia.repository.Provider#registerNodeTypes(String)
     */
    @Override
    public void registerNodeTypes() throws RepositoryException {
        registerNodeTypes(StringUtils.EMPTY);
    }

    /**
     * @see info.magnolia.repository.Provider#registerNodeTypes(java.lang.String)
     */
    @Override
    public void registerNodeTypes(String configuration) throws RepositoryException {
        if (StringUtils.isEmpty(configuration)) {
            configuration = this.repositoryMapping.getParameters().get(CUSTOM_NODETYPES);
        }

        InputStream xml = getNodeTypeDefinition(configuration);
        this.registerNodeTypes(xml);
    }

    /**
     * @see info.magnolia.repository.Provider#registerNodeTypes(java.io.InputStream)
     */
    @Override
    public void registerNodeTypes(InputStream xmlStream) throws RepositoryException {
        SimpleCredentials credentials = new SimpleCredentials(
            ContentRepository.REPOSITORY_USER,
            ContentRepository.REPOSITORY_PSWD.toCharArray());
        Session jcrSession = this.repository.login(credentials);

        try {

            Workspace workspace = jcrSession.getWorkspace();

            // should never happen
            if (xmlStream == null) {
                throw new MissingNodetypesException();
            }

            // Use Objects so that it works both with jackrabbit 1.x (NodeTypeDef) and jackrabbit 2
            // (QNodeTypeDefinition)
            Object[] types;

            try {
                types = (Object[]) NodeTypeReader.class.getMethod("read", new Class[]{InputStream.class}).invoke(
                    null,
                    new Object[]{xmlStream});
            }
            catch (Exception e) {
                throw new RepositoryException(e.getMessage(), e);
            }
            finally {
                IOUtils.closeQuietly(xmlStream);
            }

            NodeTypeManager ntMgr = workspace.getNodeTypeManager();
            NodeTypeRegistry ntReg;
            try {
                ntReg = ((NodeTypeManagerImpl) ntMgr).getNodeTypeRegistry();
            }
            catch (ClassCastException e) {
                // this could happen if the repository provider does not have proper Shared API for the
                // application server like at the moment in Jackrabbit
                log.debug("Failed to get NodeTypeRegistry: ", e);
                return;
            }

            for (int j = 0; j < types.length; j++) {
                Object def = types[j];

                Name ntname;
                try {
                    ntname = (Name) PropertyUtils.getProperty(def, "name");
                }
                catch (Exception e) {
                    throw new RepositoryException(e.getMessage(), e);
                }

                try {

                    // return value has changed in jackrabbit 2, we still have to use reflection here
                    // ntReg.getNodeTypeDef(ntname);

                    Method method = ntReg.getClass().getMethod("getNodeTypeDef", Name.class);
                    method.invoke(ntReg, ntname);
                }
                catch (IllegalArgumentException e)
                {
                    throw new RepositoryException(e.getMessage(), e);
                }
                catch (IllegalAccessException e)
                {
                    throw new RepositoryException(e.getMessage(), e);
                }
                catch (SecurityException e)
                {
                    throw new RepositoryException(e.getMessage(), e);
                }
                catch (NoSuchMethodException e)
                {
                    throw new RepositoryException(e.getMessage(), e);
                }
                catch (InvocationTargetException ite)
                {
                    if (ite.getTargetException() instanceof NoSuchNodeTypeException)
                    {
                        log.info("Registering nodetype {} on repository {}", ntname, repositoryMapping.getName());

                        try
                        {
                            // reflection for jackrabbit 1+2 compatibility
                            getMethod(NodeTypeRegistry.class, "registerNodeType").invoke(ntReg, new Object[]{def });
                        }
                        catch (Exception e)
                        {
                            throw new RepositoryException(e.getMessage(), e);
                        }
                    }
                }
            }

        }
        finally {
            jcrSession.logout();
        }
    }

    private Method getMethod(Class theclass, String methodName) throws NoSuchMethodException {
        Method[] declaredMethods = theclass.getDeclaredMethods();

        for (Method method : declaredMethods) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }

        throw new NoSuchMethodException(theclass.getName() + "." + methodName + "()");
    }

    /**
     * @param configuration
     * @return InputStream of node type definition file
     */
    private InputStream getNodeTypeDefinition(String configuration) {

        InputStream xml;

        if (StringUtils.isNotEmpty(configuration)) {

            // 1: try to load the configured file from the classpath
            xml = getClass().getResourceAsStream(configuration);
            if (xml != null) {
                log.info("Custom node types registered using {}", configuration);
                return xml;
            }

            // 2: try to load it from the file system
            File nodeTypeDefinition = new File(Path.getAbsoluteFileSystemPath(configuration));
            if (nodeTypeDefinition.exists()) {
                try {
                    return new FileInputStream(nodeTypeDefinition);
                }
                catch (FileNotFoundException e) {
                    // should never happen
                    log.error("File not found: {}", configuration);
                }
            }

            // 3: defaults to standard nodetypes
            log.error("Unable to find node type definition: {} for repository {}", configuration, this.repositoryMapping.getName());
        }

        // initialize default magnolia nodetypes
        xml = getClass().getResourceAsStream(MGNL_NODETYPES);

        return xml;
    }

    /**
     * WORKAROUND for tomcat 5.0/jdk 1.5 problem tomcat\common\endorsed contains an xml-apis.jar needed by tomcat and
     * loaded before all xmsl stuff present in the jdk (1.4 naming problem). In the xml-apis.jar file the
     * TransformerFactoryImpl is set to "org.apache.xalan.processor.TransformerFactoryImpl" instead of
     * "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl". solution: remove the file xml-apis.jar
     * from the directory OR manually change the javax.xml.transform.TransformerFactory system property
     */
    protected void checkXmlSettings() {
        if (SystemUtils.isJavaVersionAtLeast(1.5f)
            && "org.apache.xalan.processor.TransformerFactoryImpl".equals(System
                .getProperty("javax.xml.transform.TransformerFactory"))) {

            String transformerClass = "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl";

            try {
                Class.forName(transformerClass);

                System.setProperty("javax.xml.transform.TransformerFactory", transformerClass);

                log.info("Java 1.5 detected, setting system property \"javax.xml.transform.TransformerFactory\" to \"{}\"", transformerClass);
            }
            catch (Throwable e) {
                // not in the classpath. We can't assume which one to use, so just go on
            }
        }
    }

    /**
     * Checks if all workspaces are present according to the repository mapping, creates any missing workspace.
     */
    private void validateWorkspaces() throws RepositoryException {
        Iterator<String> configuredNames = repositoryMapping.getWorkspaces().iterator();
        while (configuredNames.hasNext()) {
            registerWorkspace(configuredNames.next());
        }
    }

    /**
     * @see info.magnolia.repository.Provider#registerWorkspace(java.lang.String)
     */
    @Override
    public boolean registerWorkspace(String workspaceName) throws RepositoryException {
        // check if workspace already exists
        SimpleCredentials credentials = new SimpleCredentials(
            ContentRepository.REPOSITORY_USER,
            ContentRepository.REPOSITORY_PSWD.toCharArray());
        Session jcrSession = this.repository.login(credentials);

        try {
            WorkspaceImpl defaultWorkspace = (WorkspaceImpl) jcrSession.getWorkspace();
            String[] workspaceNames = defaultWorkspace.getAccessibleWorkspaceNames();

            boolean alreadyExists = ArrayUtils.contains(workspaceNames, workspaceName);
            if (!alreadyExists) {
                defaultWorkspace.createWorkspace(workspaceName);
            }
            jcrSession.logout();

            return !alreadyExists;
        } catch (ClassCastException e) {
            // this could happen if the repository provider does not have proper Shared API for the
            // application server like at the moment in Jackrabbit
            log.debug("Unable to register workspace, will continue", e);
        } catch (Throwable t) {
            log.error("Unable to register workspace, will continue", t);
        }
        return false;
    }

    public Session getSystemSession(String workspaceName) throws RepositoryException {

        // FIXME: stop using SystemProperty, but IoC is not ready yet when this is called (config loader calls repo.init() which results in authentication calls being made and this method being invoked

        String user = SystemProperty.getProperty("magnolia.connection.jcr.admin.userId", SystemProperty.getProperty("magnolia.connection.jcr.userId", "admin"));
        String pwd = SystemProperty.getProperty("magnolia.connection.jcr.admin.password", SystemProperty.getProperty("magnolia.connection.jcr.password", "admin"));
        return this.repository.login(new SimpleCredentials(user, pwd.toCharArray()), workspaceName);
    }

}
