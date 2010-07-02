package info.magnolia.module.rest.json;

import junit.framework.TestCase;

public class AbsolutePathTest extends TestCase {

    public void testIsRoot() {

        assertTrue(new AbsolutePath("").isRoot());
        assertTrue(new AbsolutePath("/").isRoot());
        assertFalse(new AbsolutePath("untitled").isRoot());
        assertFalse(new AbsolutePath("/untitled").isRoot());
    }

    public void testPath() {

        assertEquals("/", new AbsolutePath("").path());
        assertEquals("/", new AbsolutePath("/").path());
        assertEquals("/untitled", new AbsolutePath("untitled").path());
        assertEquals("/untitled", new AbsolutePath("/untitled").path());

        assertEquals("/1/2/3/4/5", new AbsolutePath("/1/2/3/4/5/").path());
    }

    public void testToString() {

        assertEquals("/", new AbsolutePath("").toString());
        assertEquals("/", new AbsolutePath("/").toString());
        assertEquals("/untitled", new AbsolutePath("untitled").toString());
        assertEquals("/untitled", new AbsolutePath("/untitled").toString());
    }

    public void testParentPath() {

        try {
            new AbsolutePath("").parentPath();
            fail();
        } catch (IllegalStateException expected) {
        }
        try {
            new AbsolutePath("/").parentPath();
            fail();
        } catch (IllegalStateException expected) {
        }

        assertEquals("/untitled", new AbsolutePath("untitled/sub").parentPath());
        assertEquals("/untitled", new AbsolutePath("/untitled/sub").parentPath());

        assertEquals("/untitled/sub", new AbsolutePath("/untitled/sub/node").parentPath());
    }

    public void testParent() {

        try {
            new AbsolutePath("").parent();
            fail();
        } catch (IllegalStateException expected) {
        }
        try {
            new AbsolutePath("/").parent();
            fail();
        } catch (IllegalStateException expected) {
        }

        assertEquals("/", new AbsolutePath("untitled").parent().path());
        assertEquals("/", new AbsolutePath("/untitled").parent().path());

        assertEquals("/untitled", new AbsolutePath("untitled/sub").parent().path());
        assertEquals("/untitled", new AbsolutePath("/untitled/sub").parent().path());

        assertEquals("/untitled/sub", new AbsolutePath("/untitled/sub/node").parent().path());
    }

    public void testName() {
        
        assertEquals("/", new AbsolutePath("").name());
        assertEquals("untitled", new AbsolutePath("/untitled").name());
        assertEquals("sub", new AbsolutePath("/untitled/sub").name());
        assertEquals("node", new AbsolutePath("/untitled/sub/node").name());

    }

    public void testAppend() {

        AbsolutePath path = new AbsolutePath("untitled").append("sub");

        assertEquals("/untitled/sub", path.path());
        assertEquals("sub", path.name());
        assertEquals("untitled", path.parent().name());
    }
}
