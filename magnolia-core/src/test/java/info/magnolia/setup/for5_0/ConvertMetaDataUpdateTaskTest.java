/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.setup.for5_0;

import java.io.IOException;
import java.util.Arrays;
import javax.jcr.RepositoryException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.repository.RepositoryManager;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.jcr.MockSession;

/**
 * Test case for {@link ConvertMetaDataUpdateTask}.
 */
public class ConvertMetaDataUpdateTaskTest {

    private RepositoryManager repositoryManager;
    private InstallContext installContext;

    @Before
    public void setUp() throws RepositoryException, IOException {
        repositoryManager = mock(RepositoryManager.class);
        ComponentsTestUtil.setInstance(RepositoryManager.class, repositoryManager);
        when(repositoryManager.getWorkspaceNames()).thenReturn(Arrays.asList(RepositoryConstants.WEBSITE, RepositoryConstants.CONFIG));

        installContext = mock(InstallContext.class);
        MockSession websiteSession = new MockSession(RepositoryConstants.WEBSITE);
        MockSession configSession = new MockSession(RepositoryConstants.CONFIG);
        when(installContext.getJCRSession(eq(RepositoryConstants.WEBSITE))).thenReturn(websiteSession);
        when(installContext.getJCRSession(eq(RepositoryConstants.CONFIG))).thenReturn(configSession);
    }

    @After
    public void tearDown() {
        ComponentsTestUtil.clear();
    }

    @Test
    public void testConvertsAllWorkspaces() throws TaskExecutionException, RepositoryException {
        ConvertMetaDataUpdateTask task = new ConvertMetaDataUpdateTask("", "");
        task.execute(installContext);

        verify(repositoryManager).getWorkspaceNames();
        verify(installContext).getJCRSession(eq(RepositoryConstants.CONFIG));
        verify(installContext).getJCRSession(eq(RepositoryConstants.WEBSITE));
    }
}
