package info.magnolia.jackrabbit;

import info.magnolia.cms.beans.config.ShutdownManager;
import info.magnolia.cms.core.Path;
import info.magnolia.repository.Provider;
import info.magnolia.repository.RepositoryMapping;
import info.magnolia.repository.RepositoryNotInitializedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
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
import javax.servlet.ServletContextEvent;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.jackrabbit.core.WorkspaceImpl;
import org.apache.jackrabbit.core.jndi.RegistryHelper;
import org.apache.jackrabbit.core.nodetype.InvalidNodeTypeDefException;
import org.apache.jackrabbit.core.nodetype.NodeTypeDef;
import org.apache.jackrabbit.core.nodetype.NodeTypeManagerImpl;
import org.apache.jackrabbit.core.nodetype.NodeTypeRegistry;
import org.apache.jackrabbit.core.nodetype.xml.NodeTypeReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * @version $Id$
 */
public final class ProviderImpl implements Provider {

    protected static Logger log = LoggerFactory.getLogger(ProviderImpl.class);

    private static final String CONFIG_FILENAME_KEY = "configFile"; //$NON-NLS-1$

    private static final String REPOSITORY_HOME_KEY = "repositoryHome"; //$NON-NLS-1$

    private static final String CONTEXT_FACTORY_CLASS_KEY = "contextFactoryClass"; //$NON-NLS-1$

    private static final String PROVIDER_URL_KEY = "providerURL"; //$NON-NLS-1$

    private static final String BIND_NAME_KEY = "bindName"; //$NON-NLS-1$

    private static final String MGNL_NODETYPES = "mgnl_nodetypes.xml"; //$NON-NLS-1$

    private static final String CUSTOM_NODETYPES = "customNodeTypes"; //$NON-NLS-1$

    private RepositoryMapping repositoryMapping;

    private Repository repository;

    private static final String REPO_HOME_PREFIX = "${repository.home}";

    private static final int REPO_HOME_SUFIX_LEN = REPO_HOME_PREFIX.length();

    private static final String sysRepositoryHome = System.getProperty("repository.home");

    private static final String sysRepositoryHomes = System.getProperty("repository.homes");

    /**
     * Find the physical path to the repository folder
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
        if (sysRepositoryHome != null && relocate)
            return sysRepositoryHome + File.separator + tmp;

        /*
         * This is apply to all repositories if the java property is set
         */
        if (sysRepositoryHomes != null)
            return sysRepositoryHomes + File.separator + tmp;

        /*
         * Return the same value as before if neither of the above applied
         */
        return Path.getAbsoluteFileSystemPath(tmp);
    }

    /**
     * @see info.magnolia.repository.Provider#init(info.magnolia.repository.RepositoryMapping)
     */
    public void init(RepositoryMapping repositoryMapping) throws RepositoryNotInitializedException {
        checkXmlSettings();

        this.repositoryMapping = repositoryMapping;
        /* connect to repository */
        Map params = this.repositoryMapping.getParameters();
        String configFile = (String) params.get(CONFIG_FILENAME_KEY);
        configFile = Path.getAbsoluteFileSystemPath(configFile);
        String repositoryHome = (String) params.get(REPOSITORY_HOME_KEY);
        repositoryHome = getRepositoryHome(repositoryHome);
        if (log.isInfoEnabled()) {
            log.info("Loading repository at " + repositoryHome + " (config file: " + configFile + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        String contextFactoryClass = (String) params.get(CONTEXT_FACTORY_CLASS_KEY);
        String providerURL = (String) params.get(PROVIDER_URL_KEY);
        final String bindName = (String) params.get(BIND_NAME_KEY);
        final Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, contextFactoryClass);
        env.put(Context.PROVIDER_URL, providerURL);

        try {
            InitialContext ctx = new InitialContext(env);
            // first try to find the existing object if any
            try {
                this.repository = (Repository) ctx.lookup(bindName);
            }
            catch (NameNotFoundException ne) {
                if (log.isDebugEnabled()) {
                    log.debug("No JNDI bound Repository found with name - "
                        + bindName
                        + " , trying to initialize a new Repository");
                }
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

        ShutdownManager.addShutdownTask(new ShutdownManager.ShutdownTask() {

            public void execute(ServletContextEvent sce) {
                log.info("Shutting down repository bind to '" + bindName + "'");

                try {
                    Context ctx = new InitialContext(env);
                    RegistryHelper.unregisterRepository(ctx, bindName);
                }
                catch (NamingException ne) {
                    log.warn(MessageFormat.format("Unable to shutdown repository {0}: {1} {2}", new Object[]{
                        bindName,
                        ne.getClass().getName(),
                        ne.getMessage()}), ne);
                }
                catch (Throwable e) {
                    log.warn(MessageFormat.format("Failed to shutdown repository {0}: {1} {2}", new Object[]{
                        bindName,
                        e.getClass().getName(),
                        e.getMessage()}), e);
                }
            }
        });

    }

    /**
     * @see info.magnolia.repository.Provider#getUnderlineRepository()
     */
    public Repository getUnderlineRepository() throws RepositoryNotInitializedException {
        if (this.repository == null) {
            throw new RepositoryNotInitializedException("Null repository"); //$NON-NLS-1$
        }
        return this.repository;
    }

    /**
     * @see info.magnolia.repository.Provider#registerNamespace(java.lang.String, java.lang.String, javax.jcr.Workspace)
     */
    public void registerNamespace(String namespacePrefix, String uri, Workspace workspace) throws RepositoryException {
        try {
            workspace.getNamespaceRegistry().getURI(namespacePrefix);
        }
        catch (NamespaceException e) {
            log.info(e.getMessage());
            log.info("registering prefix [ " + namespacePrefix + " ] with uri " + uri); //$NON-NLS-1$ //$NON-NLS-2$
            workspace.getNamespaceRegistry().registerNamespace(namespacePrefix, uri);
        }
    }

    /**
     * @see info.magnolia.repository.Provider#unregisterNamespace(java.lang.String, javax.jcr.Workspace)
     */
    public void unregisterNamespace(String prefix, Workspace workspace) throws RepositoryException {
        workspace.getNamespaceRegistry().unregisterNamespace(prefix);
    }

    /**
     * @see info.magnolia.repository.Provider#registerNodeTypes(javax.jcr.Workspace)
     */
    public void registerNodeTypes(Workspace workspace) throws RepositoryException {

        log.info("Registering node types");
        InputStream xml;
        String customNodeTypes = (String) this.repositoryMapping.getParameters().get(CUSTOM_NODETYPES);
        if (StringUtils.isEmpty(customNodeTypes)) {
            log.debug("No custom node type definition found, registering default magnolia node types");
            xml = getClass().getClassLoader().getResourceAsStream(MGNL_NODETYPES);
        }
        else {
            File nodeTypeDefinition = new File(Path.getAbsoluteFileSystemPath(customNodeTypes));
            try {
                xml = new FileInputStream(nodeTypeDefinition);
                log.info("Custom node types registered using : " + customNodeTypes);
            }
            catch (FileNotFoundException e) {
                log.error("Unable to find node type definition : " + customNodeTypes);
                // initialize default magnolia nodetypes
                xml = getClass().getClassLoader().getResourceAsStream(MGNL_NODETYPES);
            }
        }

        NodeTypeDef[] types;
        try {
            types = NodeTypeReader.read(xml);
        }
        catch (InvalidNodeTypeDefException e) {
            throw new RepositoryException(e.getMessage(), e);
        }
        catch (IOException e) {
            throw new RepositoryException(e.getMessage(), e);
        }

        NodeTypeManager ntMgr = workspace.getNodeTypeManager();
        NodeTypeRegistry ntReg = ((NodeTypeManagerImpl) ntMgr).getNodeTypeRegistry();

        for (int j = 0; j < types.length; j++) {
            NodeTypeDef def = types[j];

            try {
                ntReg.getNodeTypeDef(def.getName());
            }
            catch (NoSuchNodeTypeException nsne) {
                log.info(MessageFormat.format("registering nodetype {0}", //$NON-NLS-1$
                    new Object[]{def.getName()}));

                try {
                    ntReg.registerNodeType(def);
                }
                catch (InvalidNodeTypeDefException e) {
                    throw new RepositoryException(e.getMessage(), e);
                }
                catch (RepositoryException e) {
                    throw new RepositoryException(e.getMessage(), e);
                }
            }

        }
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

            log.info("Java 1.5 detected, setting system property \"javax.xml.transform.TransformerFactory\" to "
                + "\"com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl\"");

            System.setProperty(
                "javax.xml.transform.TransformerFactory",
                "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
        }
    }

    /**
     * checks if all workspaces are present according to the repository mapping, creates any missing workspace
     */
    private void validateWorkspaces() throws RepositoryException {
        // check if all workspaces are present
        SimpleCredentials credentials = new SimpleCredentials("admin", "admin".toCharArray());
        Session jcrSession = this.repository.login(credentials);
        WorkspaceImpl defaultWorkspace = (WorkspaceImpl) jcrSession.getWorkspace();
        String[] workspaceNames = defaultWorkspace.getAccessibleWorkspaceNames();
        Iterator configuredNames = repositoryMapping.getWorkspaces().iterator();
        while (configuredNames.hasNext()) {
            String name = (String) configuredNames.next();
            // check if its available
            boolean available = false;
            for (int index = 0; index < workspaceNames.length; index++) {
                if (name.equalsIgnoreCase(workspaceNames[index])) {
                    available = true;
                }
            }
            if (!available) {
                // create an empty workspace
                defaultWorkspace.createWorkspace(name);
            }
        }
    }

}
