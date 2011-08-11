/**
 * This file Copyright (c) 2009-2011 Magnolia International
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
package info.magnolia.module.delta;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import info.magnolia.cms.util.WebXmlUtil;
import info.magnolia.module.InstallContext;

import java.util.ArrayList;

import org.junit.Test;
/**
 * @version $Id$
 */
public class WebXmlConditionsUtilTest {

    @Test
    public void testDoesNotWarnIfErrorDispatcherIsUsed() {
        doTestFilterDispatchersConditions("web_filterwrongdispatchers.xml", "webxmltest.ErrorIsNotMandatory", false, true);
    }

    @Test
    public void testBlocksIfRequestDispatcherIsMissing() {
        doTestFilterDispatchersConditions("web_filterwrongdispatchers.xml", "webxmltest.WithMissingRequst", false, false);
    }

    @Test
    public void testBlocksIfIncludeDispatcherIsMissing() {
        doTestFilterDispatchersConditions("web_filterwrongdispatchers.xml", "webxmltest.WithMissingInclude", false, false);
    }

    private void doTestFilterDispatchersConditions(String webxmlResource, String fakeFilterClass, boolean shouldWarn, boolean expectedResult) {
        final ArrayList conditions = new ArrayList();
        final WebXmlUtil webxml = new WebXmlUtil(WebXmlUtil.class.getResourceAsStream(webxmlResource));
        final WebXmlConditionsUtil u = new WebXmlConditionsUtil(webxml, conditions);
        u.filterMustBeRegisteredWithCorrectDispatchers(fakeFilterClass);

        assertEquals(1, conditions.size());
        final Condition condition = (Condition) conditions.get(0);

        final InstallContext installContext = createStrictMock(InstallContext.class);
        if (shouldWarn) {
            installContext.warn(isA(String.class));
        }
        replay(installContext);
        assertEquals(expectedResult, condition.check(installContext));
        verify(installContext);
    }
}
