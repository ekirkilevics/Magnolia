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
public class MgnlUserManagerTest extends TestCase {

    public void testUsernameIsValidatedUponCreation() {
        final String justCheckingIfValidateUsernameIsCalledMessage = "Yes! I wanted this method to be called !";
        final MgnlUserManager hm = new MgnlUserManager() {
            protected void validateUsername(String name) {
                throw new IllegalArgumentException(justCheckingIfValidateUsernameIsCalledMessage);
            }
        };
        try {
            hm.createUser("bleh", "blah");
            fail("should have failed");
        } catch (IllegalArgumentException e) {
            assertEquals(justCheckingIfValidateUsernameIsCalledMessage, e.getMessage());
        }
    }

    public void testUsernameCantBeNull() {
        try {
            new MgnlUserManager().validateUsername(null);
            fail("should have failed");
        } catch (IllegalArgumentException e) {
            assertEquals("null is not a valid username.", e.getMessage());
        }
    }

    public void testUsernameCantBeEmpty() {
        try {
            new MgnlUserManager().validateUsername("");
            fail("should have failed");
        } catch (IllegalArgumentException e) {
            assertEquals(" is not a valid username.", e.getMessage());
        }
    }

    public void testUsernameCantBeBlank() {
        try {
            new MgnlUserManager().validateUsername("   ");
            fail("should have failed");
        } catch (IllegalArgumentException e) {
            assertEquals("    is not a valid username.", e.getMessage());
        }
    }
}
