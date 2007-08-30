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
package info.magnolia.cms.security;

import junit.framework.TestCase;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class HierarchicalUserManagerTest extends TestCase {
    private HierarchicalUserManager hm;

    protected void setUp() throws Exception {
        super.setUp();
        hm = new HierarchicalUserManager();
        hm.setName("test-realm");
    }

    public void testParentPathIsRealmIfNameShorterThan3Chars() {
        assertEquals("/test-realm", hm.getParentPath("ab"));
    }

    public void testParentPathShouldReflectFirstLettersOfNameAndIncludeRealmName() {
        assertEquals("/test-realm/c/ca", hm.getParentPath("casimir"));
    }

    public void testParentPathShouldBeLowercased() {
        assertEquals("/test-realm/c/ca", hm.getParentPath("Casimir"));
        assertEquals("/test-realm/c/ca", hm.getParentPath("CASIMIR"));
    }
}
