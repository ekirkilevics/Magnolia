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
package info.magnolia.cms.module;

import junit.framework.TestCase;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ModuleDefinitionTest extends TestCase {
    private ModuleDefinition md;

    protected void setUp() throws Exception {
        super.setUp();
        md = new ModuleDefinition();
        PropertyDefinition p1 = new PropertyDefinition();
        PropertyDefinition p2 = new PropertyDefinition();
        PropertyDefinition p3 = new PropertyDefinition();
        p1.setName("abc");
        p1.setValue("123");
        p2.setName("baby");
        p2.setValue("you and me");
        p3.setName("doremi");
        p3.setValue("la la la");
        md.addProperty(p1);
        md.addProperty(p2);
        md.addProperty(p3);
    }

    public void testGetPropertyJustWorks() {
        assertEquals("you and me", md.getProperty("baby"));
    }

    public void testGetPropertyReturnsNullForUnknownProperties() {
        assertEquals(null, md.getProperty("Blame it on the boogie"));
    }

}
