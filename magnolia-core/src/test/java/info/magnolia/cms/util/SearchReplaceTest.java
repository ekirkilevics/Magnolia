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

import info.magnolia.cms.core.Content;
import info.magnolia.test.mock.MockUtil;
import junit.framework.TestCase;

import javax.jcr.RepositoryException;
import java.util.regex.Pattern;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class SearchReplaceTest extends TestCase {
    private Content root;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        root = MockUtil.createNode("/",
                "/top@type=mgnl:contentNode",
                "/top.text=maGnolia is Great",
                "/top.chalala=maGnolia is Great",
                "/top/plop.text=one flower",
                "/top/plep.text=two Flowers",
                "/top/plip.text=three flowers",
                "/top/plip.other=three flowers",
                "/top/plip.title=three flowers",
                "/top/plop/bleh.text=two",
                "/top/other.text=magnolia rules",
                "/top/other2.text=the rules of magnoliA",
                "/top/other3.text=the rules of MAGNOLIA are the rules",
                "/top/blah.text=This property contains an [ open bracket.",
                "/top/bloh.text=This property contains an { open bracketZ",
                "/top/bluh.regextest=This property contains what looks like [a-z] regex class"
        );
    }

    public void testCaseInsensitive() throws Exception {
        assertEquals("maGnolia is Great", prop("/top/text"));
        assertEquals("maGnolia is Great", prop("/top/chalala"));
        assertEquals("magnolia rules", prop("/top/other/text"));
        assertEquals("the rules of magnoliA", prop("/top/other2/text"));
        assertEquals("the rules of MAGNOLIA are the rules", prop("/top/other3/text"));

        ContentUtil.visit(root, new SearchReplace("text", "magnolia", "Magnolia", Pattern.CASE_INSENSITIVE));

        assertEquals("Magnolia is Great", prop("/top/text"));
        // chalala does not match the property name pattern, so it should not have been changed
        assertEquals("maGnolia is Great", prop("/top/chalala"));
        assertEquals("Magnolia rules", prop("/top/other/text"));
        assertEquals("the rules of Magnolia", prop("/top/other2/text"));
        assertEquals("the rules of Magnolia are the rules", prop("/top/other3/text"));
    }

    public void testCanUseNamePatterns() throws Exception {
        assertEquals("three flowers", prop("/top/plip/text"));
        assertEquals("three flowers", prop("/top/plip/other"));
        assertEquals("three flowers", prop("/top/plip/title"));

        ContentUtil.visit(root, new SearchReplace("other|text", "flower", "Magnolia", Pattern.CASE_INSENSITIVE));

        assertEquals("three Magnolias", prop("/top/plip/text"));
        assertEquals("three Magnolias", prop("/top/plip/other"));
        // "title" does not match the property name pattern, so it should not have been modified
        assertEquals("three flowers", prop("/top/plip/title"));
    }

    public void testCanUseJokerInNamePatterns() throws Exception {
        assertEquals("three flowers", prop("/top/plip/text"));
        assertEquals("three flowers", prop("/top/plip/other"));
        assertEquals("three flowers", prop("/top/plip/title"));

        ContentUtil.visit(root, new SearchReplace("t*", "flower", "Magnolia", Pattern.CASE_INSENSITIVE));

        assertEquals("three Magnolias", prop("/top/plip/text"));
        assertEquals("three Magnolias", prop("/top/plip/title"));
        // "other" does not match the property name pattern, so it should not have been modified
        assertEquals("three flowers", prop("/top/plip/other"));
    }

    public void testDefaultsToLiteralMatching() throws Exception {
        assertEquals("This property contains an [ open bracket.", prop("/top/blah/text"));
        assertEquals("This property contains an { open bracketZ", prop("/top/bloh/text"));
        assertEquals("This property contains what looks like [a-z] regex class", prop("/top/bluh/regextest"));

        ContentUtil.visit(root, new SearchReplace("text", "[", "("));
        ContentUtil.visit(root, new SearchReplace("text", "bracket.", "parenthesis."));
        ContentUtil.visit(root, new SearchReplace("regextest", "[a-z]", "a"));

        assertEquals("This property contains an ( open parenthesis.", prop("/top/blah/text"));
        assertEquals("This property contains an { open bracketZ", prop("/top/bloh/text"));
        // if we were not default to Literal, the result would be: Taaa aaaaaaaa aaaaaaaa aaaa aaaaa aaaa [a-a] aaaaa aaaaa
        assertEquals("This property contains what looks like a regex class", prop("/top/bluh/regextest"));
    }

    /**
     * Commodity method to check a property's string value
     */
    private String prop(String propertyPath) throws RepositoryException {
        final int slash = propertyPath.lastIndexOf('/');
        final String nodePath = propertyPath.substring(0, slash);
        final String propName = propertyPath.substring(slash + 1);
        return root.getContent(nodePath).getNodeData(propName).getString();
    }
}
