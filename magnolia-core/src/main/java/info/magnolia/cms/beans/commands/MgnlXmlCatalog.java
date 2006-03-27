package info.magnolia.cms.beans.commands;

import info.magnolia.cms.core.Path;
import org.apache.commons.chain.config.ConfigParser;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Date: Mar 27, 2006
 * Time: 10:54:32 AM
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class MgnlXmlCatalog extends MgnlBaseCatalog {
    static final String DEFAULT_PATH = System.getProperty("magnolia.commands.dir");

    public void initCatalog(String string) {
        ConfigParser parser = new ConfigParser();
        URL url = null;
        try {
            url = new URL("file://" + Path.getAbsoluteFileSystemPath(DEFAULT_PATH + "/" + string + ".xml"));
        } catch (MalformedURLException e) {
            log.error("Could not find the command file.Commands will be unusable", e);
        }
        try {
            parser.parse(this, url);
        }
        catch (Exception e) {
            log.error("Could not parse the command file.Commands will be unusable", e);
        }
    }
}
