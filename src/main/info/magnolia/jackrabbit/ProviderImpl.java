package info.magnolia.jackrabbit;

import info.magnolia.repository.Provider;
import info.magnolia.repository.RepositoryMapping;
import info.magnolia.repository.RepositoryNotInitializedException;
import java.util.Hashtable;
import java.util.Map;
import javax.jcr.NamespaceException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Workspace;
import javax.naming.Context;
import javax.naming.InitialContext;
import org.apache.jackrabbit.core.jndi.RegistryHelper;
import org.apache.log4j.Logger;


/**
 * Date: Nov 25, 2004 Time: 4:57:02 PM
 * @author Sameer Charles
 * @version 2.1
 */
public class ProviderImpl implements Provider {

    private static Logger log = Logger.getLogger(ProviderImpl.class);

    private static final String CONFIG_FILENAME_KEY = "configFile";

    private static final String REPOSITORY_HOME_KEY = "repositoryHome";

    private static final String CONTEXT_FACTORY_CLASS_KEY = "contextFactoryClass";

    private static final String PROVIDER_URL_KEY = "providerURL";

    private static final String REPOSITORY_ID_KEY = "id";

    private RepositoryMapping repositoryMapping;

    private Repository repository;

    public ProviderImpl() {
    }

    public void init(RepositoryMapping repositoryMapping) throws RepositoryNotInitializedException {
        this.repositoryMapping = repositoryMapping;
        /* connect to repository */
        Map params = this.repositoryMapping.getParameters();
        String configFile = (String) params.get(CONFIG_FILENAME_KEY);
        String repositoryHome = (String) params.get(REPOSITORY_HOME_KEY);
        String contextFactoryClass = (String) params.get(CONTEXT_FACTORY_CLASS_KEY);
        String providerURL = (String) params.get(PROVIDER_URL_KEY);
        String repositoryID = (String) params.get(REPOSITORY_ID_KEY);
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, contextFactoryClass);
        env.put(Context.PROVIDER_URL, providerURL);
        try {
            InitialContext ctx = new InitialContext(env);
            RegistryHelper.registerRepository(ctx, repositoryID, configFile, repositoryHome, true);
            this.repository = (Repository) ctx.lookup(repositoryID);
        }
        catch (Exception e) {
            throw new RepositoryNotInitializedException(e);
        }
    }

    public Repository getUnderlineRepository() throws RepositoryNotInitializedException {
        if (this.repository == null)
            throw new RepositoryNotInitializedException("Null repository");
        return this.repository;
    }

    public void registerNamespace(String namespacePrefix, String uri, Workspace workspace) throws RepositoryException {
        try {
            workspace.getNamespaceRegistry().getURI(namespacePrefix);
        }
        catch (NamespaceException e) {
            log.info(e.getMessage());
            log.info("registering prefix [ " + namespacePrefix + " ] with uri " + uri);
            workspace.getNamespaceRegistry().registerNamespace(namespacePrefix, uri);
        }
    }

    public void unregisterNamespace(String prefix, Workspace workspace) throws RepositoryException {
        workspace.getNamespaceRegistry().unregisterNamespace(prefix);
    }

    public void registerNodeType(Map definition) throws RepositoryException {
        // todo , dynamic nodetype registry.. for now use custom_nodetypes.xml
    }
}
