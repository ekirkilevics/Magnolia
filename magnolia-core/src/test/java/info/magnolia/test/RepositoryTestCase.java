/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
package info.magnolia.test;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.module.ModuleUtil;
import info.magnolia.cms.util.ClasspathResourcesUtil;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemRepositoryStrategy;
import info.magnolia.repository.Provider;
import info.magnolia.test.mock.MockWebContext;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;

import javax.jcr.ImportUUIDBehavior;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author ashapochka
 * @version $Revision: $ ($Author: $)
 */
public abstract class RepositoryTestCase extends TestCase {

    private static final String JACKRABBIT_REPO_CONF_PROPERTY = "magnolia.repositories.jackrabbit.config";
    private static final String EXTRACTED_REPO_CONF_FILE = "target/repo-conf/extracted.xml";

    protected void setUp() throws Exception {
        super.setUp();

        final Logger logger = Logger.getLogger("info.magnolia");
        final Level originalLogLevel = logger.getLevel();
        logger.setLevel(Level.WARN);

        FactoryUtil.clear();
        // TODO move that to an util
        MgnlTestCase.initDefaultImplementations();

        // create a mock web context with same repository acquiring strategy as the system context
        final MockWebContext ctx = new MockWebContext();
        ctx.setRepositoryStrategy(new SystemRepositoryStrategy(ctx));
        MgnlContext.setInstance(ctx);

        InputStream fileStream = null;
        try {
            fileStream = getPropertiesStream();
            SystemProperty.getProperties().load(fileStream);
        } finally {
            IOUtils.closeQuietly(fileStream);
        }

        ContentRepository.REPOSITORY_USER = SystemProperty.getProperty("magnolia.connection.jcr.userId");
        ContentRepository.REPOSITORY_PSWD = SystemProperty.getProperty("magnolia.connection.jcr.password");
        // extract resource to the filesystem (jackrabbit can't use a stream)
        String configFile = SystemProperty.getProperty(JACKRABBIT_REPO_CONF_PROPERTY);
        String targetFilename = Path.getAbsoluteFileSystemPath(EXTRACTED_REPO_CONF_FILE);
        File targetFile = new File(targetFilename);
        if(!targetFile.exists()){
            URL configFileURL = ClasspathResourcesUtil.getResource(configFile);
            FileUtils.copyURLToFile(configFileURL, targetFile);
        }
        SystemProperty.setProperty(JACKRABBIT_REPO_CONF_PROPERTY, EXTRACTED_REPO_CONF_FILE);
        ContentRepository.init();

        logger.setLevel(originalLogLevel);
    }

    protected InputStream getPropertiesStream() {
        return this.getClass().getResourceAsStream("/test-magnolia.properties");
    }

    protected void tearDown() throws Exception {
        super.tearDown();

        final Logger logger = Logger.getLogger("info.magnolia");
        final Level originalLogLevel = logger.getLevel();
        logger.setLevel(Level.WARN);

        Iterator i = ContentRepository.getAllRepositoryNames();
        HashSet ids = new HashSet();
        while (i.hasNext()) {
            String repositoryName = i.next().toString();
            String repositoryId = ContentRepository.getMappedRepositoryName(repositoryName);
            if (ids.contains(repositoryId)) continue;
            ids.add(repositoryId);
            Provider provider = ContentRepository.getRepositoryProvider(repositoryId);
            provider.shutdownRepository();
        }
        FileUtils.deleteDirectory(new File(SystemProperty.getProperty("magnolia.repositories.home")));

        SystemProperty.getProperties().clear();
        logger.setLevel(originalLogLevel);
    }

    protected void bootstrapSingleResource(String resource) throws Exception{
        ModuleUtil.bootstrap(new String[]{resource}, false, ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
    }

    protected void bootstrap(ClasspathResourcesUtil.Filter filter) throws Exception{
        String[] resourcesToBootstrap = ClasspathResourcesUtil.findResources(filter);
        ModuleUtil.bootstrap(resourcesToBootstrap, false, ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
    }
}
