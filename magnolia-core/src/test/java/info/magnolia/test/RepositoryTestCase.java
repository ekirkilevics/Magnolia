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
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.context.DefaultRepositoryStrategy;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemRepositoryStrategy;
import info.magnolia.repository.Provider;
import info.magnolia.test.mock.MockWebContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;

/**
 *
 * @author ashapochka
 * @version $Revision: $ ($Author: $)
 */
public abstract class RepositoryTestCase extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();

        FactoryUtil.clear();
        // TODO move that to an util
        MgnlTestCase.initDefaultImplementations();

        // create a mock web context with same repository acquiring strategy as the system context
        final MockWebContext ctx = new MockWebContext();
        ctx.setRepositoryStrategy(new SystemRepositoryStrategy(ctx));
        MgnlContext.setInstance(ctx);

        File initFile = getPropertiesFile();
        InputStream fileStream = null;
        try {
            fileStream = new FileInputStream(initFile);
            SystemProperty.getProperties().load(fileStream);
        } catch (Exception e) {
            //System.err.println(new File(".").getAbsolutePath());
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(fileStream);
        }
        try {
            ContentRepository.REPOSITORY_USER = SystemProperty.getProperty("magnolia.connection.jcr.userId");
            ContentRepository.REPOSITORY_PSWD = SystemProperty.getProperty("magnolia.connection.jcr.password");
            ContentRepository.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected File getPropertiesFile() {
        return new File("target/test-classes/test-magnolia.properties");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        SystemProperty.getProperties().clear();
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
