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
package info.magnolia.ckeditor.dialog.definition;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 * @version $Id$
 *
 */
public class CKEditorDefaultConfigurationTest {

    @Test
    public void testMagnoliaToolbarReturnsDefaultIfNoExtraOptionsAreDefined() throws Exception {
        CKEditorDefaultConfiguration config = new CKEditorDefaultConfiguration();
        //as assertion probes string equality, let's first remove all white spaces in the two strings which represent javascript arrays.
        String expected = StringUtils.deleteWhitespace(CKEditorDefaultConfiguration.PARAM_MAGNOLIA_TOOLBAR_DEFAULT);
        String actual = StringUtils.deleteWhitespace(Arrays.toString(config.getToolbar_Magnolia()));
        assertEquals(expected, actual);
    }

    @Test
    public void testMagnoliaToolbarReturnsWithAlignmentOptions() throws Exception {
        CKEditorDefaultConfiguration config = new CKEditorDefaultConfiguration();
        config.setAlignment(true);
        assertTrue(Arrays.toString(config.getToolbar_Magnolia()).contains("Justify"));
    }

    @Test
    public void testDefaulMagnoliaToolbarReturnsWithListsOptions() throws Exception {
        CKEditorDefaultConfiguration config = new CKEditorDefaultConfiguration();
        assertTrue(Arrays.toString(config.getToolbar_Magnolia()).contains("BulletedList"));
    }

    @Test
    public void testMagnoliaToolbarReturnsWithTablesOptions() throws Exception {
        CKEditorDefaultConfiguration config = new CKEditorDefaultConfiguration();
        config.setTables(true);
        assertTrue(Arrays.toString(config.getToolbar_Magnolia()).contains("Table"));
    }

    @Test
    public void testMagnoliaToolbarReturnsWithImagesOptions() throws Exception {
        CKEditorDefaultConfiguration config = new CKEditorDefaultConfiguration();
        config.setImages(true);
        assertTrue(Arrays.toString(config.getToolbar_Magnolia()).contains("Image"));
    }

    @Test
    public void testMagnoliaToolbarReturnsWithSourceOptions() throws Exception {
        CKEditorDefaultConfiguration config = new CKEditorDefaultConfiguration();
        config.setSource(true);
        assertTrue(Arrays.toString(config.getToolbar_Magnolia()).contains("Source"));
    }

    @Test
    public void testMagnoliaToolbarReturnsWithSpellCheckerOptions() throws Exception {
        CKEditorDefaultConfiguration config = new CKEditorDefaultConfiguration();
        config.setSpellChecker(true);
        assertTrue(Arrays.toString(config.getToolbar_Magnolia()).contains("SpellChecker"));
    }

    /**
     * we want [['foo, "'bar']] but not [['foo', 'bar',]] as some browsers may not like the latter.
     */
    @Test
    public void testMagnoliaToolbarReturnsJavascriptArrayWithNoDanglingComma() throws Exception {
        CKEditorDefaultConfiguration config = new CKEditorDefaultConfiguration();
        config.setSource(true);
        assertTrue(Arrays.toString(config.getToolbar_Magnolia()).matches(".*\\\"\\s*]]"));
    }

    @Test
    public void testMagnoliaToolbarReturnsEnabledSource() throws Exception {
        CKEditorDefaultConfiguration config = new CKEditorDefaultConfiguration();
        config.setSource(true);
        assertTrue(Arrays.toString(config.getToolbar_Magnolia()).contains("Source"));
    }

    @Test
    public void testMagnoliaToolbarReturnsDisabledSpellChecker() throws Exception {
        CKEditorDefaultConfiguration config = new CKEditorDefaultConfiguration();
        config.setSpellChecker(false);
        assertFalse(Arrays.toString(config.getToolbar_Magnolia()).contains("SpellChecker"));
    }

}
