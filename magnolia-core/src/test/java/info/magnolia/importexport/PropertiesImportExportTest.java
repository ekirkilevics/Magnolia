/**
 * This file Copyright (c) 2003-2009 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.importexport;

import junit.framework.TestCase;

import javax.jcr.RepositoryException;
import java.io.IOException;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class PropertiesImportExportTest extends TestCase {
    private final PropertiesImportExport pie = new PropertiesImportExport();

    public void testConvertsToStringByDefault() throws IOException, RepositoryException {
        assertEquals("foo", pie.convertNodeDataStringToObject("foo"));
        assertEquals("bar", pie.convertNodeDataStringToObject("string:bar"));
    }

    public void testConvertsToWrapperType() {
        assertEquals(Boolean.TRUE, pie.convertNodeDataStringToObject("boolean:true"));
        assertEquals(Boolean.FALSE, pie.convertNodeDataStringToObject("boolean:false"));
        assertEquals(new Integer(5), pie.convertNodeDataStringToObject("integer:5"));
    }

    public void testCanUseIntShortcutForConvertingIntegers() {
        assertEquals(new Integer(37), pie.convertNodeDataStringToObject("int:37"));
    }
}
