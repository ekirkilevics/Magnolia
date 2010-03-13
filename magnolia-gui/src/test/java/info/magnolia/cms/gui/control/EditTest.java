/**
 * This file Copyright (c) 2009-2010 Magnolia International
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
package info.magnolia.cms.gui.control;

import junit.framework.TestCase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class EditTest extends TestCase {
    public void testSingleLineEditAndTextAreaAreBothEncoded() {
        final Edit edit = new Edit(null, "m<n&copy;");
        edit.setRows("1");
        final String inputValue = extractInputValue(edit);

        final Edit textarea = new Edit(null, "m<n&copy;");
        textarea.setRows("2");
        final String txtAreaValue = extractTextAreaValue(textarea);

        assertEquals(inputValue, txtAreaValue);
        assertEquals("m&lt;n&amp;copy;", inputValue);
    }

    public void testAngularBracketsAreEncoded() {
        final Edit edit = new Edit(null, "a<b>c");
        assertEquals("a&lt;b&gt;c", extractInputValue(edit));
    }

    /**
     * If this wasn't the case, the entity would be rendered as-is in the form
     * (i.e. as an accented letter), thus losing the entity-encoding in further
     * save operations.
     */
    public void testHtmlEntitiesAreDoubleEncoded() {
        final Edit edit = new Edit(null, "th&eacute;");
        assertEquals("th&amp;eacute;", extractInputValue(edit));
    }

    private String extractTextAreaValue(Edit edit) {
        return extractValue(edit, "<textarea.*?>(.*?)</textarea>");
    }

    private String extractInputValue(Edit edit) {
        return extractValue(edit, ".*?value=\"(.*?)\".*");
    }

    private String extractValue(Edit edit, String regex) {
        edit.setSaveInfo(false);
        final String html = edit.getHtml();
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(html);
        assertTrue(matcher.matches());
        return matcher.group(1);
    }
}
