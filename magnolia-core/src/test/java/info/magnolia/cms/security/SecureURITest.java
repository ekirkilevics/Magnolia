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
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class SecureURITest extends TestCase {

    public void testUnsecureNoWildcards() {
        SecureURI.init();
        SecureURI.addUnsecure("/home/x/test.htm");
        assertTrue(SecureURI.isUnsecure("/home/x/test.htm"));
        assertFalse(SecureURI.isUnsecure("/home/x/test.html"));
        assertFalse(SecureURI.isUnsecure("/home/x/test/do.htm"));
    }

    public void testUnsecureWildcards() {
        SecureURI.init();
        SecureURI.addUnsecure("/home/x/test*");
        assertFalse(SecureURI.isUnsecure("/home/x/tes"));
        assertTrue(SecureURI.isUnsecure("/home/x/test.htm"));
        assertTrue(SecureURI.isUnsecure("/home/x/test.html"));
        assertTrue(SecureURI.isUnsecure("/home/x/test/me.html"));
        assertTrue(SecureURI.isUnsecure("/home/x/test/me/if.html"));
    }

    public void testUnsecureWildcard() {
        SecureURI.init();
        SecureURI.addUnsecure("/home/x/test?");
        assertFalse(SecureURI.isUnsecure("/home/x/tes"));
        assertTrue(SecureURI.isUnsecure("/home/x/test"));
        assertTrue(SecureURI.isUnsecure("/home/x/test."));
        assertTrue(SecureURI.isUnsecure("/home/x/testd"));
        assertFalse(SecureURI.isUnsecure("/home/x/test.html"));
        assertFalse(SecureURI.isUnsecure("/home/x/test/me.html"));
        assertFalse(SecureURI.isUnsecure("/home/x/test/me/if.html"));
    }
}
