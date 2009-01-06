/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.jackrabbit;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Path;
import info.magnolia.repository.Provider;
import info.magnolia.repository.RepositoryMapping;
import info.magnolia.repository.RepositoryNotInitializedException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;


/**
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class ProviderImpl implements Provider {
    private static final Logger log = LoggerFactory.getLogger(ProviderImpl.class);

    private static final String CONFIG_FILENAME_KEY = "configFile"; //$NON-NLS-1$

    private static final String REPOSITORY_HOME_KEY = "repositoryHome"; //$NON-NLS-1$

    private static final String CONTEXT_FACTORY_CLASS_KEY = "contextFactoryClass"; //$NON-NLS-1$

    private static final String PROVIDER_URL_KEY = "providerURL"; //$NON-NLS-1$

    private static final String BIND_NAME_KEY = "bindName"; //$NON-NLS-1$

    private static final String MGNL_NODETYPES = "/mgnl-nodetypes/magnolia-nodetypes.xml"; //$NON-NLS-1$

    private static final String CUSTOM_NODETYPES = "customNodeTypes"; //$NON-NLS-1$

    private RepositoryMapping repositoryMapping;

    private Repository repository;

    private String bindName;

    private Hashtable jndiEnv;

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
    public void init(RepositoryMapping repositoryMapping) throws RepositoryNotInitializedException {
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

        log.info("Loading repository at {} (config file: {})", repositoryHome, configFile); //$NON-NLS-1$

        bindName = (String) params.get(BIND_NAME_KEY);
        jndiEnv = new Hashtable();
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
    public Repository getUnderlineRepository() throws RepositoryNotInitializedException {
        return getUnderlyingRepository();
    }

    public Repository getUnderlyingRepository() throws RepositoryNotInitializedException {
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
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage());
            }
            log.info("Registering prefix [{}] with URI {}", namespacePrefix, uri); //$NON-NLS-1$
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
     * @see info.magnolia.repository.Provider#registerNodeTypes(String)
     */
    public void registerNodeTypes() throws RepositoryException {
        registerNodeTypes(StringUtils.EMPTY);
    }

    /**
     * @see info.magnolia.repository.Provider#registerNodeTypes(java.lang.String)
     */
    public void registerNodeTypes(String configuration) throws RepositoryException {
        if (StringUtils.isEmpty(configuration)) {
            configuration = (String) this.repositoryMapping.getParameters().get(CUSTOM_NODETYPES);
        }

        InputStream xml = getNodeTypeDefinition(configuration);
        this.registerNodeTypes(xml);
    }

    /**
     * @see info.magnolia.repository.Provider#registerNodeTypes(java.io.InputStream)
     */
    public void registerNodeTypes(InputStream xmlStream) throws RepositoryException {
        SimpleCredentials credentials = new SimpleCredentials(
            ContentRepository.REPOSITORY_USER,
            ContentRepository.REPOSITORY_PSWD.toCharArray());
        Session jcrSession = this.repository.login(credentials);
        Workspace workspace = jcrSession.getWorkspace();

        // should never happen
        if (xmlStream == null) {
            throw new MissingNodetypesException();
        }

        NodeTypeDef[] types;
        try {
            types = NodeTypeReader.read(xmlStream);
        }
        catch (InvalidNodeTypeDefException e) {
            throw new RepositoryException(e.getMessage(), e);
        }
        catch (IOException e) {
            throw new RepositoryException(e.getMessage(), e);
        }
        finally {
            IOUtils.closeQuietly(xmlStream);
        }

        NodeTypeManager ntMgr = workspace.getNodeTypeManager();
        NodeTypeRegistry ntReg;
        try {
            ntReg = ((NodeTypeManagerImpl) ntMgr).getNodeTypeRegistry();
        } catch (ClassCastException e) {
            // this could happen if the repository provider does not have proper Shared API for the
            // application server like at the moment in Jackrabbit
            log.debug("Failed to get NodeTypeRegistry: ",e);
            return;
        }

        for (int j = 0; j < types.length; j++) {
            NodeTypeDef def = types[j];

            try {
                ntReg.getNodeTypeDef(def.getName());
            }
            catch (NoSuchNodeTypeException nsne) {
                log.info("Registering nodetype {}", def.getName()); //$NON-NLS-1$

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
                    log.error("File not found: {}", xml);
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
     * checks if all workspaces are present according to the repository mapping, creates any missing workspace
     */
    private void validateWorkspaces() throws RepositoryException {
        Iterator configuredNames = repositoryMapping.getWorkspaces().iterator();
        while (configuredNames.hasNext()) {
            registerWorkspace((String) configuredNames.next());
        }
    }

    /**
     * @see info.magnolia.repository.Provider#registerWorkspace(java.lang.String)
     */
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

}
