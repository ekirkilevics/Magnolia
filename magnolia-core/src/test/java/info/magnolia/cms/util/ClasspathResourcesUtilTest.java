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
package info.magnolia.cms.util;

import junit.framework.TestCase;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ClasspathResourcesUtilTest extends TestCase {
    public void testSanitizeUrlToFile() throws MalformedURLException {
        assertSanitizeToFile("/foo/bar/baz.jar", new URL("file:///foo/bar/baz.jar"));
        assertSanitizeToFile("/foo/bar/baz.jar", new URL("file:///foo/bar/baz.jar!/"));
        assertSanitizeToFile("/foo/bar/baz.jar", new URL("file:/foo/bar/baz.jar!/"));
        assertSanitizeToFile("//foo/bar/baz.jar", new URL("jar:file:///foo/bar/baz.jar!/"));
        assertSanitizeToFile("/foo/bar/baz.jar", new URL("jar:file:/foo/bar/baz.jar!/"));
    }

    public void testOhWindoes() throws MalformedURLException {
        URL u1 = new URL("file:/C:/tomcat 5.5.20/webapps/magnoliaAuthor/WEB-INF/lib/foo-1.2.3.jar");
        URL u2 = new URL("file:/C:/tomcat%205.5.20/webapps/magnoliaAuthor/WEB-INF/lib/foo-1.2.3.jar");
        File sanitFile1 = ClasspathResourcesUtil.sanitizeToFile(u1);
        File sanitFile2 = ClasspathResourcesUtil.sanitizeToFile(u2);
        assertEquals(sanitFile1.getAbsolutePath(),sanitFile2.getAbsolutePath());
    }

    private void assertSanitizeToFile(String expectedAbsPath, URL url) throws MalformedURLException {
        final File file = ClasspathResourcesUtil.sanitizeToFile(url);
        assertEquals(new File(expectedAbsPath).getAbsolutePath(), file.getAbsolutePath());
    }
}
