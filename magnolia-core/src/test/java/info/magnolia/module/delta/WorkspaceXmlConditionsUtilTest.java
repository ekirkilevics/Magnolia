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

import static org.junit.Assert.*;
import info.magnolia.cms.core.SystemProperty;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 */
public class WorkspaceXmlConditionsUtilTest {

    private List<Condition> conditions;
    private WorkspaceXmlConditionsUtil util;

    @Before
    public void setUp() {
        conditions = new ArrayList<Condition>();
        util = new WorkspaceXmlConditionsUtil(conditions);
        SystemProperty.setProperty(SystemProperty.MAGNOLIA_APP_ROOTDIR, "src/test/resources/info/magnolia/cms/util");
    }

    @Test
    public void testParamAnalyzerIsNotAround() {
        // GIVEN - all done in setup

        // WHEN
        util.paramAnalyzerIsNotSet();

        // THEN
        assertEquals(1,conditions.size());
        assertTrue(conditions.get(0) instanceof WarnCondition);
        assertTrue("Received condition was expected to be comming from the outdated config!", conditions.get(0).getDescription().contains("/outdated/workspace.xml"));
    }


    @Test
    public void testTextFilterClassesAreNotSet() {
        // GIVEN - all done in setup

        // WHEN
        util.textFilterClassesAreNotSet();

        // THEN
        assertEquals(1,conditions.size());
        assertTrue(conditions.get(0) instanceof FalseCondition);
        assertTrue("Received condition was expected to be comming from the outdated config!", conditions.get(0).getDescription().contains("/outdated/workspace.xml"));
    }

    @After
    public void tearDown() {
        SystemProperty.setProperty(SystemProperty.MAGNOLIA_APP_ROOTDIR, "");
    }
}
