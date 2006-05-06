package info.magnolia.cms.mail;

import java.io.File;


/**
 * Date: Apr 3, 2006 Time: 9:56:00 AM
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class TestUtil {

    public static File getResourceRootFolder() {
        File f = new File(MgnlMailFactoryTest.class.getResource("/").getFile());
        return new File(f.getParentFile().getParentFile().getPath()
            + File.separator
            + "src"
            + File.separator
            + "test"
            + File.separator
            + "resources");
    }

    public static String getResourceRootFolderPath() {
        return getResourceRootFolder().getAbsolutePath();
    }

    public static File getResourceFile(String filename) {
        return new File(getResourceRootFolderPath() + File.separator + filename);
    }

    public static String getResourcePath(String filename) {
        return getResourceFile(filename).getAbsolutePath();
    }
}
