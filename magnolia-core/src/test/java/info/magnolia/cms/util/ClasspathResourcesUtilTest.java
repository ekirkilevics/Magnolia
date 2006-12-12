/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
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
