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
package info.magnolia.cms.gui.control;

import junit.framework.TestCase;


/**
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class SaveTest extends TestCase {

    /**
     * Test for rich editor cleanup. IE often insert a br at the beginning of a paragraph.
     */
    public void testGetRichEditValueStrCleanExplorerPs() {
        Save save = new Save();
        assertEquals("aaa\n\n  bbb", save.getRichEditValueStr("<P>aaa</P>\r\n<P><BR>bbb</P>", ControlSuper.RICHEDIT_KUPU));
    }
}
