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
package info.magnolia.cms.core;

import java.io.File;

import junit.framework.TestCase;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class PathTest extends TestCase {

    /**
     * Test method for {@link info.magnolia.cms.core.Path#isAbsolute(java.lang.String)}.
     */
    public void testIsAbsolute() {
        assertTrue(Path.isAbsolute("/test"));
        assertTrue(Path.isAbsolute("d:/test"));
        assertTrue(Path.isAbsolute(File.separator + "test"));
        assertFalse(Path.isAbsolute("test"));
    }

}
