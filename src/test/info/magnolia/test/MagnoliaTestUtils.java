package info.magnolia.test;

import java.io.File;


/**
 * Utility methods that can be used in JUnit test cases.
 * @author fgiust
 * @version $Revision: $ ($Author: $)
 */
public class MagnoliaTestUtils {

    /**
     * Utility class, don't instantiate.
     */
    private MagnoliaTestUtils() {
        // unused
    }

    /**
     * Returns the root directory path for project sources.
     * @return root directory path for test resources
     * @todo should use a better way to determine the project root
     */
    public static String getProjectRoot() {

        return new File(MagnoliaTestUtils.class.getResource("/test-resources.dir").getFile())
            .getParentFile()
            .getParentFile()
            .getParent();
    }

    /**
     * Returns the root directory path for test resources.
     * @return root directory path for test resources
     */
    public static String getTestResourcesDir() {
        return new File(MagnoliaTestUtils.class.getResource("/test-resources.dir").getFile()).getParent();
    }

}
