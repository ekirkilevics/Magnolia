/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.SystemProperty;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.TestMagnoliaConfigurationProperties;
import junit.framework.TestCase;

import java.io.InputStream;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ContentRepositoryTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.clear();
        final InputStream in = this.getClass().getResourceAsStream("/test-magnolia.properties");
        SystemProperty.setMagnoliaConfigurationProperties(new TestMagnoliaConfigurationProperties(in));
    }

    @Override
    protected void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        // TODO - this does nothing anymore since getProperties recreates the props instance - SystemProperty.getProperties().clear();
        SystemProperty.clear();
        MgnlContext.setInstance(null);
        super.tearDown();
    }

    public void testUnknownRepositoryShouldYieldMeaningfulExceptionMessage() {
        try {
            ContentRepository.getRepository("dummy");
            fail("should have failed, since we haven't set any repository at all");
        } catch (Throwable t) {
            assertEquals("Failed to retrieve repository 'dummy' (mapped as 'dummy'). Your Magnolia instance might not have been initialized properly.", t.getMessage());
        }
    }

    public void testUnknownRepositoryShouldAlsoYieldMeaningfulExceptionMessageForRepositoryProviders() {
        try {
            ContentRepository.getRepositoryProvider("dummy");
            fail("should have failed, since we haven't set any repository at all");
        } catch (Throwable t) {
            assertEquals("Failed to retrieve repository provider 'dummy' (mapped as 'dummy'). Your Magnolia instance might not have been initialized properly.", t.getMessage());
        }
    }
}
