/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.module.templatingcomponents.jspx;

import static org.junit.Assert.assertEquals;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.module.templatingcomponents.AuthoringUiComponent;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.servlet.jsp.JspException;

import org.junit.Test;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class AbstractTagTest {
    @Test
    public void testSplitSingleString() {
        final AbstractTag tag = new TestTag();
        final List<String> res = tag.split("hello");
        assertEquals(1, res.size());
        assertEquals(Arrays.asList("hello"), res);
    }

    @Test
    public void testSplitNull() {
        final AbstractTag tag = new TestTag();
        final List<String> res = tag.split(null);
        assertEquals(0, res.size());
    }

    @Test
    public void testSplitEmptyString() {
        final AbstractTag tag = new TestTag();
        final List<String> res = tag.split("");
        assertEquals(0, res.size());
    }

    @Test
    public void testSplit() {
        final AbstractTag tag = new TestTag();
        final List<String> res = tag.split("hello,world");
        assertEquals(2, res.size());
        assertEquals(Arrays.asList("hello", "world"), res);
    }

    @Test
    public void testSplitWithEmptyElements() {
        final AbstractTag tag = new TestTag();
        final List<String> res = tag.split("hello,,world");
        assertEquals(2, res.size());
        assertEquals(Arrays.asList("hello", "world"), res);
    }

    @Test
    public void testToStringListFromStringArray() throws JspException {
        final NewBarTag tag = new NewBarTag();
        final List<String> res = tag.mandatoryStringList(new String[]{"hello", "world"}, null);
        assertEquals(2, res.size());
        assertEquals(Arrays.asList("hello", "world"), res);
    }

    @Test
    public void testToStringListFromSet() throws JspException {
        final NewBarTag tag = new NewBarTag();
        final HashSet set = new HashSet();
        set.add("hello");
        set.add("world");
        final List<String> res = tag.mandatoryStringList(set, null);
        assertEquals(2, res.size());
        assertEquals(Arrays.asList("hello", "world"), res);
    }

    @Test
    public void testToStringListFromCommaSeparatedString() throws JspException {
        final NewBarTag tag = new NewBarTag();
        final List<String> res = tag.mandatoryStringList("hello,world", null);
        assertEquals(2, res.size());
        assertEquals(Arrays.asList("hello", "world"), res);
    }


    private static class TestTag extends AbstractTag {
        @Override
        protected AuthoringUiComponent prepareUIComponent(ServerConfiguration serverCfg, AggregationState aggState) throws JspException, IOException {
            return null;
        }
    }
}
