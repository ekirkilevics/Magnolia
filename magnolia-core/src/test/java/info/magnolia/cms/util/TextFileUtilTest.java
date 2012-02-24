/**
 * This file Copyright (c) 2007-2011 Magnolia International
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

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.util.List;

import org.junit.Test;

/**
 * @version $Id$
 */
public class TextFileUtilTest {

    @Test
    public void testGetLines() {
        final List<String> result = TextFileUtil.getLines("src/test/resources/config/current-jaas.config");
        assertEquals(17, result.size());
    }

    @Test
    public void testGetTrimmedLinesMatching() {
        final List<String> result = TextFileUtil.getTrimmedLinesMatching("src/test/resources/config/outdated-jaas.config", "^Jackrabbit.*");
        assertEquals(1, result.size());
    }

    @Test
    public void testGetTrimmedLinesMatchingWhenExpressionIsNotContained() {
        final List<String> result = TextFileUtil.getTrimmedLinesMatching("src/test/resources/config/current-jaas.config", "^Jackrabbit.*");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetLinesWhenFileIsNotExisiting() {
        try {
            TextFileUtil.getLines("no/such/file.around");
            fail("Should have thrown an exception!");
        } catch (Throwable t) {
            assertTrue(t.getCause() instanceof FileNotFoundException);
        }
    }
}
