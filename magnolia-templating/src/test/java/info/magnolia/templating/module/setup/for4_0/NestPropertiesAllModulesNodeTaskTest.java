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
package info.magnolia.templating.module.setup.for4_0;

import java.util.Arrays;

import org.junit.Test;

import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.HierarchyManagerUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.AbstractMagnoliaTestCase;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.test.mock.jcr.SessionTestUtil;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test case for {@link NestPropertiesAllModulesNodeTask}.
 *
 * @version $Id$
 */
public class NestPropertiesAllModulesNodeTaskTest extends AbstractMagnoliaTestCase {

    @Test
    public void testNestProperties() throws Exception {

        // Given
        MockSession session = SessionTestUtil.createSession(RepositoryConstants.CONFIG,
                "/modules/fooModule/templates/small.excluded=123",
                "/modules/fooModule/templates/small.included=abcdef",
                "/modules/barModule/templates/large.included=baz",
                "/modules/barModule/templates/large/parameters");

        InstallContext installContext = mock(InstallContext.class);

        when(installContext.hasModulesNode()).thenReturn(true);
        when(installContext.getModulesNode()).thenReturn(ContentUtil.asContent(session.getNode("/modules")));
        when(installContext.getHierarchyManager(RepositoryConstants.CONFIG)).thenReturn(HierarchyManagerUtil.asHierarchyManager(session));

        // When
        NestPropertiesAllModulesNodeTask task = new NestPropertiesAllModulesNodeTask(
                "",
                "",
                "templates",
                Arrays.asList("excluded"),
                "parameters",
                MgnlNodeType.NT_CONTENTNODE);

        task.execute(installContext);

        // Then
        assertTrue(session.nodeExists("/modules/fooModule/templates/small/parameters"));
        assertTrue(session.getNode("/modules/fooModule/templates/small").hasProperty("excluded"));
        assertTrue(session.getNode("/modules/fooModule/templates/small").getProperty("excluded").getString().equals("123"));
        assertFalse(session.getNode("/modules/fooModule/templates/small/parameters").hasProperty("excluded"));
        assertFalse(session.getNode("/modules/fooModule/templates/small").hasProperty("included"));
        assertTrue(session.getNode("/modules/fooModule/templates/small/parameters").hasProperty("included"));
        assertTrue(session.getNode("/modules/fooModule/templates/small/parameters").getProperty("included").getString().equals("abcdef"));

        assertTrue(session.nodeExists("/modules/barModule/templates/large/parameters"));
        assertFalse(session.getNode("/modules/barModule/templates/large").hasProperty("included"));
        assertTrue(session.getNode("/modules/barModule/templates/large/parameters").hasProperty("included"));
        assertTrue(session.getNode("/modules/barModule/templates/large/parameters").getProperty("included").getString().equals("baz"));
    }
}
