package info.magnolia.jackrabbit;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.ShutdownManager;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.Path;
import info.magnolia.repository.Provider;
import info.magnolia.repository.RepositoryMapping;
import info.magnolia.repository.RepositoryNotInitializedException;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.NamespaceException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeTypeManager;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.lang.SystemUtils;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.jndi.RegistryHelper;
import org.apache.jackrabbit.core.nodetype.InvalidNodeTypeDefException;
import org.apache.jackrabbit.core.nodetype.NodeTypeDef;
import org.apache.jackrabbit.core.nodetype.NodeTypeManagerImpl;
import org.apache.jackrabbit.core.nodetype.NodeTypeRegistry;
import org.apache.jackrabbit.core.nodetype.xml.NodeTypeReader;
import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class ProviderImpl implements Provider {

    protected static Logger log = Logger.getLogger(ProviderImpl.class);

    private static final String CONFIG_FILENAME_KEY = "configFile"; //$NON-NLS-1$

    private static final String REPOSITORY_HOME_KEY = "repositoryHome"; //$NON-NLS-1$

    private static final String CONTEXT_FACTORY_CLASS_KEY = "contextFactoryClass"; //$NON-NLS-1$

    private static final String PROVIDER_URL_KEY = "providerURL"; //$NON-NLS-1$

    private static final String BIND_NAME_KEY = "bindName"; //$NON-NLS-1$

    private static final String MGNL_NODETYPES = "mgnl_nodetypes.xml"; //$NON-NLS-1$

    private RepositoryMapping repositoryMapping;

    private Repository repository;

    /**
     * @see info.magnolia.repository.Provider#init(info.magnolia.repository.RepositoryMapping)
     */
    public void init(RepositoryMapping repositoryMapping) throws RepositoryNotInitializedException {
        this.repositoryMapping = repositoryMapping;
        /* connect to repository */
        Map params = this.repositoryMapping.getParameters();
        String configFile = (String) params.get(CONFIG_FILENAME_KEY);
        configFile = Path.getAbsoluteFileSystemPath(configFile);
        String repositoryHome = (String) params.get(REPOSITORY_HOME_KEY);
        repositoryHome = Path.getAbsoluteFileSystemPath(repositoryHome);

        if (log.isInfoEnabled()) {
            log.info("Loading repository at " + repositoryHome + " (config file: " + configFile + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        String contextFactoryClass = (String) params.get(CONTEXT_FACTORY_CLASS_KEY);
        String providerURL = (String) params.get(PROVIDER_URL_KEY);
        String bindName = (String) params.get(BIND_NAME_KEY);
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, contextFactoryClass);
        env.put(Context.PROVIDER_URL, providerURL);

        // WORKAROUND for tomcat 5.0/jdk 1.5 problem
        // tomcat\common\endorsed contains an xml-apis.jar needed by tomcat and loaded before all xmsl stuff present in
        // the jdk (1.4 naming problem). In the xml-apis.jar file the TransformerFactoryImpl is set to
        // "org.apache.xalan.processor.TransformerFactoryImpl" instead of
        // "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl".
        // solution: remove the file xml-apis.jar from the directory OR manually change the
        // javax.xml.transform.TransformerFactory system property

        if (SystemUtils.isJavaVersionAtLeast(1.5f)
            && !"com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl".equals(System
                .getProperty("javax.xml.transform.TransformerFactory"))) {

            log.info("Java 1.5 detected, setting system property \"javax.xml.transform.TransformerFactory\" to "
                + "\"com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl\"");
            System.setProperty(
                "javax.xml.transform.TransformerFactory",
                "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
        }

        try {
            InitialContext ctx = new InitialContext(env);
            RegistryHelper.registerRepository(ctx, bindName, configFile, repositoryHome, true);
            this.repository = (Repository) ctx.lookup(bindName);
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
                log.info("Shutting down repositories");

                Iterator repos = ContentRepository.getAllRepositoryNames();

                while (repos.hasNext()) {
                    String name = (String) repos.next();

                    try {
                        HierarchyManager hr = ContentRepository.getHierarchyManager(name);
                        Repository repo = hr.getWorkspace().getSession().getRepository();
                        ((RepositoryImpl) repo).shutdown();
                    }
                    catch (Throwable e) {
                        log.warn(MessageFormat.format("Unable to shutdown repository {0}: {1} {2}", new Object[]{
                            name,
                            e.getClass().getName(),
                            e.getMessage()})

                        );
                    }
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

        InputStream xml = getClass().getClassLoader().getResourceAsStream(MGNL_NODETYPES);

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
}
