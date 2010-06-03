/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.test;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.util.ClasspathResourcesUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.context.SystemRepositoryStrategy;
import info.magnolia.importexport.BootstrapUtil;
import info.magnolia.test.mock.MockContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.jndi.BindableRepositoryFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.jcr.ImportUUIDBehavior;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author ashapochka
 * @version $Revision: $ ($Author: $)
 */
public abstract class RepositoryTestCase extends MgnlTestCase {

    protected static final String REPO_CONF_PROPERTY = "magnolia.repositories.config";
    protected static final String JACKRABBIT_REPO_CONF_PROPERTY = "magnolia.repositories.jackrabbit.config";
    protected static final String EXTRACTED_REPO_CONF_FILE = "target/repositories.xml";
    protected static final String EXTRACTED_JACKRABBIT_REPO_CONF_FILE = "target/repo-conf/extracted.xml";

    private boolean autoStart = true;

    private String repositoryConfigFileName;

    private String jackrabbitRepositoryConfigFileName;

    private boolean quiet = true;

    protected void setUp() throws Exception {
        super.setUp();

        workaroundJCR1778();

        if (isAutoStart()) {
            cleanUp();
            startRepository();
        }
    }

    /**
     * @deprecated - workaround until JCR-1778 is fixed
     * @see <a href="https://issues.apache.org/jira/browse/JCR-1778">JCR-1778</a>
     */
    static void workaroundJCR1778() throws NoSuchFieldException, IllegalAccessException {
        final Field cacheField = BindableRepositoryFactory.class.getDeclaredField("cache");
        cacheField.setAccessible(true);
        final Map cache = (Map) cacheField.get(null);
        cache.clear();
    }

    protected void modifyContextesToUseRealRepository() {
        // create a mock web context with same repository acquiring strategy as the system context
        SystemContext systemContext = (SystemContext) MgnlContext.getSystemContext();
        SystemRepositoryStrategy repositoryStrategy = new SystemRepositoryStrategy(systemContext);

        //update the mock context
        ((MockContext) systemContext).setRepositoryStrategy(repositoryStrategy);
        ((MockContext) MgnlContext.getInstance()).setRepositoryStrategy(repositoryStrategy);
    }

    protected void startRepository() throws Exception {
        final Logger logger = Logger.getLogger("info.magnolia");
        final Level originalLogLevel = logger.getLevel();
        if (this.isQuiet()) {
            logger.setLevel(Level.WARN);
        }
        ContentRepository.REPOSITORY_USER = SystemProperty.getProperty("magnolia.connection.jcr.userId");
        ContentRepository.REPOSITORY_PSWD = SystemProperty.getProperty("magnolia.connection.jcr.password");

        extractConfigFile(REPO_CONF_PROPERTY, getRepositoryConfigFileStream(), EXTRACTED_REPO_CONF_FILE);
        extractConfigFile(JACKRABBIT_REPO_CONF_PROPERTY, getJackrabbitRepositoryConfigFileStream(), EXTRACTED_JACKRABBIT_REPO_CONF_FILE);

        ContentRepository.init();

        modifyContextesToUseRealRepository();

        logger.setLevel(originalLogLevel);
    }

    protected void extractConfigFile(String propertyName, InputStream configFileStream, String extractToPath) throws Exception {
        String targetFilename = Path.getAbsoluteFileSystemPath(extractToPath);
        File targetFile = new File(targetFilename);
        // extract resource to the filesystem (jackrabbit can't use a stream)
        new File(targetFile.getParent()).mkdirs();
        IOUtils.copy(configFileStream, new FileOutputStream(targetFile));
        SystemProperty.setProperty(propertyName, extractToPath);
    }

    protected InputStream getRepositoryConfigFileStream() throws Exception {
        String configFile = getRepositoryConfigFileName();
        return ClasspathResourcesUtil.getResource(configFile).openStream();
    }

    protected InputStream getJackrabbitRepositoryConfigFileStream() throws Exception {
        String configFile = getJackrabbitRepositoryConfigFileName();
        return ClasspathResourcesUtil.getResource(configFile).openStream();
    }

    protected String getRepositoryConfigFileName() {
        if (StringUtils.isEmpty(repositoryConfigFileName)) {
            repositoryConfigFileName = SystemProperty.getProperty(REPO_CONF_PROPERTY);
        }
        return repositoryConfigFileName;
    }

    protected String getJackrabbitRepositoryConfigFileName() {
        if (StringUtils.isEmpty(jackrabbitRepositoryConfigFileName)) {
            jackrabbitRepositoryConfigFileName = SystemProperty.getProperty(JACKRABBIT_REPO_CONF_PROPERTY);
        }
        return jackrabbitRepositoryConfigFileName;
    }

    protected void tearDown() throws Exception {
        if (isAutoStart()) {
            shutdownRepository(true);
        }
        SystemProperty.getProperties().clear();
        super.tearDown();
    }

    protected void shutdownRepository(boolean cleanup) throws IOException {
        final Logger logger = Logger.getLogger("info.magnolia");
        final Level originalLogLevel = logger.getLevel();
        if (this.isQuiet()) {
            logger.setLevel(Level.WARN);
        }
        MgnlContext.release();
        MgnlContext.getSystemContext().release();
        ContentRepository.shutdown();
        if (cleanup) {
            cleanUp();
        }
        logger.setLevel(originalLogLevel);
    }

    protected void cleanUp() throws IOException {
        FileUtils.deleteDirectory(new File(SystemProperty.getProperty("magnolia.repositories.home")));
    }

    protected void bootstrapSingleResource(String resource) throws Exception {
        BootstrapUtil.bootstrap(new String[]{resource}, ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
    }

    protected void bootstrap(ClasspathResourcesUtil.Filter filter) throws Exception {
        String[] resourcesToBootstrap = ClasspathResourcesUtil.findResources(filter);
        BootstrapUtil.bootstrap(resourcesToBootstrap, ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
    }

    protected boolean isAutoStart() {
        return this.autoStart;
    }

    protected void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    protected void setRepositoryConfigFileName(String repositoryConfigFileName) {
        this.repositoryConfigFileName = repositoryConfigFileName;
    }

    protected void setJackrabbitRepositoryConfigFileName(String jackrabbitRepositoryConfigFileName) {
        this.jackrabbitRepositoryConfigFileName = jackrabbitRepositoryConfigFileName;
    }

    protected boolean isQuiet() {
        return this.quiet;
    }

    protected void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }
}
