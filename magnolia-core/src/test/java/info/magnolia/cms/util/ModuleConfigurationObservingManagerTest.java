/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.cms.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Test;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.jcr.util.SessionTestUtil;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockEvent;
import info.magnolia.test.mock.jcr.MockObservationManager;
import static org.mockito.Mockito.*;

/**
 * @version $Id$
 */
public class ModuleConfigurationObservingManagerTest extends MgnlTestCase {

    private CountDownLatch latch;

    @Test
    public void testObserving() throws RepositoryException, IOException, InterruptedException {

        final Session session = SessionTestUtil.createSession(""
                + "/modules/foo/components/a\n"
                + "/modules/bar/components/b\n"
                + "/modules/zed\n"
        );

        MockUtil.getSystemMockContext().addSession(ContentRepository.CONFIG, session);

        Set<String> moduleNames = new LinkedHashSet<String>();
        moduleNames.add("foo");
        moduleNames.add("bar");
        moduleNames.add("zed");
        ModuleRegistry moduleRegistry = mock(ModuleRegistry.class);
        when(moduleRegistry.getModuleNames()).thenReturn(moduleNames);

        final AtomicInteger clearCounter = new AtomicInteger(0);
        final List<String> pathsOfReloadedNodes = new ArrayList<String>();

        ModuleConfigurationObservingManager observingManager = new ModuleConfigurationObservingManager("components", moduleRegistry) {

            @Override
            protected void onClear() throws RepositoryException {
                clearCounter.incrementAndGet();
            }

            @Override
            protected void onRegister(Node node) throws RepositoryException {
                pathsOfReloadedNodes.add(node.getPath());
                if (latch != null)
                    latch.countDown();
            }
        };

        observingManager.start();

        assertEquals(1, clearCounter.get());
        assertEquals(2, pathsOfReloadedNodes.size());
        assertEquals("/modules/foo/components", pathsOfReloadedNodes.get(0));
        assertEquals("/modules/bar/components", pathsOfReloadedNodes.get(1));

        clearCounter.set(0);
        pathsOfReloadedNodes.clear();
        latch = new CountDownLatch(3);

        session.getNode("/modules/zed").addNode("components");

        MockObservationManager observationManager = (MockObservationManager) session.getWorkspace().getObservationManager();
        observationManager.fireEventToAllListeners(new MockEvent());

        // We use a count down latch with a timeout set to 1 second more than the max delay to make the test quick
        latch.await(6000, TimeUnit.MILLISECONDS);

        assertEquals(1, clearCounter.get());
        assertEquals(3, pathsOfReloadedNodes.size());
        assertEquals("/modules/foo/components", pathsOfReloadedNodes.get(0));
        assertEquals("/modules/bar/components", pathsOfReloadedNodes.get(1));
    }
}
