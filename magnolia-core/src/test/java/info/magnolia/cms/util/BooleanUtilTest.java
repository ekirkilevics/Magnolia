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

import static info.magnolia.cms.util.BooleanUtil.toBoolean;
import junit.framework.TestCase;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class BooleanUtilTest extends TestCase {

    public void testToBooleanKnowsItsBasicEnglishVocabulary() {
        assertEquals(true, toBoolean("true", true));
        assertEquals(true, toBoolean("true", false));
        assertEquals(false, toBoolean("false", true));
        assertEquals(false, toBoolean("false", false));

        assertEquals(true, toBoolean("on", true));
        assertEquals(true, toBoolean("on", false));
        assertEquals(false, toBoolean("off", true));
        assertEquals(false, toBoolean("off", false));

        assertEquals(true, toBoolean("yes", true));
        assertEquals(true, toBoolean("yes", false));
        assertEquals(false, toBoolean("no", true));
        assertEquals(false, toBoolean("no", false));
    }

    public void testToBooleanHandlesNullsAndEmptyStringsGracefully() {
        assertEquals(true, toBoolean(null, true));
        assertEquals(false, toBoolean(null, false));
        assertEquals(true, toBoolean("", true));
        assertEquals(false, toBoolean("", false));
    }

    public void testToBooleanUsesDefaultValueForUnknownValues() {
        assertEquals(true, toBoolean("blah", true));
        assertEquals(false, toBoolean("blah", false));        
    }
}
